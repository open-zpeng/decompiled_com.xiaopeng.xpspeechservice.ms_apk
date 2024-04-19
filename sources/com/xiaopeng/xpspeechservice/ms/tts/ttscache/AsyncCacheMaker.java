package com.xiaopeng.xpspeechservice.ms.tts.ttscache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.xiaopeng.lib.framework.netchannelmodule.http.xmart.TimeoutDns;
import com.xiaopeng.lib.http.tls.HostNameVerifier;
import com.xiaopeng.lib.http.tls.SSLHelper;
import com.xiaopeng.lib.utils.info.AppInfoUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import com.xiaopeng.xpspeechservice.ms.SpeechApp;
import com.xiaopeng.xpspeechservice.ms.bean.WsConnectState;
import com.xiaopeng.xpspeechservice.ms.tts.config.AsyncCacheConfig;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import com.xiaopeng.xpspeechservice.utils.XpSysUtils;
import java.util.LinkedList;
import java.util.ListIterator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: classes.dex */
public class AsyncCacheMaker {
    private static final String HW_ID = BuildInfoUtils.getHardwareId();
    private static final int MSG_REQUEST_DONE = 101;
    private static final int REQUEST_DELAY_MS = 10000;
    private static final int REQUEST_ERROR = 0;
    private static final int REQUEST_SUCCESS = 1;
    private static final String STYLE = "assistant";
    private static final String STYLE_DEGREE = "2.0";
    private static final String TAG = "AsyncCacheMaker";
    private static final String URL_DEV = "http://logan-gateway.dev.logan.xiaopeng.local/xp-tts-boot/ttsV4/task/async-synthesis-audio";
    private static final String URL_PRE = "https://speech.deploy-test.xiaopeng.com/ttsV4/task/async-synthesis-audio";
    private static final String URL_PRODUCT = "https://speech.xiaopeng.com/ttsV4/task/async-synthesis-audio";
    private static final String URL_TEST = "http://speech-int.xiaopeng.com/ttsV4/task/async-synthesis-audio";
    private final String DEFAULT_ENV;
    private LinkedList<Bundle> mCacheItemList;
    private CacheState mCacheState;
    private final CacheState mCachingState;
    private OkHttpClient mClient;
    private boolean mEnable;
    private EventHandler mEventHandler;
    private HandlerThread mEventThread;
    private boolean mIsWsConnected;
    private long mLastRequestTime;
    private long mRequestInterval;
    private int mRequestRetryCount;
    private XpTtsCache mTtsCache;
    private final int mVersionCode;
    private final String mVersionName;
    private final CacheState mWaitingState;

    /* loaded from: classes.dex */
    private static class SingleHolder {
        private static AsyncCacheMaker instance = new AsyncCacheMaker();

        private SingleHolder() {
        }
    }

    public static AsyncCacheMaker getInstance() {
        return SingleHolder.instance;
    }

    private AsyncCacheMaker() {
        this.mEnable = true;
        this.mLastRequestTime = 0L;
        this.mIsWsConnected = false;
        this.mRequestInterval = 600000L;
        this.mRequestRetryCount = 3;
        this.mWaitingState = new WaitingState();
        this.mCachingState = new CachingState();
        this.mCacheItemList = new LinkedList<>();
        this.mEventThread = new HandlerThread("AsyncCacheTask");
        this.mEventThread.start();
        this.mEventHandler = new EventHandler(this.mEventThread.getLooper());
        this.mCacheState = this.mWaitingState;
        this.mTtsCache = XpTtsCache.getInstance();
        Context context = SpeechApp.getContext();
        this.mClient = new OkHttpClient.Builder().sslSocketFactory(SSLHelper.getTLS2SocketFactory(context), SSLHelper.getX509TrustManager(context)).connectionSpecs(SSLHelper.getConnectionSpecs()).dns(TimeoutDns.getInstance()).hostnameVerifier(HostNameVerifier.INSTANCE).build();
        EventBus.getDefault().register(this);
        if (XpSysUtils.isDevBuild()) {
            LogUtils.v(TAG, "use preproduct environment for dev version");
            this.DEFAULT_ENV = "preproduct";
        } else {
            LogUtils.v(TAG, "use product environment for rel version");
            this.DEFAULT_ENV = "product";
            this.mEnable = false;
        }
        PackageInfo info = AppInfoUtils.getPackageInfo(context, context.getPackageName());
        if (info != null) {
            this.mVersionCode = info.versionCode;
            this.mVersionName = info.versionName;
            return;
        }
        this.mVersionCode = 0;
        this.mVersionName = "";
    }

    public void makeCacheAsync(final Bundle item) {
        this.mEventHandler.postDelayed(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.1
            @Override // java.lang.Runnable
            public void run() {
                if (AsyncCacheMaker.this.mEnable) {
                    LogUtils.i(AsyncCacheMaker.TAG, "makeCacheAsync " + item.getString("txt", ""));
                    AsyncCacheMaker.this.mCacheState.enqueueItem(item);
                    return;
                }
                LogUtils.v(AsyncCacheMaker.TAG, "AsyncCache disabled");
            }
        }, 10000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCacheState(CacheState state) {
        LogUtils.i(TAG, "cache state change %s to %s", this.mCacheState.getClass().getSimpleName(), state.getClass().getSimpleName());
        this.mCacheState = state;
    }

    private void onCacheEvent(final CacheEventType event) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.2
            @Override // java.lang.Runnable
            public void run() {
                AsyncCacheMaker.this.onCacheEventInternal(event);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCacheEventInternal(CacheEventType event) {
        LogUtils.i(TAG, "onCacheEvent %s at %s", event.name(), this.mCacheState.getClass().getSimpleName());
        this.mCacheState.onEvent(event);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void nextRequestPost() {
        this.mEventHandler.postDelayed(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.3
            @Override // java.lang.Runnable
            public void run() {
                AsyncCacheMaker.this.mCacheState.onEvent(CacheEventType.EVENT_NEXT_REQUEST_FIRE);
            }
        }, this.mRequestInterval);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CacheState {
        public CacheState() {
        }

        public void enqueueItem(Bundle item) {
            LogUtils.w(AsyncCacheMaker.TAG, "Not handled enqueueItem at " + getClass().getSimpleName());
        }

        public void onEvent(CacheEventType event) {
            LogUtils.w(AsyncCacheMaker.TAG, "Not handled cache event %s at %s", event.name(), getClass().getSimpleName());
        }
    }

    /* loaded from: classes.dex */
    private class WaitingState extends CacheState {
        private WaitingState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.CacheState
        public void enqueueItem(Bundle newItem) {
            String newTxt = newItem.getString("txt", "");
            ListIterator<Bundle> iter = AsyncCacheMaker.this.mCacheItemList.listIterator();
            while (iter.hasNext()) {
                Bundle item = iter.next();
                String txt = item.getString("txt", "");
                if (txt.equals(newTxt)) {
                    return;
                }
            }
            AsyncCacheMaker.this.mCacheItemList.add(newItem);
            if (AsyncCacheMaker.this.mIsWsConnected) {
                if (AsyncCacheMaker.this.mLastRequestTime != 0) {
                    long curTime = SystemClock.elapsedRealtime();
                    if (curTime - AsyncCacheMaker.this.mLastRequestTime < AsyncCacheMaker.this.mRequestInterval) {
                        return;
                    }
                }
                AsyncCacheMaker asyncCacheMaker = AsyncCacheMaker.this;
                asyncCacheMaker.setCacheState(asyncCacheMaker.mCachingState);
                AsyncCacheMaker.this.startCaching();
            }
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.CacheState
        public void onEvent(CacheEventType event) {
            if (event == CacheEventType.EVENT_WS_CONNECTED) {
                if (AsyncCacheMaker.this.mCacheItemList.size() != 0) {
                    if (AsyncCacheMaker.this.mLastRequestTime != 0) {
                        long curTime = SystemClock.elapsedRealtime();
                        if (curTime - AsyncCacheMaker.this.mLastRequestTime < AsyncCacheMaker.this.mRequestInterval) {
                            return;
                        }
                    }
                    AsyncCacheMaker asyncCacheMaker = AsyncCacheMaker.this;
                    asyncCacheMaker.setCacheState(asyncCacheMaker.mCachingState);
                    AsyncCacheMaker.this.startCaching();
                }
            } else if (event == CacheEventType.EVENT_NEXT_REQUEST_FIRE) {
                if (AsyncCacheMaker.this.mCacheItemList.size() != 0) {
                    AsyncCacheMaker.this.startCaching();
                }
            } else {
                super.onEvent(event);
            }
        }
    }

    /* loaded from: classes.dex */
    private class CachingState extends CacheState {
        private CachingState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.CacheState
        public void enqueueItem(Bundle newItem) {
            String newTxt = newItem.getString("txt", "");
            ListIterator<Bundle> iter = AsyncCacheMaker.this.mCacheItemList.listIterator();
            while (iter.hasNext()) {
                Bundle item = iter.next();
                String txt = item.getString("txt", "");
                if (txt.equals(newTxt)) {
                    return;
                }
            }
            AsyncCacheMaker.this.mCacheItemList.add(newItem);
            AsyncCacheMaker.this.startCaching();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.CacheState
        public void onEvent(CacheEventType event) {
            if (event == CacheEventType.EVENT_WS_DISCONNECT) {
                AsyncCacheMaker asyncCacheMaker = AsyncCacheMaker.this;
                asyncCacheMaker.setCacheState(asyncCacheMaker.mWaitingState);
            } else if (event == CacheEventType.EVENT_REQUEST_DONE) {
                AsyncCacheMaker asyncCacheMaker2 = AsyncCacheMaker.this;
                asyncCacheMaker2.setCacheState(asyncCacheMaker2.mWaitingState);
                AsyncCacheMaker.this.nextRequestPost();
            } else {
                super.onEvent(event);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == AsyncCacheMaker.MSG_REQUEST_DONE) {
                AsyncCacheMaker.this.onCacheEventInternal(CacheEventType.EVENT_REQUEST_DONE);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Incorrect condition in loop: B:7:0x001f */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void startCaching() {
        /*
            r8 = this;
            java.lang.String r0 = "AsyncCacheMaker"
            java.lang.String r1 = "startCaching +++"
            com.xiaopeng.xpspeechservice.utils.LogUtils.d(r0, r1)
            java.util.LinkedList<android.os.Bundle> r1 = r8.mCacheItemList
            java.util.ListIterator r1 = r1.listIterator()
        Ld:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L3d
            java.lang.Object r2 = r1.next()
            android.os.Bundle r2 = (android.os.Bundle) r2
            r3 = 1
        L1a:
            int r4 = r8.requestCache(r2)
            r5 = 1
            if (r4 != r5) goto L22
            goto L37
        L22:
            java.lang.Object[] r5 = new java.lang.Object[r5]
            r6 = 0
            java.lang.Integer r7 = java.lang.Integer.valueOf(r3)
            r5[r6] = r7
            java.lang.String r6 = "requestCache try %d fail"
            com.xiaopeng.xpspeechservice.utils.LogUtils.w(r0, r6, r5)
            int r4 = r3 + 1
            int r5 = r8.mRequestRetryCount
            if (r3 < r5) goto L3b
            r3 = r4
        L37:
            r1.remove()
            goto Ld
        L3b:
            r3 = r4
            goto L1a
        L3d:
            long r1 = android.os.SystemClock.elapsedRealtime()
            r8.mLastRequestTime = r1
            com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker$EventHandler r1 = r8.mEventHandler
            r2 = 101(0x65, float:1.42E-43)
            r1.removeMessages(r2)
            com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker$EventHandler r1 = r8.mEventHandler
            r1.sendEmptyMessage(r2)
            java.lang.String r1 = "startCaching ---"
            com.xiaopeng.xpspeechservice.utils.LogUtils.d(r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.startCaching():void");
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onWsConnectStateChanged(final WsConnectState wsState) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.4
            @Override // java.lang.Runnable
            public void run() {
                if (wsState.state == 1) {
                    if (!AsyncCacheMaker.this.mIsWsConnected) {
                        AsyncCacheMaker.this.mIsWsConnected = true;
                        LogUtils.v(AsyncCacheMaker.TAG, "onWsConnectStateChanged " + wsState);
                        AsyncCacheMaker.this.onCacheEventInternal(CacheEventType.EVENT_WS_CONNECTED);
                    }
                } else if (AsyncCacheMaker.this.mIsWsConnected) {
                    AsyncCacheMaker.this.mIsWsConnected = false;
                    LogUtils.v(AsyncCacheMaker.TAG, "onWsConnectStateChanged " + wsState);
                    AsyncCacheMaker.this.onCacheEventInternal(CacheEventType.EVENT_WS_DISCONNECT);
                }
            }
        });
    }

    private String getRequestUrl() {
        String mode = SystemProperties.get("sys.xiaopeng.tts.asynccache_mode", this.DEFAULT_ENV);
        if ("product".equals(mode)) {
            return URL_PRODUCT;
        }
        if ("preproduct".equals(mode)) {
            return URL_PRE;
        }
        if ("test".equals(mode)) {
            return URL_TEST;
        }
        String url = SystemProperties.get("sys.xiaopeng.tts.asynccache_local", URL_DEV);
        return url;
    }

    private int requestCache(Bundle item) {
        Response response;
        ResponseBody responseBody;
        MediaType type;
        String txt = item.getString("txt", "");
        LogUtils.d(TAG, "requestCache txt " + txt);
        try {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", txt);
                jsonObject.put("ts", String.valueOf(System.currentTimeMillis()));
                jsonObject.put("hardwareId", HW_ID);
                jsonObject.put("style", STYLE);
                jsonObject.put("styleDegree", STYLE_DEGREE);
                jsonObject.put("caller", item.getString("source", ""));
                jsonObject.put("appVerCode", String.valueOf(this.mVersionCode));
                jsonObject.put("appVerName", this.mVersionName);
                LogUtils.v(TAG, "Request " + jsonObject.toString());
                MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
                RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());
                Request request = new Request.Builder().url(getRequestUrl()).post(requestBody).build();
                try {
                    response = this.mClient.newCall(request).execute();
                    responseBody = response.body();
                    type = responseBody.contentType();
                } catch (Exception e) {
                    LogUtils.e(TAG, "request fail", e);
                }
            } catch (JSONException e2) {
                LogUtils.e(TAG, "make jsonObject fail", e2);
                return 0;
            }
        } catch (Exception e3) {
            LogUtils.e(TAG, "requestCache fail", e3);
        }
        if (type.toString().equals("audio/mpeg")) {
            byte[] buffer = responseBody.bytes();
            LogUtils.v(TAG, "get buffer size %d", Integer.valueOf(buffer.length));
            this.mTtsCache.makeTtsCache(txt, buffer);
            response.close();
            return 1;
        }
        if (type.type().equals("text")) {
            LogUtils.e(TAG, "error=" + responseBody.string());
        } else {
            LogUtils.e(TAG, "error return " + type);
        }
        response.close();
        return 0;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onConfigChange(final AsyncCacheConfig config) {
        LogUtils.i(TAG, "onConfigChange " + config);
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker.5
            @Override // java.lang.Runnable
            public void run() {
                AsyncCacheMaker.this.mEnable = config.enable;
                AsyncCacheMaker.this.mRequestInterval = config.requestInterval;
                AsyncCacheMaker.this.mRequestRetryCount = config.requestRetryCount;
            }
        });
    }
}

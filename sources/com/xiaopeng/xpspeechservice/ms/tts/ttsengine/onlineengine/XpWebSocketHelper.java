package com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine;

import android.car.Car;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.lzy.okgo.model.HttpParams;
import com.xiaopeng.lib.framework.netchannelmodule.http.xmart.TimeoutDns;
import com.xiaopeng.lib.http.tls.HostNameVerifier;
import com.xiaopeng.lib.http.tls.SSLHelper;
import com.xiaopeng.lib.utils.SystemPropertyUtil;
import com.xiaopeng.lib.utils.info.AppInfoUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import com.xiaopeng.xpspeechservice.ms.SpeechApp;
import com.xiaopeng.xpspeechservice.ms.bean.NetworkState;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import com.xiaopeng.xpspeechservice.utils.WorkerHandler;
import com.xiaopeng.xpspeechservice.utils.XpSysUtils;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLHandshakeException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
/* loaded from: classes.dex */
public class XpWebSocketHelper {
    private static final int CLOSE_CLIENT_NORMAL = 1000;
    public static final int CONNECTED = 4;
    public static final int CONNECTING = 3;
    public static final int DISCONNECTED = 1;
    public static final int DISCONNECTING = 2;
    public static final int FORBID_CONNECT = 0;
    private static final String REASON_SHUTDOWN = "shutdown";
    private static final String WEB_APPLICATION_STOPPING = "The web application is stopping";
    private static final String XP_URL_DEV = "ws://dev.ai.xiaopeng.local/v2/tts";
    public static final String XP_URL_PARAM_HARDWARE = "hardwareId=";
    private static final String XP_URL_PRE_PRODUCT = "wss://speech.deploy-test.xiaopeng.com/v2/tts";
    public static final String XP_URL_PRODUCT = "wss://speech.xiaopeng.com/v2/tts";
    private static final String XP_URL_TEST = "ws://speech-int.xiaopeng.com/v2/tts";
    private final String DEFAULT_ENV;
    private IWebSocketListener mCallBack;
    private String mChannelName;
    private HandlerThread mMsgThread;
    private WorkerHandler mMsgWorkerHandler;
    private volatile OkHttpClient mOkHttpClient;
    private volatile long mStartConnectTime;
    private volatile String mUrl;
    private WorkerHandler mWorkerHandler;
    private HandlerThread mWorkerThread;
    private String TAG = "XpWebSocketHelper";
    private Object mWebSocketLock = new Object();
    private volatile WebSocket mXPWebSocket = null;
    private Map<String, String> mHeaders = new ConcurrentHashMap();
    private AtomicInteger mConnectStatus = new AtomicInteger(1);
    private AtomicBoolean netWorkAvailable = new AtomicBoolean(false);
    private WebSocketListener mWebSocketListener = new WebSocketListener() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.1
        @Override // okhttp3.WebSocketListener
        public void onOpen(WebSocket webSocket, Response response) {
            String str = XpWebSocketHelper.this.TAG;
            LogUtils.i(str, "connected at " + System.currentTimeMillis() + " webSocket = " + webSocket.toString());
            XpWebSocketHelper.this.mWorkerHandler.optPost(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.1.1
                @Override // java.lang.Runnable
                public void run() {
                    XpWebSocketHelper.this.mConnectStatus.set(4);
                    if (XpWebSocketHelper.this.mCallBack != null) {
                        XpWebSocketHelper.this.mCallBack.onOpen();
                    }
                }
            });
        }

        @Override // okhttp3.WebSocketListener
        public void onMessage(WebSocket webSocket, final String text) {
            XpWebSocketHelper.this.mMsgWorkerHandler.optPost(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.1.2
                @Override // java.lang.Runnable
                public void run() {
                    if (XpWebSocketHelper.this.mCallBack != null) {
                        XpWebSocketHelper.this.mCallBack.onMessage(text);
                    }
                }
            });
        }

        @Override // okhttp3.WebSocketListener
        public void onMessage(WebSocket webSocket, final ByteString bytes) {
            XpWebSocketHelper.this.mMsgWorkerHandler.optPost(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.1.3
                @Override // java.lang.Runnable
                public void run() {
                    if (XpWebSocketHelper.this.mCallBack != null) {
                        XpWebSocketHelper.this.mCallBack.onReceive((byte[]) bytes.toByteArray().clone());
                    }
                }
            });
        }

        @Override // okhttp3.WebSocketListener
        public void onClosing(WebSocket webSocket, int code, final String reason) {
            String str = XpWebSocketHelper.this.TAG;
            LogUtils.i(str, "websocket onClosing, code = " + code + ", reason: " + reason);
            if (!XpWebSocketHelper.REASON_SHUTDOWN.equals(reason)) {
                XpWebSocketHelper.this.mWorkerHandler.optPost(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.1.4
                    @Override // java.lang.Runnable
                    public void run() {
                        if (XpWebSocketHelper.WEB_APPLICATION_STOPPING.equals(reason)) {
                            XpWebSocketHelper.this.closeWebSocket(reason);
                        }
                    }
                });
            }
        }

        @Override // okhttp3.WebSocketListener
        public void onClosed(WebSocket webSocket, final int code, final String reason) {
            String str = XpWebSocketHelper.this.TAG;
            LogUtils.i(str, "websocket onClosed, code = " + code + ", reason " + reason);
            if (!XpWebSocketHelper.REASON_SHUTDOWN.equals(reason)) {
                XpWebSocketHelper.this.mWorkerHandler.optPost(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.1.5
                    @Override // java.lang.Runnable
                    public void run() {
                        XpWebSocketHelper.this.mConnectStatus.set(1);
                        XpWebSocketHelper.this.mXPWebSocket = null;
                        if (XpWebSocketHelper.this.mCallBack != null) {
                            XpWebSocketHelper.this.mCallBack.onClosed(code, reason);
                        }
                    }
                });
            }
        }

        @Override // okhttp3.WebSocketListener
        public void onFailure(final WebSocket webSocket, final Throwable t, @Nullable final Response response) {
            LogUtils.i(XpWebSocketHelper.this.TAG, "websocket onFailure, reason = %s, webSocket = %s, mXPWebSocket = %s", t.toString(), webSocket, XpWebSocketHelper.this.mXPWebSocket);
            XpWebSocketHelper.this.mWorkerHandler.optPost(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.1.6
                @Override // java.lang.Runnable
                public void run() {
                    XpWebSocketHelper.this.failureProcess(webSocket, t, response);
                }
            });
        }
    };
    private Runnable mConnectWebSocketRunnable = new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.3
        @Override // java.lang.Runnable
        public void run() {
            XpWebSocketHelper.this.connect();
        }
    };
    private Context mContext = SpeechApp.getContext();

    /* JADX INFO: Access modifiers changed from: private */
    public void failureProcess(WebSocket ws, final Throwable t, Response response) {
        WorkerHandler workerHandler;
        Runnable runnable;
        try {
            try {
            } catch (Exception e) {
                LogUtils.e(this.TAG, "failureProcess fail", e);
                workerHandler = this.mWorkerHandler;
                runnable = new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (XpWebSocketHelper.this.mCallBack != null) {
                            XpWebSocketHelper.this.mCallBack.onError(t);
                        }
                    }
                };
            }
            synchronized (this.mWebSocketLock) {
                if (ws == this.mXPWebSocket) {
                    if ((t instanceof IOException) && NetworkUtils.isNetworkAvailable(this.mContext)) {
                        if ((t instanceof SSLHandshakeException) && (t.getCause() instanceof CertificateException)) {
                            LogUtils.w(this.TAG, "SSLHandshakeException: CertificateException");
                            return;
                        } else if (response != null && response.code() == 200) {
                            try {
                                ResponseBody body = response.body();
                                if (body != null) {
                                    String result = body.string();
                                    LogUtils.w(this.TAG, "证书无效/服务端异常 body = %s", result);
                                    if (result != null && result.contains("\"retry\":false")) {
                                        LogUtils.w(this.TAG, "无需重试");
                                        return;
                                    }
                                }
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                    } else if (t instanceof SocketTimeoutException) {
                        LogUtils.w(this.TAG, "SocketTimeoutException");
                    }
                }
                workerHandler = this.mWorkerHandler;
                runnable = new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (XpWebSocketHelper.this.mCallBack != null) {
                            XpWebSocketHelper.this.mCallBack.onError(t);
                        }
                    }
                };
                workerHandler.optPost(runnable);
            }
        } finally {
            this.mWorkerHandler.optPost(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.2
                @Override // java.lang.Runnable
                public void run() {
                    if (XpWebSocketHelper.this.mCallBack != null) {
                        XpWebSocketHelper.this.mCallBack.onError(t);
                    }
                }
            });
        }
    }

    public XpWebSocketHelper(String channelName) {
        this.TAG += "_" + channelName;
        this.mChannelName = channelName;
        if (XpSysUtils.isDevBuild()) {
            LogUtils.v(this.TAG, "use preproduct environment for dev version");
            this.DEFAULT_ENV = "preproduct";
            return;
        }
        LogUtils.v(this.TAG, "use product environment for rel version");
        this.DEFAULT_ENV = "product";
    }

    public void init(IWebSocketListener listener) {
        String mode = SystemProperties.get("sys.xiaopeng.tts.xpcloud_mode", this.DEFAULT_ENV);
        if ("product".equals(mode)) {
            this.mUrl = XP_URL_PRODUCT;
        } else if ("preproduct".equals(mode)) {
            this.mUrl = XP_URL_PRE_PRODUCT;
        } else if ("test".equals(mode)) {
            this.mUrl = XP_URL_TEST;
        } else {
            this.mUrl = SystemProperties.get("sys.xiaopeng.tts.xpcloud_local", XP_URL_DEV);
        }
        String str = this.TAG;
        LogUtils.i(str, "init xp url " + this.mUrl);
        this.mCallBack = listener;
        this.mWorkerThread = new HandlerThread("WebSocketWorker");
        this.mWorkerThread.start();
        this.mWorkerHandler = new WorkerHandler(this.mWorkerThread.getLooper());
        this.mMsgThread = new HandlerThread("WebSocketMsg");
        this.mMsgThread.start();
        this.mMsgWorkerHandler = new WorkerHandler(this.mMsgThread.getLooper());
        this.mOkHttpClient = new OkHttpClient().newBuilder().sslSocketFactory(SSLHelper.getTLS2SocketFactory(this.mContext), SSLHelper.getX509TrustManager(this.mContext)).dns(TimeoutDns.getInstance()).pingInterval(8000L, TimeUnit.MILLISECONDS).retryOnConnectionFailure(false).hostnameVerifier(HostNameVerifier.INSTANCE).connectionSpecs(SSLHelper.getConnectionSpecs()).writeTimeout(4000L, TimeUnit.MILLISECONDS).readTimeout(4000L, TimeUnit.MILLISECONDS).connectTimeout(10000L, TimeUnit.MILLISECONDS).build();
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        Network nw = cm.getActiveNetwork();
        if (nw != null) {
            LogUtils.i(this.TAG, "we got active network");
            this.netWorkAvailable.set(true);
            connectSocketIfNeed(0L);
        } else {
            LogUtils.i(this.TAG, "no active network");
        }
        EventBus.getDefault().register(this);
    }

    private void configNet() {
        this.mHeaders.clear();
        this.mHeaders.put("vin", SystemPropertyUtil.getVIN());
        this.mHeaders.put("vid", String.valueOf(SystemPropertyUtil.getVehicleId()));
        this.mHeaders.put("hardwareId", BuildInfoUtils.getHardwareId());
        this.mHeaders.put("bid", BuildInfoUtils.getBid());
        try {
            this.mHeaders.put("carPlatform", Car.getXpCduType());
        } catch (Exception e) {
            LogUtils.e(this.TAG, "get car or cdu type fail", e);
        }
        this.mHeaders.put("carType", SystemProperties.get("ro.product.model", ""));
        this.mHeaders.put("carSoftware", SystemProperties.get("ro.xiaopeng.software", ""));
        this.mHeaders.put("romVer", BuildInfoUtils.getSystemVersion());
        int versionCode = 0;
        String versionName = "";
        Context context = this.mContext;
        PackageInfo info = AppInfoUtils.getPackageInfo(context, context.getPackageName());
        if (info != null) {
            versionCode = info.versionCode;
            versionName = info.versionName;
        }
        this.mHeaders.put("appVerCode", String.valueOf(versionCode));
        this.mHeaders.put("appVerName", versionName);
        String str = this.TAG;
        LogUtils.v(str, "request header " + this.mHeaders);
    }

    public void connectSocket(int delay) {
        if (this.netWorkAvailable.get()) {
            String str = this.TAG;
            LogUtils.i(str, "connectSocket with delay " + delay);
            connectSocketIfNeed((long) delay);
            return;
        }
        LogUtils.i(this.TAG, "do not connectSocket");
    }

    private void connectSocketIfNeed(long delay) {
        postConnectRunnable(delay);
    }

    public void sendMessage(final String message) {
        this.mWorkerHandler.optPost(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.4
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (!XpWebSocketHelper.this.mXPWebSocket.send(message)) {
                        LogUtils.e(XpWebSocketHelper.this.TAG, "send message failed, force close websocket");
                        XpWebSocketHelper.this.forceCloseWebSocket("send message failed");
                    }
                } catch (Exception e) {
                    LogUtils.e(XpWebSocketHelper.this.TAG, "webSocket sendMessage Exception", e);
                }
            }
        });
    }

    public void sendByteMessage(final byte[] bytes, final int len) {
        this.mWorkerHandler.optPost(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpWebSocketHelper.5
            @Override // java.lang.Runnable
            public void run() {
                try {
                    byte[] sendBuf = new byte[len];
                    System.arraycopy(bytes, 0, sendBuf, 0, len);
                    if (!XpWebSocketHelper.this.mXPWebSocket.send(ByteString.of(sendBuf))) {
                        LogUtils.e(XpWebSocketHelper.this.TAG, "send byte message failed, force close websocket");
                        XpWebSocketHelper.this.forceCloseWebSocket("send byte message failed");
                    }
                } catch (Exception e) {
                    LogUtils.e(XpWebSocketHelper.this.TAG, "webSocket sendByteMessage Exception", e);
                }
            }
        });
    }

    @Subscribe(sticky = HttpParams.IS_REPLACE, threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkState state) {
        String str = this.TAG;
        LogUtils.i(str, "onNetworkChange, status " + state.networkStatus);
        if (state.networkStatus == 0) {
            this.netWorkAvailable.set(true);
            if (this.mConnectStatus.get() == 1) {
                LogUtils.i(this.TAG, "network available: connect to network");
                try {
                    connectSocketIfNeed(50L);
                    return;
                } catch (Exception e) {
                    LogUtils.w(this.TAG, "onNetworkChange error", e);
                    return;
                }
            }
            return;
        }
        this.netWorkAvailable.set(false);
    }

    public boolean isWebSocketConnected() {
        return this.mConnectStatus.get() == 4;
    }

    private void postConnectRunnable(long delay) {
        this.mWorkerHandler.removeCallbacks(this.mConnectWebSocketRunnable);
        this.mWorkerHandler.optPostDelay(this.mConnectWebSocketRunnable, delay);
    }

    private String buildUrl() {
        StringBuilder builder = new StringBuilder(this.mUrl);
        builder.append("?");
        builder.append(XP_URL_PARAM_HARDWARE);
        builder.append(BuildInfoUtils.getHardwareId());
        builder.append("&");
        builder.append("playChannel=" + this.mChannelName);
        return builder.toString();
    }

    public void forceCloseWebSocket(@NonNull String reason) {
        synchronized (this.mWebSocketLock) {
            if (this.mXPWebSocket == null) {
                return;
            }
            this.mXPWebSocket.cancel();
            String str = this.TAG;
            LogUtils.i(str, "forceCloseWebSocket, reason " + reason);
            this.mWebSocketListener.onClosed(this.mXPWebSocket, 1000, reason);
        }
    }

    public void closeWebSocket(String reason) {
        String str = this.TAG;
        LogUtils.i(str, "closeWebSocket, reason " + reason);
        synchronized (this.mWebSocketLock) {
            if (this.mXPWebSocket != null) {
                this.mConnectStatus.set(2);
                this.mXPWebSocket.close(1000, reason);
            }
        }
    }

    public void shutdown() {
        LogUtils.i(this.TAG, REASON_SHUTDOWN);
        EventBus.getDefault().unregister(this);
        closeWebSocket(REASON_SHUTDOWN);
        this.mWorkerHandler.removeCallbacksAndMessages(null);
        this.mWorkerThread.quit();
        this.mMsgWorkerHandler.removeCallbacksAndMessages(null);
        this.mMsgThread.quit();
    }

    private Request getRequest(String url) {
        configNet();
        Request.Builder builder = new Request.Builder().get().url(url);
        builder.addHeader("timestamp", String.valueOf(System.currentTimeMillis()));
        if (this.mHeaders.size() > 0) {
            for (String key : this.mHeaders.keySet()) {
                builder.addHeader(key, this.mHeaders.get(key));
            }
        }
        return builder.build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void connect() {
        synchronized (this.mWebSocketLock) {
            String str = this.TAG;
            LogUtils.i(str, "status = " + this.mConnectStatus.get() + " connect");
            if (this.mConnectStatus.get() != 1) {
                return;
            }
            if (TextUtils.isEmpty(BuildInfoUtils.getHardwareId())) {
                LogUtils.e(this.TAG, "hardware id is empty");
                return;
            }
            this.mConnectStatus.set(3);
            this.mStartConnectTime = System.currentTimeMillis();
            Request request = getRequest(buildUrl());
            LogUtils.i(this.TAG, "request = %s", request.toString());
            WebSocket webSocket = this.mOkHttpClient.newWebSocket(request, this.mWebSocketListener);
            this.mXPWebSocket = webSocket;
            String str2 = this.TAG;
            LogUtils.i(str2, "start connect websocket at " + System.currentTimeMillis() + " mXPWebSocket = " + this.mXPWebSocket);
        }
    }

    public boolean checkUrlChanged() {
        String url;
        String mode = SystemProperties.get("sys.xiaopeng.tts.xpcloud_mode", this.DEFAULT_ENV);
        if ("product".equals(mode)) {
            url = XP_URL_PRODUCT;
        } else if ("preproduct".equals(mode)) {
            url = XP_URL_PRE_PRODUCT;
        } else if ("test".equals(mode)) {
            url = XP_URL_TEST;
        } else {
            url = SystemProperties.get("sys.xiaopeng.tts.xpcloud_local", XP_URL_DEV);
        }
        if (!this.mUrl.equals(url)) {
            this.mUrl = url;
            return true;
        }
        return false;
    }
}

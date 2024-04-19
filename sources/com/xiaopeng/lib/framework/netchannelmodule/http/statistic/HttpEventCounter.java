package com.xiaopeng.lib.framework.netchannelmodule.http.statistic;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.framework.netchannelmodule.common.ContextNetStatusProvider;
import com.xiaopeng.lib.utils.info.DeviceInfoUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: classes.dex */
public enum HttpEventCounter {
    INSTANCE;
    
    private static final String EVENT_NAME = "net_http_event";
    private static final long MAX_VALUE_COUNT = 10;
    private static final String TAG = "HttpEventCounter";
    private IDataLog mDataLog;
    private Map<String, EventData> mEventData = new HashMap();
    private EventListener mEventListener = new EventListener() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1
        @Override // okhttp3.EventListener
        public void callStart(final Call call) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.1
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).callStart();
                }
            }, "callStart");
        }

        @Override // okhttp3.EventListener
        public void dnsStart(final Call call, final String domainName) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.2
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).dnsStart(domainName);
                }
            }, "dnsStart");
        }

        @Override // okhttp3.EventListener
        public void dnsEnd(final Call call, final String domainName, final List<InetAddress> inetAddressList) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.3
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).dnsEnd(domainName, inetAddressList);
                }
            }, "dnsEnd");
        }

        @Override // okhttp3.EventListener
        public void connectStart(final Call call, final InetSocketAddress inetSocketAddress, final Proxy proxy) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.4
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).connectStart(inetSocketAddress, proxy);
                }
            }, "connectStart");
        }

        @Override // okhttp3.EventListener
        public void secureConnectStart(final Call call) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.5
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).secureConnectStart();
                }
            }, "secureConnectStart");
        }

        @Override // okhttp3.EventListener
        public void secureConnectEnd(final Call call, @Nullable final Handshake handshake) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.6
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).secureConnectEnd(handshake);
                }
            }, "secureConnectEnd");
        }

        @Override // okhttp3.EventListener
        public void connectEnd(final Call call, final InetSocketAddress inetSocketAddress, final Proxy proxy, @Nullable final Protocol protocol) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.7
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).connectEnd(inetSocketAddress, proxy, protocol);
                }
            }, "connectEnd");
        }

        @Override // okhttp3.EventListener
        public void connectFailed(final Call call, final InetSocketAddress inetSocketAddress, final Proxy proxy, @Nullable final Protocol protocol, final IOException ioe) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.8
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).connectFailed(inetSocketAddress, proxy, protocol, ioe);
                }
            }, "connectFailed");
        }

        @Override // okhttp3.EventListener
        public void connectionAcquired(final Call call, final Connection connection) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.9
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).connectionAcquired(connection);
                }
            }, "connectionAcquired");
        }

        @Override // okhttp3.EventListener
        public void connectionReleased(final Call call, final Connection connection) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.10
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).connectionReleased(connection);
                }
            }, "connectionReleased");
        }

        @Override // okhttp3.EventListener
        public void requestHeadersStart(final Call call) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.11
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).requestHeadersStart();
                }
            }, "requestHeadersStart");
        }

        @Override // okhttp3.EventListener
        public void requestHeadersEnd(final Call call, final Request request) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.12
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).requestHeadersEnd(request);
                }
            }, "requestHeadersEnd");
        }

        @Override // okhttp3.EventListener
        public void requestBodyStart(final Call call) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.13
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).requestBodyStart();
                }
            }, "requestBodyStart");
        }

        @Override // okhttp3.EventListener
        public void requestBodyEnd(final Call call, final long byteCount) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.14
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).requestBodyEnd(byteCount);
                }
            }, "requestBodyEnd");
        }

        @Override // okhttp3.EventListener
        public void responseHeadersStart(final Call call) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.15
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).responseHeadersStart();
                }
            }, "responseHeadersStart");
        }

        @Override // okhttp3.EventListener
        public void responseHeadersEnd(final Call call, final Response response) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.16
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).responseHeadersEnd(response);
                }
            }, "responseHeadersEnd");
        }

        @Override // okhttp3.EventListener
        public void responseBodyStart(final Call call) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.17
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).responseBodyStart();
                }
            }, "responseBodyStart");
        }

        @Override // okhttp3.EventListener
        public void responseBodyEnd(final Call call, final long byteCount) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.18
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).responseBodyEnd(byteCount);
                }
            }, "responseBodyEnd");
        }

        @Override // okhttp3.EventListener
        public void callEnd(final Call call) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.19
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).callEnd();
                    HttpEventCounter.this.commitData();
                }
            }, "callEnd");
        }

        @Override // okhttp3.EventListener
        public void callFailed(final Call call, final IOException ioe) {
            HttpEventCounter.this.runOnWorkThread(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpEventCounter.1.20
                @Override // java.lang.Runnable
                public void run() {
                    HttpEventCounter.this.getEventData(call).callFailed(ioe);
                    HttpEventCounter.this.commitData();
                }
            }, "callFailed");
        }
    };
    private final Handler mHandler;
    private long mLastHour;

    HttpEventCounter() {
        HandlerThread thread = new HandlerThread(TAG, 10);
        thread.start();
        this.mHandler = new Handler(thread.getLooper());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void commitData() {
        Context context = ContextNetStatusProvider.getApplicationContext();
        if (context == null) {
            return;
        }
        long currentHour = System.currentTimeMillis() / 3600000;
        if (this.mLastHour == 0) {
            this.mLastHour = currentHour;
        }
        if (this.mEventData.size() > MAX_VALUE_COUNT || this.mLastHour < currentHour) {
            Iterator<Map.Entry<String, EventData>> it = this.mEventData.entrySet().iterator();
            JSONArray array = new JSONArray();
            while (it.hasNext()) {
                Map.Entry<String, EventData> et = it.next();
                EventData data = et.getValue();
                if (data.readyPublish()) {
                    array.put(data.toJson());
                    it.remove();
                }
            }
            if (this.mEventData.size() > MAX_VALUE_COUNT) {
                this.mEventData.clear();
            }
            if (array.length() > 0) {
                boolean international = DeviceInfoUtils.isInternationalVer();
                if (!international) {
                    if (this.mDataLog == null) {
                        this.mDataLog = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
                    }
                    this.mDataLog.sendStatOriginData(EVENT_NAME, buildData(context, array));
                }
            }
            this.mLastHour = currentHour;
        }
    }

    private String buildData(Context context, JSONArray detail) {
        JSONObject object = new JSONObject();
        try {
            object.put("pk", context.getPackageName());
            object.put("dt", detail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public EventListener getEventListener() {
        return this.mEventListener;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public EventData getEventData(Call call) {
        EventData data = this.mEventData.get(call.toString());
        if (data == null) {
            data = new EventData(call);
        }
        this.mEventData.put(call.toString(), data);
        return data;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runOnWorkThread(Runnable run, String method) {
        this.mHandler.post(run);
    }
}

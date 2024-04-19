package com.xiaopeng.lib.framework.netchannelmodule.websocket;

import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig;
import com.xiaopeng.lib.framework.netchannelmodule.http.ConfigImpl;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
/* loaded from: classes.dex */
public final class WebSocketConfig implements IWebSocketConfig {
    private OkHttpClient mClient;
    private SSLSocketFactory mSslSocketFactory;
    private X509TrustManager mTrustManager;
    private boolean mShowLog = false;
    private String mLogTag = "RxWebSocket";
    private long mReconnectInterval = 1000;
    private long mPingInterval = 1000;

    public WebSocketConfig() {
        this.mClient = new OkHttpClient();
        this.mClient = ConfigImpl.getCurrentHttpClient();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig
    public void apply() {
        X509TrustManager x509TrustManager;
        RxWebSocketInternal instance = RxWebSocketInternal.getInstance();
        instance.showLog(this.mShowLog, this.mLogTag);
        instance.client(this.mClient);
        instance.reconnectInterval(this.mReconnectInterval);
        instance.pingInterval(this.mPingInterval);
        SSLSocketFactory sSLSocketFactory = this.mSslSocketFactory;
        if (sSLSocketFactory != null && (x509TrustManager = this.mTrustManager) != null) {
            instance.sslSocketFactory(sSLSocketFactory, x509TrustManager);
        }
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig
    public WebSocketConfig client(OkHttpClient client) {
        this.mClient = client;
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig
    public WebSocketConfig sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        this.mSslSocketFactory = sslSocketFactory;
        this.mTrustManager = trustManager;
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig
    public WebSocketConfig reconnectInterval(long interval) {
        this.mReconnectInterval = interval;
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig
    public WebSocketConfig pingInterval(long interval) {
        this.mPingInterval = interval;
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig
    public WebSocketConfig disableLog() {
        this.mShowLog = false;
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig
    public WebSocketConfig showLog() {
        this.mShowLog = true;
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig
    public WebSocketConfig showLog(String logTag) {
        this.mShowLog = true;
        this.mLogTag = logTag;
        return this;
    }
}

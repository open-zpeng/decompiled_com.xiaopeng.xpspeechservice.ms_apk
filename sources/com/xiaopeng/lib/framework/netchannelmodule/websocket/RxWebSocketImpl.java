package com.xiaopeng.lib.framework.netchannelmodule.websocket;

import android.support.annotation.NonNull;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IRxWebSocket;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketConfig;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;
import okio.ByteString;
/* loaded from: classes.dex */
public final class RxWebSocketImpl implements IRxWebSocket {
    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IRxWebSocket
    public IWebSocketConfig config() {
        return new WebSocketConfig();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IRxWebSocket
    public RxWebSocketImpl header(@NonNull String key, @NonNull String value) {
        RxWebSocketInternal.getInstance().header(key, value);
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IRxWebSocket
    public Observable<IWebSocketInfo> get(@NonNull String url) {
        return RxWebSocketInternal.getInstance().getWebSocketInfo(url);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IRxWebSocket
    public Observable<IWebSocketInfo> get(@NonNull String url, long timeout) {
        return RxWebSocketInternal.getInstance().getWebSocketInfo(url, timeout, TimeUnit.MILLISECONDS);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IRxWebSocket
    public void send(@NonNull String url, @NonNull String msg) {
        RxWebSocketInternal.getInstance().send(url, msg);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IRxWebSocket
    public void send(@NonNull String url, @NonNull ByteString byteString) {
        RxWebSocketInternal.getInstance().send(url, byteString);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IRxWebSocket
    public void close(@NonNull String url) {
        RxWebSocketInternal.getInstance().close(url);
    }
}

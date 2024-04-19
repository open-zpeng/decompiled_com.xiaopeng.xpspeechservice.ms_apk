package com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket;

import android.support.annotation.NonNull;
import io.reactivex.Observable;
import okio.ByteString;
/* loaded from: classes.dex */
public interface IRxWebSocket {
    void close(@NonNull String url);

    IWebSocketConfig config();

    Observable<IWebSocketInfo> get(@NonNull String url);

    Observable<IWebSocketInfo> get(@NonNull String url, long timeout);

    IRxWebSocket header(@NonNull String key, @NonNull String value);

    void send(@NonNull String url, @NonNull String msg) throws Exception;

    void send(@NonNull String url, @NonNull ByteString byteString) throws Exception;
}

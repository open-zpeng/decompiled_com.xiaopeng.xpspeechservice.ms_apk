package com.xiaopeng.lib.framework.netchannelmodule.websocket;

import android.support.annotation.Nullable;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo;
import okhttp3.WebSocket;
import okio.ByteString;
/* loaded from: classes.dex */
public class WebSocketInfo implements IWebSocketInfo {
    private static final String EMPTY_REASON_STRING = "";
    private ByteString mByteStringMessage;
    private ClosedReason mClosedReason;
    private boolean mOnReconnect;
    private IWebSocketInfo.STATE mState;
    private String mStringMessage;
    private WebSocket mWebSocket;

    /* loaded from: classes.dex */
    private class ClosedReason {
        private int mCode;
        private String mReason;

        public ClosedReason(int code, String reason) {
            this.mCode = code;
            this.mReason = reason;
        }

        private ClosedReason() {
        }

        public int code() {
            return this.mCode;
        }

        public String reason() {
            return this.mReason;
        }
    }

    private WebSocketInfo() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebSocketInfo(WebSocket webSocket, IWebSocketInfo.STATE state) {
        this.mWebSocket = webSocket;
        this.mState = state;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebSocketInfo(WebSocket webSocket, String mString) {
        this.mWebSocket = webSocket;
        this.mStringMessage = mString;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebSocketInfo(WebSocket webSocket, ByteString byteString) {
        this.mWebSocket = webSocket;
        this.mByteStringMessage = byteString;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static IWebSocketInfo createReconnect() {
        WebSocketInfo socketInfo = new WebSocketInfo();
        socketInfo.mOnReconnect = true;
        return socketInfo;
    }

    public WebSocket getWebSocket() {
        return this.mWebSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.mWebSocket = webSocket;
    }

    public void setClosedReason(int code, String reason) {
        this.mClosedReason = new ClosedReason(code, reason);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo
    @Nullable
    public String stringMessage() {
        return this.mStringMessage;
    }

    public void stringMessage(String string) {
        this.mStringMessage = string;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo
    @Nullable
    public ByteString byteStringMessage() {
        return this.mByteStringMessage;
    }

    public void byteStringMessage(ByteString byteString) {
        this.mByteStringMessage = byteString;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo
    public boolean isOnOpen() {
        return this.mState == IWebSocketInfo.STATE.OPEN;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo
    public boolean isClosed() {
        return this.mState == IWebSocketInfo.STATE.CLOSED;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo
    public boolean isOnReconnect() {
        return this.mOnReconnect;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo
    public int closedReasonCode() {
        ClosedReason closedReason = this.mClosedReason;
        if (closedReason == null) {
            return 0;
        }
        return closedReason.code();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo
    public String closedReason() {
        ClosedReason closedReason = this.mClosedReason;
        if (closedReason == null || closedReason.reason() == null) {
            return "";
        }
        return this.mClosedReason.reason();
    }
}

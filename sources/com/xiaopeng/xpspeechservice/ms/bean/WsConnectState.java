package com.xiaopeng.xpspeechservice.ms.bean;
/* loaded from: classes.dex */
public class WsConnectState {
    public static final int CONNECTED = 1;
    public static final int UNCONNECT = 0;
    public int state;

    public WsConnectState(int state) {
        this.state = state;
    }

    public String toString() {
        return "WsConnectState " + this.state;
    }
}

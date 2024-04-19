package com.xiaopeng.xpspeechservice.ms.bean;
/* loaded from: classes.dex */
public class NetworkState {
    public static final int NETWORK_AVAILABLE = 0;
    public static final int NETWORK_LOSING = 1;
    public static final int NETWORK_LOST = 2;
    public int networkStatus;

    public NetworkState(int status) {
        this.networkStatus = -1;
        this.networkStatus = status;
    }
}

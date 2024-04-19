package com.xiaopeng.xpspeechservice.ms.tts.config;
/* loaded from: classes.dex */
public class OnlineEngineConfig {
    public int dataLengthLimit;
    public int dataTimeOutInterval;
    public int preCacheSize;

    public OnlineEngineConfig(int length, int interval, int size) {
        this.dataLengthLimit = length;
        this.dataTimeOutInterval = interval;
        this.preCacheSize = size;
    }

    public String toString() {
        return "data length limit " + this.dataLengthLimit + " data timeout interval " + this.dataTimeOutInterval + " pre cache size " + this.preCacheSize;
    }
}

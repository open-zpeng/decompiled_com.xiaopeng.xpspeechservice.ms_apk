package com.xiaopeng.xpspeechservice.ms.tts.config;
/* loaded from: classes.dex */
public class AsyncCacheConfig {
    public boolean enable;
    public long requestInterval;
    public int requestRetryCount;

    public AsyncCacheConfig(boolean enable, long requestInterval, int requestRetryCount) {
        this.enable = enable;
        this.requestInterval = requestInterval;
        this.requestRetryCount = requestRetryCount;
    }

    public String toString() {
        return "enable " + this.enable + " requestInterval " + this.requestInterval + " requestRetryCount " + this.requestRetryCount;
    }
}

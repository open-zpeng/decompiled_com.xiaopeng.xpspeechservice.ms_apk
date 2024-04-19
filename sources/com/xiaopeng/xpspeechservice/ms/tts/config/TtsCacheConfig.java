package com.xiaopeng.xpspeechservice.ms.tts.config;
/* loaded from: classes.dex */
public class TtsCacheConfig {
    public long expiredTime;
    public long maxCacheSize;
    public long validPeriod;

    public TtsCacheConfig(long expiredTime, long validPeriod, long maxCacheSize) {
        this.expiredTime = expiredTime;
        this.validPeriod = validPeriod;
        this.maxCacheSize = maxCacheSize;
    }

    public String toString() {
        return "expiredTime " + this.expiredTime + " validPeriod " + this.validPeriod + " maxCacheSize " + this.maxCacheSize;
    }
}

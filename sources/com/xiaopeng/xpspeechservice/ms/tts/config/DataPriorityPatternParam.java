package com.xiaopeng.xpspeechservice.ms.tts.config;
/* loaded from: classes.dex */
public class DataPriorityPatternParam {
    public int offlineStartLatency;
    public int onlineWaitTime;
    public String pattern;

    public DataPriorityPatternParam(String pattern, int offlineStartLatency, int onlineWaitTime) {
        this.pattern = pattern;
        this.offlineStartLatency = offlineStartLatency;
        this.onlineWaitTime = onlineWaitTime;
    }

    public String toString() {
        return "pattern " + this.pattern + " offlineStartLatency " + this.offlineStartLatency + " onlineWaitTime " + this.onlineWaitTime;
    }
}

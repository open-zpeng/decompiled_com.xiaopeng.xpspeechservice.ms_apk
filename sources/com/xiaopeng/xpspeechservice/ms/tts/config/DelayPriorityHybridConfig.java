package com.xiaopeng.xpspeechservice.ms.tts.config;
/* loaded from: classes.dex */
public class DelayPriorityHybridConfig {
    public int offlineStartLatency;
    public int onlineWaitTime;

    public DelayPriorityHybridConfig(int offlineStartLatency, int onlineWaitTime) {
        this.offlineStartLatency = offlineStartLatency;
        this.onlineWaitTime = onlineWaitTime;
    }

    public String toString() {
        return "offline start latency " + this.offlineStartLatency + " online wait time " + this.onlineWaitTime;
    }
}

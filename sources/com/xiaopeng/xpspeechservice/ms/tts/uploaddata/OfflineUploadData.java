package com.xiaopeng.xpspeechservice.ms.tts.uploaddata;
/* loaded from: classes.dex */
public class OfflineUploadData {
    public int offlineFirstDataLatency;
    public int offlineSynthTime;
    public EngineEndType state;

    public OfflineUploadData(int offlineFirstDataLatency, int offlineSynthTime, EngineEndType state) {
        this.offlineFirstDataLatency = offlineFirstDataLatency;
        this.offlineSynthTime = offlineSynthTime;
        this.state = state;
    }
}

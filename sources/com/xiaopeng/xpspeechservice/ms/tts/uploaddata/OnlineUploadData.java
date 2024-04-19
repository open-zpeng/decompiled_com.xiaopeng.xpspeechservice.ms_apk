package com.xiaopeng.xpspeechservice.ms.tts.uploaddata;
/* loaded from: classes.dex */
public class OnlineUploadData {
    public int onlineDataLength;
    public int onlineDataMaxLatency;
    public String onlineDataType;
    public int onlineDecodeLatency;
    public int onlineFirstDataLatency;
    public String onlineMsgId;
    public int onlineSynthTime;
    public EngineEndType state;

    public OnlineUploadData(String onlineDataType, String onlineMsgId, int onlineFirstDataLatency, int onlineDataMaxLatency, int onlineDecodeLatency, int onlineSynthTime, int onlineDataLength, EngineEndType state) {
        this.onlineDataType = onlineDataType;
        this.onlineMsgId = onlineMsgId;
        this.onlineFirstDataLatency = onlineFirstDataLatency;
        this.onlineDataMaxLatency = onlineDataMaxLatency;
        this.onlineDecodeLatency = onlineDecodeLatency;
        this.onlineSynthTime = onlineSynthTime;
        this.onlineDataLength = onlineDataLength;
        this.state = state;
    }
}

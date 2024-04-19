package com.xiaopeng.xpspeechservice.ms.tts.uploaddata;
/* loaded from: classes.dex */
public class HybridUploadData {
    public int dataLength;
    public int dataMaxLatency;
    public long endTime;
    public int firstFrameLatency;
    public TtsModeType mode;
    public EngineEndType state;

    public HybridUploadData(TtsModeType mode, int firstFrameLatency, int dataMaxLatency, int dataLength, EngineEndType state, long endTime) {
        this.mode = mode;
        this.firstFrameLatency = firstFrameLatency;
        this.dataMaxLatency = dataMaxLatency;
        this.dataLength = dataLength;
        this.state = state;
        this.endTime = endTime;
    }
}

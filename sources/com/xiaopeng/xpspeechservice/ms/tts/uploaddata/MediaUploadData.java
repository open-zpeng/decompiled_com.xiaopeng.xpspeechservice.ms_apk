package com.xiaopeng.xpspeechservice.ms.tts.uploaddata;
/* loaded from: classes.dex */
public class MediaUploadData {
    public int dataLength;
    public int dataMaxLatency;
    public long endTime;
    public int mediaDecodeLatency;
    public int mediaFirstFrameLatency;
    public TtsModeType mode;
    public EngineEndType state;

    public MediaUploadData(TtsModeType mode, int mediaDecodeLatency, int mediaFirstFrameLatency, int dataMaxLatency, int dataLength, EngineEndType state, long endTime) {
        this.mode = mode;
        this.mediaDecodeLatency = mediaDecodeLatency;
        this.mediaFirstFrameLatency = mediaFirstFrameLatency;
        this.dataMaxLatency = dataMaxLatency;
        this.dataLength = dataLength;
        this.state = state;
        this.endTime = endTime;
    }
}

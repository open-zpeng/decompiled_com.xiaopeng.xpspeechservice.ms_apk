package com.xiaopeng.xpspeechservice.ms.tts.uploaddata;
/* loaded from: classes.dex */
public class SrcUploadData {
    public String source;
    public long startTime;
    public String text;

    public SrcUploadData(String text, String source, long startTime) {
        this.text = text;
        this.source = source;
        this.startTime = startTime;
    }
}

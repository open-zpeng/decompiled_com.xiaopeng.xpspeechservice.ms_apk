package com.xiaopeng.speech.tts;
/* loaded from: classes.dex */
public class XpTtsStackItem {
    public String nlgId;
    public int predictTime;
    public int priority;
    public float rate;
    public String source;
    public int streamType;
    public String text;
    public long time;
    public String ttsId;
    public float volume;

    public XpTtsStackItem(String source, int predictTime, int priority, long time, String text, String ttsId, String nlgId, int streamType, float rate, float volume) {
        this.source = source;
        this.predictTime = predictTime;
        this.priority = priority;
        this.time = time;
        this.text = text;
        this.ttsId = ttsId;
        this.nlgId = nlgId;
        this.streamType = streamType;
        this.rate = rate;
        this.volume = volume;
    }
}

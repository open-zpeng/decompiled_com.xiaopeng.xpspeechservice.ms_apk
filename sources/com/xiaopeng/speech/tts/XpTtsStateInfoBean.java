package com.xiaopeng.speech.tts;
/* loaded from: classes.dex */
public class XpTtsStateInfoBean {
    public int channel;
    public XpTtsInfoExtra extra;
    public String nlgId;
    public int predictTime;
    public int priority;
    public float rate;
    public String source;
    public int streamType;
    public String text;
    public long time;
    public String ttsId;
    public String type;
    public float volume;

    public XpTtsStateInfoBean(XpTtsInfoExtra extra, String source, int priority, int predictTime, long time, String text, String ttsId, String nlgId, int channel, int streamType, float rate, float volume, String type) {
        this.extra = extra;
        this.source = source;
        this.priority = priority;
        this.predictTime = predictTime;
        this.time = time;
        this.text = text;
        this.ttsId = ttsId;
        this.nlgId = nlgId;
        this.channel = channel;
        this.streamType = streamType;
        this.rate = rate;
        this.volume = volume;
        this.type = type;
    }
}

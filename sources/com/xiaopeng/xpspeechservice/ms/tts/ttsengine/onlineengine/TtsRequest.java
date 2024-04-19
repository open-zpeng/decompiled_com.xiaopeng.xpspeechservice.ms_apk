package com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine;
/* loaded from: classes.dex */
public class TtsRequest {
    private String caller;
    private String msgId;
    private String msgType;
    private String text;
    private String textType;

    public TtsRequest(String msgId, String msgType, String textType, String text, String caller) {
        this.msgId = msgId;
        this.msgType = msgType;
        this.textType = textType;
        this.text = text;
        this.caller = caller;
    }
}

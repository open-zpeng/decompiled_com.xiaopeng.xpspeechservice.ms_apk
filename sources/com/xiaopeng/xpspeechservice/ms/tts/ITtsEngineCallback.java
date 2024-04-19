package com.xiaopeng.xpspeechservice.ms.tts;
/* loaded from: classes.dex */
public interface ITtsEngineCallback {
    void onEvent(EventType eventType);

    void onEvent(EventType eventType, Object obj);
}

package com.xiaopeng.xpspeechservice.ms.tts.ttsengine;

import android.os.Bundle;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback;
/* loaded from: classes.dex */
public interface IHybridEngine {
    void onHybridEvent(EventType eventType, Object obj);

    void speak(Bundle bundle, IEngineCallback iEngineCallback);

    void stop();
}

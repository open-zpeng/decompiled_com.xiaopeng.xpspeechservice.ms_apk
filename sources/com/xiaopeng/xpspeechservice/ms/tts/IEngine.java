package com.xiaopeng.xpspeechservice.ms.tts;

import android.os.Bundle;
/* loaded from: classes.dex */
public interface IEngine {
    int speak(Bundle bundle, IEngineCallback iEngineCallback);

    void stop();
}

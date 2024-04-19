package com.xiaopeng.xpspeechservice.ms.tts.ttsengine;

import android.os.Bundle;
/* loaded from: classes.dex */
public interface ITtsEngine {
    int getData(byte[] bArr);

    void speak(Bundle bundle);

    void stop();
}

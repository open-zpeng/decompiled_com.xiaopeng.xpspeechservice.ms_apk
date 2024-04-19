package com.xiaopeng.xpspeechservice.ms.tts.ttsengine;

import android.os.Bundle;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
/* loaded from: classes.dex */
public interface IDataCallback {
    int getData(byte[] bArr);

    void onEvent(EventType eventType);

    void onEvent(EventType eventType, Bundle bundle);
}

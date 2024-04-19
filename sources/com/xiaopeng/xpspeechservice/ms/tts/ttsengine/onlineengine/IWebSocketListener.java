package com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine;
/* loaded from: classes.dex */
public interface IWebSocketListener {
    void onClosed(int i, String str);

    void onError(Throwable th);

    void onMessage(String str);

    void onOpen();

    void onReceive(byte[] bArr);
}

package com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine;

import com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback;
/* loaded from: classes.dex */
public class NetworkController {
    private static final String TAG = "NetworkController";
    private ITtsEngineCallback mCallback;
    private XpWebSocketHelper mXpWebSocketHelper;

    public NetworkController(ITtsEngineCallback cb) {
        this.mCallback = cb;
    }
}

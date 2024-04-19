package com.xiaopeng.speech.tts;

import com.xiaopeng.speech.tts.XpTextToSpeechServiceBase;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class XpPlaybackQueueItem implements Runnable {
    private final Object mCallerIdentity;
    private final XpTextToSpeechServiceBase.UtteranceProgressDispatcher mDispatcher;

    @Override // java.lang.Runnable
    public abstract void run();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void stop(int i);

    /* JADX INFO: Access modifiers changed from: package-private */
    public XpPlaybackQueueItem(XpTextToSpeechServiceBase.UtteranceProgressDispatcher dispatcher, Object callerIdentity) {
        this.mDispatcher = dispatcher;
        this.mCallerIdentity = callerIdentity;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Object getCallerIdentity() {
        return this.mCallerIdentity;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public XpTextToSpeechServiceBase.UtteranceProgressDispatcher getDispatcher() {
        return this.mDispatcher;
    }
}

package com.microsoft.cognitiveservices.speech.audio;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
/* loaded from: classes.dex */
public class AudioOutputStream implements AutoCloseable {
    protected SafeHandle streamHandle;

    static {
        try {
            Class.forName(SpeechConfig.class.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AudioOutputStream(IntRef intRef) {
        Contracts.throwIfNull(intRef, "stream");
        this.streamHandle = new SafeHandle(intRef.getValue(), SafeHandleType.AudioOutputStream);
    }

    public static PullAudioOutputStream createPullStream() {
        return PullAudioOutputStream.create();
    }

    public static PushAudioOutputStream createPushStream(PushAudioOutputStreamCallback pushAudioOutputStreamCallback) {
        return PushAudioOutputStream.create(pushAudioOutputStreamCallback);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        SafeHandle safeHandle = this.streamHandle;
        if (safeHandle != null) {
            safeHandle.close();
        }
        this.streamHandle = null;
    }

    public SafeHandle getImpl() {
        return this.streamHandle;
    }
}

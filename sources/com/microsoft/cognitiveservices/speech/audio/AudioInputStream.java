package com.microsoft.cognitiveservices.speech.audio;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
/* loaded from: classes.dex */
public class AudioInputStream implements AutoCloseable {
    protected SafeHandle streamHandle;

    static {
        try {
            Class.forName(SpeechConfig.class.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public AudioInputStream(SafeHandle safeHandle) {
        this.streamHandle = null;
        Contracts.throwIfNull(safeHandle, "stream");
        this.streamHandle = safeHandle;
    }

    public static PullAudioInputStream createPullStream(PullAudioInputStreamCallback pullAudioInputStreamCallback) {
        return PullAudioInputStream.create(pullAudioInputStreamCallback);
    }

    public static PullAudioInputStream createPullStream(PullAudioInputStreamCallback pullAudioInputStreamCallback, AudioStreamFormat audioStreamFormat) {
        return PullAudioInputStream.create(pullAudioInputStreamCallback, audioStreamFormat);
    }

    public static PushAudioInputStream createPushStream() {
        return PushAudioInputStream.create();
    }

    public static PushAudioInputStream createPushStream(AudioStreamFormat audioStreamFormat) {
        return PushAudioInputStream.create(audioStreamFormat);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        SafeHandle safeHandle = this.streamHandle;
        if (safeHandle != null) {
            safeHandle.close();
            this.streamHandle = null;
        }
    }

    public SafeHandle getImpl() {
        return this.streamHandle;
    }
}

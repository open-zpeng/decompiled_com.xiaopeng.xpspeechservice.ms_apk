package com.microsoft.cognitiveservices.speech.audio;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
/* loaded from: classes.dex */
public final class PullAudioInputStream extends AudioInputStream {
    private PullAudioInputStreamCallback callbackHandle;

    static {
        try {
            Class.forName(SpeechConfig.class.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    protected PullAudioInputStream(SafeHandle safeHandle, PullAudioInputStreamCallback pullAudioInputStreamCallback) {
        super(safeHandle);
        Contracts.throwIfNull(this.streamHandle, "streamHandle");
        this.callbackHandle = pullAudioInputStreamCallback;
        Contracts.throwIfFail(setStreamCallbacks(this.streamHandle));
    }

    public static PullAudioInputStream create(PullAudioInputStreamCallback pullAudioInputStreamCallback) {
        SafeHandle safeHandle = new SafeHandle(0L, SafeHandleType.AudioInputStream);
        Contracts.throwIfFail(createPullAudioInputStream(safeHandle, null));
        return new PullAudioInputStream(safeHandle, pullAudioInputStreamCallback);
    }

    public static PullAudioInputStream create(PullAudioInputStreamCallback pullAudioInputStreamCallback, AudioStreamFormat audioStreamFormat) {
        SafeHandle safeHandle = new SafeHandle(0L, SafeHandleType.AudioInputStream);
        Contracts.throwIfFail(createPullAudioInputStream(safeHandle, audioStreamFormat.getImpl()));
        return new PullAudioInputStream(safeHandle, pullAudioInputStreamCallback);
    }

    private static final native long createPullAudioInputStream(SafeHandle safeHandle, SafeHandle safeHandle2);

    private PullAudioInputStreamCallback getCallbackHandle() {
        return this.callbackHandle;
    }

    private final native long setStreamCallbacks(SafeHandle safeHandle);

    @Override // com.microsoft.cognitiveservices.speech.audio.AudioInputStream, java.lang.AutoCloseable
    public void close() {
        this.callbackHandle = null;
        super.close();
    }
}

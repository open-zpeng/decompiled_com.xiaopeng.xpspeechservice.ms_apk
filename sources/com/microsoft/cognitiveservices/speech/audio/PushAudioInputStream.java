package com.microsoft.cognitiveservices.speech.audio;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.util.CloseGuard;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
/* loaded from: classes.dex */
public final class PushAudioInputStream extends AudioInputStream {
    private CloseGuard cg;

    static {
        try {
            Class.forName(SpeechConfig.class.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    protected PushAudioInputStream(SafeHandle safeHandle) {
        super(safeHandle);
        this.cg = new CloseGuard();
    }

    public static PushAudioInputStream create() {
        SafeHandle safeHandle = new SafeHandle(0L, SafeHandleType.AudioInputStream);
        Contracts.throwIfFail(createPushAudioInputStream(safeHandle, null));
        return new PushAudioInputStream(safeHandle);
    }

    public static PushAudioInputStream create(AudioStreamFormat audioStreamFormat) {
        SafeHandle safeHandle = new SafeHandle(0L, SafeHandleType.AudioInputStream);
        Contracts.throwIfFail(createPushAudioInputStream(safeHandle, audioStreamFormat.getImpl()));
        return new PushAudioInputStream(safeHandle);
    }

    private static final native long createPushAudioInputStream(SafeHandle safeHandle, SafeHandle safeHandle2);

    private final native long pushAudioInputStreamClose(SafeHandle safeHandle);

    private final native long pushAudioInputStreamWrite(SafeHandle safeHandle, byte[] bArr, int i);

    @Override // com.microsoft.cognitiveservices.speech.audio.AudioInputStream, java.lang.AutoCloseable
    public void close() {
        this.cg.closeObject();
        if (this.streamHandle != null) {
            Contracts.throwIfFail(pushAudioInputStreamClose(this.streamHandle));
        }
        super.close();
    }

    public void write(byte[] bArr) {
        this.cg.enterUseObject();
        try {
            Contracts.throwIfFail(pushAudioInputStreamWrite(this.streamHandle, bArr, bArr.length));
        } finally {
            this.cg.exitUseObject();
        }
    }
}

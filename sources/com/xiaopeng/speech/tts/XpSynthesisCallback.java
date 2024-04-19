package com.xiaopeng.speech.tts;

import android.os.Bundle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/* loaded from: classes.dex */
public interface XpSynthesisCallback {

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface SupportedAudioFormat {
    }

    int audioAvailable(byte[] bArr, int i, int i2);

    int done();

    void error();

    void error(int i);

    int getMaxBufferSize();

    boolean hasFinished();

    boolean hasStarted();

    int start(int i, int i2, int i3);

    void stop();

    void uploadInfo(Bundle bundle);

    default void rangeStart(int markerInFrames, int start, int end) {
    }
}

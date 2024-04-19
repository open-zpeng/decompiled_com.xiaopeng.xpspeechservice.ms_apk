package com.xiaopeng.speech.tts;

import android.os.Bundle;
import android.util.Log;
import com.xiaopeng.speech.tts.XpTextToSpeechServiceBase;
/* loaded from: classes.dex */
class XpSimpleSynthesisCallback implements XpSynthesisCallback {
    private static final boolean DBG = true;
    private static final int MIN_AUDIO_BUFFER_SIZE = 8192;
    private static final String TAG = "XpSimpleSynthesisCallback";
    private final XpTextToSpeechServiceBase.UtteranceProgressDispatcher mDispatcher;
    private final Object mStateLock = new Object();
    private volatile boolean mStarted = false;
    private volatile boolean mDone = false;
    protected volatile int mStatusCode = 0;

    /* JADX INFO: Access modifiers changed from: package-private */
    public XpSimpleSynthesisCallback(XpTextToSpeechServiceBase.UtteranceProgressDispatcher dispatcher) {
        this.mDispatcher = dispatcher;
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public void stop() {
        Log.d(TAG, "stop()");
        synchronized (this.mStateLock) {
            if (this.mDone) {
                Log.w(TAG, "stop() already done");
            } else if (this.mStatusCode == -2) {
                Log.w(TAG, "stop() called twice");
            } else {
                this.mStatusCode = -2;
                this.mDispatcher.dispatchOnStop();
            }
        }
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public int getMaxBufferSize() {
        return MIN_AUDIO_BUFFER_SIZE;
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public boolean hasStarted() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mStarted;
        }
        return z;
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public boolean hasFinished() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mDone;
        }
        return z;
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public int start(int sampleRateInHz, int audioFormat, int channelCount) {
        Log.d(TAG, "start(" + sampleRateInHz + "," + audioFormat + "," + channelCount + ")");
        if (audioFormat != 3 && audioFormat != 2 && audioFormat != 4) {
            Log.w(TAG, "Audio format encoding " + audioFormat + " not supported. Please use one of AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT or AudioFormat.ENCODING_PCM_FLOAT");
        }
        synchronized (this.mStateLock) {
            if (this.mStatusCode == -2) {
                Log.d(TAG, "stop() called before start(), returning.");
                return -1;
            } else if (this.mStatusCode != 0) {
                Log.d(TAG, "Error was raised");
                return -1;
            } else if (this.mStarted) {
                Log.w(TAG, "Duplicate call to start()");
                return -1;
            } else {
                this.mStarted = true;
                this.mDispatcher.dispatchOnBeginSynthesis(sampleRateInHz, audioFormat, channelCount);
                this.mDispatcher.dispatchOnStart();
                return 0;
            }
        }
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public int audioAvailable(byte[] buffer, int offset, int length) {
        Log.d(TAG, "audioAvailable(byte[" + buffer.length + "]," + offset + "," + length + ")");
        if (length > getMaxBufferSize() || length <= 0) {
            throw new IllegalArgumentException("buffer is too large or of zero length (" + length + " bytes)");
        }
        synchronized (this.mStateLock) {
            if (this.mStatusCode == -2) {
                Log.w(TAG, "already stop");
                return -1;
            }
            this.mDispatcher.dispatchOnAudioAvailable(buffer);
            return 0;
        }
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public int done() {
        Log.d(TAG, "done()");
        synchronized (this.mStateLock) {
            if (this.mDone) {
                Log.w(TAG, "Duplicate call to done()");
                return -1;
            } else if (this.mStatusCode == -2) {
                Log.w(TAG, "already stop");
                return -1;
            } else {
                int statusCode = this.mStatusCode;
                this.mDone = true;
                if (statusCode == 0) {
                    this.mDispatcher.dispatchOnSuccess();
                    return 0;
                }
                this.mDispatcher.dispatchOnError(statusCode);
                return 0;
            }
        }
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public void error() {
        error(-3);
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public void error(int errorCode) {
        Log.d(TAG, "error " + errorCode);
        synchronized (this.mStateLock) {
            if (this.mDone) {
                return;
            }
            if (this.mStatusCode == -2) {
                Log.w(TAG, "already stop");
            } else {
                this.mStatusCode = errorCode;
            }
        }
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public void uploadInfo(Bundle info) {
        this.mDispatcher.dispatchTtsInfo(info);
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public void rangeStart(int markerInFrames, int start, int end) {
    }
}

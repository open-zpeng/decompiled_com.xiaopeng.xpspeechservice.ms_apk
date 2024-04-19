package com.xiaopeng.speech.tts;

import android.os.Bundle;
import android.util.Log;
import com.xiaopeng.speech.tts.XpTextToSpeechServiceBase;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class XpPlaybackSynthesisCallback implements XpSynthesisCallback {
    private static final boolean DBG = true;
    private static final int MIN_AUDIO_BUFFER_SIZE = 8192;
    private static final String TAG = "XpPlaybackSynthesisRequest";
    private final XpTextToSpeechServiceBase.AudioOutputParams mAudioParams;
    private final XpAudioPlaybackHandler mAudioTrackHandler;
    private final Object mCallerIdentity;
    private final XpTextToSpeechServiceBase.UtteranceProgressDispatcher mDispatcher;
    private final Object mStateLock = new Object();
    private XpSynthesisPlaybackQueueItem mItem = null;
    private volatile boolean mDone = false;
    protected volatile int mStatusCode = 0;

    /* JADX INFO: Access modifiers changed from: package-private */
    public XpPlaybackSynthesisCallback(XpTextToSpeechServiceBase.AudioOutputParams audioParams, XpAudioPlaybackHandler audioTrackHandler, XpTextToSpeechServiceBase.UtteranceProgressDispatcher dispatcher, Object callerIdentity) {
        this.mAudioParams = audioParams;
        this.mAudioTrackHandler = audioTrackHandler;
        this.mDispatcher = dispatcher;
        this.mCallerIdentity = callerIdentity;
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public void stop() {
        Log.d(TAG, "stop()");
        synchronized (this.mStateLock) {
            if (this.mDone) {
                return;
            }
            if (this.mStatusCode == -2) {
                Log.w(TAG, "stop() called twice");
                return;
            }
            XpSynthesisPlaybackQueueItem item = this.mItem;
            this.mStatusCode = -2;
            if (item != null) {
                item.stop(-2);
            } else {
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
            z = this.mItem != null;
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
        this.mDispatcher.dispatchOnBeginSynthesis(sampleRateInHz, audioFormat, channelCount);
        int channelConfig = XpBlockingAudioTrack.getChannelConfig(channelCount);
        synchronized (this.mStateLock) {
            if (channelConfig == 0) {
                Log.e(TAG, "Unsupported number of channels :" + channelCount);
                this.mStatusCode = -5;
                return -1;
            } else if (this.mStatusCode == -2) {
                Log.d(TAG, "stop() called before start(), returning.");
                return -1;
            } else if (this.mStatusCode != 0) {
                Log.d(TAG, "Error was raised " + this.mStatusCode);
                return -1;
            } else if (this.mItem != null) {
                Log.e(TAG, "Start called twice");
                return -1;
            } else {
                XpSynthesisPlaybackQueueItem item = new XpSynthesisPlaybackQueueItem(this.mAudioParams, sampleRateInHz, audioFormat, channelCount, this.mDispatcher, this.mCallerIdentity);
                this.mAudioTrackHandler.enqueue(item);
                this.mItem = item;
                return 0;
            }
        }
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public int audioAvailable(byte[] buffer, int offset, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("buffer is too large or of zero length (" + length + " bytes)");
        }
        synchronized (this.mStateLock) {
            if (this.mStatusCode == -2) {
                Log.w(TAG, "audioAvailable: playback already stopped");
                return -1;
            } else if (this.mItem == null) {
                Log.e(TAG, "audioAvailable: item is still null");
                this.mStatusCode = -5;
                return -1;
            } else if (this.mStatusCode != 0) {
                Log.d(TAG, "audioAvailable: Error was raised " + this.mStatusCode);
                return -1;
            } else {
                XpSynthesisPlaybackQueueItem item = this.mItem;
                byte[] bufferCopy = new byte[length];
                System.arraycopy(buffer, offset, bufferCopy, 0, length);
                try {
                    item.put(bufferCopy);
                    return 0;
                } catch (InterruptedException ie) {
                    synchronized (this.mStateLock) {
                        Log.e(TAG, "item put interrupted", ie);
                        this.mStatusCode = -5;
                        return -1;
                    }
                }
            }
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
                Log.d(TAG, "Request has been aborted.");
                return -1;
            } else {
                this.mDone = true;
                if (this.mItem == null) {
                    Log.w(TAG, "done() was called before start() call");
                    if (this.mStatusCode == 0) {
                        this.mDispatcher.dispatchOnSuccess();
                    } else {
                        this.mDispatcher.dispatchOnError(this.mStatusCode);
                    }
                    return -1;
                }
                XpSynthesisPlaybackQueueItem item = this.mItem;
                int statusCode = this.mStatusCode;
                if (statusCode == 0) {
                    item.done();
                    return 0;
                }
                item.stop(statusCode);
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
        Log.d(TAG, "error() [will call stop]");
        synchronized (this.mStateLock) {
            if (this.mDone) {
                return;
            }
            this.mStatusCode = errorCode;
        }
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public void uploadInfo(Bundle info) {
        this.mDispatcher.dispatchTtsInfo(info);
    }

    @Override // com.xiaopeng.speech.tts.XpSynthesisCallback
    public void rangeStart(int markerInFrames, int start, int end) {
        XpSynthesisPlaybackQueueItem xpSynthesisPlaybackQueueItem = this.mItem;
        if (xpSynthesisPlaybackQueueItem == null) {
            Log.e(TAG, "mItem is null");
        } else {
            xpSynthesisPlaybackQueueItem.rangeStart(markerInFrames, start, end);
        }
    }
}

package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.util.AsyncThreadService;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.EventHandlerImpl;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
import java.util.concurrent.atomic.AtomicInteger;
/* loaded from: classes.dex */
public class Recognizer implements AutoCloseable {
    private AudioConfig audioInputKeepAlive;
    protected SafeHandle recoHandle;
    protected AtomicInteger eventCounter = new AtomicInteger(0);
    public final EventHandlerImpl<SessionEventArgs> sessionStarted = new EventHandlerImpl<>(this.eventCounter);
    public final EventHandlerImpl<SessionEventArgs> sessionStopped = new EventHandlerImpl<>(this.eventCounter);
    public final EventHandlerImpl<RecognitionEventArgs> speechStartDetected = new EventHandlerImpl<>(this.eventCounter);
    public final EventHandlerImpl<RecognitionEventArgs> speechEndDetected = new EventHandlerImpl<>(this.eventCounter);
    protected boolean disposed = false;
    private final Object recognizerLock = new Object();
    private int activeAsyncRecognitionCounter = 0;

    /* JADX INFO: Access modifiers changed from: protected */
    public Recognizer(AudioConfig audioConfig) {
        this.recoHandle = null;
        this.audioInputKeepAlive = null;
        AsyncThreadService.initialize();
        this.recoHandle = new SafeHandle(0L, SafeHandleType.Recognizer);
        this.audioInputKeepAlive = audioConfig;
    }

    private final native long recognizeOnce(SafeHandle safeHandle, IntRef intRef);

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long canceledSetCallback(long j);

    @Override // java.lang.AutoCloseable
    public void close() {
        synchronized (this.recognizerLock) {
            if (this.activeAsyncRecognitionCounter != 0) {
                throw new IllegalStateException("Cannot dispose a recognizer while async recognition is running. Await async recognitions to avoid unexpected disposals.");
            }
            dispose(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void dispose(boolean z) {
        if (this.disposed) {
            return;
        }
        AsyncThreadService.shutdown();
        this.audioInputKeepAlive = null;
        this.disposed = true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void doAsyncRecognitionAction(Runnable runnable) {
        synchronized (this.recognizerLock) {
            this.activeAsyncRecognitionCounter++;
        }
        if (this.disposed) {
            throw new IllegalStateException(getClass().getName());
        }
        try {
            runnable.run();
            synchronized (this.recognizerLock) {
                this.activeAsyncRecognitionCounter--;
            }
        } catch (Throwable th) {
            synchronized (this.recognizerLock) {
                this.activeAsyncRecognitionCounter--;
                throw th;
            }
        }
    }

    public SafeHandle getImpl() {
        return this.recoHandle;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long getPropertyBagFromRecognizerHandle(SafeHandle safeHandle, IntRef intRef);

    /* JADX INFO: Access modifiers changed from: protected */
    public long recognize() {
        Contracts.throwIfNull(this.recoHandle, "Invalid recognizer handle");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(recognizeOnce(this.recoHandle, intRef));
        return intRef.getValue();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long recognizedSetCallback(long j);

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long recognizingSetCallback(long j);

    protected void sessionStartedEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            SessionEventArgs sessionEventArgs = new SessionEventArgs(j, true);
            EventHandlerImpl<SessionEventArgs> eventHandlerImpl = this.sessionStarted;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, sessionEventArgs);
            }
        } catch (Exception e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long sessionStartedSetCallback(long j);

    protected void sessionStoppedEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            SessionEventArgs sessionEventArgs = new SessionEventArgs(j, true);
            EventHandlerImpl<SessionEventArgs> eventHandlerImpl = this.sessionStopped;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, sessionEventArgs);
            }
        } catch (Exception e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long sessionStoppedSetCallback(long j);

    protected void speechEndDetectedEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            RecognitionEventArgs recognitionEventArgs = new RecognitionEventArgs(j, true);
            EventHandlerImpl<RecognitionEventArgs> eventHandlerImpl = this.speechEndDetected;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, recognitionEventArgs);
            }
        } catch (Exception e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long speechEndDetectedSetCallback(long j);

    protected void speechStartDetectedEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            RecognitionEventArgs recognitionEventArgs = new RecognitionEventArgs(j, true);
            EventHandlerImpl<RecognitionEventArgs> eventHandlerImpl = this.speechStartDetected;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, recognitionEventArgs);
            }
        } catch (Exception e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long speechStartDetectedSetCallback(long j);

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long startContinuousRecognition(SafeHandle safeHandle);

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long startKeywordRecognition(SafeHandle safeHandle, SafeHandle safeHandle2);

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long stopContinuousRecognition(SafeHandle safeHandle);

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long stopKeywordRecognition(SafeHandle safeHandle);
}

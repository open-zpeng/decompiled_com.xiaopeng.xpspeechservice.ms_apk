package com.microsoft.cognitiveservices.speech.translation;

import com.microsoft.cognitiveservices.speech.PropertyCollection;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.Recognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.util.AsyncThreadService;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.EventHandlerImpl;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
/* loaded from: classes.dex */
public final class TranslationRecognizer extends Recognizer {
    static Set<TranslationRecognizer> translationRecognizerObjects = Collections.synchronizedSet(new HashSet());
    public final EventHandlerImpl<TranslationRecognitionCanceledEventArgs> canceled;
    private PropertyCollection propertyHandle;
    public final EventHandlerImpl<TranslationRecognitionEventArgs> recognized;
    public final EventHandlerImpl<TranslationRecognitionEventArgs> recognizing;
    public final EventHandlerImpl<TranslationSynthesisEventArgs> synthesizing;
    SafeHandle translationSynthesisHandle;

    public TranslationRecognizer(SpeechTranslationConfig speechTranslationConfig) {
        super(null);
        this.recognizing = new EventHandlerImpl<>(this.eventCounter);
        this.recognized = new EventHandlerImpl<>(this.eventCounter);
        this.canceled = new EventHandlerImpl<>(this.eventCounter);
        this.synthesizing = new EventHandlerImpl<>(this.eventCounter);
        this.propertyHandle = null;
        this.translationSynthesisHandle = null;
        Contracts.throwIfNull(speechTranslationConfig, "stc");
        Contracts.throwIfNull(this.recoHandle, "recoHandle");
        Contracts.throwIfFail(createTranslationRecognizerFromConfig(this.recoHandle, speechTranslationConfig.getImpl(), null));
        initialize();
    }

    public TranslationRecognizer(SpeechTranslationConfig speechTranslationConfig, AudioConfig audioConfig) {
        super(audioConfig);
        this.recognizing = new EventHandlerImpl<>(this.eventCounter);
        this.recognized = new EventHandlerImpl<>(this.eventCounter);
        this.canceled = new EventHandlerImpl<>(this.eventCounter);
        this.synthesizing = new EventHandlerImpl<>(this.eventCounter);
        this.propertyHandle = null;
        this.translationSynthesisHandle = null;
        Contracts.throwIfNull(speechTranslationConfig, "stc");
        Contracts.throwIfFail(audioConfig == null ? createTranslationRecognizerFromConfig(this.recoHandle, speechTranslationConfig.getImpl(), null) : createTranslationRecognizerFromConfig(this.recoHandle, speechTranslationConfig.getImpl(), audioConfig.getImpl()));
        initialize();
    }

    private final native long addTargetLanguage(SafeHandle safeHandle, String str);

    private void canceledEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            TranslationRecognitionCanceledEventArgs translationRecognitionCanceledEventArgs = new TranslationRecognitionCanceledEventArgs(j, true);
            EventHandlerImpl<TranslationRecognitionCanceledEventArgs> eventHandlerImpl = this.canceled;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, translationRecognitionCanceledEventArgs);
            }
        } catch (Exception e) {
        }
    }

    private final native long createTranslationRecognizerFromConfig(SafeHandle safeHandle, SafeHandle safeHandle2, SafeHandle safeHandle3);

    private void initialize() {
        this.translationSynthesisHandle = new SafeHandle(this.recoHandle.getValue(), SafeHandleType.TranslationSynthesis);
        this.recognizing.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.4
            @Override // java.lang.Runnable
            public void run() {
                TranslationRecognizer.translationRecognizerObjects.add(this);
                Contracts.throwIfFail(TranslationRecognizer.this.recognizingSetCallback(this.recoHandle.getValue()));
            }
        });
        this.recognized.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.5
            @Override // java.lang.Runnable
            public void run() {
                TranslationRecognizer.translationRecognizerObjects.add(this);
                Contracts.throwIfFail(TranslationRecognizer.this.recognizedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.synthesizing.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.6
            @Override // java.lang.Runnable
            public void run() {
                TranslationRecognizer.translationRecognizerObjects.add(this);
                Contracts.throwIfFail(TranslationRecognizer.this.synthesizingSetCallback(this.recoHandle.getValue()));
            }
        });
        this.canceled.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.7
            @Override // java.lang.Runnable
            public void run() {
                TranslationRecognizer.translationRecognizerObjects.add(this);
                Contracts.throwIfFail(TranslationRecognizer.this.canceledSetCallback(this.recoHandle.getValue()));
            }
        });
        this.sessionStarted.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.8
            @Override // java.lang.Runnable
            public void run() {
                TranslationRecognizer.translationRecognizerObjects.add(this);
                Contracts.throwIfFail(TranslationRecognizer.this.sessionStartedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.sessionStopped.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.9
            @Override // java.lang.Runnable
            public void run() {
                TranslationRecognizer.translationRecognizerObjects.add(this);
                Contracts.throwIfFail(TranslationRecognizer.this.sessionStoppedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.speechStartDetected.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.10
            @Override // java.lang.Runnable
            public void run() {
                TranslationRecognizer.translationRecognizerObjects.add(this);
                Contracts.throwIfFail(TranslationRecognizer.this.speechStartDetectedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.speechEndDetected.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.11
            @Override // java.lang.Runnable
            public void run() {
                TranslationRecognizer.translationRecognizerObjects.add(this);
                Contracts.throwIfFail(TranslationRecognizer.this.speechEndDetectedSetCallback(this.recoHandle.getValue()));
            }
        });
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(getPropertyBagFromRecognizerHandle(this.recoHandle, intRef));
        this.propertyHandle = new PropertyCollection(intRef);
    }

    private void recognizedEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            TranslationRecognitionEventArgs translationRecognitionEventArgs = new TranslationRecognitionEventArgs(j, true);
            EventHandlerImpl<TranslationRecognitionEventArgs> eventHandlerImpl = this.recognized;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, translationRecognitionEventArgs);
            }
        } catch (Exception e) {
        }
    }

    private void recognizingEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            TranslationRecognitionEventArgs translationRecognitionEventArgs = new TranslationRecognitionEventArgs(j, true);
            EventHandlerImpl<TranslationRecognitionEventArgs> eventHandlerImpl = this.recognizing;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, translationRecognitionEventArgs);
            }
        } catch (Exception e) {
        }
    }

    private final native long removeTargetLanguage(SafeHandle safeHandle, String str);

    private void synthesizingEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            TranslationSynthesisEventArgs translationSynthesisEventArgs = new TranslationSynthesisEventArgs(j, true);
            EventHandlerImpl<TranslationSynthesisEventArgs> eventHandlerImpl = this.synthesizing;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, translationSynthesisEventArgs);
            }
        } catch (Exception e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final native long synthesizingSetCallback(long j);

    public void addTargetLanguage(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        Contracts.throwIfFail(addTargetLanguage(this.recoHandle, str));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.microsoft.cognitiveservices.speech.Recognizer
    public void dispose(boolean z) {
        if (!this.disposed && z) {
            PropertyCollection propertyCollection = this.propertyHandle;
            if (propertyCollection != null) {
                propertyCollection.close();
                this.propertyHandle = null;
            }
            SafeHandle safeHandle = this.translationSynthesisHandle;
            if (safeHandle != null) {
                safeHandle.close();
                this.translationSynthesisHandle = null;
            }
            if (this.recoHandle != null) {
                this.recoHandle.close();
                this.recoHandle = null;
            }
            translationRecognizerObjects.remove(this);
            super.dispose(z);
        }
    }

    public String getAuthorizationToken() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceAuthorization_Token);
    }

    public PropertyCollection getProperties() {
        return this.propertyHandle;
    }

    public SafeHandle getRecoImpl() {
        return this.recoHandle;
    }

    public String getSpeechRecognitionLanguage() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_RecoLanguage);
    }

    public ArrayList<String> getTargetLanguages() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_TranslationToLanguages).split(",")) {
            arrayList.add(str);
        }
        return arrayList;
    }

    public String getVoiceName() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_TranslationVoice);
    }

    public Future<TranslationRecognitionResult> recognizeOnceAsync() {
        return AsyncThreadService.submit(new Callable<TranslationRecognitionResult>() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public TranslationRecognitionResult call() {
                final TranslationRecognitionResult[] translationRecognitionResultArr = new TranslationRecognitionResult[1];
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        translationRecognitionResultArr[0] = new TranslationRecognitionResult(this.recognize());
                    }
                });
                return translationRecognitionResultArr[0];
            }
        });
    }

    public void removeTargetLanguage(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        Contracts.throwIfFail(removeTargetLanguage(this.recoHandle, str));
    }

    public void setAuthorizationToken(String str) {
        Contracts.throwIfNullOrWhitespace(str, "token");
        this.propertyHandle.setProperty(PropertyId.SpeechServiceAuthorization_Token, str);
    }

    public Future<Void> startContinuousRecognitionAsync() {
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.2
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.startContinuousRecognition(TranslationRecognizer.this.recoHandle);
                    }
                });
                return null;
            }
        });
    }

    public Future<Void> stopContinuousRecognitionAsync() {
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.3
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.stopContinuousRecognition(TranslationRecognizer.this.recoHandle);
                    }
                });
                return null;
            }
        });
    }
}

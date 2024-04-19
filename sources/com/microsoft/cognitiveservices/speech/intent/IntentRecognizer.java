package com.microsoft.cognitiveservices.speech.intent;

import com.microsoft.cognitiveservices.speech.EmbeddedSpeechConfig;
import com.microsoft.cognitiveservices.speech.KeywordRecognitionModel;
import com.microsoft.cognitiveservices.speech.PropertyCollection;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.Recognizer;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.util.AsyncThreadService;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.EventHandlerImpl;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.JsonBuilder;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.litepal.util.Const;
/* loaded from: classes.dex */
public final class IntentRecognizer extends Recognizer {
    static Set<IntentRecognizer> intentRecognizerObjects = Collections.synchronizedSet(new HashSet());
    public final EventHandlerImpl<IntentRecognitionCanceledEventArgs> canceled;
    private PropertyCollection propertyHandle;
    public final EventHandlerImpl<IntentRecognitionEventArgs> recognized;
    public final EventHandlerImpl<IntentRecognitionEventArgs> recognizing;

    public IntentRecognizer(EmbeddedSpeechConfig embeddedSpeechConfig) {
        super(null);
        this.recognizing = new EventHandlerImpl<>(this.eventCounter);
        this.recognized = new EventHandlerImpl<>(this.eventCounter);
        this.canceled = new EventHandlerImpl<>(this.eventCounter);
        this.propertyHandle = null;
        Contracts.throwIfNull(embeddedSpeechConfig, "embeddedSpeechConfig");
        Contracts.throwIfNull(this.recoHandle, "recoHandle");
        Contracts.throwIfFail(createIntentRecognizerFromConfig(this.recoHandle, embeddedSpeechConfig.getImpl(), null));
        initialize();
    }

    public IntentRecognizer(EmbeddedSpeechConfig embeddedSpeechConfig, AudioConfig audioConfig) {
        super(audioConfig);
        this.recognizing = new EventHandlerImpl<>(this.eventCounter);
        this.recognized = new EventHandlerImpl<>(this.eventCounter);
        this.canceled = new EventHandlerImpl<>(this.eventCounter);
        this.propertyHandle = null;
        Contracts.throwIfNull(embeddedSpeechConfig, "embeddedSpeechConfig");
        Contracts.throwIfNull(this.recoHandle, "recoHandle");
        Contracts.throwIfFail(audioConfig == null ? createIntentRecognizerFromConfig(this.recoHandle, embeddedSpeechConfig.getImpl(), null) : createIntentRecognizerFromConfig(this.recoHandle, embeddedSpeechConfig.getImpl(), audioConfig.getImpl()));
        initialize();
    }

    public IntentRecognizer(SpeechConfig speechConfig) {
        super(null);
        this.recognizing = new EventHandlerImpl<>(this.eventCounter);
        this.recognized = new EventHandlerImpl<>(this.eventCounter);
        this.canceled = new EventHandlerImpl<>(this.eventCounter);
        this.propertyHandle = null;
        Contracts.throwIfNull(speechConfig, "speechConfig");
        Contracts.throwIfNull(this.recoHandle, "recoHandle");
        Contracts.throwIfFail(createIntentRecognizerFromConfig(this.recoHandle, speechConfig.getImpl(), null));
        initialize();
    }

    public IntentRecognizer(SpeechConfig speechConfig, AudioConfig audioConfig) {
        super(audioConfig);
        this.recognizing = new EventHandlerImpl<>(this.eventCounter);
        this.recognized = new EventHandlerImpl<>(this.eventCounter);
        this.canceled = new EventHandlerImpl<>(this.eventCounter);
        this.propertyHandle = null;
        Contracts.throwIfNull(speechConfig, "speechConfig");
        Contracts.throwIfFail(audioConfig == null ? createIntentRecognizerFromConfig(this.recoHandle, speechConfig.getImpl(), null) : createIntentRecognizerFromConfig(this.recoHandle, speechConfig.getImpl(), audioConfig.getImpl()));
        initialize();
    }

    private final native long addIntent(SafeHandle safeHandle, String str, SafeHandle safeHandle2);

    private String buildModelJson(PatternMatchingModel patternMatchingModel) {
        JsonBuilder jsonBuilder = new JsonBuilder();
        jsonBuilder.setString(jsonBuilder.addItem(jsonBuilder.root, 0, "modelId"), patternMatchingModel.getId());
        int addItem = jsonBuilder.addItem(jsonBuilder.root, 0, "intents");
        jsonBuilder.setJson(addItem, "[]");
        int i = 0;
        for (PatternMatchingIntent patternMatchingIntent : patternMatchingModel.getIntents().values()) {
            int addItem2 = jsonBuilder.addItem(addItem, i, null);
            jsonBuilder.setString(jsonBuilder.addItem(addItem2, 0, "id"), patternMatchingIntent.getId());
            int addItem3 = jsonBuilder.addItem(addItem2, 0, "phrases");
            jsonBuilder.setJson(addItem3, "[]");
            int i2 = 0;
            for (String str : patternMatchingIntent.Phrases) {
                jsonBuilder.setString(jsonBuilder.addItem(addItem3, i2, null), str);
                i2++;
            }
            i++;
        }
        int addItem4 = jsonBuilder.addItem(jsonBuilder.root, 0, "entities");
        jsonBuilder.setJson(addItem4, "[]");
        int i3 = 0;
        for (PatternMatchingEntity patternMatchingEntity : patternMatchingModel.getEntities().values()) {
            int addItem5 = jsonBuilder.addItem(addItem4, i3, null);
            jsonBuilder.setString(jsonBuilder.addItem(addItem5, 0, "id"), patternMatchingEntity.getId());
            jsonBuilder.setInteger(jsonBuilder.addItem(addItem5, 0, Const.TableSchema.COLUMN_TYPE), patternMatchingEntity.getType().getValue());
            jsonBuilder.setInteger(jsonBuilder.addItem(addItem5, 0, "mode"), patternMatchingEntity.getMatchMode().getValue());
            int addItem6 = jsonBuilder.addItem(addItem5, 0, "phrases");
            jsonBuilder.setJson(addItem6, "[]");
            int i4 = 0;
            for (String str2 : patternMatchingEntity.Phrases) {
                jsonBuilder.setString(jsonBuilder.addItem(addItem6, i4, null), str2);
                i4++;
            }
            i3++;
        }
        return jsonBuilder.toString();
    }

    private void canceledEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            IntentRecognitionCanceledEventArgs intentRecognitionCanceledEventArgs = new IntentRecognitionCanceledEventArgs(j, true);
            EventHandlerImpl<IntentRecognitionCanceledEventArgs> eventHandlerImpl = this.canceled;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, intentRecognitionCanceledEventArgs);
            }
        } catch (Exception e) {
        }
    }

    private final native long clearLanguageModels(SafeHandle safeHandle);

    private final native long createIntentRecognizerFromConfig(SafeHandle safeHandle, SafeHandle safeHandle2, SafeHandle safeHandle3);

    private final native long importPatternMatchingModel(SafeHandle safeHandle, String str);

    private void initialize() {
        this.recognizing.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.7
            @Override // java.lang.Runnable
            public void run() {
                IntentRecognizer.intentRecognizerObjects.add(this);
                Contracts.throwIfFail(IntentRecognizer.this.recognizingSetCallback(this.recoHandle.getValue()));
            }
        });
        this.recognized.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.8
            @Override // java.lang.Runnable
            public void run() {
                IntentRecognizer.intentRecognizerObjects.add(this);
                Contracts.throwIfFail(IntentRecognizer.this.recognizedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.canceled.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.9
            @Override // java.lang.Runnable
            public void run() {
                IntentRecognizer.intentRecognizerObjects.add(this);
                Contracts.throwIfFail(IntentRecognizer.this.canceledSetCallback(this.recoHandle.getValue()));
            }
        });
        this.sessionStarted.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.10
            @Override // java.lang.Runnable
            public void run() {
                IntentRecognizer.intentRecognizerObjects.add(this);
                Contracts.throwIfFail(IntentRecognizer.this.sessionStartedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.sessionStopped.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.11
            @Override // java.lang.Runnable
            public void run() {
                IntentRecognizer.intentRecognizerObjects.add(this);
                Contracts.throwIfFail(IntentRecognizer.this.sessionStoppedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.speechStartDetected.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.12
            @Override // java.lang.Runnable
            public void run() {
                IntentRecognizer.intentRecognizerObjects.add(this);
                Contracts.throwIfFail(IntentRecognizer.this.speechStartDetectedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.speechEndDetected.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.13
            @Override // java.lang.Runnable
            public void run() {
                IntentRecognizer.intentRecognizerObjects.add(this);
                Contracts.throwIfFail(IntentRecognizer.this.speechEndDetectedSetCallback(this.recoHandle.getValue()));
            }
        });
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(getPropertyBagFromRecognizerHandle(this.recoHandle, intRef));
        this.propertyHandle = new PropertyCollection(intRef);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final native long recognizeTextOnce(SafeHandle safeHandle, String str, IntRef intRef);

    private void recognizedEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            IntentRecognitionEventArgs intentRecognitionEventArgs = new IntentRecognitionEventArgs(j, true);
            EventHandlerImpl<IntentRecognitionEventArgs> eventHandlerImpl = this.recognized;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, intentRecognitionEventArgs);
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
            IntentRecognitionEventArgs intentRecognitionEventArgs = new IntentRecognitionEventArgs(j, true);
            EventHandlerImpl<IntentRecognitionEventArgs> eventHandlerImpl = this.recognizing;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, intentRecognitionEventArgs);
            }
        } catch (Exception e) {
        }
    }

    public void addAllIntents(LanguageUnderstandingModel languageUnderstandingModel) {
        Contracts.throwIfNull(languageUnderstandingModel, "model");
        Contracts.throwIfFail(addIntent(this.recoHandle, (String) null, IntentTrigger.fromModel(languageUnderstandingModel.getImpl()).getImpl()));
    }

    public void addAllIntents(LanguageUnderstandingModel languageUnderstandingModel, String str) {
        Contracts.throwIfNull(languageUnderstandingModel, "model");
        Contracts.throwIfNullOrWhitespace(str, "intentId");
        Contracts.throwIfFail(addIntent(this.recoHandle, str, IntentTrigger.fromModel(languageUnderstandingModel.getImpl()).getImpl()));
    }

    public void addIntent(LanguageUnderstandingModel languageUnderstandingModel, String str) {
        Contracts.throwIfNull(languageUnderstandingModel, "model");
        Contracts.throwIfNullOrWhitespace(str, "intentName");
        Contracts.throwIfFail(addIntent(this.recoHandle, str, IntentTrigger.fromModel(languageUnderstandingModel.getImpl(), str).getImpl()));
    }

    public void addIntent(LanguageUnderstandingModel languageUnderstandingModel, String str, String str2) {
        Contracts.throwIfNull(languageUnderstandingModel, "model");
        Contracts.throwIfNullOrWhitespace(str, "intentName");
        Contracts.throwIfNullOrWhitespace(str2, "intentId");
        Contracts.throwIfFail(addIntent(this.recoHandle, str2, IntentTrigger.fromModel(languageUnderstandingModel.getImpl(), str).getImpl()));
    }

    public void addIntent(String str) {
        Contracts.throwIfNullOrWhitespace(str, "simplePhrase");
        Contracts.throwIfFail(addIntent(this.recoHandle, str, IntentTrigger.fromPhrase(str).getImpl()));
    }

    public void addIntent(String str, String str2) {
        Contracts.throwIfNullOrWhitespace(str, "simplePhrase");
        Contracts.throwIfNullOrWhitespace(str2, "intentId");
        Contracts.throwIfFail(addIntent(this.recoHandle, str2, IntentTrigger.fromPhrase(str).getImpl()));
    }

    public boolean applyLanguageModels(Collection<LanguageUnderstandingModel> collection) throws NullPointerException {
        Contracts.throwIfNull(collection, "collection");
        Contracts.throwIfFail(clearLanguageModels(this.recoHandle));
        boolean z = true;
        for (LanguageUnderstandingModel languageUnderstandingModel : collection) {
            Contracts.throwIfNull(languageUnderstandingModel, "model");
            if (languageUnderstandingModel instanceof PatternMatchingModel) {
                Contracts.throwIfFail(importPatternMatchingModel(this.recoHandle, buildModelJson((PatternMatchingModel) languageUnderstandingModel).toString()));
            } else if (languageUnderstandingModel instanceof LanguageUnderstandingModel) {
                Contracts.throwIfFail(addIntent(this.recoHandle, (String) null, IntentTrigger.fromModel(languageUnderstandingModel.getImpl()).getImpl()));
                z = false;
            }
        }
        return z;
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
            if (this.recoHandle != null) {
                this.recoHandle.close();
                this.recoHandle = null;
            }
            intentRecognizerObjects.remove(this);
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

    public Future<IntentRecognitionResult> recognizeOnceAsync() {
        return AsyncThreadService.submit(new Callable<IntentRecognitionResult>() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public IntentRecognitionResult call() {
                final IntentRecognitionResult[] intentRecognitionResultArr = new IntentRecognitionResult[1];
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        intentRecognitionResultArr[0] = new IntentRecognitionResult(this.recognize());
                    }
                });
                return intentRecognitionResultArr[0];
            }
        });
    }

    public Future<IntentRecognitionResult> recognizeOnceAsync(final String str) {
        return AsyncThreadService.submit(new Callable<IntentRecognitionResult>() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public IntentRecognitionResult call() {
                final IntentRecognitionResult[] intentRecognitionResultArr = new IntentRecognitionResult[1];
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        IntRef intRef = new IntRef(0L);
                        Contracts.throwIfNull(this, "Invalid recognizer handle");
                        Contracts.throwIfFail(IntentRecognizer.this.recognizeTextOnce(this.getImpl(), str, intRef));
                        intentRecognitionResultArr[0] = new IntentRecognitionResult(intRef.getValue());
                    }
                });
                return intentRecognitionResultArr[0];
            }
        });
    }

    public void setAuthorizationToken(String str) {
        Contracts.throwIfNullOrWhitespace(str, "token");
        this.propertyHandle.setProperty(PropertyId.SpeechServiceAuthorization_Token, str);
    }

    public Future<Void> startContinuousRecognitionAsync() {
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.3
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.startContinuousRecognition(IntentRecognizer.this.recoHandle);
                    }
                });
                return null;
            }
        });
    }

    public Future<Void> startKeywordRecognitionAsync(final KeywordRecognitionModel keywordRecognitionModel) {
        Contracts.throwIfNull(keywordRecognitionModel, "model");
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.5
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.5.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.startKeywordRecognition(IntentRecognizer.this.recoHandle, keywordRecognitionModel.getImpl());
                    }
                });
                return null;
            }
        });
    }

    public Future<Void> stopContinuousRecognitionAsync() {
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.4
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.4.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.stopContinuousRecognition(IntentRecognizer.this.recoHandle);
                    }
                });
                return null;
            }
        });
    }

    public Future<Void> stopKeywordRecognitionAsync() {
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.6
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.intent.IntentRecognizer.6.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.stopKeywordRecognition(IntentRecognizer.this.recoHandle);
                    }
                });
                return null;
            }
        });
    }
}

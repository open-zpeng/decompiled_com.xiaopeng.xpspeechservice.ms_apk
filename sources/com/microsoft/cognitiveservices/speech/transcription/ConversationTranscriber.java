package com.microsoft.cognitiveservices.speech.transcription;

import com.microsoft.cognitiveservices.speech.OutputFormat;
import com.microsoft.cognitiveservices.speech.PropertyCollection;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.Recognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.util.AsyncThreadService;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.EventHandlerImpl;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
/* loaded from: classes.dex */
public final class ConversationTranscriber extends Recognizer {
    static Set<ConversationTranscriber> conversationTranscriberObjects = Collections.synchronizedSet(new HashSet());
    public final EventHandlerImpl<ConversationTranscriptionCanceledEventArgs> canceled;
    private AtomicInteger eventCounter;
    private PropertyCollection propertyHandle;
    public final EventHandlerImpl<ConversationTranscriptionEventArgs> transcribed;
    public final EventHandlerImpl<ConversationTranscriptionEventArgs> transcribing;

    public ConversationTranscriber() {
        super(null);
        this.eventCounter = new AtomicInteger(0);
        this.transcribing = new EventHandlerImpl<>(this.eventCounter);
        this.transcribed = new EventHandlerImpl<>(this.eventCounter);
        this.canceled = new EventHandlerImpl<>(this.eventCounter);
        this.propertyHandle = null;
        Contracts.throwIfNull(this.recoHandle, "recoHandle");
        Contracts.throwIfFail(createConversationTranscriberFromConfig(this.recoHandle, null));
        initialize();
    }

    public ConversationTranscriber(AudioConfig audioConfig) {
        super(audioConfig);
        this.eventCounter = new AtomicInteger(0);
        this.transcribing = new EventHandlerImpl<>(this.eventCounter);
        this.transcribed = new EventHandlerImpl<>(this.eventCounter);
        this.canceled = new EventHandlerImpl<>(this.eventCounter);
        this.propertyHandle = null;
        Contracts.throwIfFail(audioConfig == null ? createConversationTranscriberFromConfig(this.recoHandle, null) : createConversationTranscriberFromConfig(this.recoHandle, audioConfig.getImpl()));
        initialize();
    }

    private void canceledEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            ConversationTranscriptionCanceledEventArgs conversationTranscriptionCanceledEventArgs = new ConversationTranscriptionCanceledEventArgs(j, true);
            EventHandlerImpl<ConversationTranscriptionCanceledEventArgs> eventHandlerImpl = this.canceled;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, conversationTranscriptionCanceledEventArgs);
            }
        } catch (Exception e) {
        }
    }

    private final native long createConversationTranscriberFromConfig(SafeHandle safeHandle, SafeHandle safeHandle2);

    private void initialize() {
        this.transcribing.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.5
            @Override // java.lang.Runnable
            public void run() {
                ConversationTranscriber.conversationTranscriberObjects.add(this);
                Contracts.throwIfFail(ConversationTranscriber.this.recognizingSetCallback(this.recoHandle.getValue()));
            }
        });
        this.transcribed.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.6
            @Override // java.lang.Runnable
            public void run() {
                ConversationTranscriber.conversationTranscriberObjects.add(this);
                Contracts.throwIfFail(ConversationTranscriber.this.recognizedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.canceled.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.7
            @Override // java.lang.Runnable
            public void run() {
                ConversationTranscriber.conversationTranscriberObjects.add(this);
                Contracts.throwIfFail(ConversationTranscriber.this.canceledSetCallback(this.recoHandle.getValue()));
            }
        });
        this.sessionStarted.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.8
            @Override // java.lang.Runnable
            public void run() {
                ConversationTranscriber.conversationTranscriberObjects.add(this);
                Contracts.throwIfFail(ConversationTranscriber.this.sessionStartedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.sessionStopped.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.9
            @Override // java.lang.Runnable
            public void run() {
                ConversationTranscriber.conversationTranscriberObjects.add(this);
                Contracts.throwIfFail(ConversationTranscriber.this.sessionStoppedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.speechStartDetected.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.10
            @Override // java.lang.Runnable
            public void run() {
                ConversationTranscriber.conversationTranscriberObjects.add(this);
                Contracts.throwIfFail(ConversationTranscriber.this.speechStartDetectedSetCallback(this.recoHandle.getValue()));
            }
        });
        this.speechEndDetected.updateNotificationOnConnected(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.11
            @Override // java.lang.Runnable
            public void run() {
                ConversationTranscriber.conversationTranscriberObjects.add(this);
                Contracts.throwIfFail(ConversationTranscriber.this.speechEndDetectedSetCallback(this.recoHandle.getValue()));
            }
        });
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(getPropertyBagFromRecognizerHandle(this.recoHandle, intRef));
        this.propertyHandle = new PropertyCollection(intRef);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final native long joinConversation(SafeHandle safeHandle, SafeHandle safeHandle2);

    /* JADX INFO: Access modifiers changed from: private */
    public final native long leaveConversation(SafeHandle safeHandle);

    private void recognizedEventCallback(long j) {
        try {
            Contracts.throwIfNull(this, "recognizer");
            if (this.disposed) {
                return;
            }
            ConversationTranscriptionEventArgs conversationTranscriptionEventArgs = new ConversationTranscriptionEventArgs(j, true);
            EventHandlerImpl<ConversationTranscriptionEventArgs> eventHandlerImpl = this.transcribed;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, conversationTranscriptionEventArgs);
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
            ConversationTranscriptionEventArgs conversationTranscriptionEventArgs = new ConversationTranscriptionEventArgs(j, true);
            EventHandlerImpl<ConversationTranscriptionEventArgs> eventHandlerImpl = this.transcribing;
            if (eventHandlerImpl != null) {
                eventHandlerImpl.fireEvent(this, conversationTranscriptionEventArgs);
            }
        } catch (Exception e) {
        }
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
            conversationTranscriberObjects.remove(this);
            super.dispose(z);
        }
    }

    public OutputFormat getOutputFormat() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceResponse_RequestDetailedResultTrueFalse).equals("true") ? OutputFormat.Detailed : OutputFormat.Simple;
    }

    public PropertyCollection getProperties() {
        return this.propertyHandle;
    }

    public String getSpeechRecognitionLanguage() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_RecoLanguage);
    }

    public SafeHandle getTranscriberImpl() {
        return this.recoHandle;
    }

    public Future<Void> joinConversationAsync(final Conversation conversation) {
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.1
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.joinConversation(ConversationTranscriber.this.recoHandle, conversation.getImpl());
                    }
                });
                return null;
            }
        });
    }

    public Future<Void> leaveConversationAsync() {
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.2
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.leaveConversation(ConversationTranscriber.this.recoHandle);
                    }
                });
                return null;
            }
        });
    }

    public Future<Void> startTranscribingAsync() {
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.3
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.startContinuousRecognition(ConversationTranscriber.this.recoHandle);
                    }
                });
                return null;
            }
        });
    }

    public Future<Void> stopTranscribingAsync() {
        return AsyncThreadService.submit(new Callable<Void>() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.4
            @Override // java.util.concurrent.Callable
            public Void call() {
                this.doAsyncRecognitionAction(new Runnable() { // from class: com.microsoft.cognitiveservices.speech.transcription.ConversationTranscriber.4.1
                    @Override // java.lang.Runnable
                    public void run() {
                        this.stopContinuousRecognition(ConversationTranscriber.this.recoHandle);
                    }
                });
                return null;
            }
        });
    }
}

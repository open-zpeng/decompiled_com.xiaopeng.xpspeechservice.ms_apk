package com.microsoft.cognitiveservices.speech.transcription;

import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.StringRef;
/* loaded from: classes.dex */
public class ConversationTranscriptionResult extends SpeechRecognitionResult {
    private String userId;
    private String utteranceId;

    /* JADX INFO: Access modifiers changed from: protected */
    public ConversationTranscriptionResult(long j) {
        super(j);
        if (j != 0) {
            StringRef stringRef = new StringRef("");
            Contracts.throwIfFail(getUserId(this.resultHandle, stringRef));
            this.userId = stringRef.getValue();
            StringRef stringRef2 = new StringRef("");
            Contracts.throwIfFail(getUtteranceId(this.resultHandle, stringRef2));
            this.utteranceId = stringRef2.getValue();
        }
    }

    private final native long getUserId(SafeHandle safeHandle, StringRef stringRef);

    private final native long getUtteranceId(SafeHandle safeHandle, StringRef stringRef);

    @Override // com.microsoft.cognitiveservices.speech.SpeechRecognitionResult, com.microsoft.cognitiveservices.speech.RecognitionResult
    public void close() {
        super.close();
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUtteranceId() {
        return this.utteranceId;
    }

    @Override // com.microsoft.cognitiveservices.speech.SpeechRecognitionResult
    public String toString() {
        return "ResultId:" + getResultId() + " Status:" + getReason() + " UserId:" + this.userId + " UtteranceId:" + this.utteranceId + " Recognized text:<" + getText() + ">.";
    }
}

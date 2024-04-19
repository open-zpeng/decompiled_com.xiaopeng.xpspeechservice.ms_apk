package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
/* loaded from: classes.dex */
public class KeywordRecognitionEventArgs extends RecognitionEventArgs {
    private KeywordRecognitionResult result;

    KeywordRecognitionEventArgs(long j) {
        super(j);
        storeEventData(false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public KeywordRecognitionEventArgs(long j, boolean z) {
        super(j);
        storeEventData(z);
    }

    private void storeEventData(boolean z) {
        Contracts.throwIfNull(this.eventHandle, "eventHandle");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(getRecognitionResult(this.eventHandle, intRef));
        this.result = new KeywordRecognitionResult(intRef.getValue());
        Contracts.throwIfNull(getSessionId(), "SessionId");
        if (z) {
            super.close();
        }
    }

    public final KeywordRecognitionResult getResult() {
        return this.result;
    }

    @Override // com.microsoft.cognitiveservices.speech.RecognitionEventArgs, com.microsoft.cognitiveservices.speech.SessionEventArgs
    public String toString() {
        return "SessionId:" + getSessionId() + " ResultId:" + this.result.getResultId() + " Reason:" + this.result.getReason() + "> Recognized text:<" + this.result.getText() + ">.";
    }
}

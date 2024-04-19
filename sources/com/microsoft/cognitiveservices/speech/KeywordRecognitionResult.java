package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.util.Contracts;
/* loaded from: classes.dex */
public final class KeywordRecognitionResult extends RecognitionResult {
    /* JADX INFO: Access modifiers changed from: package-private */
    public KeywordRecognitionResult(long j) {
        super(j);
        Contracts.throwIfNull(this.resultHandle, "resultHandle");
    }

    @Override // com.microsoft.cognitiveservices.speech.RecognitionResult
    public void close() {
        super.close();
    }

    public String toString() {
        return "ResultId:" + getResultId() + " Reason:" + getReason() + "> Recognized text:<" + getText() + ">.";
    }
}

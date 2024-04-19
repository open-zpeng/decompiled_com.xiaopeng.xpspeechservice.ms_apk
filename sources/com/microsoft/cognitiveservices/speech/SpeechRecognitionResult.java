package com.microsoft.cognitiveservices.speech;
/* loaded from: classes.dex */
public class SpeechRecognitionResult extends RecognitionResult {
    /* JADX INFO: Access modifiers changed from: protected */
    public SpeechRecognitionResult(long j) {
        super(j);
    }

    @Override // com.microsoft.cognitiveservices.speech.RecognitionResult
    public void close() {
        super.close();
    }

    public String toString() {
        return "ResultId:" + getResultId() + " Status:" + getReason() + " Recognized text:<" + getText() + ">.";
    }
}

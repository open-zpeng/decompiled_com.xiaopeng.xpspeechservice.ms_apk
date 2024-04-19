package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
/* loaded from: classes.dex */
public class NoMatchDetails {
    private NoMatchReason reason;

    protected NoMatchDetails(RecognitionResult recognitionResult) {
        Contracts.throwIfNull(recognitionResult, "result");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(getResultReason(recognitionResult.getImpl(), intRef));
        this.reason = NoMatchReason.values()[((int) intRef.getValue()) - 1];
    }

    public static NoMatchDetails fromResult(RecognitionResult recognitionResult) {
        return new NoMatchDetails(recognitionResult);
    }

    private final native long getResultReason(SafeHandle safeHandle, IntRef intRef);

    public void close() {
    }

    public NoMatchReason getReason() {
        return this.reason;
    }

    public String toString() {
        return "NoMatchReason:" + this.reason;
    }
}

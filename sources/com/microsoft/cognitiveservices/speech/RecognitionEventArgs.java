package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import java.math.BigInteger;
/* loaded from: classes.dex */
public class RecognitionEventArgs extends SessionEventArgs {
    public BigInteger offset;

    public RecognitionEventArgs(long j) {
        super(j);
        storeEventData(false);
    }

    public RecognitionEventArgs(long j, boolean z) {
        super(j);
        storeEventData(z);
    }

    private final native BigInteger getOffset(SafeHandle safeHandle, IntRef intRef);

    private void storeEventData(boolean z) {
        Contracts.throwIfNull(this.eventHandle, "eventHandle");
        IntRef intRef = new IntRef(0L);
        this.offset = getOffset(this.eventHandle, intRef);
        Contracts.throwIfFail(intRef.getValue());
        if (z) {
            super.close();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final native long getRecognitionResult(SafeHandle safeHandle, IntRef intRef);

    @Override // com.microsoft.cognitiveservices.speech.SessionEventArgs
    public String toString() {
        return "SessionId: " + getSessionId() + " Offset: " + this.offset.toString() + ".";
    }
}

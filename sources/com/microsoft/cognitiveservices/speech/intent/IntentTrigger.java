package com.microsoft.cognitiveservices.speech.intent;

import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
/* loaded from: classes.dex */
public final class IntentTrigger {
    private SafeHandle triggerHandle;

    private IntentTrigger(IntRef intRef) {
        this.triggerHandle = null;
        this.triggerHandle = new SafeHandle(intRef.getValue(), SafeHandleType.IntentTrigger);
    }

    private static final native long createFromLanguageUnderstandingModel(IntRef intRef, SafeHandle safeHandle, String str);

    private static final native long createFromPhrase(IntRef intRef, String str);

    public static IntentTrigger fromModel(SafeHandle safeHandle) {
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(createFromLanguageUnderstandingModel(intRef, safeHandle, null));
        return new IntentTrigger(intRef);
    }

    public static IntentTrigger fromModel(SafeHandle safeHandle, String str) {
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(createFromLanguageUnderstandingModel(intRef, safeHandle, str));
        return new IntentTrigger(intRef);
    }

    public static IntentTrigger fromPhrase(String str) {
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(createFromPhrase(intRef, str));
        return new IntentTrigger(intRef);
    }

    public SafeHandle getImpl() {
        return this.triggerHandle;
    }
}

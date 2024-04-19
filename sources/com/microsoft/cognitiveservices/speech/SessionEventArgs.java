package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
import com.microsoft.cognitiveservices.speech.util.StringRef;
/* loaded from: classes.dex */
public class SessionEventArgs {
    protected SafeHandle eventHandle;
    private String sessionId;
    private final int maxUUID = 36;
    private final int maxSessionID = 37;

    public SessionEventArgs(long j) {
        this.eventHandle = null;
        Contracts.throwIfNull(j, "eventArgs");
        this.eventHandle = new SafeHandle(j, SafeHandleType.Event);
        storeEventData(false);
    }

    public SessionEventArgs(long j, boolean z) {
        this.eventHandle = null;
        Contracts.throwIfNull(j, "eventArgs");
        this.eventHandle = new SafeHandle(j, SafeHandleType.Event);
        storeEventData(z);
    }

    private final native long getSessionId(SafeHandle safeHandle, StringRef stringRef, int i);

    private void storeEventData(boolean z) {
        StringRef stringRef = new StringRef("");
        Contracts.throwIfFail(getSessionId(this.eventHandle, stringRef, 37));
        this.sessionId = stringRef.getValue();
        Contracts.throwIfNull(this.sessionId, "SessionId");
        if (z) {
            close();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void close() {
        SafeHandle safeHandle = this.eventHandle;
        if (safeHandle != null) {
            safeHandle.close();
            this.eventHandle = null;
        }
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String toString() {
        return "SessionId: " + this.sessionId + ".";
    }
}

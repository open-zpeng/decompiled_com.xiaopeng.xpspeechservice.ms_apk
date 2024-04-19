package com.microsoft.cognitiveservices.speech;
/* loaded from: classes.dex */
public final class ConnectionEventArgs extends SessionEventArgs {
    public ConnectionEventArgs(long j) {
        super(j);
        storeEventData(false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ConnectionEventArgs(long j, boolean z) {
        super(j);
        storeEventData(z);
    }

    private void storeEventData(boolean z) {
        if (z) {
            super.close();
        }
    }
}

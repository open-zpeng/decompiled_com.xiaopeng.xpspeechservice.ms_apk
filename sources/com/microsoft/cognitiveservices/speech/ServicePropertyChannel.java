package com.microsoft.cognitiveservices.speech;
/* loaded from: classes.dex */
public enum ServicePropertyChannel {
    UriQueryParameter(0),
    HttpHeader(1);
    
    private final int id;

    ServicePropertyChannel(int i) {
        this.id = i;
    }

    public int getValue() {
        return this.id;
    }
}

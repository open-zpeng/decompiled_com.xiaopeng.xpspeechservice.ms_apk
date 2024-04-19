package com.microsoft.cognitiveservices.speech.translation;

import com.lzy.okgo.cookie.SerializableCookie;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import java.net.URI;
import java.util.ArrayList;
/* loaded from: classes.dex */
public final class SpeechTranslationConfig extends SpeechConfig implements AutoCloseable {
    private boolean disposed;

    static {
        try {
            Class.forName(SpeechConfig.class.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private SpeechTranslationConfig(long j) {
        super(j);
        this.disposed = false;
    }

    private final native long addTargetLanguage(SafeHandle safeHandle, String str);

    private static final native long fromAuthorizationToken(IntRef intRef, String str, String str2);

    public static SpeechTranslationConfig fromAuthorizationToken(String str, String str2) {
        Contracts.throwIfNullOrWhitespace(str, "authorizationToken");
        Contracts.throwIfNullOrWhitespace(str2, "region");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(fromAuthorizationToken(intRef, str, str2));
        return new SpeechTranslationConfig(intRef.getValue());
    }

    private static final native long fromEndpoint(IntRef intRef, String str, String str2);

    public static SpeechTranslationConfig fromEndpoint(URI uri) {
        Contracts.throwIfNull(uri, "endpoint");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(fromEndpoint(intRef, uri.toString(), null));
        return new SpeechTranslationConfig(intRef.getValue());
    }

    public static SpeechTranslationConfig fromEndpoint(URI uri, String str) {
        Contracts.throwIfNull(uri, "endpoint");
        if (str != null) {
            IntRef intRef = new IntRef(0L);
            Contracts.throwIfFail(fromEndpoint(intRef, uri.toString(), str));
            return new SpeechTranslationConfig(intRef.getValue());
        }
        throw new NullPointerException("subscriptionKey");
    }

    private static final native long fromHost(IntRef intRef, String str, String str2);

    public static SpeechTranslationConfig fromHost(URI uri) {
        Contracts.throwIfNull(uri, SerializableCookie.HOST);
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(fromHost(intRef, uri.toString(), null));
        return new SpeechTranslationConfig(intRef.getValue());
    }

    public static SpeechTranslationConfig fromHost(URI uri, String str) {
        Contracts.throwIfNull(uri, SerializableCookie.HOST);
        if (str != null) {
            IntRef intRef = new IntRef(0L);
            Contracts.throwIfFail(fromHost(intRef, uri.toString(), str));
            return new SpeechTranslationConfig(intRef.getValue());
        }
        throw new NullPointerException("subscriptionKey");
    }

    private static final native long fromSubscription(IntRef intRef, String str, String str2);

    public static SpeechTranslationConfig fromSubscription(String str, String str2) {
        Contracts.throwIfIllegalSubscriptionKey(str, "subscriptionKey");
        Contracts.throwIfNullOrWhitespace(str2, "region");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(fromSubscription(intRef, str, str2));
        return new SpeechTranslationConfig(intRef.getValue());
    }

    private final native long removeTargetLanguage(SafeHandle safeHandle, String str);

    public void addTargetLanguage(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        Contracts.throwIfFail(addTargetLanguage(this.speechConfigHandle, str));
    }

    @Override // com.microsoft.cognitiveservices.speech.SpeechConfig, java.lang.AutoCloseable
    public void close() {
        if (this.disposed) {
            return;
        }
        super.close();
        this.disposed = true;
    }

    @Override // com.microsoft.cognitiveservices.speech.SpeechConfig
    public SafeHandle getImpl() {
        return super.getImpl();
    }

    public ArrayList<String> getTargetLanguages() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_TranslationToLanguages).split(",")) {
            arrayList.add(str);
        }
        return arrayList;
    }

    public String getVoiceName() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_TranslationVoice);
    }

    public void removeTargetLanguage(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        Contracts.throwIfFail(removeTargetLanguage(this.speechConfigHandle, str));
    }

    public void setVoiceName(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_TranslationVoice, str);
    }
}

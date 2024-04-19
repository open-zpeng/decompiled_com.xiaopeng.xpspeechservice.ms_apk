package com.microsoft.cognitiveservices.speech;

import com.lzy.okgo.cookie.SerializableCookie;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
import java.net.URI;
/* loaded from: classes.dex */
public class SpeechConfig implements AutoCloseable {
    public static Class<?> speechConfigClass;
    private boolean disposed = false;
    protected PropertyCollection propertyHandle;
    protected SafeHandle speechConfigHandle;

    static {
        Class<?> cls;
        speechConfigClass = null;
        try {
            cls = Class.forName("com.microsoft.cognitiveservices.speech.NativeLibraryLoader");
        } catch (Error | Exception e) {
            System.loadLibrary("Microsoft.CognitiveServices.Speech.java.bindings");
            System.loadLibrary("Microsoft.CognitiveServices.Speech.core");
        }
        if (cls == null) {
            throw new NullPointerException("no native loader available");
        }
        try {
            cls.getMethod("loadNativeBinding", new Class[0]).invoke(null, new Object[0]);
        } catch (Exception e2) {
        }
        setTempDirectory(System.getProperty("java.io.tmpdir"));
        speechConfigClass = SpeechConfig.class;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public SpeechConfig(long j) {
        this.speechConfigHandle = null;
        this.propertyHandle = null;
        Contracts.throwIfNull(j, "handleValue");
        this.speechConfigHandle = new SafeHandle(j, SafeHandleType.SpeechConfig);
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(getPropertyBag(this.speechConfigHandle, intRef));
        this.propertyHandle = new PropertyCollection(intRef);
        this.propertyHandle.setProperty("AZAC-SDK-PROGRAMMING-LANGUAGE", "Java");
    }

    private static final native long fromAuthorizationToken(IntRef intRef, String str, String str2);

    public static SpeechConfig fromAuthorizationToken(String str, String str2) {
        Contracts.throwIfNullOrWhitespace(str, "authorizationToken");
        Contracts.throwIfNullOrWhitespace(str2, "region");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(fromAuthorizationToken(intRef, str, str2));
        return new SpeechConfig(intRef.getValue());
    }

    private static final native long fromEndpoint(IntRef intRef, String str, String str2);

    public static SpeechConfig fromEndpoint(URI uri) {
        Contracts.throwIfNull(uri, "endpoint");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(fromEndpoint(intRef, uri.toString(), null));
        return new SpeechConfig(intRef.getValue());
    }

    public static SpeechConfig fromEndpoint(URI uri, String str) {
        Contracts.throwIfNull(uri, "endpoint");
        if (str != null) {
            IntRef intRef = new IntRef(0L);
            Contracts.throwIfFail(fromEndpoint(intRef, uri.toString(), str));
            return new SpeechConfig(intRef.getValue());
        }
        throw new NullPointerException("subscriptionKey");
    }

    private static final native long fromHost(IntRef intRef, String str, String str2);

    public static SpeechConfig fromHost(URI uri) {
        Contracts.throwIfNull(uri, SerializableCookie.HOST);
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(fromHost(intRef, uri.toString(), null));
        return new SpeechConfig(intRef.getValue());
    }

    public static SpeechConfig fromHost(URI uri, String str) {
        Contracts.throwIfNull(uri, SerializableCookie.HOST);
        if (str != null) {
            IntRef intRef = new IntRef(0L);
            Contracts.throwIfFail(fromHost(intRef, uri.toString(), str));
            return new SpeechConfig(intRef.getValue());
        }
        throw new NullPointerException("subscriptionKey");
    }

    private static final native long fromSubscription(IntRef intRef, String str, String str2);

    public static SpeechConfig fromSubscription(String str, String str2) {
        Contracts.throwIfIllegalSubscriptionKey(str, "subscriptionKey");
        Contracts.throwIfNullOrWhitespace(str2, "region");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(fromSubscription(intRef, str, str2));
        return new SpeechConfig(intRef.getValue());
    }

    private final native long getPropertyBag(SafeHandle safeHandle, IntRef intRef);

    private final native long setAudioOutputFormat(SafeHandle safeHandle, int i);

    private final native long setProfanity(SafeHandle safeHandle, int i);

    private final native long setServiceProperty(SafeHandle safeHandle, String str, String str2, int i);

    private static final native void setTempDirectory(String str);

    @Override // java.lang.AutoCloseable
    public void close() {
        if (this.disposed) {
            return;
        }
        PropertyCollection propertyCollection = this.propertyHandle;
        if (propertyCollection != null) {
            propertyCollection.close();
            this.propertyHandle = null;
        }
        SafeHandle safeHandle = this.speechConfigHandle;
        if (safeHandle != null) {
            safeHandle.close();
            this.speechConfigHandle = null;
        }
        this.disposed = true;
    }

    public void enableAudioLogging() {
        this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_EnableAudioLogging, "true");
    }

    public void enableDictation() {
        this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_RecoMode, "DICTATION");
    }

    public String getAuthorizationToken() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceAuthorization_Token);
    }

    public String getEndpointId() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_EndpointId);
    }

    public SafeHandle getImpl() {
        return this.speechConfigHandle;
    }

    public OutputFormat getOutputFormat() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceResponse_RequestDetailedResultTrueFalse).equals("true") ? OutputFormat.Detailed : OutputFormat.Simple;
    }

    public String getProperty(PropertyId propertyId) {
        return this.propertyHandle.getProperty(propertyId);
    }

    public String getProperty(String str) {
        return this.propertyHandle.getProperty(str);
    }

    public String getSpeechRecognitionLanguage() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_RecoLanguage);
    }

    public String getSpeechSynthesisLanguage() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_SynthLanguage);
    }

    public String getSpeechSynthesisOutputFormat() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_SynthOutputFormat);
    }

    public String getSpeechSynthesisVoiceName() {
        return this.propertyHandle.getProperty(PropertyId.SpeechServiceConnection_SynthVoice);
    }

    public void requestWordLevelTimestamps() {
        this.propertyHandle.setProperty(PropertyId.SpeechServiceResponse_RequestWordLevelTimestamps, "true");
    }

    public void setAuthorizationToken(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        this.propertyHandle.setProperty(PropertyId.SpeechServiceAuthorization_Token, str);
    }

    public void setEndpointId(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_EndpointId, str);
    }

    public void setOutputFormat(OutputFormat outputFormat) {
        this.propertyHandle.setProperty(PropertyId.SpeechServiceResponse_RequestDetailedResultTrueFalse, outputFormat == OutputFormat.Detailed ? "true" : "false");
    }

    public void setProfanity(ProfanityOption profanityOption) {
        Contracts.throwIfFail(setProfanity(this.speechConfigHandle, profanityOption.getValue()));
    }

    public void setProperty(PropertyId propertyId, String str) {
        this.propertyHandle.setProperty(propertyId, str);
    }

    public void setProperty(String str, String str2) {
        Contracts.throwIfNullOrWhitespace(str, "name");
        this.propertyHandle.setProperty(str, str2);
    }

    public void setProxy(String str, int i, String str2, String str3) {
        Contracts.throwIfNullOrWhitespace(str, "proxyHostName");
        Contracts.throwIfNull(str2, "proxyUserName");
        Contracts.throwIfNull(str3, "proxyPassword");
        if (i <= 0) {
            throw new IllegalArgumentException("invalid proxy port");
        }
        this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_ProxyHostName, str);
        this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_ProxyPort, Integer.toString(i));
        if (str2 != "") {
            this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_ProxyUserName, str2);
        }
        if (str3 != "") {
            this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_ProxyPassword, str3);
        }
    }

    public void setServiceProperty(String str, String str2, ServicePropertyChannel servicePropertyChannel) {
        Contracts.throwIfNullOrWhitespace(str, "name");
        Contracts.throwIfNullOrWhitespace(str2, "value");
        Contracts.throwIfFail(setServiceProperty(this.speechConfigHandle, str, str2, servicePropertyChannel.getValue()));
    }

    public void setSpeechRecognitionLanguage(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_RecoLanguage, str);
    }

    public void setSpeechSynthesisLanguage(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_SynthLanguage, str);
    }

    public void setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat speechSynthesisOutputFormat) {
        Contracts.throwIfFail(setAudioOutputFormat(this.speechConfigHandle, speechSynthesisOutputFormat.getValue()));
    }

    public void setSpeechSynthesisVoiceName(String str) {
        Contracts.throwIfNullOrWhitespace(str, "value");
        this.propertyHandle.setProperty(PropertyId.SpeechServiceConnection_SynthVoice, str);
    }
}

package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.TelemetryManager;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public final class EmbeddedSpeechConfig implements AutoCloseable {
    SpeechConfig config;
    private boolean disposed = false;

    static {
        try {
            Class.forName(SpeechConfig.class.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    EmbeddedSpeechConfig(long j) {
        this.config = null;
        this.config = new SpeechConfig(j);
        TelemetryManager.getSingleton();
    }

    private static final native long createEmbeddedSpeechConfig(IntRef intRef);

    private static final native long embeddedSpeechConfigAddPath(SafeHandle safeHandle, String str);

    public static final EmbeddedSpeechConfig fromPath(String str) {
        Contracts.throwIfNullOrWhitespace(str, "path");
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(createEmbeddedSpeechConfig(intRef));
        EmbeddedSpeechConfig embeddedSpeechConfig = new EmbeddedSpeechConfig(intRef.getValue());
        Contracts.throwIfFail(embeddedSpeechConfigAddPath(embeddedSpeechConfig.config.getImpl(), str));
        return embeddedSpeechConfig;
    }

    public static final EmbeddedSpeechConfig fromPaths(List<String> list) {
        if (list == null || list.size() == 0) {
            throw new IllegalArgumentException("paths cannot be null or empty");
        }
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(createEmbeddedSpeechConfig(intRef));
        EmbeddedSpeechConfig embeddedSpeechConfig = new EmbeddedSpeechConfig(intRef.getValue());
        for (String str : list) {
            Contracts.throwIfFail(embeddedSpeechConfigAddPath(embeddedSpeechConfig.config.getImpl(), str));
        }
        return embeddedSpeechConfig;
    }

    private final native long getNumSpeechRecognitionModels(SafeHandle safeHandle, IntRef intRef);

    private final native long getSpeechRecognitionModel(SafeHandle safeHandle, int i, IntRef intRef);

    @Override // java.lang.AutoCloseable
    public final void close() {
        if (this.disposed) {
            return;
        }
        SpeechConfig speechConfig = this.config;
        if (speechConfig != null) {
            speechConfig.close();
            this.config = null;
        }
        this.disposed = true;
    }

    public SafeHandle getImpl() {
        Contracts.throwIfNull(this.config, "config");
        return this.config.getImpl();
    }

    public String getProperty(PropertyId propertyId) {
        return this.config.getProperty(propertyId);
    }

    public String getProperty(String str) {
        return this.config.getProperty(str);
    }

    public final String getSpeechRecognitionModelName() {
        return this.config.getProperty(PropertyId.SpeechServiceConnection_RecoModelName);
    }

    public final List<SpeechRecognitionModel> getSpeechRecognitionModels() {
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(getNumSpeechRecognitionModels(this.config.getImpl(), intRef));
        int value = (int) intRef.getValue();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < value; i++) {
            IntRef intRef2 = new IntRef(0L);
            Contracts.throwIfFail(getSpeechRecognitionModel(this.config.getImpl(), i, intRef2));
            arrayList.add(new SpeechRecognitionModel(intRef2));
        }
        return arrayList;
    }

    public final OutputFormat getSpeechRecognitionOutputFormat() {
        return this.config.getOutputFormat();
    }

    public final String getSpeechSynthesisOutputFormat() {
        return this.config.getSpeechSynthesisOutputFormat();
    }

    public final String getSpeechSynthesisVoiceName() {
        return this.config.getProperty(PropertyId.SpeechServiceConnection_SynthOfflineVoice);
    }

    public final void setProfanity(ProfanityOption profanityOption) {
        this.config.setProfanity(profanityOption);
    }

    public void setProperty(PropertyId propertyId, String str) {
        this.config.setProperty(propertyId, str);
    }

    public void setProperty(String str, String str2) {
        this.config.setProperty(str, str2);
    }

    public final void setSpeechRecognitionModel(String str, String str2) {
        Contracts.throwIfNullOrWhitespace(str, "name");
        this.config.setProperty(PropertyId.SpeechServiceConnection_RecoModelName, str);
        if (str2 == null || str2.length() == 0) {
            return;
        }
        this.config.setProperty(PropertyId.SpeechServiceConnection_RecoModelKey, str2);
    }

    public final void setSpeechRecognitionOutputFormat(OutputFormat outputFormat) {
        this.config.setOutputFormat(outputFormat);
    }

    public final void setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat speechSynthesisOutputFormat) {
        this.config.setSpeechSynthesisOutputFormat(speechSynthesisOutputFormat);
    }

    public final void setSpeechSynthesisVoice(String str, String str2) {
        Contracts.throwIfNullOrWhitespace(str, "name");
        this.config.setProperty(PropertyId.SpeechServiceConnection_SynthOfflineVoice, str);
        if (str2 == null || str2.length() == 0) {
            return;
        }
        this.config.setProperty(PropertyId.SpeechServiceConnection_SynthModelKey, str2);
    }
}

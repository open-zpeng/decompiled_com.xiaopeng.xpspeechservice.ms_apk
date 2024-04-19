package com.microsoft.msttsengine;
/* loaded from: classes.dex */
public class SpeechSynthesizerConfig {
    private String synthesizerLanguage;
    private String synthesizerModelPath;

    public static SpeechSynthesizerConfig fromLocalModels(String modelPath, String language) {
        return new SpeechSynthesizerConfig(modelPath, language);
    }

    public SpeechSynthesizerConfig(String modelPath, String language) {
        this.synthesizerModelPath = modelPath;
        this.synthesizerLanguage = language;
    }

    public String GetModelPath() {
        return this.synthesizerModelPath;
    }

    public String GetModelLanguage() {
        return this.synthesizerLanguage;
    }
}

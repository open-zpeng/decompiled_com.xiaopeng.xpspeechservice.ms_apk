package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.util.Contracts;
/* loaded from: classes.dex */
public final class AutoDetectSourceLanguageResult {
    private String _language;

    private AutoDetectSourceLanguageResult(String str) {
        this._language = str;
    }

    public static AutoDetectSourceLanguageResult fromResult(SpeechRecognitionResult speechRecognitionResult) {
        Contracts.throwIfNull(speechRecognitionResult, "speechRecognitionResult cannot be null");
        return new AutoDetectSourceLanguageResult(speechRecognitionResult.getProperties().getProperty(PropertyId.SpeechServiceConnection_AutoDetectSourceLanguageResult));
    }

    public String getLanguage() {
        return this._language;
    }
}

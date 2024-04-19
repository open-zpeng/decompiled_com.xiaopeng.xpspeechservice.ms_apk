package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.util.Contracts;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: classes.dex */
public final class PronunciationAssessmentResult {
    private double accuracyScore;
    private double completenessScore;
    private double fluencyScore;
    private double pronunciationScore;
    private List<WordLevelTimingResult> words;

    private PronunciationAssessmentResult(PropertyCollection propertyCollection) {
        this.accuracyScore = Double.parseDouble(propertyCollection.getProperty("AccuracyScore"));
        this.pronunciationScore = Double.parseDouble(propertyCollection.getProperty("PronScore"));
        this.completenessScore = Double.parseDouble(propertyCollection.getProperty("CompletenessScore"));
        this.fluencyScore = Double.parseDouble(propertyCollection.getProperty("FluencyScore"));
        String property = propertyCollection.getProperty(PropertyId.SpeechServiceResponse_JsonResult);
        if (property == null || property.isEmpty()) {
            return;
        }
        this.words = new ArrayList();
        try {
            JSONArray jSONArray = new JSONObject(property).getJSONArray("NBest").getJSONObject(0).getJSONArray("Words");
            for (int i = 0; i < jSONArray.length(); i++) {
                this.words.add(new WordLevelTimingResult(jSONArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
        }
    }

    public static PronunciationAssessmentResult fromResult(SpeechRecognitionResult speechRecognitionResult) {
        Contracts.throwIfNull(speechRecognitionResult, "speechRecognitionResult cannot be null");
        if (speechRecognitionResult.getProperties().getProperty("AccuracyScore").isEmpty()) {
            return null;
        }
        return new PronunciationAssessmentResult(speechRecognitionResult.getProperties());
    }

    public Double getAccuracyScore() {
        return Double.valueOf(this.accuracyScore);
    }

    public Double getCompletenessScore() {
        return Double.valueOf(this.completenessScore);
    }

    public Double getFluencyScore() {
        return Double.valueOf(this.fluencyScore);
    }

    public Double getPronunciationScore() {
        return Double.valueOf(this.pronunciationScore);
    }

    public List<WordLevelTimingResult> getWords() {
        return this.words;
    }
}

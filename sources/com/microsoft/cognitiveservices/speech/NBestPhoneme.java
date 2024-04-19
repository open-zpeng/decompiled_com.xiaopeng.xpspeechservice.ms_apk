package com.microsoft.cognitiveservices.speech;

import org.json.JSONObject;
/* loaded from: classes.dex */
public class NBestPhoneme {
    private String phoneme;
    private double score;

    /* JADX INFO: Access modifiers changed from: package-private */
    public NBestPhoneme(JSONObject jSONObject) {
        this.phoneme = jSONObject.optString("Phoneme");
        this.score = jSONObject.optDouble("Score");
    }

    public String getPhoneme() {
        return this.phoneme;
    }

    public double getScore() {
        return this.score;
    }
}

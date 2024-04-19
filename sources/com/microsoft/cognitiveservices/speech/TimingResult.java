package com.microsoft.cognitiveservices.speech;

import org.json.JSONObject;
/* loaded from: classes.dex */
public class TimingResult {
    private long duration;
    private long offset;

    /* JADX INFO: Access modifiers changed from: package-private */
    public TimingResult(JSONObject jSONObject) {
        this.offset = jSONObject.optLong("Offset");
        this.duration = jSONObject.optLong("Duration");
    }

    public long getDuration() {
        return this.duration;
    }

    public long getOffset() {
        return this.offset;
    }
}

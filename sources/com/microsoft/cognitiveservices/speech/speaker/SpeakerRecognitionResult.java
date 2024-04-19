package com.microsoft.cognitiveservices.speech.speaker;

import com.microsoft.cognitiveservices.speech.PropertyCollection;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
import com.microsoft.cognitiveservices.speech.util.StringRef;
/* loaded from: classes.dex */
public final class SpeakerRecognitionResult implements AutoCloseable {
    private String profileId;
    private PropertyCollection properties;
    private ResultReason reason;
    private SafeHandle resultHandle;
    private String resultId;
    private double score;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SpeakerRecognitionResult(long j) {
        this.resultHandle = null;
        this.properties = null;
        this.resultId = "";
        this.profileId = "";
        this.score = 0.0d;
        Contracts.throwIfNull(j, "result");
        this.resultHandle = new SafeHandle(j, SafeHandleType.SpeakerRecognitionResult);
        StringRef stringRef = new StringRef("");
        Contracts.throwIfFail(getResultId(this.resultHandle, stringRef));
        this.resultId = stringRef.getValue();
        IntRef intRef = new IntRef(0L);
        Contracts.throwIfFail(getResultReason(this.resultHandle, intRef));
        this.reason = ResultReason.values()[(int) intRef.getValue()];
        IntRef intRef2 = new IntRef(0L);
        Contracts.throwIfFail(getPropertyBagFromResult(this.resultHandle, intRef2));
        this.properties = new PropertyCollection(intRef2);
        this.profileId = this.properties.getProperty("speakerrecognition.profileid");
        String property = this.properties.getProperty("speakerrecognition.score");
        this.score = property.isEmpty() ? 0.0d : Double.parseDouble(property);
    }

    private final native long getPropertyBagFromResult(SafeHandle safeHandle, IntRef intRef);

    private final native long getResultId(SafeHandle safeHandle, StringRef stringRef);

    private final native long getResultReason(SafeHandle safeHandle, IntRef intRef);

    @Override // java.lang.AutoCloseable
    public void close() {
        SafeHandle safeHandle = this.resultHandle;
        if (safeHandle != null) {
            safeHandle.close();
            this.resultHandle = null;
        }
        PropertyCollection propertyCollection = this.properties;
        if (propertyCollection != null) {
            propertyCollection.close();
            this.properties = null;
        }
    }

    public SafeHandle getImpl() {
        Contracts.throwIfNull(this.resultHandle, "result");
        return this.resultHandle;
    }

    public String getProfileId() {
        return this.profileId;
    }

    public PropertyCollection getProperties() {
        return this.properties;
    }

    public ResultReason getReason() {
        return this.reason;
    }

    public String getResultId() {
        return this.resultId;
    }

    public Double getScore() {
        return Double.valueOf(this.score);
    }

    public String toString() {
        return "ResultId:" + getResultId() + " Reason:" + getReason() + " Recognized profileId:" + getProfileId() + " Json:" + this.properties.getProperty(PropertyId.SpeechServiceResponse_JsonResult);
    }
}

package com.microsoft.cognitiveservices.speech;

import com.microsoft.cognitiveservices.speech.util.Contracts;
import com.microsoft.cognitiveservices.speech.util.IntRef;
import com.microsoft.cognitiveservices.speech.util.SafeHandle;
import com.microsoft.cognitiveservices.speech.util.SafeHandleType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class VoiceInfo implements AutoCloseable {
    private SynthesisVoiceGender gender;
    private String localName;
    private String locale;
    private String name;
    private PropertyCollection properties;
    private String shortName;
    private List<String> styleList;
    private SafeHandle voiceInfoHandle;
    private String voicePath;
    private SynthesisVoiceType voiceType;

    /* JADX INFO: Access modifiers changed from: protected */
    public VoiceInfo(IntRef intRef) {
        this.voiceInfoHandle = null;
        Contracts.throwIfNull(intRef, "result");
        this.voiceInfoHandle = new SafeHandle(intRef.getValue(), SafeHandleType.VoiceInfo);
        this.name = getName(this.voiceInfoHandle);
        this.locale = getLocale(this.voiceInfoHandle);
        this.shortName = getShortName(this.voiceInfoHandle);
        this.localName = getLocalName(this.voiceInfoHandle);
        IntRef intRef2 = new IntRef(0L);
        Contracts.throwIfFail(getVoiceType(this.voiceInfoHandle, intRef2));
        this.voiceType = SynthesisVoiceType.values()[((int) intRef2.getValue()) - 1];
        String styleListString = getStyleListString(this.voiceInfoHandle);
        this.styleList = styleListString.isEmpty() ? new ArrayList<>() : Arrays.asList(styleListString.split("\\|"));
        this.voicePath = getVoicePath(this.voiceInfoHandle);
        IntRef intRef3 = new IntRef(0L);
        Contracts.throwIfFail(getPropertyBagFromResult(this.voiceInfoHandle, intRef3));
        this.properties = new PropertyCollection(intRef3);
        String property = this.properties.getProperty("Gender");
        this.gender = property.equals("Female") ? SynthesisVoiceGender.Female : property.equals("Male") ? SynthesisVoiceGender.Male : SynthesisVoiceGender.Unknown;
    }

    private final native String getLocalName(SafeHandle safeHandle);

    private final native String getLocale(SafeHandle safeHandle);

    private final native String getName(SafeHandle safeHandle);

    private final native long getPropertyBagFromResult(SafeHandle safeHandle, IntRef intRef);

    private final native String getShortName(SafeHandle safeHandle);

    private final native String getStyleListString(SafeHandle safeHandle);

    private final native String getVoicePath(SafeHandle safeHandle);

    private final native long getVoiceType(SafeHandle safeHandle, IntRef intRef);

    @Override // java.lang.AutoCloseable
    public void close() {
        SafeHandle safeHandle = this.voiceInfoHandle;
        if (safeHandle != null) {
            safeHandle.close();
            this.voiceInfoHandle = null;
        }
        PropertyCollection propertyCollection = this.properties;
        if (propertyCollection != null) {
            propertyCollection.close();
            this.properties = null;
        }
    }

    public SynthesisVoiceGender getGender() {
        return this.gender;
    }

    public SafeHandle getImpl() {
        return this.voiceInfoHandle;
    }

    public String getLocalName() {
        return this.localName;
    }

    public String getLocale() {
        return this.locale;
    }

    public String getName() {
        return this.name;
    }

    public PropertyCollection getProperties() {
        return this.properties;
    }

    public String getShortName() {
        return this.shortName;
    }

    public List<String> getStyleList() {
        return this.styleList;
    }

    public String getVoicePath() {
        return this.voicePath;
    }

    public SynthesisVoiceType getVoiceType() {
        return this.voiceType;
    }
}

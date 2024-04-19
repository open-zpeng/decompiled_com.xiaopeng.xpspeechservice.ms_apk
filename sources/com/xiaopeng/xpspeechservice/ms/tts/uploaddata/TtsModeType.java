package com.xiaopeng.xpspeechservice.ms.tts.uploaddata;
/* loaded from: classes.dex */
public enum TtsModeType {
    UNSET("unset"),
    HTTP_MEDIA("http-media"),
    PROMPT("prompt"),
    CACHE("cache"),
    ONLINE("online"),
    OFFLINE("offline");
    
    private String desc;

    TtsModeType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }
}

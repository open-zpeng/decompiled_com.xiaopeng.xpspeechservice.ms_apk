package com.xiaopeng.xpspeechservice.ms.tts.uploaddata;
/* loaded from: classes.dex */
public enum EngineEndType {
    NOT_STARTED("not-started"),
    END("end"),
    STOP("stop"),
    ERROR("error"),
    TIMEOUT("timeout"),
    WS_UNCONNECT("ws_unconnect"),
    SERVER_ERROR("server-error"),
    NETWORK_ERROR("network-error"),
    DATA_PKG_TIMEOUT("pkg-timeout"),
    TEXT_TOO_LONG("text-too-long"),
    DECODE_ERROR("decode-error");
    
    private String desc;

    EngineEndType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }
}

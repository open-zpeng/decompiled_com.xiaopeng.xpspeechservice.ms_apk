package com.xiaopeng.xpspeechservice.ms.tts.config;

import java.util.List;
/* loaded from: classes.dex */
public class HybridEngineBusinessConfig {
    public List<BusinessConfig> businessConfigList;

    public HybridEngineBusinessConfig(List<BusinessConfig> list) {
        this.businessConfigList = list;
    }

    public String toString() {
        if (this.businessConfigList == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("HybridEngineBusinessConfig ");
        for (BusinessConfig caller : this.businessConfigList) {
            sb.append(caller);
            sb.append(" ");
        }
        return sb.toString();
    }
}

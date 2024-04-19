package com.xiaopeng.xpspeechservice.ms.tts.config;

import java.util.List;
/* loaded from: classes.dex */
public class HybridEngineSelectConfig {
    public List<DataPriorityCaller> dataPriorityCallerList;

    public HybridEngineSelectConfig(List<DataPriorityCaller> list) {
        this.dataPriorityCallerList = list;
    }

    public String toString() {
        if (this.dataPriorityCallerList == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (DataPriorityCaller caller : this.dataPriorityCallerList) {
            sb.append(caller);
        }
        return sb.toString();
    }
}

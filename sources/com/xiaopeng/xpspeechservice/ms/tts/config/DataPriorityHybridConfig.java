package com.xiaopeng.xpspeechservice.ms.tts.config;

import java.util.List;
/* loaded from: classes.dex */
public class DataPriorityHybridConfig {
    public List<DataPriorityParam> dataPriorityParamList;

    public DataPriorityHybridConfig(List<DataPriorityParam> list) {
        this.dataPriorityParamList = list;
    }

    public String toString() {
        if (this.dataPriorityParamList == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (DataPriorityParam param : this.dataPriorityParamList) {
            sb.append(param);
        }
        return sb.toString();
    }
}

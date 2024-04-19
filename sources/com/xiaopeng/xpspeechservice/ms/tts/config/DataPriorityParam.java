package com.xiaopeng.xpspeechservice.ms.tts.config;

import java.util.List;
/* loaded from: classes.dex */
public class DataPriorityParam {
    public String packageName;
    public List<DataPriorityPatternParam> patternParamList;

    public DataPriorityParam(String name, List<DataPriorityPatternParam> list) {
        this.packageName = name;
        this.patternParamList = list;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("packageName ");
        sb.append(this.packageName);
        sb.append(" patternParamList ");
        List<DataPriorityPatternParam> list = this.patternParamList;
        if (list == null) {
            sb.append("null ");
        } else {
            for (DataPriorityPatternParam pattern : list) {
                sb.append(pattern);
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}

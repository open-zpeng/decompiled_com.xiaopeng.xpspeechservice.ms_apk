package com.xiaopeng.xpspeechservice.ms.tts.config;
/* loaded from: classes.dex */
public class DataPriorityCaller {
    public String[] exceptionPatternList;
    public String packageName;

    public DataPriorityCaller(String name, String[] list) {
        this.packageName = name;
        this.exceptionPatternList = list;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("packageName ");
        sb.append(this.packageName);
        sb.append(" exceptionPatternList ");
        String[] strArr = this.exceptionPatternList;
        if (strArr == null) {
            sb.append("null ");
        } else {
            for (String pattern : strArr) {
                sb.append(pattern);
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}

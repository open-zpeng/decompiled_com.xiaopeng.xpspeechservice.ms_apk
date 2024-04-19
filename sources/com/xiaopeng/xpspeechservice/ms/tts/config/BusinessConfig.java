package com.xiaopeng.xpspeechservice.ms.tts.config;
/* loaded from: classes.dex */
public class BusinessConfig {
    public String[] businessList;
    public String hybridMode;
    public int onlineWaitTime;
    public String packageName;

    public BusinessConfig(String packageName, String[] businessList, String hybridMode, int onlineWaitTime) {
        this.packageName = packageName;
        this.businessList = businessList;
        this.hybridMode = hybridMode;
        this.onlineWaitTime = onlineWaitTime;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("BusinessConfig ");
        sb.append("packageName ");
        sb.append(this.packageName);
        sb.append(" businessList ");
        String[] strArr = this.businessList;
        if (strArr == null) {
            sb.append("null ");
        } else {
            for (String business : strArr) {
                sb.append(business);
                sb.append(" ");
            }
        }
        sb.append("hybridMode ");
        sb.append(this.hybridMode);
        sb.append("onlineWaitTime ");
        sb.append(String.valueOf(this.onlineWaitTime));
        return sb.toString();
    }
}

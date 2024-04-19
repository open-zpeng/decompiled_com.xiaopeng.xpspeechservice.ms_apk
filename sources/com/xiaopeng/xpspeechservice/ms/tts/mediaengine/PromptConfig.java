package com.xiaopeng.xpspeechservice.ms.tts.mediaengine;

import java.util.Map;
/* loaded from: classes.dex */
public class PromptConfig {
    public int headLength;
    public Map<String, FileLocationBean> map;
    public String path;

    public String toString() {
        return "PromptConfig headLength=" + this.headLength + " map=" + this.map;
    }
}

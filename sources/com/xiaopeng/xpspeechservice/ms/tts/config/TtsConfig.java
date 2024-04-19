package com.xiaopeng.xpspeechservice.ms.tts.config;
/* loaded from: classes.dex */
public class TtsConfig {
    public HybridEngineSelectConfig hybridEngineSelectConfig = null;
    public DelayPriorityHybridConfig delayPriorityHybridConfig = null;
    public DataPriorityHybridConfig dataPriorityHybridConfig = null;
    public HybridEngineBusinessConfig hybridEngineBusinessConfig = null;
    public OnlineEngineConfig onlineEngineConfig = null;
    public TtsCacheConfig ttsCacheConfig = null;
    public AsyncCacheConfig asyncCacheConfig = null;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        HybridEngineSelectConfig hybridEngineSelectConfig = this.hybridEngineSelectConfig;
        if (hybridEngineSelectConfig != null) {
            sb.append(hybridEngineSelectConfig);
        }
        DelayPriorityHybridConfig delayPriorityHybridConfig = this.delayPriorityHybridConfig;
        if (delayPriorityHybridConfig != null) {
            sb.append(delayPriorityHybridConfig);
            sb.append(" ");
        }
        DataPriorityHybridConfig dataPriorityHybridConfig = this.dataPriorityHybridConfig;
        if (dataPriorityHybridConfig != null) {
            sb.append(dataPriorityHybridConfig);
        }
        HybridEngineBusinessConfig hybridEngineBusinessConfig = this.hybridEngineBusinessConfig;
        if (hybridEngineBusinessConfig != null) {
            sb.append(hybridEngineBusinessConfig);
        }
        OnlineEngineConfig onlineEngineConfig = this.onlineEngineConfig;
        if (onlineEngineConfig != null) {
            sb.append(onlineEngineConfig);
            sb.append(" ");
        }
        TtsCacheConfig ttsCacheConfig = this.ttsCacheConfig;
        if (ttsCacheConfig != null) {
            sb.append(ttsCacheConfig);
            sb.append(" ");
        }
        AsyncCacheConfig asyncCacheConfig = this.asyncCacheConfig;
        if (asyncCacheConfig != null) {
            sb.append(asyncCacheConfig);
        }
        return sb.toString();
    }
}

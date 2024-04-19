package com.xiaopeng.xpspeechservice.ms.tts.config;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.lzy.okgo.cache.CacheEntity;
import com.xiaopeng.lib.framework.configuration.ConfigurationModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.configurationmodule.ConfigurationChangeEvent;
import com.xiaopeng.lib.framework.moduleinterface.configurationmodule.IConfiguration;
import com.xiaopeng.lib.framework.moduleinterface.configurationmodule.IConfigurationData;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.xpspeechservice.ms.SpeechApp;
import com.xiaopeng.xpspeechservice.ms.bean.PowerState;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
/* loaded from: classes.dex */
public class ConfigModel {
    private static final String CONFIG_KEY = "ttsConfig";
    private static final String TAG = "ConfigModel";
    private EventBus mEventBus;

    private ConfigModel() {
        LogUtils.v(TAG, "ConfigModel construct");
        this.mEventBus = EventBus.getDefault();
        this.mEventBus.register(this);
        loadDefaultConfig();
        getConfigurationInterface().init(SpeechApp.getApplication(), "com.xiaopeng.xpspeechservice.ms");
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.config.ConfigModel.1
            @Override // java.lang.Runnable
            public void run() {
                ConfigModel.this.getConfig();
            }
        }, 20000L);
    }

    /* loaded from: classes.dex */
    private static class SingleHolder {
        private static ConfigModel instance = new ConfigModel();

        private SingleHolder() {
        }
    }

    public static ConfigModel getInstance() {
        return SingleHolder.instance;
    }

    private IConfiguration getConfigurationInterface() {
        return (IConfiguration) Module.get(ConfigurationModuleEntry.class).get(IConfiguration.class);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onConfigurationChanged(ConfigurationChangeEvent event) {
        List<IConfigurationData> list = event.getChangeList();
        if (list != null) {
            for (IConfigurationData data : list) {
                String key = data.getKey();
                String value = data.getValue();
                LogUtils.i(TAG, "key=" + key + " value=" + value);
                if (key.equals(CONFIG_KEY) && !TextUtils.isEmpty(value)) {
                    configParser(value);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onPowerStateChange(PowerState power) {
        LogUtils.i(TAG, "onPowerStateChange " + power.state);
        if (power.state == 1) {
            ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.config.ConfigModel.2
                @Override // java.lang.Runnable
                public void run() {
                    ConfigModel.this.getConfig();
                }
            }, 10000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getConfig() {
        LogUtils.i(TAG, "getConfig");
        String configValue = getConfigurationInterface().getConfiguration(CONFIG_KEY, null);
        if (!TextUtils.isEmpty(configValue)) {
            configParser(configValue);
        }
    }

    private void configParser(String json) {
        LogUtils.i(TAG, "configParser " + json);
        TtsConfig config = (TtsConfig) new Gson().fromJson(json, (Class<Object>) TtsConfig.class);
        LogUtils.v(TAG, "config " + config);
        if (config.hybridEngineSelectConfig != null) {
            this.mEventBus.post(config.hybridEngineSelectConfig);
        }
        if (config.delayPriorityHybridConfig != null) {
            this.mEventBus.post(config.delayPriorityHybridConfig);
        }
        if (config.dataPriorityHybridConfig != null) {
            this.mEventBus.post(config.dataPriorityHybridConfig);
        }
        if (config.hybridEngineBusinessConfig != null) {
            this.mEventBus.post(config.hybridEngineBusinessConfig);
        }
        if (config.onlineEngineConfig != null) {
            this.mEventBus.post(config.onlineEngineConfig);
        }
        if (config.ttsCacheConfig != null) {
            this.mEventBus.post(config.ttsCacheConfig);
        }
        if (config.asyncCacheConfig != null) {
            this.mEventBus.post(config.asyncCacheConfig);
        }
    }

    private void loadDefaultConfig() {
        List<DataPriorityCaller> dataPriorityCallerList = new ArrayList<>();
        String[] exceptions = {"^(准备出发|开始导航).*"};
        dataPriorityCallerList.add(new DataPriorityCaller("com.xiaopeng.montecarlo", exceptions));
        dataPriorityCallerList.add(new DataPriorityCaller("com.xiaopeng.aiassistant", null));
        HybridEngineSelectConfig hybridEngineSelectConfig = new HybridEngineSelectConfig(dataPriorityCallerList);
        List<DataPriorityParam> dataPriorityParamList = new ArrayList<>();
        List<DataPriorityPatternParam> patternParamList = new ArrayList<>();
        patternParamList.add(new DataPriorityPatternParam("^(前方(五|六|七)百米|请减速|请注意|前方红绿灯|当前.*车速).*", 2300, 2500));
        patternParamList.add(new DataPriorityPatternParam("^(前方((八|九)百米|.*公里)|正在通过|即将进入).*", 3800, 4000));
        patternParamList.add(new DataPriorityPatternParam("^(您已进入|您已离开|请沿).*", 4800, 5000));
        DataPriorityParam dataPriorityParam = new DataPriorityParam("com.xiaopeng.montecarlo", patternParamList);
        dataPriorityParamList.add(dataPriorityParam);
        DataPriorityHybridConfig dataPriorityHybridConfig = new DataPriorityHybridConfig(dataPriorityParamList);
        List<BusinessConfig> businessConfigList = new ArrayList<>();
        String[] business = {"Wiki", "MakePauses"};
        BusinessConfig businessConfig = new BusinessConfig("com.xiaopeng.carspeechservice", business, "latency", 700);
        businessConfigList.add(businessConfig);
        HybridEngineBusinessConfig hybridEngineBusinessConfig = new HybridEngineBusinessConfig(businessConfigList);
        TtsConfig ttsConfig = new TtsConfig();
        ttsConfig.hybridEngineSelectConfig = hybridEngineSelectConfig;
        ttsConfig.dataPriorityHybridConfig = dataPriorityHybridConfig;
        ttsConfig.hybridEngineBusinessConfig = hybridEngineBusinessConfig;
        String json = new Gson().toJson(ttsConfig);
        LogUtils.d(TAG, "default config " + json);
        configParser(json);
    }

    private void configTest() {
        List<DataPriorityCaller> dataPriorityCallerList = new ArrayList<>();
        String[] exceptions1 = {"^(准备出发|开始导航).*"};
        dataPriorityCallerList.add(new DataPriorityCaller("com.xiaopeng.montecarlo", exceptions1));
        String[] exceptions2 = {"^(准备出发|开始导航).*", "^(出隧道后|隧道内).*"};
        dataPriorityCallerList.add(new DataPriorityCaller("com.xiaopeng.ttssettings", exceptions2));
        HybridEngineSelectConfig hybridEngineSelectConfig = new HybridEngineSelectConfig(dataPriorityCallerList);
        DelayPriorityHybridConfig delayPriorityHybridConfig = new DelayPriorityHybridConfig(250, 450);
        List<DataPriorityParam> dataPriorityParamList = new ArrayList<>();
        List<DataPriorityPatternParam> patternParamList = new ArrayList<>();
        patternParamList.add(new DataPriorityPatternParam("^(前方(五|六|七)百米|请减速|请注意|前方红绿灯|当前.*车速).*", 2300, 2500));
        patternParamList.add(new DataPriorityPatternParam("^(前方((八|九)百米|.*公里)|正在通过|即将进入).*", 3800, 4000));
        patternParamList.add(new DataPriorityPatternParam("^(您已进入|您已离开|请沿).*", 4800, 5000));
        DataPriorityParam dataPriorityParam = new DataPriorityParam("com.xiaopeng.montecarlo", patternParamList);
        dataPriorityParamList.add(dataPriorityParam);
        DataPriorityParam dataPriorityParam2 = new DataPriorityParam("com.xiaopeng.ttssettings", patternParamList);
        dataPriorityParamList.add(dataPriorityParam2);
        DataPriorityHybridConfig dataPriorityHybridConfig = new DataPriorityHybridConfig(dataPriorityParamList);
        List<BusinessConfig> businessConfigList = new ArrayList<>();
        String[] business = {"Wiki", "MakePauses"};
        BusinessConfig businessConfig = new BusinessConfig("com.xiaopeng.carspeechservice", business, "latency", 700);
        businessConfigList.add(businessConfig);
        String[] business2 = {"Book", "Movie"};
        BusinessConfig businessConfig2 = new BusinessConfig("com.xiaopeng.ttssettings", business2, CacheEntity.DATA, 1200);
        businessConfigList.add(businessConfig2);
        HybridEngineBusinessConfig hybridEngineBusinessConfig = new HybridEngineBusinessConfig(businessConfigList);
        OnlineEngineConfig onlineEngineConfig = new OnlineEngineConfig(2980, 4500, 250);
        TtsCacheConfig ttsCacheConfig = new TtsCacheConfig(1639872000000L, 2592000000L, 204800L);
        AsyncCacheConfig asyncCacheConfig = new AsyncCacheConfig(false, 10000L, 2);
        TtsConfig ttsConfig = new TtsConfig();
        ttsConfig.hybridEngineSelectConfig = hybridEngineSelectConfig;
        ttsConfig.delayPriorityHybridConfig = delayPriorityHybridConfig;
        ttsConfig.dataPriorityHybridConfig = dataPriorityHybridConfig;
        ttsConfig.hybridEngineBusinessConfig = hybridEngineBusinessConfig;
        ttsConfig.onlineEngineConfig = onlineEngineConfig;
        ttsConfig.ttsCacheConfig = ttsCacheConfig;
        ttsConfig.asyncCacheConfig = asyncCacheConfig;
        String json = new Gson().toJson(ttsConfig);
        LogUtils.d(TAG, "configTest " + json);
        configParser(json);
    }
}

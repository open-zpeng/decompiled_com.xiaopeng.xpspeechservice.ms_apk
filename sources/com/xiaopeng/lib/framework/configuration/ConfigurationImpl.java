package com.xiaopeng.lib.framework.configuration;

import android.app.Application;
import com.xiaopeng.lib.framework.configuration.internal.Proxy;
import com.xiaopeng.lib.framework.moduleinterface.configurationmodule.IConfiguration;
/* loaded from: classes.dex */
public class ConfigurationImpl implements IConfiguration {
    @Override // com.xiaopeng.lib.framework.moduleinterface.configurationmodule.IConfiguration
    public void init(Application application, String appID) {
        Proxy.getInstance().init(application, appID);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.configurationmodule.IConfiguration
    public String getConfiguration(String key, String defaultValue) {
        return Proxy.getInstance().getConfiguration(key, defaultValue);
    }
}

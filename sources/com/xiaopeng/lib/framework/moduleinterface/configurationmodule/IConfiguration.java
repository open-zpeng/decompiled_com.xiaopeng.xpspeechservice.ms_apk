package com.xiaopeng.lib.framework.moduleinterface.configurationmodule;

import android.app.Application;
import android.support.annotation.NonNull;
/* loaded from: classes.dex */
public interface IConfiguration {
    String getConfiguration(String key, String defaultValue);

    void init(@NonNull Application application, @NonNull String appID);
}

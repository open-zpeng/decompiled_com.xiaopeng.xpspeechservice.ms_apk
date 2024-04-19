package com.xiaopeng.lib.framework.moduleinterface.syncmodule;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;
/* loaded from: classes.dex */
public interface ISync {
    void init(@NonNull Application application, @NonNull String appID, @NonNull String appSecret, @NonNull String vehicleSeries, @NonNull String vin, @NonNull String hardwareID);

    void onNetworkAvailable();

    void onUserChanged(@NonNull Long uid);

    void restore();

    void save(@Nullable List<SyncData> data);

    void saveAll(@Nullable List<SyncData> list);
}

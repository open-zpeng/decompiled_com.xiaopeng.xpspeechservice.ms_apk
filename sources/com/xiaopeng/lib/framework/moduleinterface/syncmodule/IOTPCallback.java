package com.xiaopeng.lib.framework.moduleinterface.syncmodule;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
/* loaded from: classes.dex */
public interface IOTPCallback {
    void onError(@NonNull String seq, @Nullable Integer code, @Nullable String errMsg);

    void onGetOTP(@NonNull String seq, @NonNull String deviceID, @NonNull String otp, long uid);
}

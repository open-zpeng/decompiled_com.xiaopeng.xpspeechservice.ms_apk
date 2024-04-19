package com.xiaopeng.lib.framework.moduleinterface.accountmodule;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
/* loaded from: classes.dex */
public interface IAccount {
    @Nullable
    IUserInfo getUserInfo() throws AbsException;

    void init(@NonNull Application application, @Nullable String appId) throws AbsException;

    void init(@NonNull Application application, @Nullable String appId, @Nullable String packageName) throws AbsException;

    void login() throws AbsException;

    void logout() throws AbsException;

    void requestOAuth(@Nullable ICallback<IAuthInfo, IError> callback);

    void requestOAuth(@NonNull String appID, @Nullable ICallback<IAuthInfo, IError> callback);

    void requestOTP(@NonNull String deviceID, @NonNull ICallback<IOTPInfo, IError> callback);
}

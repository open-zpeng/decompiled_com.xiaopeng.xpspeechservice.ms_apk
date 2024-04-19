package com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http;

import android.support.annotation.NonNull;
import java.util.Map;
/* loaded from: classes.dex */
public interface IBizHelper {
    @NonNull
    IBizHelper appId(@NonNull String value);

    @NonNull
    IRequest build();

    @NonNull
    IRequest buildWithSecretKey(@NonNull String secretKey);

    @NonNull
    IBizHelper customTokensForAuth(@NonNull String[] tokens);

    @NonNull
    @Deprecated
    IBizHelper enableIrdetoEncoding();

    @NonNull
    IBizHelper enableSecurityEncoding();

    @NonNull
    IBizHelper extendBizHeader(@NonNull String header, @NonNull String value);

    @NonNull
    IBizHelper get(@NonNull String url);

    @NonNull
    IBizHelper needAuthorizationInfo();

    @NonNull
    IBizHelper needAuthorizationInfo(@NonNull Map<String, String> extParams);

    @NonNull
    IBizHelper post(@NonNull String url, @NonNull String jsonBody);

    @NonNull
    IBizHelper uid(@NonNull String value);
}

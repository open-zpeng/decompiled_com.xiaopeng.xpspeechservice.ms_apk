package com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http;

import android.support.annotation.NonNull;
/* loaded from: classes.dex */
public interface IHttp {
    IBizHelper bizHelper();

    void cancelTag(Object tag);

    IConfig config();

    IRequest get(@NonNull String url);

    IRequest head(@NonNull String url);

    IRequest post(@NonNull String url);

    IRequest requestXmartBiz(@NonNull String url);
}

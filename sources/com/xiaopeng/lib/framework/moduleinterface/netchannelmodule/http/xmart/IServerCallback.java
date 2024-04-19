package com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.xmart;
/* loaded from: classes.dex */
public interface IServerCallback {
    public static final int CODE_SUCCESS = 200;

    void onFailure(IXmartResponse response);

    void onSuccess(IXmartResponse response);
}

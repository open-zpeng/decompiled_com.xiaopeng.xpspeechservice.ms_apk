package com.xiaopeng.lib.framework.netchannelmodule.http.xmart;

import android.support.annotation.NonNull;
import com.lzy.okgo.model.Response;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.xmart.IServerCallback;
import com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter.BasePostRequestAdapter;
/* loaded from: classes.dex */
public class ServerRequest extends BasePostRequestAdapter<ServerBean> {
    public ServerRequest(String url) {
        super(url);
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter.BasePostRequestAdapter, com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IRequest
    public void execute(@NonNull IServerCallback callback) {
        super.execute(new ServerCallbackImplAdapter(callback));
    }

    /* loaded from: classes.dex */
    public class ServerCallbackImplAdapter extends ServerCallbackImpl {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private IServerCallback mOuterCallback;

        public ServerCallbackImplAdapter(IServerCallback callback) {
            this.mOuterCallback = callback;
        }

        @Override // com.lzy.okgo.callback.AbsCallback, com.lzy.okgo.callback.Callback
        public void onError(Response<ServerBean> response) {
            super.onError(response);
            this.mOuterCallback.onFailure(new ServerResponse(response));
        }

        @Override // com.lzy.okgo.callback.Callback
        public void onSuccess(Response<ServerBean> response) {
            this.mOuterCallback.onSuccess(new ServerResponse(response));
        }
    }
}

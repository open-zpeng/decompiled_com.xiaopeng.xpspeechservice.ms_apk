package com.xiaopeng.lib.framework.netchannelmodule.http;

import android.support.annotation.NonNull;
import com.lzy.okgo.OkGo;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IConfig;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IRequest;
import com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter.GetRequestAdapter;
import com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter.HeadRequestAdapter;
import com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter.PostRequestAdapter;
import com.xiaopeng.lib.framework.netchannelmodule.http.xmart.ServerRequest;
/* loaded from: classes.dex */
public class HttpImpl implements IHttp {
    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp
    public void cancelTag(Object tag) {
        OkGo.getInstance().cancelTag(tag);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp
    public IRequest get(@NonNull String url) {
        return new GetRequestAdapter(url);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp
    public IRequest head(@NonNull String url) {
        return new HeadRequestAdapter(url);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp
    public IRequest post(@NonNull String url) {
        return new PostRequestAdapter(url);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp
    public IRequest requestXmartBiz(@NonNull String url) {
        return new ServerRequest(url);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp
    public IConfig config() {
        return new ConfigImpl();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp
    public IBizHelper bizHelper() {
        return new XmartBizHelper();
    }
}

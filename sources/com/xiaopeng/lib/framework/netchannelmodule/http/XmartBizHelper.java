package com.xiaopeng.lib.framework.netchannelmodule.http;

import android.support.annotation.NonNull;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IRequest;
import com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter.GetRequestAdapter;
import com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter.PostRequestAdapter;
import com.xiaopeng.lib.framework.netchannelmodule.http.xmart.bizapi.BizConstants;
import com.xiaopeng.lib.framework.netchannelmodule.http.xmart.bizapi.BizRequestBuilder;
import java.util.Map;
/* loaded from: classes.dex */
public final class XmartBizHelper implements IBizHelper {
    private BizRequestBuilder mBizRequestBuilder = null;

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public /* bridge */ /* synthetic */ IBizHelper needAuthorizationInfo(@NonNull Map map) {
        return needAuthorizationInfo((Map<String, String>) map);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public XmartBizHelper get(@NonNull String url) {
        GetRequestAdapter request = new GetRequestAdapter(url);
        this.mBizRequestBuilder = new BizRequestBuilder(request, BizConstants.METHOD.GET);
        return this;
    }

    @NonNull
    public XmartBizHelper get(@NonNull String url, @NonNull Map<String, String> para) {
        GetRequestAdapter request = new GetRequestAdapter(url);
        this.mBizRequestBuilder = new BizRequestBuilder(request, BizConstants.METHOD.GET);
        request.params(para, true);
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public XmartBizHelper post(@NonNull String url, @NonNull String jsonBody) {
        PostRequestAdapter request = new PostRequestAdapter(url);
        this.mBizRequestBuilder = new BizRequestBuilder(request, BizConstants.METHOD.POST);
        this.mBizRequestBuilder.body(jsonBody);
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public XmartBizHelper enableIrdetoEncoding() {
        this.mBizRequestBuilder.enableIrdetoEncoding();
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public IBizHelper enableSecurityEncoding() {
        this.mBizRequestBuilder.enableSecurityEncoding();
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public XmartBizHelper needAuthorizationInfo() {
        this.mBizRequestBuilder.needAuthorizationInfo(null);
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public XmartBizHelper needAuthorizationInfo(@NonNull Map<String, String> extParams) {
        this.mBizRequestBuilder.needAuthorizationInfo(extParams);
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public XmartBizHelper appId(@NonNull String value) {
        this.mBizRequestBuilder.appId(value);
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public XmartBizHelper uid(@NonNull String value) {
        this.mBizRequestBuilder.uid(value);
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public XmartBizHelper extendBizHeader(@NonNull String header, @NonNull String value) {
        this.mBizRequestBuilder.setExtHeader(header, value);
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public XmartBizHelper customTokensForAuth(@NonNull String[] tokens) {
        this.mBizRequestBuilder.customTokensForAuth(tokens);
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public IRequest build() {
        return this.mBizRequestBuilder.build(null);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IBizHelper
    @NonNull
    public IRequest buildWithSecretKey(@NonNull String secretKey) {
        return this.mBizRequestBuilder.build(secretKey);
    }
}

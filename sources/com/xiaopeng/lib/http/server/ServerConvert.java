package com.xiaopeng.lib.http.server;

import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.convert.Converter;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;
/* loaded from: classes.dex */
public class ServerConvert implements Converter<ServerBean> {
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.lzy.okgo.convert.Converter
    public ServerBean convertResponse(Response response) throws Throwable {
        ResponseBody body = response.body();
        if (body == null) {
            throw new IllegalStateException("null");
        }
        ServerBean bean = new ServerBean();
        JSONObject jsonObject = new JSONObject(body.string());
        bean.setCode(jsonObject.getInt("code"));
        try {
            bean.setData(jsonObject.getString(CacheEntity.DATA));
        } catch (Throwable th) {
        }
        try {
            bean.setMsg(jsonObject.getString("msg"));
        } catch (Throwable th2) {
        }
        return bean;
    }
}

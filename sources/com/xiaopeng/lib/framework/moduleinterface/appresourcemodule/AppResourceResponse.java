package com.xiaopeng.lib.framework.moduleinterface.appresourcemodule;

import android.content.res.AssetFileDescriptor;
import android.support.annotation.Keep;
import android.text.TextUtils;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.model.Progress;
import org.json.JSONException;
import org.json.JSONObject;
@Keep
/* loaded from: classes.dex */
public class AppResourceResponse {
    private long mCode;
    private String mData;
    private Object mExtra;
    private String mMsg;
    private AppResourceRequest mRequest;
    private String mResult;

    public AppResourceResponse request(AppResourceRequest request) {
        this.mRequest = request;
        return this;
    }

    public AppResourceResponse result(String result) {
        this.mResult = result;
        return this;
    }

    public AppResourceResponse data(String data) {
        this.mData = data;
        return this;
    }

    public AppResourceResponse code(long code) {
        this.mCode = code;
        return this;
    }

    public AppResourceResponse msg(String msg) {
        this.mMsg = msg;
        return this;
    }

    public String msg() {
        return this.mMsg;
    }

    public Object extra() {
        return this.mExtra;
    }

    public AppResourceResponse extra(Object mExtra) {
        this.mExtra = mExtra;
        return this;
    }

    public long code() {
        return this.mCode;
    }

    public AppResourceRequest request() {
        return this.mRequest;
    }

    public String result() {
        return this.mResult;
    }

    public String data() {
        return this.mData;
    }

    public AssetFileDescriptor getFileDescriptor() {
        Object obj = this.mExtra;
        if (obj instanceof AssetFileDescriptor) {
            return (AssetFileDescriptor) obj;
        }
        return null;
    }

    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put(Progress.REQUEST, this.mRequest);
            object.put("result", this.mResult);
            object.put(CacheEntity.DATA, this.mData);
            object.put("code", this.mCode);
            object.put("msg", this.mMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static AppResourceResponse from(String src) {
        try {
            if (!TextUtils.isEmpty(src)) {
                JSONObject object = new JSONObject(src);
                AppResourceResponse response = new AppResourceResponse();
                response.mRequest = AppResourceRequest.from(object.optString(Progress.REQUEST));
                response.mResult = object.optString("result");
                response.mData = object.optString(CacheEntity.DATA);
                response.mCode = object.optLong("code");
                response.mMsg = object.optString("msg");
                return response;
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

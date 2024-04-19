package com.xiaopeng.lib.http.server;

import com.google.gson.annotations.SerializedName;
import com.lzy.okgo.cache.CacheEntity;
/* loaded from: classes.dex */
public class ServerBean {
    @SerializedName("code")
    private int mCode;
    @SerializedName(CacheEntity.DATA)
    private String mData;
    @SerializedName("msg")
    private String mMsg;

    public int getCode() {
        return this.mCode;
    }

    public void setCode(int code) {
        this.mCode = code;
    }

    public String getData() {
        return this.mData;
    }

    public void setData(String data) {
        this.mData = data;
    }

    public String getMsg() {
        return this.mMsg;
    }

    public void setMsg(String msg) {
        this.mMsg = msg;
    }
}

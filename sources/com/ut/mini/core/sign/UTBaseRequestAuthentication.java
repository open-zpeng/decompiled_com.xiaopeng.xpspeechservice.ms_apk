package com.ut.mini.core.sign;

import com.alibaba.mtl.log.d.i;
import com.alibaba.mtl.log.d.j;
/* loaded from: classes.dex */
public class UTBaseRequestAuthentication implements IUTRequestAuthentication {
    private boolean E;
    private String ac;
    private String g;

    @Override // com.ut.mini.core.sign.IUTRequestAuthentication
    public String getAppkey() {
        return this.g;
    }

    public String getAppSecret() {
        return this.ac;
    }

    public UTBaseRequestAuthentication(String aAppkey, String aAppSecret) {
        this.g = null;
        this.ac = null;
        this.E = false;
        this.g = aAppkey;
        this.ac = aAppSecret;
    }

    public UTBaseRequestAuthentication(String aAppkey, String aAppSecret, boolean isEncode) {
        this.g = null;
        this.ac = null;
        this.E = false;
        this.g = aAppkey;
        this.ac = aAppSecret;
        this.E = isEncode;
    }

    public boolean isEncode() {
        return this.E;
    }

    @Override // com.ut.mini.core.sign.IUTRequestAuthentication
    public String getSign(String toBeSignedStr) {
        if (this.g == null || this.ac == null) {
            i.a("UTBaseRequestAuthentication", "There is no appkey,please check it!");
            return null;
        } else if (toBeSignedStr == null) {
            return null;
        } else {
            return j.a(j.m19a((toBeSignedStr + this.ac).getBytes()));
        }
    }
}

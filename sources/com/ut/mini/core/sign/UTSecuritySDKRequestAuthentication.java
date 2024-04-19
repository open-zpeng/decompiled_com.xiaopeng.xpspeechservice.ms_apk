package com.ut.mini.core.sign;

import com.alibaba.mtl.log.d.i;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
/* loaded from: classes.dex */
public class UTSecuritySDKRequestAuthentication implements IUTRequestAuthentication {
    private String ad;
    private String g;
    private Object b = null;
    private Object c = null;
    private Class a = null;

    /* renamed from: a  reason: collision with other field name */
    private Field f159a = null;

    /* renamed from: b  reason: collision with other field name */
    private Field f161b = null;

    /* renamed from: c  reason: collision with other field name */
    private Field f162c = null;

    /* renamed from: a  reason: collision with other field name */
    private Method f160a = null;
    private int z = 1;
    private boolean F = false;

    @Override // com.ut.mini.core.sign.IUTRequestAuthentication
    public String getAppkey() {
        return this.g;
    }

    public UTSecuritySDKRequestAuthentication(String aAppkey, String authCode) {
        this.g = null;
        this.g = aAppkey;
        this.ad = authCode;
    }

    /* JADX WARN: Removed duplicated region for block: B:17:0x0051 A[Catch: all -> 0x00d7, TRY_ENTER, TRY_LEAVE, TryCatch #5 {, blocks: (B:3:0x0001, B:41:0x00e3, B:15:0x0046, B:17:0x0051, B:23:0x008f, B:36:0x00bb, B:18:0x0079, B:26:0x009f), top: B:51:0x0001 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private synchronized void D() {
        /*
            Method dump skipped, instructions count: 234
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.ut.mini.core.sign.UTSecuritySDKRequestAuthentication.D():void");
    }

    @Override // com.ut.mini.core.sign.IUTRequestAuthentication
    public String getSign(String toBeSignedStr) {
        Class cls;
        if (!this.F) {
            D();
        }
        if (this.g == null) {
            i.a("UTSecuritySDKRequestAuthentication:getSign", "There is no appkey,please check it!");
            return null;
        } else if (toBeSignedStr == null) {
            return null;
        } else {
            if (this.b != null && (cls = this.a) != null && this.f159a != null && this.f161b != null && this.f162c != null && this.f160a != null && this.c != null) {
                try {
                    Object newInstance = cls.newInstance();
                    this.f159a.set(newInstance, this.g);
                    ((Map) this.f161b.get(newInstance)).put("INPUT", toBeSignedStr);
                    this.f162c.set(newInstance, Integer.valueOf(this.z));
                    return (String) this.f160a.invoke(this.c, newInstance, this.ad);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e2) {
                    e2.printStackTrace();
                } catch (InstantiationException e3) {
                    e3.printStackTrace();
                } catch (InvocationTargetException e4) {
                    e4.printStackTrace();
                }
            }
            return null;
        }
    }

    public String getAuthCode() {
        return this.ad;
    }
}

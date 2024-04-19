package com.xiaopeng.lib.utils.log;
/* loaded from: classes.dex */
public class XLogExplosionException extends RuntimeException {
    XLogExplosionException(String packageName) {
        super("不好意思，我在喷Log，1分钟喷了128k，我的包名是：" + packageName);
    }
}

package com.xiaopeng.lib.utils.log;
/* loaded from: classes.dex */
public class XLogLoadLibraryException extends Exception {
    /* JADX INFO: Access modifiers changed from: package-private */
    public XLogLoadLibraryException(Throwable t) {
        super("Load library fail, please check /system/lib/libc++_shared.so and /system/lib/libmarsxlog.so", t);
    }
}

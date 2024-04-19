package com.xiaopeng.lib.framework.moduleinterface.accountmodule;
/* loaded from: classes.dex */
public abstract class AbsException extends Exception {
    public abstract int getCode();

    public AbsException() {
    }

    public AbsException(int code) {
    }

    public AbsException(String message) {
        super(message);
    }

    public AbsException(int code, String message) {
        super(message);
    }
}

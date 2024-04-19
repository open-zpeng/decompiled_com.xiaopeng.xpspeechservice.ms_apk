package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public class NotSupportV1Exception extends Exception {
    public NotSupportV1Exception() {
        this("this api is not support for carControllerModuleImplement V1");
    }

    public NotSupportV1Exception(String detailMessage) {
        super(detailMessage);
    }

    public NotSupportV1Exception(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NotSupportV1Exception(Throwable throwable) {
        super(throwable);
    }
}

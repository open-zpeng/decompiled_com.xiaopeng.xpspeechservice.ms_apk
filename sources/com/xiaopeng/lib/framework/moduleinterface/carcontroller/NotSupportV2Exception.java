package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public class NotSupportV2Exception extends Exception {
    public NotSupportV2Exception() {
        this("this api is not support for carControllerModuleImplement V2");
    }

    public NotSupportV2Exception(String detailMessage) {
        super(detailMessage);
    }

    public NotSupportV2Exception(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NotSupportV2Exception(Throwable throwable) {
        super(throwable);
    }
}

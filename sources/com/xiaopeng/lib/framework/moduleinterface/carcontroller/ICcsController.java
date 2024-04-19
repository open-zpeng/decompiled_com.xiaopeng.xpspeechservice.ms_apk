package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public interface ICcsController extends ILifeCycle {

    /* loaded from: classes.dex */
    public static class CcsFaultInfoEventMsg extends AbstractEventMsg<Integer> {
    }

    int getCcsFaultInfo() throws Exception;
}

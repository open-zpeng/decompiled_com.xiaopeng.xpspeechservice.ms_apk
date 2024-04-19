package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public interface IDcdcController extends ILifeCycle {

    /* loaded from: classes.dex */
    public static class DcdcActTempEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class DcdcFailStInfoEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class DcdcRealInputCurrentEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class DcdcStatusEventMsg extends AbstractEventMsg<Integer> {
    }

    int getDcdcActTemp() throws Exception;

    int getDcdcFailStInfo() throws Exception;

    float getDcdcRealInputCurrent() throws Exception;

    int getDcdcStatus() throws Exception;
}

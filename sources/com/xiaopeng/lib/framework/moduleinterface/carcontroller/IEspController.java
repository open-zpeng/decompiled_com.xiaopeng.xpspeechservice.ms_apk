package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public interface IEspController extends ILifeCycle {

    /* loaded from: classes.dex */
    public static class AVHEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class AVHFaultEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class ESPEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class ESPFaultEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class EpbWarningLampOnEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class EpsWarningLampOnEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class EspEbdFaultStatusEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class EspHbbFaultStatusEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class HDCEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class HDCFaultEventMsg extends AbstractEventMsg<Boolean> {
    }

    boolean getAVH() throws Exception;

    boolean getAvhFault() throws Exception;

    boolean getESP() throws Exception;

    int getEspEbdFaultStatus() throws Exception;

    boolean getEspFault() throws Exception;

    int getEspHbbFaultStatus() throws Exception;

    boolean getHDC() throws Exception;

    boolean getHdcFault() throws Exception;

    boolean isAbsFault() throws Exception;

    boolean isAvhFault() throws Exception;

    boolean isEpbWarningLampOn() throws Exception;

    boolean isEpsWarningLampOn() throws Exception;

    boolean isEspFault() throws Exception;

    boolean isHdcFault() throws Exception;

    void setAVH(boolean enable) throws Exception;

    void setESP(boolean enable) throws Exception;

    void setHDC(boolean enable) throws Exception;
}

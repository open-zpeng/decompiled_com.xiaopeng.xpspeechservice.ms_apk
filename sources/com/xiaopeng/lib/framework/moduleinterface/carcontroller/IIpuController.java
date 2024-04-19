package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public interface IIpuController extends ILifeCycle {

    /* loaded from: classes.dex */
    public static class CtrlCurrEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class CtrlTempEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class CtrlVoltEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class IpuFailStInfoEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class IpuKl15VoltageEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class IpuVendorCodeEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class MotorStatusEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class MotorTempEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class RollSpeedEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class TorqueEventMsg extends AbstractEventMsg<Float> {
    }

    int getCtrlCurr() throws Exception;

    int getCtrlTemp() throws Exception;

    int getCtrlVolt() throws Exception;

    int getIpuFailStInfo() throws Exception;

    int getIpuInvtOverTempFault() throws Exception;

    int getIpuInvtOverTempFlag() throws Exception;

    int getIpuInvtOverTempWarn() throws Exception;

    int getIpuKl15Voltage() throws Exception;

    int getIpuMotOverTempFault() throws Exception;

    int getIpuMotOverTempFlag() throws Exception;

    int getIpuMotOverTempWarn() throws Exception;

    int getIpuMotorOverTempFault() throws Exception;

    int getIpuMotorOverTempWarn() throws Exception;

    int getIpuNtcOverTempFault() throws Exception;

    int getIpuNtcOverTempWarn() throws Exception;

    int getIpuVendorCode() throws Exception;

    int getMotorStatus() throws Exception;

    int getMotorTemp() throws Exception;

    int getRollSpeed() throws Exception;

    float getTorque() throws Exception;
}

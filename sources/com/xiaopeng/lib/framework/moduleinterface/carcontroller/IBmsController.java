package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public interface IBmsController extends ILifeCycle {
    public static final int DC_CHARGE_STOP_REASON = 4;
    public static final int DTC_CODE_ERROR = 3;
    public static final int DTC_CODE_NORMAL = 0;

    /* loaded from: classes.dex */
    public static class ACMaxCurrentEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class ActualSocStatusEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class BatteryCurrEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class BatteryMaximumVoltEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class BatteryMinimumVoltEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class BatteryTempMaxEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class BatteryTempMaxNumEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BatteryTempMinNumEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BatteryTempUnbalEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BatteryTotalVoltEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BmsBatteryStatusEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BmsBatteryTempUnbalEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsBatteryTemperatureOverLvlEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsBatteryVoltageOverLvlEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsBatteryVoltageUnderLvlEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsCellVoltageOverLvlEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsCellVoltageUnbalEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BmsCellVoltageUnderLvlEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsCfgAndCellCfgEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsConfigEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BmsDtcPageAndFbFltAtLowTemperatureEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsDtcPageAndHvilFltEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsErrorNumberEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BmsFailureLvlEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BmsIsoFltLvlEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsPowerCellTemperatureAllEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class BmsPowerCellVoltAllEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class CellVoltMaxEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class CellVoltMaxNumEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class CellVoltMinEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class CellVoltMinNumEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class DCCurrentEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class DCVoltEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class DcChargeStopReasonEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class DtcChargeCurrentOverEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class DtcErrorStopCurrentEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class InsulationStEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class TemperatureMaxEventMsg extends AbstractEventMsg<Float> {
    }

    float getAcMaxCurrent() throws Exception;

    float getActualSocStatus() throws Exception;

    int getAddedElectricity();

    float getBatteryCurrent() throws Exception;

    float getBatteryMaximumVolt() throws Exception;

    float getBatteryMinimumVolt() throws Exception;

    int[] getBatteryTempUnbal() throws Exception;

    int getBatteryTotalVolt() throws Exception;

    int getBmsBatteryStatus() throws Exception;

    int[] getBmsBatteryTemperatureOverLvl() throws Exception;

    int[] getBmsBatteryVoltageOverLvl() throws Exception;

    int[] getBmsBatteryVoltageUnderLvl() throws Exception;

    int[] getBmsCellVoltageOverLvl() throws Exception;

    int[] getBmsCellVoltageUnderLvl() throws Exception;

    int[] getBmsCfgAndCellCfg() throws Exception;

    int getBmsConfig() throws Exception;

    int[] getBmsDtcPageAndFbFltAtLowTemperature() throws Exception;

    int[] getBmsDtcPageAndHvilFlt() throws Exception;

    int getBmsErrorNumber() throws Exception;

    int getBmsFailureLvl() throws Exception;

    int[] getBmsIsoFltLvl() throws Exception;

    byte[] getBmsPowerCellTemperatureAll() throws Exception;

    byte[] getBmsPowerCellVoltAll() throws Exception;

    int getCellVoltageUnbal() throws Exception;

    int getChargeMode() throws Exception;

    float getDCCurrent() throws Exception;

    float getDCVolt() throws Exception;

    int getDcChargeStopReason() throws Exception;

    int getDtcChargeCurrentOver() throws Exception;

    int getDtcErrorStopCurrent() throws Exception;

    int getInsulationResistance() throws Exception;

    float getTempMax() throws Exception;

    int getTempMaxNum() throws Exception;

    int getTempMinNum() throws Exception;

    float getTemperatureMax() throws Exception;

    float getVoltMax() throws Exception;

    int getVoltMaxNum() throws Exception;

    float getVoltMin() throws Exception;

    int getVoltMinNum() throws Exception;
}

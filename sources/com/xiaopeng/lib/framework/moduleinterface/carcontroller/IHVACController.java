package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public interface IHVACController extends ILifeCycle {

    /* loaded from: classes.dex */
    public static class HVACAutoModeBlowLevelEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class HVACAutoModeEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class HVACCirculationModeEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class HVACEconEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class HVACFrontDefrostModeEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class HVACInnerTempEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class HVACPowerModeEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class HVACQualityInnerPM25ValueEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class HVACQualityOutsideLevelEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class HVACQualityOutsideStatusEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class HVACQualityPurgeModeEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class HVACTempACModeEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class HVACTempDriverValueEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class HVACTempPTCStatusEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class HVACTempPsnValueEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class HVACTempSyncModEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class HVACWindBlowModeEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class HVACWindSpeedLevelEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class OutsideAirTempEventMsg extends AbstractEventMsg<Float> {
    }

    int[] getCompressorErrorInfo() throws Exception;

    boolean getHVACAutoMode() throws Exception;

    int getHVACAutoModeBlowLevel() throws Exception;

    int getHVACCirculationMode() throws Exception;

    boolean getHVACEcon() throws Exception;

    boolean getHVACFrontDefrostMode() throws Exception;

    float getHVACInnerTemp() throws Exception;

    boolean getHVACPowerMode() throws Exception;

    int getHVACQualityInnerPM25Value() throws Exception;

    int getHVACQualityOutsideLevel() throws Exception;

    int getHVACQualityOutsideStatus() throws Exception;

    boolean getHVACQualityPurgeMode() throws Exception;

    boolean getHVACTempACMode() throws Exception;

    float getHVACTempDriverValue() throws Exception;

    float getHVACTempPsnValue() throws Exception;

    boolean getHVACTempSyncMode() throws Exception;

    int getHVACWindBlowMode() throws Exception;

    int getHVACWindSpeedLevel() throws Exception;

    float getOutsideAirTemp() throws Exception;

    int getPtcError() throws Exception;

    int getTempPTCStatus() throws Exception;

    boolean isError() throws Exception;

    void setHVACAutoMode() throws Exception;

    void setHVACAutoMode(boolean enable) throws Exception;

    void setHVACAutoModeBlowLevel(int level) throws Exception;

    void setHVACCirculationMode() throws Exception;

    void setHVACCirculationMode(int mode) throws Exception;

    void setHVACEcon(boolean isOpen) throws Exception;

    void setHVACFrontDefrostMode() throws Exception;

    void setHVACFrontDefrostMode(boolean enable) throws Exception;

    @Deprecated
    void setHVACPowerMode() throws Exception;

    void setHVACPowerMode(boolean enable) throws Exception;

    void setHVACQualityPurgeMode() throws Exception;

    void setHVACQualityPurgeMode(boolean enable) throws Exception;

    void setHVACTempACMode() throws Exception;

    void setHVACTempACMode(boolean enable) throws Exception;

    void setHVACTempDriverDown() throws Exception;

    void setHVACTempDriverDown(float value) throws Exception;

    void setHVACTempDriverUp() throws Exception;

    void setHVACTempDriverUp(float value) throws Exception;

    void setHVACTempDriverValue(float level) throws Exception;

    void setHVACTempPsnDown() throws Exception;

    void setHVACTempPsnDown(float value) throws Exception;

    void setHVACTempPsnUp() throws Exception;

    void setHVACTempPsnUp(float value) throws Exception;

    void setHVACTempPsnValue(float level) throws Exception;

    void setHVACTempSyncMode() throws Exception;

    void setHVACTempSyncMode(boolean enable) throws Exception;

    void setHVACWindBlowMode(int mode) throws Exception;

    void setHVACWindSpeedDown() throws Exception;

    void setHVACWindSpeedDown(int value) throws Exception;

    void setHVACWindSpeedLevel(int level) throws Exception;

    void setHVACWindSpeedUp() throws Exception;

    void setHVACWindSpeedUp(int value) throws Exception;

    void setTempPTCStatus(int status) throws Exception;
}

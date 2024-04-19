package com.xiaopeng.lib.framework.moduleinterface.carcontroller;

import java.util.HashMap;
import java.util.LinkedList;
/* loaded from: classes.dex */
public interface IIcmController extends ILifeCycle, com.xiaopeng.lib.framework.moduleinterface.carcontroller.v2extend.IIcmController {
    public static final int DISTRACTION_LEVEL_L1 = 1;
    public static final int DISTRACTION_LEVEL_NONE = 0;
    public static final int DMS_MODE_EXTENDED = 1;
    public static final int DMS_MODE_NORMAL = 0;
    public static final int FATIGUE_LEVEL_L1 = 1;
    public static final int FATIGUE_LEVEL_L2 = 2;
    public static final int FATIGUE_LEVEL_NONE = 0;
    public static final int ICM_ALARM_STATUS_VOLUME_POWER = 2;
    public static final int ICM_ALARM_STATUS_VOLUME_SOFT = 0;
    public static final int ICM_ALARM_STATUS_VOLUME_STANDARD = 1;
    @Deprecated
    public static final int ICM_DAY_NIGHT_MODE_DAY = 0;
    @Deprecated
    public static final int ICM_DAY_NIGHT_MODE_NIGHT = 1;
    public static final int ICM_METER_ALARM_VOLUME_POWER = 2;
    public static final int ICM_METER_ALARM_VOLUME_SOFT = 0;
    public static final int ICM_METER_ALARM_VOLUME_STANDARD = 1;
    public static final int ICM_METER_TIME_FORMAT_12_HOUR = 2;
    public static final int ICM_METER_TIME_FORMAT_24_HOUR = 1;

    /* loaded from: classes.dex */
    public static class AlarmVolumeEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ICMBTContactsEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ICMDriverTempValueEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class ICMLightChangeEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ICMSystemTimeValueEventMsg extends AbstractEventMsg<Integer> {
    }

    @Deprecated
    /* loaded from: classes.dex */
    public static class ICMWindBlowModeEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ICMWindLevelEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class IcmConnectEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class IcmDayNightEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class IcmDriveTotalMileageEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class IcmFeedbackEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class IcmLastChargeMileageEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class IcmMediaSourceEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class IcmMileageAEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class IcmMileageBEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class IcmNavigationEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class IcmScreenLightEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class IcmSendResultEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class IcmStartUpMileageEventMsg extends AbstractEventMsg<Float> {
    }

    /* loaded from: classes.dex */
    public static class IcmTemperatureEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class IcmWindModeEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class IcmWindPowerEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class SpeedLimitWarningSwitchEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class SpeedLimitWarningValueEventMsg extends AbstractEventMsg<Integer> {
    }

    int getContacts();

    float getDriveTotalMileage() throws Exception;

    float getICMDriverTempValue() throws Exception;

    int[] getICMSystemTimeValue() throws Exception;

    @Deprecated
    int getICMWindBlowMode() throws Exception;

    int getICMWindLevel() throws Exception;

    int getIcmAlarmVolume() throws Exception;

    boolean getIcmConnectionState() throws Exception;

    boolean getIcmDayNightSwitch() throws Exception;

    int getIcmFeedback() throws Exception;

    boolean getIcmMediaSource() throws Exception;

    boolean getIcmNavigation() throws Exception;

    boolean getIcmScreenLight() throws Exception;

    boolean getIcmTemperature() throws Exception;

    boolean getIcmWindMode() throws Exception;

    boolean getIcmWindPower() throws Exception;

    float getLastChargeMileage() throws Exception;

    float getLastStartUpMileage() throws Exception;

    float getMeterMileageA() throws Exception;

    float getMeterMileageB() throws Exception;

    boolean getSpeedLimitWarningSwitch() throws Exception;

    int getSpeedLimitWarningValue() throws Exception;

    int getWheelEvent();

    void resetMeterMileageA() throws Exception;

    void resetMeterMileageB() throws Exception;

    void sendBinMsg(int rpcNum, byte[] bjson, byte[] bbin) throws Exception;

    void sendContacts(byte[] contactsJson) throws Exception;

    @Deprecated
    void sendRomBinMsgNew(byte[] bytes) throws Exception;

    void sendSpeechStateInfo(byte[] info) throws Exception;

    void setBtStateMessage(byte[] json) throws Exception;

    void setICMDriverTempValue(float value) throws Exception;

    void setICMSystemTimeValue(int hours, int minutes) throws Exception;

    void setICMWindBlowMode(int mode) throws Exception;

    void setICMWindLevel(int level) throws Exception;

    void setIcmAccount(byte[] account) throws Exception;

    void setIcmAlarmVolume(int volumeLevel) throws Exception;

    @Deprecated
    void setIcmDayNightMode(int icmDayNightMode) throws Exception;

    void setIcmDayNightSwitch(boolean enable) throws Exception;

    void setIcmDistractionLevel(int level) throws Exception;

    void setIcmDmsMode(int mode) throws Exception;

    void setIcmFatigueLevel(int level) throws Exception;

    void setIcmMediaSource(boolean enable) throws Exception;

    void setIcmMultiProperty(LinkedList<HashMap<Integer, Object>> propertyHashMap) throws Exception;

    void setIcmNavigation(boolean enable) throws Exception;

    void setIcmScreenLight(boolean enable) throws Exception;

    void setIcmTemperature(boolean enable) throws Exception;

    void setIcmTimeFormat(int index) throws Exception;

    void setIcmWindMode(boolean enable) throws Exception;

    void setIcmWindPower(boolean enable) throws Exception;

    void setMeterBackLightLevel(int level) throws Exception;

    void setMeterSoundState(int type, int volume, boolean mute) throws Exception;

    void setMusicInfo(byte[] json, byte[] image) throws Exception;

    void setNavigationInfo(byte[] json) throws Exception;

    void setNetRadioInfo(byte[] json, byte[] image) throws Exception;

    void setRadioInfo(byte[] json) throws Exception;

    void setSpeedLimitWarningSwitch(boolean enable) throws Exception;

    void setSpeedLimitWarningValue(int value) throws Exception;

    void setWeatherInfo(byte[] json) throws Exception;
}

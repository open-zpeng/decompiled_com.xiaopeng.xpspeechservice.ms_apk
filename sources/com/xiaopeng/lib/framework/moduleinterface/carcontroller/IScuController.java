package com.xiaopeng.lib.framework.moduleinterface.carcontroller;
/* loaded from: classes.dex */
public interface IScuController extends ILifeCycle, com.xiaopeng.lib.framework.moduleinterface.carcontroller.v2extend.IScuController {
    public static final int ASSLINE_CHANGED_DISABLE = 1;
    public static final int ASSLINE_CHANGED_ENABLE = 2;
    public static final int ASSLINE_CHANGED_INVALID = 0;
    public static final int DMS_MODE_EXTENDED = 1;
    public static final int DMS_MODE_NORMAL = 0;
    public static final int DOOR_OPEN_WARNING_OFF = 1;
    public static final int DOOR_OPEN_WARNING_ON = 2;
    public static final int RESPONSE_SCU_ACTIVE = 3;
    public static final int RESPONSE_SCU_NO_FAULT = 0;
    public static final int RESPONSE_SCU_PERMANENT_ERROR = 2;
    public static final int RESPONSE_SCU_TEMPORARY_ERROR = 1;
    public static final int ROAD_ATTR_ROAD_FASTWAY_IN_CITY = 2;
    public static final int ROAD_ATTR_ROAD_HIGHWAY = 1;
    public static final int SCU_AUTO_PARK_CMD_RESET = 0;
    public static final int SCU_AUTO_PARK_IN_CANCLE = 2;
    public static final int SCU_AUTO_PARK_IN_CONTINUE = 4;
    public static final int SCU_AUTO_PARK_IN_START = 1;
    public static final int SCU_AUTO_PARK_OUT_CANCLE = 2;
    public static final int SCU_AUTO_PARK_OUT_CONTINUE = 4;
    public static final int SCU_AUTO_PARK_OUT_START = 3;
    public static final int SCU_BLIND_AREA_DETECTION_WARNING_DISABLE = 0;
    public static final int SCU_BLIND_AREA_DETECTION_WARNING_ENABLE = 1;
    public static final int SCU_BUTTON_OFF = 0;
    public static final int SCU_BUTTON_ON = 1;
    public static final int SCU_FRONT_COLLISION_DISABLE = 0;
    public static final int SCU_FRONT_COLLISION_ENABLE = 1;
    public static final int SCU_INTELLIGENT_SPEED_LIMIT_DISABLE = 0;
    public static final int SCU_INTELLIGENT_SPEED_LIMIT_ENABLE = 1;
    public static final int SCU_LANE_CHANGE_ASSIST_DISABLE = 0;
    public static final int SCU_LANE_CHANGE_ASSIST_ENABLE = 1;
    public static final int SCU_LANE_DEPARTURE_WARNING_DISABLE = 0;
    public static final int SCU_LANE_DEPARTURE_WARNING_ENABLE = 1;
    public static final int SCU_LANE_MIDDLE_ASSIST_DISABLE = 0;
    public static final int SCU_LANE_MIDDLE_ASSIST_ENABLE = 1;
    public static final int SCU_OTA_TAG_0 = 0;
    public static final int SCU_OTA_TAG_1 = 1;
    public static final int SCU_OTA_TAG_2 = 2;
    public static final int SCU_OTA_TAG_3 = 3;
    public static final int SCU_PHONEPKBUTTON_APIN = 1;
    public static final int SCU_PHONEPKBUTTON_APOUT = 2;
    public static final int SCU_PHONEPKBUTTON_CANCEL = 5;
    public static final int SCU_PHONEPKBUTTON_CONTINUE = 4;
    public static final int SCU_PHONEPKBUTTON_INVALID = 0;
    public static final int SCU_PHONEPKBUTTON_SUSPENG = 3;
    public static final int SCU_PHONESMBUTTON_CALLBACKWORK = 3;
    public static final int SCU_PHONESMBUTTON_CALLENTER = 1;
    public static final int SCU_PHONESMBUTTON_CALLFORWARD = 2;
    public static final int SCU_PHONESMBUTTON_EXITMODE = 4;
    public static final int SCU_PHONESMBUTTON_INVALID = 0;
    public static final int SCU_REVERSING_DISPLAY_ACTIVE_NO = 0;
    public static final int SCU_REVERSING_DISPLAY_ACTIVE_YES = 1;
    public static final int SCU_ROAD_ATTR_HOME_PARKING = 6;
    public static final int SCU_ROAD_ATTR_NOT_DEFINED = 0;
    public static final int SCU_ROAD_ATTR_PARKING_TOWER = 4;
    public static final int SCU_ROAD_ATTR_PRIVATE_ROAD = 2;
    public static final int SCU_ROAD_ATTR_PUBLIC_ROAD = 1;
    public static final int SCU_ROAD_ATTR_ROAD_PARKING = 5;
    public static final int SCU_ROAD_ATTR_UNDERGROUND_PARKING = 3;
    public static final int SCU_SIDE_REVERSING_WARNING_DISABLE = 0;
    public static final int SCU_SIDE_REVERSING_WARNING_ENABLE = 1;
    public static final int SCU_STEER_WARNING_Lvl1 = 1;
    public static final int SCU_STEER_WARNING_Lvl2 = 2;
    public static final int SCU_STEER_WARNING_Lvl3 = 3;
    public static final int SCU_SUPER_PARK_ACTIVE_NO = 0;
    public static final int SCU_SUPER_PARK_ACTIVE_YES = 1;
    public static final int SEATBELTREQ_FASTEN = 1;
    public static final int SEATBELTREQ_NONE = 0;

    /* loaded from: classes.dex */
    public static class AccLKAWarningEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class AltimeterEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class AssLineChangedEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class AutoParkEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class BlindAreaDetectionWarningEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class FactoryScuTest322EventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class FactoryScuTest3FDEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class FactoryScuTest3FEEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class FrontCollisionSecurityEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class IntelligentSpeedLimitEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class KeyParkEventMsg extends AbstractEventMsg<Boolean> {
    }

    @Deprecated
    /* loaded from: classes.dex */
    public static class LaneChangeAssistEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class LaneDepartureWarningEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ParkingProcessPathEventMsg extends AbstractEventMsg<float[]> {
    }

    /* loaded from: classes.dex */
    public static class RadarWarningVoiceStatusEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class RearMirrorCtrl extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuAebAlarmSwitchStateEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuAutoParkErrorCodeEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuAutoRoadTipsAddEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuAvmBox1UpdateEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuAvmBox2UpdateEventMsg extends AbstractEventMsg<float[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuBSDWarningEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuFrontMinDistanceEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuFrontRadarFaultStEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuFrontRadarLevelEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuLDWWarningEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuLKAWarningEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuLocation2UpdateEventMsg extends AbstractEventMsg<float[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuLocationUpdateEventMsg extends AbstractEventMsg<float[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuModeIndexEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuNearestEnableRadarEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuOperationTipUpdateEventMsg extends AbstractEventMsg<Integer> {

        /* loaded from: classes.dex */
        public static class Error extends AbsError {
        }
    }

    /* loaded from: classes.dex */
    public static class ScuRCTAWarningEventMsg extends AbstractEventMsg<int[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuRearMinDistanceEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuReversingDisplayActiveEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuRoadAttributesEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuRoadVoiceTipsEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuSensorFeature1UpdateEventMsg extends AbstractEventMsg<float[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuSensorFeature2UpdateEventMsg extends AbstractEventMsg<float[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuSlot1UpdateEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuSlot2UpdateEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuSlot3UpdateEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuSlot4UpdateEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuSlot5UpdateEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuSteerWaringLvlEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class ScuSuperParkActiveResponeEventMsg extends AbstractEventMsg<Boolean> {
    }

    /* loaded from: classes.dex */
    public static class ScuTailRadarFaultStEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class ScuTailRadarLevelEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class SideReversingWarningEventMsg extends AbstractEventMsg<Integer> {
    }

    /* loaded from: classes.dex */
    public static class SlotThetaEventMsg extends AbstractEventMsg<byte[]> {
    }

    /* loaded from: classes.dex */
    public static class TargetParkingPositionEventMsg extends AbstractEventMsg<byte[]> {
    }

    int getAccExitReason() throws Exception;

    int getAccLkaWarning() throws Exception;

    int getAebAlarmSwitchState() throws Exception;

    byte[] getAltimeter() throws Exception;

    boolean getAssLineChanged() throws Exception;

    int getAutoParkErrorCode() throws Exception;

    int getAutoParkSwitch() throws Exception;

    int getAutoRoadTips() throws Exception;

    int getBlindAreaDetectionWarning() throws Exception;

    int[] getFrontCameraFault() throws Exception;

    int getFrontCollisionSecurity() throws Exception;

    int getFrontMmRadarFault() throws Exception;

    float[] getFrontRadarData() throws Exception;

    int[] getFrontRadarFaultSt() throws Exception;

    int[] getFrontRadarLevel() throws Exception;

    int getIcmAlarmFaultState() throws Exception;

    int getIntelligentSpeedLimit() throws Exception;

    boolean getKeyPark() throws Exception;

    @Deprecated
    int getLaneChangeAssist() throws Exception;

    int getLaneDepartureWarning() throws Exception;

    int getLccExitReason() throws Exception;

    float[] getMileageExtraParams() throws Exception;

    int getModeIndex() throws Exception;

    int getNearestEnableRadar() throws Exception;

    int getParkStatus() throws Exception;

    int getParkingOperationTips() throws Exception;

    float[] getParkingProcessPath() throws Exception;

    boolean getRadarWarningVoiceStatus() throws Exception;

    int[] getRearMmRadarFault() throws Exception;

    int getRoadVoiceTips() throws Exception;

    int[] getScu322LogData() throws Exception;

    int[] getScu3FDExtendLogData() throws Exception;

    int getScuFrontMinDistance() throws Exception;

    int getScuModeIndex() throws Exception;

    int getScuRearMinDistance() throws Exception;

    int getScuSteerWaringLvl() throws Exception;

    int getSideReversingWarning() throws Exception;

    byte[] getSlotTheta() throws Exception;

    float[] getTailRadarData() throws Exception;

    int[] getTailRadarFaultSt() throws Exception;

    int[] getTailRadarLevel() throws Exception;

    byte[] getTargetParkingPosition() throws Exception;

    void setAssLineChanged(int type) throws Exception;

    void setAutoParkInState(int state) throws Exception;

    void setAutoParkOutState(int state) throws Exception;

    void setAutoParkSwitch(int enable) throws Exception;

    void setAutoPilotLocationInfo(float latitude, float longitude, float altitude, float bearing, float accuracy, float gpsSpeed, long gpsTime) throws Exception;

    void setBlindAreaDetectionWarning(int type) throws Exception;

    void setComonHomeSlotId(int value) throws Exception;

    void setDetailRoadClass(int roadClass) throws Exception;

    void setDoorOpenWarningSwitch(int value) throws Exception;

    void setFactoryScuTest(int cmd) throws Exception;

    void setFreeParking1Data(float rx, float ry, float rtheta, int state, int attr, float ds, float r) throws Exception;

    void setFreeParking2Data(float rx, float ry, float rtheta, int state, int attr, float ds, float r) throws Exception;

    void setFrontCollisionSecurity(int type) throws Exception;

    void setIntelligentSpeedLimit(int type) throws Exception;

    void setKeyPark(boolean enable) throws Exception;

    void setLaneChangeAssist(int type) throws Exception;

    void setLaneDepartureWarning(int type) throws Exception;

    void setLaneMiddleAssist(int enable) throws Exception;

    void setLocalWeather(int network, int temperature, int humidity, int weather) throws Exception;

    void setLocationInfo(float latitude, float longitude, float altitude, float bearing, float accuracy, long gpsTime) throws Exception;

    void setParkLotChoseIndex2Scu(int index) throws Exception;

    void setParkLotRecvIndex2Scu(int index) throws Exception;

    void setPhoneAPButton(int action) throws Exception;

    void setPhoneSMButton(int mode) throws Exception;

    void setRadarWarningVoiceStatus(boolean enable) throws Exception;

    void setRoadAttributes(int parking, int road) throws Exception;

    void setScuDmsMode(int mode) throws Exception;

    void setScuOtaTagStatus(int tag) throws Exception;

    void setScuRoadAttr(int attr) throws Exception;

    void setSeatBeltReq(int req) throws Exception;

    void setSideReversingWarning(int type) throws Exception;

    void setSuperParkMode(boolean active) throws Exception;
}

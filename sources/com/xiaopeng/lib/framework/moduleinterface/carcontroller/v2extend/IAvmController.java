package com.xiaopeng.lib.framework.moduleinterface.carcontroller.v2extend;

import com.xiaopeng.lib.framework.moduleinterface.carcontroller.AbstractEventMsg;
/* loaded from: classes.dex */
public interface IAvmController {

    /* loaded from: classes.dex */
    public static class V2FrontRadarDataEventMsg extends AbstractEventMsg<float[]> {
    }

    /* loaded from: classes.dex */
    public static class V2TailRadarDataEventMsg extends AbstractEventMsg<float[]> {
    }

    int getOverlayWorkSt() throws Exception;

    void setMultipleDisplayProperties(int displayMode, int calibration, int overlayWorkSt, int transparentChassisWorkSt, int fineTuneMode) throws Exception;

    void setOverlayWorkSt(int status) throws Exception;
}

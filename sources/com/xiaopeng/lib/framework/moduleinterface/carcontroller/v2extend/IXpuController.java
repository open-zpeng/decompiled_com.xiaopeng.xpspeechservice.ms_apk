package com.xiaopeng.lib.framework.moduleinterface.carcontroller.v2extend;

import com.xiaopeng.lib.framework.moduleinterface.carcontroller.AbstractEventMsg;
import com.xiaopeng.lib.framework.moduleinterface.carcontroller.ILifeCycle;
/* loaded from: classes.dex */
public interface IXpuController extends ILifeCycle {

    /* loaded from: classes.dex */
    public static class NedcSwitchEventMsg extends AbstractEventMsg<Integer> {
    }

    int getNedcSwitchStatus() throws Exception;

    void setNedcSwitch(int onOff) throws Exception;
}

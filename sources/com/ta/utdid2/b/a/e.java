package com.ta.utdid2.b.a;

import com.xiaopeng.lib.framework.moduleinterface.carcontroller.IRadioController;
/* compiled from: IntUtils.java */
/* loaded from: classes.dex */
public class e {
    public static byte[] getBytes(int i) {
        byte[] bArr = {(byte) ((r1 >> 8) % IRadioController.TEF663x_PCHANNEL), (byte) (r1 % IRadioController.TEF663x_PCHANNEL), (byte) (r1 % IRadioController.TEF663x_PCHANNEL), (byte) (i % IRadioController.TEF663x_PCHANNEL)};
        int i2 = i >> 8;
        int i3 = i2 >> 8;
        return bArr;
    }
}

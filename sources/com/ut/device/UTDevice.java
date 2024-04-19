package com.ut.device;

import android.content.Context;
/* loaded from: classes.dex */
public class UTDevice {
    public static String getUtdid(Context context) {
        return com.ta.utdid2.device.UTDevice.getUtdid(context);
    }

    public static String getAid(String appName, String token, Context context) {
        return com.ta.utdid2.a.a.a(context).a(appName, token, getUtdid(context));
    }

    public static void getAidAsync(String appName, String token, Context context, a callback) {
        com.ta.utdid2.a.a.a(context).a(appName, token, getUtdid(context), callback);
    }
}

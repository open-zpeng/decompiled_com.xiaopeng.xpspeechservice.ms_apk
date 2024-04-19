package com.xiaopeng.xpspeechservice.utils;

import android.os.SystemProperties;
/* loaded from: classes.dex */
public class XpSysUtils {
    private static final String BUILD_TYPE = "ro.build.type";
    private static final String TYPE_USER = "user";

    public static boolean isUserBuild() {
        String type = SystemProperties.get(BUILD_TYPE, TYPE_USER);
        if (type.equals(TYPE_USER)) {
            return true;
        }
        return false;
    }

    public static boolean isDevBuild() {
        String version = SystemProperties.get("ro.xiaopeng.software", "DEV");
        if (version.endsWith("DEV")) {
            return true;
        }
        return false;
    }
}

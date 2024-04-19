package com.xiaopeng.lib.utils.info;

import android.os.SystemProperties;
import android.util.Log;
/* loaded from: classes.dex */
public class DeviceInfoUtils {
    public static final int POWER_STATUS_FAKESLEEP = 1;
    public static final int POWER_STATUS_NORMAL = 0;
    public static final int POWER_STATUS_SLEEP = 2;
    public static final int POWER_STATUS_UNKNOWN = -1;
    private static final String PROPERTY_POWER_STATUS = "sys.power.xp_power_status";
    private static final String PROPERTY_PRODUCT_MODEL = "ro.product.model";

    public static int getPowerStatus() {
        return SystemProperties.getInt(PROPERTY_POWER_STATUS, -1);
    }

    public static String getProductModel() {
        return SystemProperties.get(PROPERTY_PRODUCT_MODEL, "");
    }

    public static boolean isInternationalVer() {
        boolean internation = true;
        try {
            internation = !getVersionInCountryCode().contains("ZH");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.i("netChannel", "isInternationVersion :\t" + internation);
        return internation;
    }

    public static String getVersionInCountryCode() {
        String versionFinger = SystemProperties.get("ro.xiaopeng.software", "");
        if (!"".equals(versionFinger) && versionFinger != null) {
            String countryCode = versionFinger.substring(7, 9);
            return countryCode;
        }
        return versionFinger;
    }
}

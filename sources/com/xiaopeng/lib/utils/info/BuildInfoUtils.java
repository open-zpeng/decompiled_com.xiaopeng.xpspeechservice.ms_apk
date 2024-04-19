package com.xiaopeng.lib.utils.info;

import android.os.Build;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.xiaopeng.lib.utils.SystemPropertyUtil;
/* loaded from: classes.dex */
public class BuildInfoUtils {
    public static final String BID_LAN = "4";
    public static final String BID_PT_SPECIAL_1 = "5";
    public static final String BID_PT_SPECIAL_2 = "6";
    public static final String BID_WAN = "1";
    public static final String UNKNOWN = "unknown";
    private static String mFullSoftwareVersion = null;

    public static String getSystemVersion() {
        String version;
        int index;
        int end;
        String result = getString("ro.product.firmware");
        if (UNKNOWN.equals(result) && (index = (version = getFullSystemVersion()).indexOf("_")) > 1 && (end = version.indexOf("_", index + 1)) > index) {
            return version.substring(index + 1, end);
        }
        return result;
    }

    public static String getIccid() {
        return SystemPropertyUtil.getIccid();
    }

    public static String getFullSystemVersion() {
        String str = mFullSoftwareVersion;
        if (str != null) {
            return str;
        }
        mFullSoftwareVersion = getString("ro.xiaopeng.software");
        if (UNKNOWN.equals(mFullSoftwareVersion)) {
            mFullSoftwareVersion = getString("ro.build.display.id");
        }
        return mFullSoftwareVersion;
    }

    public static String getHardwareVersion() {
        return getString("ro.xiaopeng.hardware");
    }

    public static int getHardwareVersionCode() {
        String version = getHardwareVersion();
        if (!UNKNOWN.equals(version) && version.length() >= 5) {
            String subVersion = version.substring(3, version.length());
            try {
                return Integer.parseInt(subVersion);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    public static String getHardwareId() {
        return getString("persist.sys.mcu.hardwareId");
    }

    public static String getDeviceId() {
        return "xp/" + getHardwareId();
    }

    public static String getBid() {
        int index;
        String version = getFullSystemVersion();
        return (!TextUtils.isEmpty(version) && (index = version.indexOf("_")) > 1) ? version.substring(index - 1, index) : BID_LAN;
    }

    public static boolean isLanVersion() {
        return BID_LAN.equals(getBid());
    }

    public static boolean isPTSpecialVersion() {
        return BID_PT_SPECIAL_1.equals(getBid()) || BID_PT_SPECIAL_2.equals(getBid());
    }

    public static boolean isEngVersion() {
        return "eng".equals(Build.TYPE);
    }

    public static boolean isUserDebugVersion() {
        return "userdebug".equals(Build.TYPE);
    }

    public static boolean isDebuggableVersion() {
        return isEngVersion() || isUserDebugVersion();
    }

    public static boolean isUserVersion() {
        return "user".equals(Build.TYPE);
    }

    private static String getString(String property) {
        return SystemProperties.get(property, UNKNOWN);
    }

    @VisibleForTesting
    static boolean isDeviceApproved() {
        String deviceInfo = getString("ro.product.manufacturer");
        return "XiaoPeng".equals(deviceInfo) || "XPENG".equals(deviceInfo);
    }

    public static void checkoutDeviceLegality() {
        if (!isDeviceApproved()) {
            System.exit(0);
        }
    }
}

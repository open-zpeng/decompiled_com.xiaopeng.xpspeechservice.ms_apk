package com.xiaopeng.lib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
/* loaded from: classes.dex */
public class NetUtils {
    private static final String GOBINET_PROPERTY_KEY = "persist.sys.ril.gobinet";
    private static final String GOBINET_STATE_FILE = "/sys/class/net/xpusb0/operstate";
    private static final String GOBINET_VALUE_ON = "on";
    public static final int NETWORK_TYPE_2G = 1;
    public static final int NETWORK_TYPE_3G = 2;
    public static final int NETWORK_TYPE_4G = 3;
    public static final int NETWORK_TYPE_NONE = 0;
    public static final int NETWORK_TYPE_WIFI = 10;
    public static final String TRAFFIC_STATUS_CHAGNE_ACTION = "com.xiaopeng.action.TRAFFIC_STATUS_CHANGE";
    public static final int TRAFFIC_STATUS_TYPE_APN_ERROR = 2;
    public static final int TRAFFIC_STATUS_TYPE_AVAILABLE = 3;
    public static final String TRAFFIC_STATUS_TYPE_KEY = "persist.sys.xp.4g.st";
    public static final int TRAFFIC_STATUS_TYPE_RUNOUT = 1;

    public static boolean checkNetState(Context context) {
        NetworkInfo[] info;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivity == null || (info = connectivity.getAllNetworkInfo()) == null) {
            return false;
        }
        for (NetworkInfo networkInfo : info) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWifiEnabled(Context context) {
        if (context == null) {
            throw new NullPointerException("Global context is null");
        }
        WifiManager wifiMgr = (WifiManager) context.getSystemService("wifi");
        if (wifiMgr.getWifiState() == 3) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo wifiInfo = connManager.getNetworkInfo(1);
            if (wifiInfo != null) {
                return wifiInfo.isConnected();
            }
            return false;
        }
        return false;
    }

    public static boolean isMobileNetwork(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info != null && info.getType() == 0) {
            return true;
        }
        return false;
    }

    public static int getNetworkType(Context context) {
        if (context == null) {
            return 0;
        }
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info != null) {
            if (info.getType() == 0) {
                switch (info.getSubtype()) {
                    case 1:
                    case 2:
                    case 4:
                    case 7:
                        return 1;
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 10:
                    case 12:
                    case 14:
                    case 15:
                        return 2;
                    case 13:
                        return 3;
                }
            } else if (info.getType() == 1) {
                return 10;
            }
        }
        return 0;
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivity != null && (info = connectivity.getActiveNetworkInfo()) != null && info.isAvailable() && info.isConnected()) {
            return true;
        }
        return false;
    }

    public static String getMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService("wifi");
        WifiInfo info = wifi.getConnectionInfo();
        if (info == null) {
            return "";
        }
        return info.getMacAddress();
    }

    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        return telephonyManager.getDeviceId();
    }

    public static String getNetworkOperatorName(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        return telephonyManager.getNetworkOperatorName();
    }

    public static int getSimState(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        return telephonyManager.getSimState();
    }

    public static boolean isTrafficRunOut() {
        return getTrafficStatus() == 1;
    }

    public static int getTrafficStatus() {
        return SystemProperties.getInt(TRAFFIC_STATUS_TYPE_KEY, 3);
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x003d, code lost:
        if (com.xiaopeng.lib.utils.info.BuildInfoUtils.UNKNOWN.equals(r3) != false) goto L18;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean isSystemApnReady() {
        /*
            r0 = 1
            java.lang.String r1 = "persist.sys.ril.gobinet"
            java.lang.String r1 = android.os.SystemProperties.get(r1)
            java.lang.String r2 = "on"
            boolean r1 = r2.equals(r1)
            if (r1 != 0) goto L10
            return r0
        L10:
            java.io.File r1 = new java.io.File
            java.lang.String r2 = "/sys/class/net/xpusb0/operstate"
            r1.<init>(r2)
            boolean r2 = r1.exists()
            if (r2 != 0) goto L1e
            return r0
        L1e:
            r0 = 0
            r2 = 0
            java.io.BufferedReader r3 = new java.io.BufferedReader     // Catch: java.lang.Throwable -> L41 java.io.IOException -> L43
            java.io.FileReader r4 = new java.io.FileReader     // Catch: java.lang.Throwable -> L41 java.io.IOException -> L43
            r4.<init>(r1)     // Catch: java.lang.Throwable -> L41 java.io.IOException -> L43
            r3.<init>(r4)     // Catch: java.lang.Throwable -> L41 java.io.IOException -> L43
            r2 = r3
            java.lang.String r3 = r2.readLine()     // Catch: java.lang.Throwable -> L41 java.io.IOException -> L43
            java.lang.String r4 = "up"
            boolean r4 = r4.equals(r3)     // Catch: java.lang.Throwable -> L41 java.io.IOException -> L43
            if (r4 != 0) goto L3f
            java.lang.String r4 = "unknown"
            boolean r4 = r4.equals(r3)     // Catch: java.lang.Throwable -> L41 java.io.IOException -> L43
            if (r4 == 0) goto L47
        L3f:
            r0 = 1
            goto L47
        L41:
            r3 = move-exception
            goto L4c
        L43:
            r3 = move-exception
            r3.printStackTrace()     // Catch: java.lang.Throwable -> L41
        L47:
            com.xiaopeng.lib.utils.FileUtils.closeQuietly(r2)
            return r0
        L4c:
            com.xiaopeng.lib.utils.FileUtils.closeQuietly(r2)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xiaopeng.lib.utils.NetUtils.isSystemApnReady():boolean");
    }
}

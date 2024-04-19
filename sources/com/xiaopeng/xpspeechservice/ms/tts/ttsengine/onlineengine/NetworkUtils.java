package com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.xiaopeng.lib.http.CommonUtils;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.MD5Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
public class NetworkUtils {
    public static boolean isNetworkAvailable(Context context) {
        String info;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
            if (ni != null && ni.isAvailable() && ni.isConnected()) {
                return true;
            }
            if (ni != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("network type = ");
                sb.append(ni.getType());
                sb.append(", ");
                sb.append(ni.isAvailable() ? "available" : "inavailable");
                sb.append(", ");
                sb.append(ni.isConnected() ? "" : "not");
                sb.append(" connected");
                info = sb.toString();
            } else {
                info = "no active network";
            }
            LogUtils.d(info);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getNetworkClass(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return "not connected";
        }
        if (info.getType() == 1) {
            return "WIFI";
        }
        if (info.getType() == 0) {
            int networkType = info.getSubtype();
            if (networkType != 19) {
                switch (networkType) {
                    case 1:
                    case 2:
                    case 4:
                    case 7:
                    case 11:
                        return "2G";
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 10:
                    case 12:
                    case 14:
                    case 15:
                        return "3G";
                    case 13:
                        return "4G";
                    default:
                        return "unknow";
                }
            }
            return "4G";
        }
        return "unknow";
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {
            return true;
        }
        return false;
    }

    public static String sign(Context context, Map<String, String> kV, long time) {
        String k = sortParameterAndValues(kV);
        return MD5Utils.getMD5("xmart:appid:002" + time + k + CommonUtils.CAR_APP_SEC).toLowerCase();
    }

    private static String sortParameterAndValues(Map<String, String> parameterMap) {
        if (parameterMap == null) {
            return "";
        }
        Set<String> keySet = parameterMap.keySet();
        List<String> keyList = new ArrayList<>();
        for (String key : keySet) {
            keyList.add(key);
        }
        Collections.sort(keyList);
        StringBuffer sb = new StringBuffer();
        for (String key2 : keyList) {
            if (!"app_id".equals(key2) && !"timestamp".equals(key2) && !"sign".equals(key2)) {
                sb.append(key2);
                String valArr = parameterMap.get(key2);
                sb.append(valArr);
            }
        }
        return sb.toString();
    }
}

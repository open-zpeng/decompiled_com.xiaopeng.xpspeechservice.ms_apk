package com.xiaopeng.lib.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.util.Base64;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.UploadInfo;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
/* loaded from: classes.dex */
public final class SecurityCommon {
    private static final String TAG = "SecurityCommon";
    private static Boolean sIsSystemUid;

    public static boolean checkSystemUid(@Nullable Context context) {
        if (sIsSystemUid == null) {
            if (context == null) {
                return false;
            }
            try {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 128);
                sIsSystemUid = Boolean.valueOf(ai.uid == 1000);
            } catch (Exception e) {
                LogUtils.w(TAG, "init uid error!", e);
                sIsSystemUid = false;
            }
        }
        return sIsSystemUid.booleanValue();
    }

    public static String getBuildInfoFlag() {
        if (BuildInfoUtils.isLanVersion()) {
            return BuildInfoUtils.BID_WAN;
        }
        return UploadInfo.VERSION;
    }

    public static String base64UrlEncode(byte[] simple) {
        String s = new String(Base64.encode(simple, 11));
        return s.split("=")[0].replace('+', '-').replace('/', '_');
    }

    public static String parseByte2HexStr(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(buf.length * 2);
        for (int i = 0; i < buf.length; i++) {
            result.append("0123456789ABCDEF".charAt((buf[i] >> 4) & 15));
            result.append("0123456789ABCDEF".charAt(buf[i] & 15));
        }
        return result.toString();
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        int len = hexStr.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexStr.substring(i * 2, (i * 2) + 2), 16).byteValue();
        }
        return result;
    }
}

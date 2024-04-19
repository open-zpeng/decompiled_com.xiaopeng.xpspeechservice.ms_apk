package com.xiaopeng.lib.http;

import android.content.Context;
import com.xiaopeng.lib.security.ISecurityModule;
import com.xiaopeng.lib.security.SecurityCommon;
import com.xiaopeng.lib.security.irdeto.IrdetoSecurity;
import com.xiaopeng.lib.utils.LogUtils;
import java.util.Map;
@Deprecated
/* loaded from: classes.dex */
public class IrdetoUtils {
    private static final String TAG = "IrdetoUtils";
    private static boolean sIsInitSuccess;
    public static final String[] TOKEN_ALL = {"accessToken", "refreshToken"};
    public static final String[] TOKEN_AC = {"accessToken"};
    private static ISecurityModule sSecurityUtils = IrdetoSecurity.getInstance();

    /* loaded from: classes.dex */
    public interface ResultListener extends ISecurityModule.ResultListener {
    }

    public static synchronized void init(Context context) {
        synchronized (IrdetoUtils.class) {
            if (sIsInitSuccess) {
                LogUtils.d(TAG, "sSecureSDKManager has init");
                return;
            }
            try {
                sSecurityUtils.init(context);
                sIsInitSuccess = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean isIrdetoInitSuccess() {
        return sSecurityUtils.isInitAndIndivSuccess();
    }

    public static synchronized boolean isIrdetoInitSuccessWithoutIndiv() {
        boolean z;
        synchronized (IrdetoUtils.class) {
            z = sIsInitSuccess;
        }
        return z;
    }

    public static synchronized void destroy() {
        synchronized (IrdetoUtils.class) {
            sSecurityUtils.destroy();
            sIsInitSuccess = false;
        }
    }

    public static String encode(String str) {
        return sSecurityUtils.encode(str);
    }

    public static String decode(String str) {
        return sSecurityUtils.decode(str);
    }

    public static String getString(String key) {
        return sSecurityUtils.getString(key);
    }

    public static void setString(String key, String value) {
        sSecurityUtils.setString(key, value);
    }

    public static void deleteString(String key) {
        sSecurityUtils.deleteString(key);
    }

    public static String buildTokenData(String[] ids, byte[] data) {
        return sSecurityUtils.buildTokenData(ids, data);
    }

    public static String parseByte2HexStr(byte[] buf) {
        return SecurityCommon.parseByte2HexStr(buf);
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        return SecurityCommon.parseHexStr2Byte(hexStr);
    }

    public static String cryptoDecode(String key, String Iv, byte[] data) {
        return sSecurityUtils.cryptoDecode(key, Iv, data);
    }

    public static byte[] cryptoDecodeInByteArray(String key, String Iv, byte[] data) {
        return sSecurityUtils.cryptoDecodeInByteArray(key, Iv, data);
    }

    public static String base64UrlEncode(byte[] simple) {
        return SecurityCommon.base64UrlEncode(simple);
    }

    public static String sign(Context context, Map<String, String> kV, long time) {
        return Security.sign(context, kV, time);
    }

    public static void asyncGetMCUSecurityKey(ICallback<byte[], String> callback) {
        sSecurityUtils.asyncGetMCUSecurityKey(callback);
    }

    public static synchronized void asyncSaveToken(String[] ids, String[] token, Runnable runnable) {
        synchronized (IrdetoUtils.class) {
            sSecurityUtils.asyncSaveToken(ids, token, runnable);
        }
    }

    public static synchronized void asyncSaveTokenWithListener(String[] ids, String[] token, ResultListener listener) {
        synchronized (IrdetoUtils.class) {
            sSecurityUtils.asyncSaveTokenWithListener(ids, token, listener);
        }
    }
}

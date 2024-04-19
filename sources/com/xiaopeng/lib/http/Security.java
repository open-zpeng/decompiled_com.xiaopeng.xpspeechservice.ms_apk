package com.xiaopeng.lib.http;

import android.content.Context;
import android.os.Build;
import com.xiaopeng.lib.security.ISecurityModule;
import com.xiaopeng.lib.security.SecurityCommon;
import com.xiaopeng.lib.security.SecurityModuleFactory;
import com.xiaopeng.lib.security.irdeto.IrdetoSecurity;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.MD5Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
/* loaded from: classes.dex */
public class Security {
    private static final String TAG = "Security";
    private static Context sContext;
    private static ISecurityModule sDepreciateUtils;
    private static boolean sIsInitSuccess;
    public static final String[] TOKEN_ALL = {"accessToken", "refreshToken"};
    public static final String[] TOKEN_AC = {"accessToken"};
    private static boolean sUseDepreciate = false;
    private static ISecurityModule sSecurityUtils = SecurityModuleFactory.getSecurityModule();

    /* loaded from: classes.dex */
    public interface ResultListener extends ISecurityModule.ResultListener {
    }

    static {
        if (Build.VERSION.SDK_INT == 19) {
            sDepreciateUtils = IrdetoSecurity.getInstance();
        }
    }

    /* loaded from: classes.dex */
    public enum EncryptionType {
        IRDETO(1),
        TEE_RANDOM_KEY(2),
        NONE_ENCODING(3);
        
        private int value;

        EncryptionType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static void useIrdeto(boolean use) {
        sUseDepreciate = use;
    }

    public static EncryptionType getActiveEncryptionType() {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            return iSecurityModule.getEncryptionType();
        }
        return sSecurityUtils.getEncryptionType();
    }

    public static synchronized void init(Context context) {
        synchronized (Security.class) {
            if (sIsInitSuccess) {
                LogUtils.d(TAG, "sSecureSDKManager has init");
                return;
            }
            sContext = context;
            sIsInitSuccess = false;
            try {
                sSecurityUtils.init(context);
                sIsInitSuccess = true;
                if (sDepreciateUtils != null) {
                    try {
                        sDepreciateUtils.init(context);
                        if (!sDepreciateUtils.isInitAndIndivSuccess()) {
                            sDepreciateUtils = null;
                        }
                    } catch (Exception e) {
                        sDepreciateUtils = null;
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean isInitSuccess() {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            return iSecurityModule.isInitAndIndivSuccess();
        }
        return sSecurityUtils.isInitAndIndivSuccess();
    }

    public static synchronized boolean isInitSuccessWithoutIndiv() {
        boolean z;
        synchronized (Security.class) {
            z = sIsInitSuccess;
        }
        return z;
    }

    public static synchronized void destroy() {
        synchronized (Security.class) {
            sSecurityUtils.destroy();
            sIsInitSuccess = false;
            if (sDepreciateUtils != null) {
                try {
                    sDepreciateUtils.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String encode(String str) {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            return iSecurityModule.encode(str);
        }
        return sSecurityUtils.encode(str);
    }

    public static String decode(String str) {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            return iSecurityModule.decode(str);
        }
        return sSecurityUtils.decode(str);
    }

    public static String getString(String key) {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            return iSecurityModule.getString(key);
        }
        return sSecurityUtils.getString(key);
    }

    public static void setString(String key, String value) {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            iSecurityModule.setString(key, value);
        } else {
            sSecurityUtils.setString(key, value);
        }
    }

    public static void deleteString(String key) {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            iSecurityModule.deleteString(key);
        } else {
            sSecurityUtils.deleteString(key);
        }
    }

    public static String buildTokenData(String[] ids, byte[] data) {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            return iSecurityModule.buildTokenData(ids, data);
        }
        return sSecurityUtils.buildTokenData(ids, data);
    }

    public static String parseByte2HexStr(byte[] buf) {
        return SecurityCommon.parseByte2HexStr(buf);
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        return SecurityCommon.parseHexStr2Byte(hexStr);
    }

    public static String cryptoDecode(String key, String Iv, byte[] data) {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            return iSecurityModule.cryptoDecode(key, Iv, data);
        }
        return sSecurityUtils.cryptoDecode(key, Iv, data);
    }

    public static byte[] cryptoDecodeInByteArray(String key, String Iv, byte[] data) {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            return iSecurityModule.cryptoDecodeInByteArray(key, Iv, data);
        }
        return sSecurityUtils.cryptoDecodeInByteArray(key, Iv, data);
    }

    public static String base64UrlEncode(byte[] simple) {
        return SecurityCommon.base64UrlEncode(simple);
    }

    public static String sign(Context context, Map<String, String> kV, long time) {
        String k = sortParameterAndValues(kV);
        return MD5Utils.getMD5("xmart:appid:002" + time + k + CommonUtils.CAR_APP_SEC).toLowerCase();
    }

    public static void asyncGetMCUSecurityKey(ICallback<byte[], String> callback) {
        ISecurityModule iSecurityModule;
        if (sUseDepreciate && (iSecurityModule = sDepreciateUtils) != null) {
            iSecurityModule.asyncGetMCUSecurityKey(callback);
        } else {
            sSecurityUtils.asyncGetMCUSecurityKey(callback);
        }
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

    public static synchronized void asyncSaveToken(String[] ids, String[] token, Runnable runnable) {
        synchronized (Security.class) {
            if (sUseDepreciate && sDepreciateUtils != null) {
                sDepreciateUtils.asyncSaveToken(ids, token, runnable);
            } else {
                sSecurityUtils.asyncSaveToken(ids, token, runnable);
            }
        }
    }

    public static synchronized void asyncSaveTokenWithListener(String[] ids, String[] token, ResultListener listener) {
        synchronized (Security.class) {
            if (sUseDepreciate && sDepreciateUtils != null) {
                sDepreciateUtils.asyncSaveTokenWithListener(ids, token, listener);
            } else {
                sSecurityUtils.asyncSaveTokenWithListener(ids, token, listener);
            }
        }
    }
}

package com.xiaopeng.lib.utils.config;

import android.text.TextUtils;
import android.util.Log;
import com.xiaopeng.lib.utils.FileUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
/* loaded from: classes.dex */
public final class EnvConfig {
    static final String KEY_EXPIRED_TIME = "expired_time";
    static final String KEY_MAIN_HOST = "main_host";
    static final String PER_ENV_FILE_PATH = "/sdcard/pre_env.ini";
    private static final String TAG = "EnvConfig";
    private static Properties sConfigs = new Properties();

    static {
        init();
    }

    private EnvConfig() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Properties cloneConfig() {
        Properties prop = new Properties();
        prop.putAll(sConfigs);
        return prop;
    }

    public static boolean hasKey(String key) {
        return sConfigs.containsKey(key);
    }

    public static boolean hasValidConfig() {
        return sConfigs.size() > 0 && hasKey(KEY_MAIN_HOST);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void removeKey(String key) {
        if (!TextUtils.isEmpty(key)) {
            sConfigs.remove(key);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setString(String key, String val) {
        if (val == null) {
            val = "";
        }
        sConfigs.put(key, val);
    }

    public static String getString(String key, String fallback) {
        if (BuildInfoUtils.isDebuggableVersion()) {
            String result = sConfigs.getProperty(key);
            if (!TextUtils.isEmpty(result)) {
                return result;
            }
        }
        return fallback;
    }

    public static String getHostInString(String defLanVerValue, String defUserVerVal) {
        return getString(KEY_MAIN_HOST, defLanVerValue, defUserVerVal);
    }

    public static String getString(String key, String defLanVerValue, String defUserVerVal) {
        if (BuildInfoUtils.isDebuggableVersion()) {
            String result = sConfigs.getProperty(key);
            if (!TextUtils.isEmpty(result)) {
                return result;
            }
        }
        if (BuildInfoUtils.isLanVersion()) {
            return defLanVerValue;
        }
        return defUserVerVal;
    }

    public static int getInt(String key, int fallback) {
        if (!BuildInfoUtils.isDebuggableVersion()) {
            return fallback;
        }
        String val = sConfigs.getProperty(key);
        if (TextUtils.isEmpty(val)) {
            return fallback;
        }
        try {
            int result = Integer.parseInt(val);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return fallback;
        }
    }

    public static int getInt(String key, int defLanVerValue, int defUserVerVal) {
        if (BuildInfoUtils.isDebuggableVersion()) {
            String val = sConfigs.getProperty(key);
            if (!TextUtils.isEmpty(val)) {
                try {
                    int result = Integer.parseInt(val);
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (BuildInfoUtils.isLanVersion()) {
            return defLanVerValue;
        }
        return defUserVerVal;
    }

    public static long getLong(String key, long fallback) {
        if (!BuildInfoUtils.isDebuggableVersion()) {
            return fallback;
        }
        String val = sConfigs.getProperty(key);
        if (TextUtils.isEmpty(val)) {
            return fallback;
        }
        try {
            long result = Long.parseLong(val);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return fallback;
        }
    }

    public static long getLong(String key, long defLanVerValue, long defUserVerVal) {
        if (BuildInfoUtils.isDebuggableVersion()) {
            String val = sConfigs.getProperty(key);
            if (!TextUtils.isEmpty(val)) {
                try {
                    long result = Long.parseLong(val);
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (BuildInfoUtils.isLanVersion()) {
            return defLanVerValue;
        }
        return defUserVerVal;
    }

    public static double getDouble(String key, double fallback) {
        if (!BuildInfoUtils.isDebuggableVersion()) {
            return fallback;
        }
        String val = sConfigs.getProperty(key);
        if (TextUtils.isEmpty(val)) {
            return fallback;
        }
        try {
            double result = Double.parseDouble(val);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return fallback;
        }
    }

    public static double getDouble(String key, double defLanVerValue, double defUserVerVal) {
        if (BuildInfoUtils.isDebuggableVersion()) {
            String val = sConfigs.getProperty(key);
            if (!TextUtils.isEmpty(val)) {
                try {
                    double result = Double.parseDouble(val);
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (BuildInfoUtils.isLanVersion()) {
            return defLanVerValue;
        }
        return defUserVerVal;
    }

    private static boolean strToBoolean(String val) {
        if ("0".equals(val)) {
            return false;
        }
        if (BuildInfoUtils.BID_WAN.equals(val)) {
            return true;
        }
        return Boolean.parseBoolean(val);
    }

    public static boolean getBoolean(String key, boolean fallback) {
        if (BuildInfoUtils.isDebuggableVersion()) {
            String val = sConfigs.getProperty(key);
            if (!TextUtils.isEmpty(val)) {
                return strToBoolean(val);
            }
        }
        return fallback;
    }

    public static boolean getBoolean(String key, boolean defLanVerValue, boolean defUserVerVal) {
        if (BuildInfoUtils.isDebuggableVersion()) {
            String val = sConfigs.getProperty(key);
            if (!TextUtils.isEmpty(val)) {
                return strToBoolean(val);
            }
        }
        if (BuildInfoUtils.isLanVersion()) {
            return defLanVerValue;
        }
        return defUserVerVal;
    }

    private static long convertDateStringToMillis(String dateStr) {
        try {
            String pattern = "yyyyMMdd HH:mm:ss";
            int pos = dateStr.indexOf(":");
            if (pos < 0) {
                int pos2 = dateStr.indexOf(" ");
                if (pos2 < 0) {
                    if (dateStr.length() <= 8) {
                        pattern = "yyyyMMdd";
                    } else {
                        pattern = "yyyyMMddHHmmss";
                    }
                } else {
                    pattern = "yyyyMMdd HHmmss";
                }
            }
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            Date date = formatter.parse(dateStr);
            long expiredTime = date.getTime();
            return expiredTime;
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }

    private static void init() {
        File file;
        if (BuildInfoUtils.isDebuggableVersion()) {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                try {
                    file = new File(PER_ENV_FILE_PATH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (file.exists()) {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    sConfigs.load(bis);
                    Log.w(TAG, "<<<< warning, load file: pre_env.ini !!!");
                    String expired = sConfigs.getProperty(KEY_EXPIRED_TIME, null);
                    if (!TextUtils.isEmpty(expired)) {
                        long expiredTime = convertDateStringToMillis(expired);
                        if (expiredTime > 0 && System.currentTimeMillis() >= expiredTime) {
                            Log.w(TAG, "<<<< file pre_env.ini is expired!");
                            sConfigs.clear();
                        }
                    }
                }
            } finally {
                FileUtils.closeQuietly(bis);
                FileUtils.closeQuietly(fis);
            }
        }
    }
}

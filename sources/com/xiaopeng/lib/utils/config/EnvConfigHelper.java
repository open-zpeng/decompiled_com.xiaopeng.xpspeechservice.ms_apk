package com.xiaopeng.lib.utils.config;

import android.text.TextUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
/* loaded from: classes.dex */
public class EnvConfigHelper {
    static final int DEF_EXPIRE_INTERVAL_MILLIS = 43200000;
    static final String DEF_MAIN_HOST = "https://xmart.deploy-test.xiaopeng.com";
    static final String KEY_MAIN_HOST = "main_host";
    private static final String TAG = "EnvConfig";

    private static String getNextExpireTime() {
        long nextExpiredTime = System.currentTimeMillis() + 43200000;
        Date date = new Date(nextExpiredTime);
        String nextExpireStr = new SimpleDateFormat("yyyyMMdd HHmmss").format(date);
        return nextExpireStr;
    }

    static void saveToPath(Properties prop, String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            File file = new File(path);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            prop.store(bos, "pre env file");
        } finally {
            try {
            } finally {
            }
        }
    }

    public static void saveFile() {
        final Properties prop = EnvConfig.cloneConfig();
        ThreadUtils.post(0, new Runnable() { // from class: com.xiaopeng.lib.utils.config.EnvConfigHelper.1
            @Override // java.lang.Runnable
            public void run() {
                EnvConfigHelper.saveToPath(prop, "/sdcard/pre_env.ini");
            }
        });
    }

    public static void setKey(String key, String value, boolean save) {
        EnvConfig.setString(key, value);
        if (save) {
            saveFile();
        }
    }

    public static void removeKey(String key, boolean save) {
        if (EnvConfig.hasKey(key)) {
            EnvConfig.removeKey(key);
            if (save) {
                saveFile();
            }
        }
    }

    public static void checkAndSetMainHost() {
        EnvConfig.setString(KEY_MAIN_HOST, DEF_MAIN_HOST);
        String expireTime = getNextExpireTime();
        EnvConfig.setString("expired_time", expireTime);
        saveFile();
    }

    public static void removeMainHost() {
        removeKey(KEY_MAIN_HOST, true);
    }
}

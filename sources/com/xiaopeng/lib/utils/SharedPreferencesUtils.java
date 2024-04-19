package com.xiaopeng.lib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import java.util.Map;
/* loaded from: classes.dex */
public class SharedPreferencesUtils {
    private static final String SP_NAME = "shared_pref";
    private static final String TAG = "SharedPreferencesUtils";
    private SharedPreferences.Editor editor;
    private SharedPreferences sp;
    private static volatile SharedPreferencesUtils instance = null;
    private static int initCount = 0;

    private SharedPreferencesUtils(@NonNull Context context, String spName, int mode) {
        spName = TextUtils.isEmpty(spName) ? context.getPackageName() : spName;
        spName = TextUtils.isEmpty(spName) ? SP_NAME : spName;
        Log.v(TAG, "spName=" + spName);
        this.sp = context.getSharedPreferences(spName, 0);
        this.editor = this.sp.edit();
        this.editor.apply();
    }

    private static void init(@NonNull Context context) {
        init(context, null);
    }

    private static void init(@NonNull Context context, String spName) {
        if (instance == null) {
            synchronized (SharedPreferencesUtils.class) {
                if (instance == null) {
                    instance = new SharedPreferencesUtils(context.getApplicationContext(), spName, 0);
                    initCount++;
                }
            }
        }
    }

    public static SharedPreferencesUtils getInstance(@NonNull Context context) {
        init(context);
        return instance;
    }

    @Deprecated
    public static int getInitCount() {
        return initCount;
    }

    public void putString(String key, String value) {
        this.editor.putString(key, value).commit();
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        return this.sp.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        this.editor.putInt(key, value).commit();
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int defaultValue) {
        return this.sp.getInt(key, defaultValue);
    }

    public void putLong(String key, long value) {
        this.editor.putLong(key, value).commit();
    }

    public long getLong(String key) {
        return getLong(key, -1L);
    }

    public long getLong(String key, long defaultValue) {
        return this.sp.getLong(key, defaultValue);
    }

    public void putFloat(String key, float value) {
        this.editor.putFloat(key, value).commit();
    }

    public float getFloat(String key) {
        return getFloat(key, -1.0f);
    }

    public float getFloat(String key, float defaultValue) {
        return this.sp.getFloat(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        this.editor.putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.sp.getBoolean(key, defaultValue);
    }

    public Map<String, ?> getAll() {
        return this.sp.getAll();
    }

    public void remove(String key) {
        this.editor.remove(key).commit();
    }

    public boolean contains(String key) {
        return this.sp.contains(key);
    }

    public void clear() {
        this.editor.clear().commit();
    }
}

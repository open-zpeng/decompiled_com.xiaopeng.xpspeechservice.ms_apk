package com.xiaopeng.datalog.helper;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;
/* loaded from: classes.dex */
public class SharedPreferenceHelper {
    private static final String LOCAL_DATA_FILE = "local_data";
    private static volatile SharedPreferenceHelper mInstance;
    private SharedPreferences.Editor editor;
    private SharedPreferences sp;

    private SharedPreferenceHelper(Context context) {
        this.sp = context.getSharedPreferences(LOCAL_DATA_FILE, 0);
        this.editor = this.sp.edit();
        this.editor.apply();
    }

    public static SharedPreferenceHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SharedPreferenceHelper.class) {
                if (mInstance == null) {
                    mInstance = new SharedPreferenceHelper(context);
                }
            }
        }
        return mInstance;
    }

    public void putString(String key, String value) {
        this.editor.putString(key, value).apply();
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        return this.sp.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        this.editor.putInt(key, value).apply();
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int defaultValue) {
        return this.sp.getInt(key, defaultValue);
    }

    public void putLong(String key, long value) {
        this.editor.putLong(key, value).apply();
    }

    public long getLong(String key) {
        return getLong(key, -1L);
    }

    public long getLong(String key, long defaultValue) {
        return this.sp.getLong(key, defaultValue);
    }

    public void putFloat(String key, float value) {
        this.editor.putFloat(key, value).apply();
    }

    public float getFloat(String key) {
        return getFloat(key, -1.0f);
    }

    public float getFloat(String key, float defaultValue) {
        return this.sp.getFloat(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        this.editor.putBoolean(key, value).apply();
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
        this.editor.remove(key).apply();
    }

    public boolean contains(String key) {
        return this.sp.contains(key);
    }

    public void clear() {
        this.editor.clear().apply();
    }
}

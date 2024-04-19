package com.xiaopeng.lib.utils.view;

import android.util.SparseLongArray;
/* loaded from: classes.dex */
public class ShakeUtil {
    private static SparseLongArray mHashMap = new SparseLongArray();

    public static synchronized boolean canExecute(int id) {
        boolean flag;
        synchronized (ShakeUtil.class) {
            long mill = System.currentTimeMillis();
            long oldMill = mHashMap.get(id);
            if (Math.abs(mill - oldMill) > 300) {
                flag = true;
                mHashMap.put(id, mill);
            } else {
                flag = false;
            }
        }
        return flag;
    }

    public static synchronized boolean canExecuteLong(int id) {
        boolean flag;
        synchronized (ShakeUtil.class) {
            long mill = System.currentTimeMillis();
            long oldMill = mHashMap.get(id);
            if (Math.abs(mill - oldMill) > 1000) {
                flag = true;
                mHashMap.put(id, mill);
            } else {
                flag = false;
            }
        }
        return flag;
    }

    public static synchronized boolean canExecuteLong(int id, int duration) {
        boolean flag;
        synchronized (ShakeUtil.class) {
            long mill = System.currentTimeMillis();
            long oldMill = mHashMap.get(id);
            if (Math.abs(mill - oldMill) > duration) {
                flag = true;
                mHashMap.put(id, mill);
            } else {
                flag = false;
            }
        }
        return flag;
    }
}

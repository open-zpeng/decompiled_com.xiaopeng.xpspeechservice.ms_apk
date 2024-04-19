package com.ta.utdid2.b.a;

import java.lang.reflect.Method;
/* compiled from: DebugUtils.java */
/* loaded from: classes.dex */
public class d {
    private static Class<?> a;

    /* renamed from: a  reason: collision with other field name */
    private static Method f138a;
    private static Method b;
    public static boolean e;

    public static int getInt(String key, int def) {
        a();
        try {
            return ((Integer) b.invoke(a, key, Integer.valueOf(def))).intValue();
        } catch (Exception e2) {
            e2.printStackTrace();
            return def;
        }
    }

    static {
        e = getInt("alidebug", 0) == 1;
        a = null;
        f138a = null;
        b = null;
    }

    private static void a() {
        try {
            if (a == null) {
                a = Class.forName("android.os.SystemProperties");
                f138a = a.getDeclaredMethod("get", String.class);
                b = a.getDeclaredMethod("getInt", String.class, Integer.TYPE);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}

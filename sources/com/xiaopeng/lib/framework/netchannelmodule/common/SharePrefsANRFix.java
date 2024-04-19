package com.xiaopeng.lib.framework.netchannelmodule.common;

import android.text.TextUtils;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
/* loaded from: classes.dex */
public class SharePrefsANRFix {
    private static volatile boolean hooked = false;

    public static void fix() {
        if (hooked) {
            return;
        }
        hooked = true;
        try {
            Class clazz = Class.forName("android.app.QueuedWork");
            Field lowFinisher = clazz.getDeclaredField("sPendingWorkFinishers");
            if (lowFinisher != null) {
                lowFinisher.setAccessible(true);
                Object obj = lowFinisher.get(null);
                if (obj instanceof ConcurrentLinkedQueue) {
                    ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.common.SharePrefsANRFix.1
                        @Override // java.util.concurrent.ConcurrentLinkedQueue, java.util.AbstractQueue, java.util.AbstractCollection, java.util.Collection, java.util.Queue
                        public boolean add(Runnable runnable) {
                            String name = runnable.getClass().getName();
                            if (!TextUtils.isEmpty(name) && name.contains("android.app.SharedPreferencesImpl$EditorImpl")) {
                                return false;
                            }
                            return super.add((AnonymousClass1) runnable);
                        }
                    };
                    queue.addAll((ConcurrentLinkedQueue) obj);
                    lowFinisher.set(null, queue);
                }
            }
        } catch (Exception e) {
        }
        try {
            Class clazz2 = Class.forName("android.app.QueuedWork");
            Field highFinisher = clazz2.getDeclaredField("sFinishers");
            if (highFinisher != null) {
                highFinisher.setAccessible(true);
                Object obj2 = highFinisher.get(null);
                if (obj2 instanceof LinkedList) {
                    LinkedList<Runnable> queue2 = new LinkedList<Runnable>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.common.SharePrefsANRFix.2
                        @Override // java.util.LinkedList, java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List, java.util.Deque, java.util.Queue
                        public boolean add(Runnable runnable) {
                            String name = runnable.getClass().getName();
                            if (!TextUtils.isEmpty(name) && name.contains("android.app.SharedPreferencesImpl$EditorImpl")) {
                                return false;
                            }
                            return super.add((AnonymousClass2) runnable);
                        }
                    };
                    queue2.addAll((LinkedList) obj2);
                    highFinisher.set(null, queue2);
                }
            }
        } catch (Exception e2) {
        }
    }
}

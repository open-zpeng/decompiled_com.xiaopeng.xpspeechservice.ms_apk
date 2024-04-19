package com.xiaopeng.lib.framework.module;

import java.util.concurrent.ConcurrentHashMap;
/* loaded from: classes.dex */
public class Module {
    private static ConcurrentHashMap<Class, IModuleEntry> sModuleMap = new ConcurrentHashMap<>();

    public static void register(Class moduleClass, IModuleEntry moduleEntry) {
        if (moduleClass == null || moduleEntry == null || sModuleMap.containsKey(moduleClass)) {
            return;
        }
        sModuleMap.put(moduleClass, moduleEntry);
    }

    public static IModuleEntry get(Class moduleClass) {
        return sModuleMap.get(moduleClass);
    }
}

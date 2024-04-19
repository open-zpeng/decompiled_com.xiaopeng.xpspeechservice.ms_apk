package com.xiaopeng.lib.framework.moduleinterface.datalogmodule;
/* loaded from: classes.dex */
public interface ICounter {
    int count(String key);

    int count(String key, int value);

    void debug(boolean debug);
}

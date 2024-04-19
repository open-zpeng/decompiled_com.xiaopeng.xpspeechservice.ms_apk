package com.xiaopeng.lib.framework.moduleinterface.syncmodule;
/* loaded from: classes.dex */
public class SyncData {
    public String key;
    public String value;

    public SyncData() {
    }

    public SyncData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String toString() {
        return "SyncData { key:" + this.key + "; value:" + this.value + "; }";
    }
}

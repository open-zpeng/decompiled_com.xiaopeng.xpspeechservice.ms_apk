package com.ut.mini.plugin;
/* loaded from: classes.dex */
public abstract class UTPluginMsgDispatchDelegate {
    private Object g;

    public final Object getMsgObj() {
        return this.g;
    }

    public UTPluginMsgDispatchDelegate(Object aMsgObject) {
        this.g = null;
        this.g = aMsgObject;
    }

    public boolean isMatchPlugin(UTPlugin lPlugin) {
        return true;
    }

    public Object getDispatchObject(UTPlugin lPlugin) {
        return this.g;
    }
}

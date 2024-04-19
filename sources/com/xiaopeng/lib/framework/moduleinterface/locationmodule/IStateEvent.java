package com.xiaopeng.lib.framework.moduleinterface.locationmodule;
/* loaded from: classes.dex */
public interface IStateEvent {

    /* loaded from: classes.dex */
    public enum TYPE {
        BOUND,
        UNBOUND
    }

    TYPE type();
}

package com.xiaopeng.lib.framework.moduleinterface.datalogmodule;
/* loaded from: classes.dex */
public interface IStatEventBuilder {
    IStatEvent build();

    IStatEventBuilder setEventName(String name);

    IStatEventBuilder setProperty(String key, char value);

    IStatEventBuilder setProperty(String key, Number value);

    IStatEventBuilder setProperty(String key, String value);

    IStatEventBuilder setProperty(String key, boolean value);
}

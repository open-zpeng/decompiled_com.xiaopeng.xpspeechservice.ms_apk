package com.xiaopeng.lib.framework.moduleinterface.datalogmodule;
/* loaded from: classes.dex */
public interface IStatEvent {
    public static final String CUSTOM_DEVICE_MCUVER = "_mcuver";
    public static final String CUSTOM_EVENT = "_event";
    public static final String CUSTOM_MODULE = "_module";
    public static final String CUSTOM_MODULE_VERSION = "_module_version";
    public static final String CUSTOM_NETWORK = "_network";
    public static final String CUSTOM_STARTUP = "_st_time";
    public static final String CUSTOM_TIMESTAMP = "_time";
    public static final String CUSTOM_UID = "_uid";

    String getEventName();

    void put(String key, Boolean value);

    void put(String key, Character value);

    void put(String key, Number value);

    void put(String key, String value);

    void setEventName(String eventName);

    String toJson();
}

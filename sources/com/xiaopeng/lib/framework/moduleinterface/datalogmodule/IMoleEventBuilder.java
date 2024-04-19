package com.xiaopeng.lib.framework.moduleinterface.datalogmodule;
/* loaded from: classes.dex */
public interface IMoleEventBuilder {
    IMoleEvent build();

    IMoleEventBuilder setButtonId(String buttonId);

    IMoleEventBuilder setEvent(String event);

    IMoleEventBuilder setModule(String module);

    IMoleEventBuilder setPageId(String pageId);

    IMoleEventBuilder setProperty(String key, char value);

    IMoleEventBuilder setProperty(String key, Number value);

    IMoleEventBuilder setProperty(String key, String value);

    IMoleEventBuilder setProperty(String key, boolean value);
}

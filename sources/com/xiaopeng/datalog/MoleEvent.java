package com.xiaopeng.datalog;

import android.content.Context;
import android.os.SystemClock;
import com.google.gson.reflect.TypeToken;
import com.xiaopeng.datalog.helper.GlobalGsonInstance;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEvent;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent;
import com.xiaopeng.lib.utils.SystemPropertyUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/* loaded from: classes.dex */
public class MoleEvent implements IMoleEvent {
    public static final String KEY_BUTTON_ID = "button_id";
    public static final String KEY_PAGE_ID = "page_id";
    private Map<String, Object> properties = new ConcurrentHashMap();

    /* JADX INFO: Access modifiers changed from: package-private */
    public MoleEvent(Context context) {
        this.properties.put(IStatEvent.CUSTOM_TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
        this.properties.put(IStatEvent.CUSTOM_MODULE_VERSION, StatEvent.getVersion(context));
        this.properties.put(IStatEvent.CUSTOM_NETWORK, StatEvent.getNetworkType(context));
        this.properties.put(IStatEvent.CUSTOM_STARTUP, Long.valueOf(SystemClock.uptimeMillis() / 1000));
        this.properties.put(IStatEvent.CUSTOM_DEVICE_MCUVER, StatEvent.getMCUVer());
        this.properties.put(IStatEvent.CUSTOM_UID, Long.valueOf(SystemPropertyUtil.getAccountUid()));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void put(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        this.properties.put(key, value);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void put(String key, Number value) {
        if (key == null || value == null) {
            return;
        }
        this.properties.put(key, value);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void put(String key, Boolean value) {
        if (key == null || value == null) {
            return;
        }
        this.properties.put(key, value);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void put(String key, Character value) {
        if (key == null || value == null) {
            return;
        }
        this.properties.put(key, value);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEvent
    public String toJson() {
        return GlobalGsonInstance.getInstance().getGson().toJson(this.properties, new TypeToken<Map<String, Object>>() { // from class: com.xiaopeng.datalog.MoleEvent.1
        }.getType());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void checkValid() {
        if (!this.properties.containsKey(KEY_PAGE_ID)) {
            throw new IllegalArgumentException("Please call setPageId() first");
        }
        if (!this.properties.containsKey(KEY_BUTTON_ID)) {
            throw new IllegalArgumentException("Please call setButtonId() first");
        }
        if (!this.properties.containsKey(IStatEvent.CUSTOM_MODULE)) {
            throw new IllegalArgumentException("Please call setModule() first");
        }
    }
}

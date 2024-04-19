package com.xiaopeng.datalog;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.reflect.TypeToken;
import com.xiaopeng.datalog.helper.GlobalGsonInstance;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent;
import com.xiaopeng.lib.utils.SystemPropertyUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/* loaded from: classes.dex */
public class StatEvent implements IStatEvent {
    private static final String TAG = "StatEvent";
    private static String sMcuVersion;
    private static String sVersion;
    private String eventName;
    private Map<String, Object> properties = new ConcurrentHashMap();

    /* JADX INFO: Access modifiers changed from: package-private */
    public StatEvent(Context context) {
        this.properties.put(IStatEvent.CUSTOM_TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
        this.properties.put(IStatEvent.CUSTOM_MODULE_VERSION, getVersion(context));
        this.properties.put(IStatEvent.CUSTOM_NETWORK, getNetworkType(context));
        this.properties.put(IStatEvent.CUSTOM_STARTUP, Long.valueOf(SystemClock.uptimeMillis() / 1000));
        this.properties.put(IStatEvent.CUSTOM_DEVICE_MCUVER, getMCUVer());
        this.properties.put(IStatEvent.CUSTOM_UID, Long.valueOf(SystemPropertyUtil.getAccountUid()));
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent
    public String getEventName() {
        return this.eventName;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent
    public String toJson() {
        this.properties.put(IStatEvent.CUSTOM_MODULE, getModuleNameFromEvent(this.eventName));
        this.properties.put(IStatEvent.CUSTOM_EVENT, this.eventName);
        return GlobalGsonInstance.getInstance().getGson().toJson(this.properties, new TypeToken<Map<String, Object>>() { // from class: com.xiaopeng.datalog.StatEvent.1
        }.getType());
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent
    public void put(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        this.properties.put(key, value);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent
    public void put(String key, Number value) {
        if (key == null || value == null) {
            return;
        }
        this.properties.put(key, value);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent
    public void put(String key, Boolean value) {
        if (key == null || value == null) {
            return;
        }
        this.properties.put(key, value);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent
    public void put(String key, Character value) {
        if (key == null || value == null) {
            return;
        }
        this.properties.put(key, value);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getModuleNameFromEvent(String eventId) {
        String[] list;
        return (TextUtils.isEmpty(eventId) || (list = eventId.split("_")) == null || list.length <= 0) ? "" : list[0];
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getVersion(Context context) {
        if (TextUtils.isEmpty(sVersion)) {
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                sVersion = pi.versionName;
            } catch (Exception e) {
                Log.w(TAG, "getVersion fail!", e);
                return "Unknown";
            }
        }
        return sVersion;
    }

    public String toString() {
        return "StatEvent{eventName='" + this.eventName + "'}";
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getMCUVer() {
        if (TextUtils.isEmpty(sMcuVersion)) {
            sMcuVersion = SystemProperties.get("persist.sys.mcu.version");
        }
        if (TextUtils.isEmpty(sMcuVersion)) {
            sMcuVersion = SystemProperties.get("sys.mcu.version");
        }
        return sMcuVersion;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getNetworkType(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info != null) {
            if (info.getType() == 0) {
                switch (info.getSubtype()) {
                    case 1:
                    case 2:
                    case 4:
                    case 7:
                        return "2G";
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 10:
                    case 12:
                    case 14:
                    case 15:
                        return "3G";
                    case 11:
                    default:
                        return "";
                    case 13:
                        return "4G";
                }
            } else if (info.getType() == 1) {
                return "WIFI";
            } else {
                return "";
            }
        }
        return "";
    }
}

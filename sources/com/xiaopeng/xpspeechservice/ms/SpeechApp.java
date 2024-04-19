package com.xiaopeng.xpspeechservice.ms;

import android.app.Application;
import android.content.Context;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.configuration.ConfigurationModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp;
import com.xiaopeng.lib.framework.netchannelmodule.NetworkChannelsEntry;
import com.xiaopeng.lib.http.HttpsUtils;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;
import org.litepal.exceptions.InvalidAttributesException;
/* loaded from: classes.dex */
public class SpeechApp extends Application {
    private static final String TAG = "SpeechApp";
    private static SpeechApp sApp;

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        sApp = this;
        init();
    }

    public static Context getContext() {
        return sApp.getApplicationContext();
    }

    public static Application getApplication() {
        return sApp;
    }

    private void init() {
        LogUtils.v(TAG, "init +++");
        removeEventBusLog();
        initDataBase();
        registerModule();
        initNetwork();
        LogUtils.v(TAG, "init ---");
    }

    private void initDataBase() {
        LitePal.initialize(this);
        try {
            LitePal.getDatabase();
        } catch (Exception e) {
            if (e.toString().contains("Can't downgrade database from version") || e.toString().contains(InvalidAttributesException.VERSION_IS_EARLIER_THAN_CURRENT)) {
                boolean isDeleted = LitePal.deleteDatabase("xpTtsCache");
                LogUtils.e(TAG, "delete is: " + isDeleted);
            }
        }
    }

    private void registerModule() {
        Module.register(ConfigurationModuleEntry.class, new ConfigurationModuleEntry());
        Module.register(DataLogModuleEntry.class, new DataLogModuleEntry(this));
        Module.register(NetworkChannelsEntry.class, new NetworkChannelsEntry());
    }

    private void removeEventBusLog() {
        try {
            EventBus.builder().sendNoSubscriberEvent(false).logNoSubscriberMessages(false).logSubscriberExceptions(false).installDefaultEventBus();
        } catch (Exception e) {
            LogUtils.e("event bus init error", e);
        }
    }

    private void initNetwork() {
        HttpsUtils.init(this, true);
        IHttp http = (IHttp) Module.get(NetworkChannelsEntry.class).get(IHttp.class);
        http.config().applicationContext(this).enableTrafficStats().apply();
    }
}

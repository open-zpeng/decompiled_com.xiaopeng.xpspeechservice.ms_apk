package com.xiaopeng.datalog.stat;

import android.content.Context;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEvent;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent;
import java.util.List;
/* loaded from: classes.dex */
public class StatEventHelper {
    private static final String TAG = "StatEventHelper";
    private static volatile StatEventHelper sInstance;
    private AbstractStatDelegate mStatDelegate;

    private StatEventHelper(Context sApplicationContext) {
        this.mStatDelegate = new StatMqttDelegate(sApplicationContext);
    }

    public static void init(Context context) {
        sInstance = new StatEventHelper(context);
    }

    public static StatEventHelper getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("has not init the StatEventHeler");
        }
        return sInstance;
    }

    public void uploadCdu(IStatEvent event) {
        this.mStatDelegate.uploadCdu(event);
    }

    public void uploadCdu(IMoleEvent event) {
        this.mStatDelegate.uploadCdu(event);
    }

    public void uploadCan(String data) {
        this.mStatDelegate.uploadCan(data);
    }

    public void uploadLogImmediately(String eventName, String data) {
        this.mStatDelegate.uploadLogImmediately(eventName, data);
    }

    public void uploadCduWithFiles(IStatEvent event, List<String> filePaths) {
        this.mStatDelegate.uploadCduWithFiles(event, filePaths);
    }

    public void uploadFiles(List<String> filePaths) {
        this.mStatDelegate.uploadFiles(filePaths);
    }

    public String uploadRecentSystemLog() {
        return this.mStatDelegate.uploadRecentSystemLog();
    }

    public void uploadLogOrigin(String eventName, String data) {
        this.mStatDelegate.uploadLogOrigin(eventName, data);
    }
}

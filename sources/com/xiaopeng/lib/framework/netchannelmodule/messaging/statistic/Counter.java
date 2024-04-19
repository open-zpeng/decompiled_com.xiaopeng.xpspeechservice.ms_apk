package com.xiaopeng.lib.framework.netchannelmodule.messaging.statistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.framework.netchannelmodule.common.BackgroundHandler;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.info.DeviceInfoUtils;
import java.util.concurrent.atomic.AtomicLong;
/* loaded from: classes.dex */
public class Counter {
    private static final boolean COMMIT_ACCORDING_TO_DATE = false;
    private static final boolean DEBUG = GlobalConfig.debuggable();
    private static final String EVENT_NAME = "net_mqtt_stat";
    private static final String KEY_INIT_FAILED = "net_mqtt_init_failed";
    private static final String KEY_LAST_DATE = "net_mqtt_date";
    private static final String KEY_PUBLISHED = "net_mqtt_pub";
    private static final String KEY_PUBLISHED_SIZE = "net_mqtt_pub_size";
    private static final String KEY_PUBLISH_FAILED = "net_mqtt_pub_failed";
    private static final String KEY_RECEIVED = "net_mqtt_recv";
    private static final String KEY_TRY_PUBLISH = "net_mqtt_try_pub";
    private static final long MAX_VALUE_THRESHOLD = 4611686018427387903L;
    private static final long MESSAGE_APPLY_DELAY = 10000;
    private static final long MIN_COMMIT_COUNT = 10;
    private static final String MOLE_TRAFFIC_MQTT_BUTTON_ID = "B003";
    private static final long ONE_DAY_MILLS = 86400000;
    private static final String STAT_KEY_INIT_FAILED = "init_fail";
    private static final String STAT_KEY_LAST_DATE = "date";
    private static final String STAT_KEY_NETWORK_TYPE = "net";
    private static final String STAT_KEY_PACKAGE_NAME = "pack";
    private static final String STAT_KEY_PUBLISHED = "pub";
    private static final String STAT_KEY_PUBLISHED_SIZE_KB = "pub_size";
    private static final String STAT_KEY_PUBLISH_FAILED = "fail";
    private static final String STAT_KEY_RECEIVED = "recv";
    private static final String STAT_KEY_TRY_PUBLISH = "try";
    private static final String TAG = "NetChannel-Counter";
    private static final long UNPUBLISHED_COUNT_THRESHOLD = 128;
    private final Runnable mApplyRunnable = new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.statistic.Counter.1
        @Override // java.lang.Runnable
        public void run() {
            Counter.this.tryToCommitData(false);
            Counter.this.mEditor.apply();
        }
    };
    private final SharedPreferences.Editor mEditor;
    private final BackgroundHandler mHandler;
    private final AtomicLong mInitFailedCount;
    private final AtomicLong mLastDate;
    private final boolean mNeedToPost;
    private final AtomicLong mPublishFailedCount;
    private final AtomicLong mPublishedCount;
    private final AtomicLong mPublishedSize;
    private final AtomicLong mReceivedCount;
    private final AtomicLong mTryToPublishCount;

    public Counter(Context context, BackgroundHandler handler, boolean needToPost) {
        this.mNeedToPost = needToPost;
        this.mHandler = handler;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mEditor = preferences.edit();
        long currentDate = System.currentTimeMillis() / ONE_DAY_MILLS;
        this.mLastDate = new AtomicLong(preferences.getLong(KEY_LAST_DATE, currentDate));
        this.mTryToPublishCount = new AtomicLong(0L);
        this.mPublishFailedCount = new AtomicLong(0L);
        this.mPublishedCount = new AtomicLong(0L);
        this.mReceivedCount = new AtomicLong(0L);
        this.mInitFailedCount = new AtomicLong(0L);
        this.mPublishedSize = new AtomicLong(0L);
        try {
            this.mTryToPublishCount.set(preferences.getLong(KEY_TRY_PUBLISH, 0L));
            this.mPublishFailedCount.set(preferences.getLong(KEY_PUBLISH_FAILED, 0L));
            this.mPublishedCount.set(preferences.getLong(KEY_PUBLISHED, 0L));
            this.mReceivedCount.set(preferences.getLong(KEY_RECEIVED, 0L));
            this.mInitFailedCount.set(preferences.getLong(KEY_INIT_FAILED, 0L));
            this.mPublishedSize.set(preferences.getLong(KEY_PUBLISHED_SIZE, 0L));
        } catch (Exception e) {
        }
        debugInfo();
    }

    public void increaseTryingToPublish() {
        long count = this.mTryToPublishCount.incrementAndGet();
        long sent = this.mPublishedCount.get();
        long unpublished = count - sent;
        if (unpublished >= UNPUBLISHED_COUNT_THRESHOLD && ((unpublished >> 6) << 6) == unpublished) {
            MqttChannel.getInstance().disconnectedDueToException(new Throwable("un publish too large"));
        }
        this.mEditor.putLong(KEY_TRY_PUBLISH, count);
        if (((count >> 2) << 2) == count) {
            apply();
        }
        debugInfo();
    }

    public void increasePublishedCountWithSize(long size) {
        long result = this.mPublishedCount.incrementAndGet();
        this.mEditor.putLong(KEY_PUBLISHED, result);
        this.mEditor.putLong(KEY_PUBLISHED_SIZE, this.mPublishedSize.addAndGet(size));
        debugInfo();
    }

    public void increaseReceivedCount() {
        long count = this.mReceivedCount.incrementAndGet();
        this.mEditor.putLong(KEY_RECEIVED, count);
        if (((count >> 2) << 2) == count) {
            apply();
        }
        debugInfo();
    }

    public void increasePublishFailureCount() {
        long result = this.mPublishFailedCount.incrementAndGet();
        this.mEditor.putLong(KEY_PUBLISH_FAILED, result);
        debugInfo();
    }

    public void increaseInitFailedCount() {
        long result = this.mInitFailedCount.incrementAndGet();
        this.mEditor.putLong(KEY_INIT_FAILED, result);
        debugInfo();
    }

    public void commitForcibly() {
        if (!this.mHandler.hasCallbacks(this.mApplyRunnable)) {
            this.mHandler.postDelayed(this.mApplyRunnable, MESSAGE_APPLY_DELAY);
        }
    }

    private void apply() {
        if (!this.mHandler.hasCallbacks(this.mApplyRunnable)) {
            this.mHandler.postDelayed(this.mApplyRunnable, MESSAGE_APPLY_DELAY);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void tryToCommitData(boolean forcibly) {
        long currentDate = System.currentTimeMillis() / ONE_DAY_MILLS;
        long tryToPublish = this.mTryToPublishCount.get();
        long received = this.mReceivedCount.get();
        long size = this.mPublishedSize.get();
        if (forcibly || size >= MAX_VALUE_THRESHOLD || this.mLastDate.get() < currentDate) {
            long success = this.mPublishedCount.get();
            long fail = this.mPublishFailedCount.get();
            long initFail = this.mInitFailedCount.get();
            long lastDate = this.mLastDate.get();
            clearCounters();
            this.mLastDate.set(currentDate);
            this.mEditor.putLong(KEY_LAST_DATE, this.mLastDate.get());
            if (this.mNeedToPost && tryToPublish + received > MIN_COMMIT_COUNT) {
                boolean international = DeviceInfoUtils.isInternationalVer();
                if (!international) {
                    IDataLog dataLogService = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
                    dataLogService.sendStatData(dataLogService.buildMoleEvent().setEvent(EVENT_NAME).setPageId(GlobalConfig.MOLE_PAGE_ID).setButtonId(MOLE_TRAFFIC_MQTT_BUTTON_ID).setProperty(STAT_KEY_PACKAGE_NAME, GlobalConfig.getApplicationSimpleName()).setProperty(STAT_KEY_TRY_PUBLISH, Long.valueOf(tryToPublish)).setProperty(STAT_KEY_PUBLISH_FAILED, Long.valueOf(fail)).setProperty(STAT_KEY_PUBLISHED, Long.valueOf(success)).setProperty(STAT_KEY_PUBLISHED_SIZE_KB, Long.valueOf(size >> MIN_COMMIT_COUNT)).setProperty(STAT_KEY_RECEIVED, Long.valueOf(received)).setProperty(STAT_KEY_INIT_FAILED, Long.valueOf(initFail)).setProperty("date", Long.valueOf(lastDate)).setProperty(STAT_KEY_NETWORK_TYPE, Integer.valueOf(GlobalConfig.getNetworkType())).build());
                }
            }
        }
    }

    @VisibleForTesting
    public void clearCounters() {
        this.mTryToPublishCount.set(0L);
        this.mEditor.putLong(KEY_TRY_PUBLISH, 0L);
        this.mPublishedCount.set(0L);
        this.mEditor.putLong(KEY_PUBLISHED, 0L);
        this.mPublishFailedCount.set(0L);
        this.mEditor.putLong(KEY_PUBLISH_FAILED, 0L);
        this.mPublishedSize.set(0L);
        this.mEditor.putLong(KEY_PUBLISHED_SIZE, 0L);
        this.mReceivedCount.set(0L);
        this.mEditor.putLong(KEY_RECEIVED, 0L);
        this.mInitFailedCount.set(0L);
        this.mEditor.putLong(KEY_INIT_FAILED, 0L).apply();
    }

    private void debugInfo() {
        if (DEBUG) {
            String message = "[MQTTSTATUS] try to pub:" + this.mTryToPublishCount.get() + " published:" + this.mPublishedCount.get() + " publish fail:" + this.mPublishFailedCount.get() + " received:" + this.mReceivedCount.get() + " init fail:" + this.mInitFailedCount.get() + " published size(KB):" + (this.mPublishedSize.get() >> MIN_COMMIT_COUNT) + " package: " + GlobalConfig.getApplicationSimpleName() + " net:" + GlobalConfig.getNetworkType();
            LogUtils.v(TAG, message);
        }
    }
}

package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.lib.utils.info.DeviceInfoUtils;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
/* loaded from: classes.dex */
public final class StorageCounter implements Handler.Callback {
    private static final long DAILY_TRAFFIC_QUOTA = 307200;
    private static final boolean DEBUG = GlobalConfig.debuggable();
    private static final String EVENT_NAME = "net_rs_stat";
    private static final String KEY_ERROR_CODES = "net_rs_codes";
    private static final String KEY_FAILED = "net_rs_failed";
    private static final String KEY_LAST_DATE = "net_rs_date";
    private static final String KEY_REJECT = "net_rs_rej";
    private static final String KEY_REQUEST = "net_rs_request";
    private static final String KEY_SUCCEED = "net_rs_succeed";
    private static final String KEY_TRAFFIC_SIZE = "net_rs_size";
    private static final short MAX_ERROR_CODES = 10;
    private static final long MAX_VALUE_THRESHOLD = 4611686018427387903L;
    private static final int MESSAGE_APPLY = 1;
    private static final long MESSAGE_APPLY_DELAY = 10000;
    private static final String MOLE_TRAFFIC_OSS_BUTTON_ID = "B005";
    private static final long ONE_DAY_MILLS = 86400000;
    private static final String STAT_KEY_ERROR_CODES = "codes";
    private static final String STAT_KEY_FAILED = "fail";
    private static final String STAT_KEY_LAST_DATE = "date";
    private static final String STAT_KEY_PACKAGE_NAME = "pack";
    private static final String STAT_KEY_REJECT = "rej";
    private static final String STAT_KEY_REQUEST = "req";
    private static final String STAT_KEY_SUCCEED = "suc";
    private static final String STAT_KEY_TRAFFIC_SIZE_IN_KB = "size";
    private static final String TAG = "NetChannel.RemoteStorage.Counter";
    private SharedPreferences.Editor mEditor;
    private HashMap<String, Long> mErrorCodes;
    private volatile boolean mExceedTrafficQuota;
    private AtomicLong mFailedCount;
    private final Handler mHandler;
    private Gson mJsonConverter;
    private AtomicLong mLastDate;
    private AtomicInteger mRejectCount;
    private AtomicLong mRequestCount;
    private AtomicLong mSucceedCount;
    private AtomicLong mTrafficSize;

    private StorageCounter() {
        this.mExceedTrafficQuota = false;
        this.mHandler = new Handler(ThreadUtils.getLooper(0), this);
    }

    public static StorageCounter getInstance() {
        return Holder.INSTANCE;
    }

    public void init(@NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mEditor = preferences.edit();
        long currentDate = System.currentTimeMillis() / ONE_DAY_MILLS;
        this.mLastDate = new AtomicLong(preferences.getLong(KEY_LAST_DATE, currentDate));
        this.mRequestCount = new AtomicLong(0L);
        this.mFailedCount = new AtomicLong(0L);
        this.mSucceedCount = new AtomicLong(0L);
        this.mTrafficSize = new AtomicLong(0L);
        this.mRejectCount = new AtomicInteger(0);
        this.mJsonConverter = new Gson();
        try {
            this.mRequestCount.set(preferences.getLong(KEY_REQUEST, 0L));
            this.mSucceedCount.set(preferences.getLong(KEY_SUCCEED, 0L));
            this.mFailedCount.set(preferences.getLong(KEY_FAILED, 0L));
            this.mTrafficSize.set(preferences.getLong(KEY_TRAFFIC_SIZE, 0L));
            this.mRejectCount.set(preferences.getInt(KEY_REJECT, 0));
            this.mErrorCodes = initFromPreference(preferences);
        } catch (Exception e) {
        }
        debugInfo();
    }

    public void increaseRequestCount() {
        ensureInitialized();
        long count = this.mRequestCount.incrementAndGet();
        this.mEditor.putLong(KEY_REQUEST, count);
    }

    public void increaseFailureWithCode(String code, long size) {
        long count = this.mFailedCount.incrementAndGet();
        this.mEditor.putLong(KEY_FAILED, count);
        this.mExceedTrafficQuota = this.mTrafficSize.addAndGet(size >> 10) > DAILY_TRAFFIC_QUOTA;
        updateErrorCodes(code);
        apply();
        debugInfo();
    }

    public void increaseSucceedWithSize(long size) {
        long count = this.mSucceedCount.incrementAndGet();
        long size2 = this.mTrafficSize.addAndGet(size >> 10);
        this.mExceedTrafficQuota = size2 > DAILY_TRAFFIC_QUOTA;
        this.mEditor.putLong(KEY_SUCCEED, count);
        this.mEditor.putLong(KEY_TRAFFIC_SIZE, size2);
        apply();
        debugInfo();
    }

    public void increaseRejectCount() {
        int count = this.mRejectCount.incrementAndGet();
        this.mEditor.putInt(KEY_REJECT, count);
        apply();
        debugInfo();
    }

    public boolean isExceedTrafficQuota() {
        if (this.mExceedTrafficQuota) {
            long currentDate = System.currentTimeMillis() / ONE_DAY_MILLS;
            if (this.mLastDate.get() < currentDate) {
                this.mExceedTrafficQuota = false;
            }
        }
        return this.mExceedTrafficQuota;
    }

    private void apply() {
        Handler handler = this.mHandler;
        if (handler != null && !handler.hasMessages(1)) {
            this.mHandler.sendEmptyMessageDelayed(1, MESSAGE_APPLY_DELAY);
        }
    }

    private void commitData() {
        long currentDate = System.currentTimeMillis() / ONE_DAY_MILLS;
        long requestCount = this.mRequestCount.get();
        long trafficSize = this.mTrafficSize.get();
        if (requestCount >= MAX_VALUE_THRESHOLD || trafficSize >= MAX_VALUE_THRESHOLD || this.mLastDate.get() < currentDate) {
            long success = this.mSucceedCount.get();
            long fail = this.mFailedCount.get();
            long lastDate = this.mLastDate.get();
            long reject = this.mRejectCount.get();
            String errorCodes = getErrorCodesInString();
            clearCounters();
            this.mLastDate.set(currentDate);
            this.mEditor.putLong(KEY_LAST_DATE, this.mLastDate.get());
            boolean international = DeviceInfoUtils.isInternationalVer();
            if (!international) {
                IDataLog dataLogService = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
                dataLogService.sendStatData(dataLogService.buildMoleEvent().setEvent(EVENT_NAME).setPageId(GlobalConfig.MOLE_PAGE_ID).setButtonId(MOLE_TRAFFIC_OSS_BUTTON_ID).setProperty(STAT_KEY_PACKAGE_NAME, GlobalConfig.getApplicationSimpleName()).setProperty(STAT_KEY_REQUEST, Long.valueOf(requestCount)).setProperty(STAT_KEY_FAILED, Long.valueOf(fail)).setProperty(STAT_KEY_SUCCEED, Long.valueOf(success)).setProperty(STAT_KEY_REJECT, Long.valueOf(reject)).setProperty(STAT_KEY_TRAFFIC_SIZE_IN_KB, Long.valueOf(trafficSize)).setProperty(STAT_KEY_ERROR_CODES, errorCodes).setProperty("date", Long.valueOf(lastDate)).build());
            }
        }
    }

    @VisibleForTesting
    public void clearCounters() {
        this.mRequestCount.set(0L);
        this.mEditor.putLong(KEY_REQUEST, 0L);
        this.mFailedCount.set(0L);
        this.mEditor.putLong(KEY_FAILED, 0L);
        this.mSucceedCount.set(0L);
        this.mEditor.putLong(KEY_SUCCEED, 0L);
        this.mTrafficSize.set(0L);
        this.mEditor.putLong(KEY_TRAFFIC_SIZE, 0L);
        this.mRejectCount.set(0);
        this.mEditor.putInt(KEY_REJECT, 0);
        this.mExceedTrafficQuota = false;
        synchronized (this) {
            this.mErrorCodes.clear();
        }
        this.mEditor.putString(KEY_ERROR_CODES, "");
    }

    private void debugInfo() {
        if (DEBUG) {
            String message = "[RemoteStorage] Request:" + this.mRequestCount.get() + " Failed:" + this.mFailedCount.get() + " Succeed:" + this.mSucceedCount.get() + " Size(KB):" + this.mTrafficSize.get() + " ErrorCodes:" + getErrorCodesInString() + " Package:" + GlobalConfig.getApplicationSimpleName() + " Net:" + GlobalConfig.getNetworkType();
            LogUtils.v(TAG, message);
        }
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        if (msg.what == 1) {
            commitData();
            this.mEditor.apply();
        }
        return true;
    }

    /* loaded from: classes.dex */
    private static final class Holder {
        private static final StorageCounter INSTANCE = new StorageCounter();

        private Holder() {
        }
    }

    private void ensureInitialized() {
        if (this.mEditor == null) {
            throw new RuntimeException("StorageCounter has not been initialized yet!");
        }
    }

    private HashMap<String, Long> initFromPreference(@NonNull SharedPreferences preferences) {
        HashMap<String, Long> errorCodesMap = null;
        String codes = preferences.getString(KEY_ERROR_CODES, "");
        if (!TextUtils.isEmpty(codes)) {
            Type type = new TypeToken<HashMap<String, Long>>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic.StorageCounter.1
            }.getType();
            try {
                errorCodesMap = (HashMap) this.mJsonConverter.fromJson(codes, type);
            } catch (Exception e) {
            }
        }
        if (errorCodesMap == null) {
            return new HashMap<>(10);
        }
        return errorCodesMap;
    }

    private void updateErrorCodes(String code) {
        if (TextUtils.isEmpty(code)) {
            return;
        }
        synchronized (this) {
            boolean hasCode = this.mErrorCodes.containsKey(code);
            if (hasCode || this.mErrorCodes.size() < 10) {
                long cnt = 0;
                if (hasCode) {
                    cnt = this.mErrorCodes.get(code).longValue();
                }
                this.mErrorCodes.put(code, Long.valueOf(1 + cnt));
            }
            if (this.mJsonConverter == null) {
                this.mJsonConverter = new Gson();
            }
            this.mEditor.putString(KEY_ERROR_CODES, this.mJsonConverter.toJson(this.mErrorCodes));
        }
    }

    private String getErrorCodesInString() {
        StringBuilder builder = new StringBuilder();
        synchronized (this) {
            for (String key : this.mErrorCodes.keySet()) {
                builder.append(key + ":" + this.mErrorCodes.get(key) + ";");
            }
        }
        return builder.toString();
    }

    public static boolean isInternationVersion() {
        boolean internation = DeviceInfoUtils.isInternationalVer();
        Log.i("netChannel", "isInternationVersion :\t" + internation);
        return internation;
    }
}

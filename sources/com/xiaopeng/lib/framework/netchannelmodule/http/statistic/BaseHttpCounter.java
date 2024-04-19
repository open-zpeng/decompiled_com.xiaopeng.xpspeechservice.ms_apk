package com.xiaopeng.lib.framework.netchannelmodule.http.statistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.framework.netchannelmodule.common.SharedPrefsUtil;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic.StorageCounter;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.lang.reflect.Type;
import java.util.HashMap;
/* loaded from: classes.dex */
public abstract class BaseHttpCounter implements Handler.Callback {
    private static final long HALF_DAY_MILLS = 43200000;
    private static final short MAX_DOMAINS_IN_DETAIL_LIST = 15;
    private static final long MAX_VALUE_THRESHOLD = 104857600;
    private static final int MESSAGE_APPLY = 1;
    private static final long MESSAGE_APPLY_DELAY = 10000;
    private static final String STAT_KEY_DETAILS = "details";
    private static final String STAT_KEY_FAILED = "fail";
    private static final String STAT_KEY_LAST_DATE = "date";
    private static final String STAT_KEY_PACKAGE_NAME = "pack";
    private static final String STAT_KEY_REQUEST = "req";
    private static final String STAT_KEY_RX_SIZE_IN_KB = "rx";
    private static final String STAT_KEY_SUCCEED = "suc";
    private static final String STAT_KEY_TX_SIZE_IN_KB = "tx";
    private static final String TAG = "NetChannel.Http.Counter";
    private int MIN_SAVING_INTERVAL;
    private SharedPreferences.Editor mEditor;
    private boolean mEnabled;
    private final Handler mHandler;
    private Gson mJsonConverter;
    private long mLastDate;
    private long mLastDetailSavingTime;
    private final String mMoleButtonId;
    private long mRequestCount;
    private final String mStatEventName;
    private TrafficData mTotalData;
    private HashMap<String, TrafficData> mTrafficDetails;
    private static final boolean DEBUG = GlobalConfig.debuggable();
    protected static String sKeyFailed = "net_http_failed";
    protected static String sKeySucceed = "net_http_succeed";
    protected static String sKeyRequest = "net_http_request";
    protected static String sKeyTrafficRx = "net_http_rx";
    protected static String sKeyTrafficTx = "net_http_tx";
    protected static String sKeyDetails = "net_http_details";
    protected static String sKeyLastDate = "net_http_date";

    private BaseHttpCounter() {
        this.mEnabled = false;
        this.MIN_SAVING_INTERVAL = 30000;
        this.mStatEventName = "";
        this.mMoleButtonId = "";
        this.mHandler = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public BaseHttpCounter(@NonNull String eventName, @NonNull String buttonId) {
        this.mEnabled = false;
        this.MIN_SAVING_INTERVAL = 30000;
        this.mStatEventName = eventName;
        this.mMoleButtonId = buttonId;
        this.mHandler = new Handler(ThreadUtils.getLooper(0), this);
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        if (msg.what == 1) {
            synchronized (this) {
                commitData();
                this.mEditor.apply();
            }
        }
        return true;
    }

    public synchronized void init() {
        Context context = GlobalConfig.getApplicationContext();
        this.mEnabled = context != null;
        if (this.mEnabled) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            try {
                this.mEditor = preferences.edit();
            } catch (OutOfMemoryError e) {
                SharedPrefsUtil.clearNetChannelSharedPrefs(context);
                System.exit(0);
            }
            long currentDate = System.currentTimeMillis() / HALF_DAY_MILLS;
            this.mLastDate = preferences.getLong(sKeyLastDate, currentDate);
            this.mRequestCount = 0L;
            this.mTotalData = new TrafficData();
            this.mJsonConverter = new Gson();
            this.mTrafficDetails = new HashMap<>(15);
            try {
                this.mRequestCount = preferences.getLong(sKeyRequest, 0L);
                this.mTotalData.setCount(preferences.getLong(sKeySucceed, 0L), preferences.getLong(sKeyFailed, 0L));
                this.mTotalData.setSize(preferences.getLong(sKeyTrafficRx, 0L), preferences.getLong(sKeyTrafficTx, 0L));
                this.mTrafficDetails = initFromPreference(preferences);
            } catch (Exception ex) {
                LogUtils.e(TAG, "Failure to load from preference due to " + ex);
            }
            debugInfo();
        }
    }

    public synchronized void increaseRequest(String domain, long sent) {
        if (this.mEnabled) {
            ensureInitialized();
            this.mRequestCount++;
            this.mEditor.putLong(sKeyRequest, this.mRequestCount);
            this.mTotalData.addSentSize(sent);
            TrafficData data = getTrafficDataByDomain(domain);
            if (data != null) {
                data.addSentSize(sent);
                saveDetailData();
            }
        }
    }

    public synchronized void increaseFailureWithSize(@Nullable String domain, long received) {
        TrafficData data;
        if (this.mEnabled) {
            this.mTotalData.increaseFailed(received);
            this.mEditor.putLong(sKeyFailed, this.mTotalData.failed());
            this.mEditor.putLong(sKeyTrafficRx, this.mTotalData.receivedSize());
            if (domain != null && (data = getTrafficDataByDomain(domain)) != null) {
                data.increaseFailed(received);
                saveDetailData();
            }
            apply();
            debugInfo();
        }
    }

    public synchronized void increaseSucceedWithSize(@Nullable String domain, long received) {
        TrafficData data;
        if (this.mEnabled) {
            this.mTotalData.increaseSucceed(received);
            this.mEditor.putLong(sKeySucceed, this.mTotalData.succeed());
            this.mEditor.putLong(sKeyTrafficRx, this.mTotalData.receivedSize());
            if (domain != null && (data = getTrafficDataByDomain(domain)) != null) {
                data.increaseSucceed(received);
                saveDetailData();
            }
            apply();
            debugInfo();
        }
    }

    public synchronized void addReceivedSize(@Nullable String domain, long received) {
        TrafficData data;
        if (this.mEnabled) {
            this.mTotalData.addReceivedSize(received);
            this.mEditor.putLong(sKeyTrafficRx, this.mTotalData.receivedSize());
            if (domain != null && (data = getTrafficDataByDomain(domain)) != null) {
                data.addReceivedSize(received);
                saveDetailData();
            }
            apply();
            debugInfo();
        }
    }

    public synchronized void addSentSize(@Nullable String domain, long sent) {
        TrafficData data;
        if (this.mEnabled) {
            this.mTotalData.addSentSize(sent);
            this.mEditor.putLong(sKeyTrafficTx, this.mTotalData.sentSize());
            if (domain != null && (data = getTrafficDataByDomain(domain)) != null) {
                data.addSentSize(sent);
                saveDetailData();
            }
            apply();
            debugInfo();
        }
    }

    private void apply() {
        Handler handler = this.mHandler;
        if (handler != null && !handler.hasMessages(1)) {
            this.mHandler.sendEmptyMessageDelayed(1, MESSAGE_APPLY_DELAY);
        }
    }

    private synchronized void commitData() {
        long currentDate = System.currentTimeMillis() / HALF_DAY_MILLS;
        if (this.mTotalData.receivedSize() >= MAX_VALUE_THRESHOLD || this.mTotalData.sentSize() >= MAX_VALUE_THRESHOLD || this.mLastDate < currentDate) {
            String details = this.mJsonConverter.toJson(this.mTrafficDetails);
            if (!StorageCounter.isInternationVersion()) {
                IDataLog dataLogService = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
                dataLogService.sendStatData(dataLogService.buildMoleEvent().setEvent(this.mStatEventName).setPageId(GlobalConfig.MOLE_PAGE_ID).setButtonId(this.mMoleButtonId).setProperty(STAT_KEY_PACKAGE_NAME, GlobalConfig.getApplicationSimpleName()).setProperty(STAT_KEY_REQUEST, Long.valueOf(this.mRequestCount)).setProperty(STAT_KEY_FAILED, Long.valueOf(this.mTotalData.failed())).setProperty(STAT_KEY_SUCCEED, Long.valueOf(this.mTotalData.succeed())).setProperty(STAT_KEY_RX_SIZE_IN_KB, Long.valueOf(this.mTotalData.receivedSize() >> 10)).setProperty(STAT_KEY_TX_SIZE_IN_KB, Long.valueOf(this.mTotalData.sentSize() >> 10)).setProperty(STAT_KEY_DETAILS, details).setProperty("date", Long.valueOf(this.mLastDate)).build());
            }
            this.mLastDate = currentDate;
            this.mEditor.putLong(sKeyLastDate, this.mLastDate);
            clearCounters();
        }
    }

    @VisibleForTesting
    public synchronized void clearCounters() {
        if (this.mEnabled) {
            this.mRequestCount = 0L;
            this.mEditor.putLong(sKeyRequest, 0L);
            this.mEditor.putLong(sKeyFailed, 0L);
            this.mEditor.putLong(sKeySucceed, 0L);
            this.mEditor.putLong(sKeyTrafficRx, 0L);
            this.mEditor.putLong(sKeyTrafficTx, 0L);
            this.mTotalData.reset();
            this.mTrafficDetails.clear();
            this.mEditor.putString(sKeyDetails, "");
        }
    }

    private synchronized void debugInfo() {
        if (DEBUG) {
            String message = "[Http (" + this.mStatEventName + ")]: Request:" + this.mRequestCount + " Total:" + this.mTotalData + " Details:" + this.mJsonConverter.toJson(this.mTrafficDetails) + " Package:" + GlobalConfig.getApplicationSimpleName() + " Net:" + GlobalConfig.getNetworkType();
            LogUtils.v(TAG, message);
        }
    }

    private void ensureInitialized() {
        if (this.mEditor == null) {
            throw new RuntimeException("StorageCounter has not been initialized yet!");
        }
    }

    private void saveDetailData() {
        if (System.currentTimeMillis() - this.mLastDetailSavingTime < this.MIN_SAVING_INTERVAL) {
            return;
        }
        this.mEditor.putString(sKeyDetails, this.mJsonConverter.toJson(this.mTrafficDetails));
        this.mLastDetailSavingTime = System.currentTimeMillis();
    }

    private HashMap<String, TrafficData> initFromPreference(@NonNull SharedPreferences preferences) {
        HashMap<String, TrafficData> domainDetails = null;
        String codes = preferences.getString(sKeyDetails, "");
        if (!TextUtils.isEmpty(codes)) {
            Type type = new TypeToken<HashMap<String, TrafficData>>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.statistic.BaseHttpCounter.1
            }.getType();
            try {
                domainDetails = (HashMap) this.mJsonConverter.fromJson(codes, type);
            } catch (Exception e) {
            }
        }
        if (domainDetails == null) {
            return new HashMap<>(15);
        }
        return domainDetails;
    }

    @Nullable
    private TrafficData getTrafficDataByDomain(String domain) {
        if (this.mTrafficDetails.containsKey(domain)) {
            return this.mTrafficDetails.get(domain);
        }
        if (this.mTrafficDetails.size() >= 15) {
            return null;
        }
        TrafficData data = new TrafficData();
        this.mTrafficDetails.put(domain, data);
        return data;
    }
}

package com.xiaopeng.xpspeechservice.ms.tts.ttsengine;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.IEngine;
import com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.config.BusinessConfig;
import com.xiaopeng.xpspeechservice.ms.tts.config.DataPriorityCaller;
import com.xiaopeng.xpspeechservice.ms.tts.config.HybridEngineBusinessConfig;
import com.xiaopeng.xpspeechservice.ms.tts.config.HybridEngineSelectConfig;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
/* loaded from: classes.dex */
public class XpHybridEngine implements IEngine {
    private static final String HYBRID_MODE_DATA = "data";
    private static final String HYBRID_MODE_LATENCY = "latency";
    private Handler mDataHandler;
    private HandlerThread mDataThread;
    private volatile IHybridEngine mEngine;
    private Handler mEventHandler;
    private HandlerThread mEventThread;
    private XpOfflineController mOfflineEngine;
    private XpOnlineController mOnlineEngine;
    private XpDataPriorityHybridEngine mXpDataPriorityHybridEngine;
    private XpLatencyPriorityHybridEngine mXpLatencyPriorityHybridEngine;
    private String TAG = "XpHybridEngine";
    private List<DataPriorityCaller> mDataPriorityCallerList = null;
    private List<BusinessConfig> mBusinessConfigList = null;

    public XpHybridEngine(String channelName) {
        this.TAG += "_" + channelName;
        LogUtils.d(this.TAG, "XpHybridEngine construct");
        this.mEventThread = new HandlerThread("TtsEventThread_" + channelName);
        this.mEventThread.start();
        this.mEventHandler = new Handler(this.mEventThread.getLooper());
        this.mDataThread = new HandlerThread("TtsDataThread_" + channelName);
        this.mDataThread.start();
        this.mDataHandler = new Handler(this.mDataThread.getLooper());
        EventBus.getDefault().register(this);
        MyEngineCallback engineCallback = new MyEngineCallback();
        this.mOfflineEngine = new XpOfflineController(channelName, engineCallback, this.mEventHandler);
        this.mOnlineEngine = new XpOnlineController(channelName, engineCallback, this.mEventHandler);
        this.mXpLatencyPriorityHybridEngine = new XpLatencyPriorityHybridEngine(channelName, this.mOfflineEngine, this.mOnlineEngine, this.mEventHandler, this.mDataHandler);
        this.mXpDataPriorityHybridEngine = new XpDataPriorityHybridEngine(channelName, this.mOfflineEngine, this.mOnlineEngine, this.mEventHandler, this.mDataHandler);
    }

    public void init() {
        LogUtils.i(this.TAG, "init");
        this.mOfflineEngine.init();
        this.mOnlineEngine.init();
    }

    public void shutdown() {
        LogUtils.i(this.TAG, "shutdown");
        this.mOfflineEngine.shutdown();
        this.mOnlineEngine.shutdown();
    }

    private IHybridEngine selectEngine(Bundle params) {
        int i;
        List<BusinessConfig> list;
        IHybridEngine engine = null;
        String packageName = params.getString("source", BuildInfoUtils.UNKNOWN);
        String hybridMode = params.getString("hybridMode", "");
        if (HYBRID_MODE_LATENCY.equals(hybridMode)) {
            engine = this.mXpLatencyPriorityHybridEngine;
        } else if ("data".equals(hybridMode)) {
            engine = this.mXpDataPriorityHybridEngine;
        }
        int onlineWaitTime = params.getInt("onlineWaitTime", -1);
        int i2 = 0;
        if (engine != null && onlineWaitTime >= 0) {
            LogUtils.v(this.TAG, "selectEngine app set hybridMode %s onlineWaitTime %s", hybridMode, Integer.valueOf(onlineWaitTime));
            return engine;
        }
        String business = params.getString("business", null);
        if (business == null || (list = this.mBusinessConfigList) == null) {
            i = 0;
        } else {
            Iterator<BusinessConfig> it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    i = 0;
                    break;
                }
                BusinessConfig config = it.next();
                if (packageName.equals(config.packageName)) {
                    String[] strArr = config.businessList;
                    int length = strArr.length;
                    int i3 = 0;
                    while (true) {
                        if (i3 >= length) {
                            i = i2;
                            break;
                        }
                        String targetBusiness = strArr[i3];
                        if (!business.equals(targetBusiness)) {
                            i3++;
                            i2 = 0;
                        } else {
                            if (engine == null) {
                                if (HYBRID_MODE_LATENCY.equals(config.hybridMode)) {
                                    engine = this.mXpLatencyPriorityHybridEngine;
                                } else if ("data".equals(config.hybridMode)) {
                                    engine = this.mXpDataPriorityHybridEngine;
                                }
                            }
                            if (onlineWaitTime < 0 && config.onlineWaitTime >= 0) {
                                onlineWaitTime = config.onlineWaitTime;
                                params.putInt("onlineWaitTime", onlineWaitTime);
                            }
                            if (engine == null) {
                                i = 0;
                            } else {
                                LogUtils.v(this.TAG, "selectEngine match business %s hybridMode %s onlineWaitTime %s", business, config.hybridMode, Integer.valueOf(onlineWaitTime));
                                return engine;
                            }
                        }
                    }
                }
            }
        }
        if (engine == null) {
            String txt = params.getString("txt", "");
            List<DataPriorityCaller> list2 = this.mDataPriorityCallerList;
            if (list2 != null) {
                Iterator<DataPriorityCaller> it2 = list2.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    DataPriorityCaller caller = it2.next();
                    if (packageName.equals(caller.packageName)) {
                        engine = this.mXpDataPriorityHybridEngine;
                        if (caller.exceptionPatternList != null) {
                            String[] strArr2 = caller.exceptionPatternList;
                            int length2 = strArr2.length;
                            int i4 = i;
                            while (true) {
                                if (i4 >= length2) {
                                    break;
                                }
                                String pattern = strArr2[i4];
                                if (!Pattern.matches(pattern, txt)) {
                                    i4++;
                                } else {
                                    engine = this.mXpLatencyPriorityHybridEngine;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (engine == null) {
                IHybridEngine engine2 = this.mXpLatencyPriorityHybridEngine;
                return engine2;
            }
            return engine;
        }
        return engine;
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.IEngine
    public int speak(Bundle params, IEngineCallback cb) {
        LogUtils.i(this.TAG, "speak");
        this.mEngine = selectEngine(params);
        this.mEngine.speak(params, cb);
        return 0;
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.IEngine
    public void stop() {
        LogUtils.i(this.TAG, "stop");
        this.mEngine.stop();
    }

    /* loaded from: classes.dex */
    private class MyEngineCallback implements ITtsEngineCallback {
        private MyEngineCallback() {
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback
        public void onEvent(EventType event) {
            onEvent(event, null);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback
        public void onEvent(EventType event, Object obj) {
            XpHybridEngine.this.mEngine.onHybridEvent(event, obj);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onConfigChange(HybridEngineSelectConfig config) {
        String str = this.TAG;
        LogUtils.i(str, "onConfigChange " + config);
        this.mDataPriorityCallerList = config.dataPriorityCallerList;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onConfigChange(HybridEngineBusinessConfig config) {
        String str = this.TAG;
        LogUtils.i(str, "onConfigChange " + config);
        this.mBusinessConfigList = config.businessConfigList;
    }
}

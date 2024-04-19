package com.xiaopeng.xpspeechservice.ms.tts.ttsengine;

import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.xiaopeng.lib.framework.moduleinterface.carcontroller.IInputController;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.config.DataPriorityHybridConfig;
import com.xiaopeng.xpspeechservice.ms.tts.config.DataPriorityParam;
import com.xiaopeng.xpspeechservice.ms.tts.config.DataPriorityPatternParam;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.EngineEndType;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.HybridUploadData;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.TtsModeType;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
/* loaded from: classes.dex */
public class XpDataPriorityHybridEngine implements IHybridEngine {
    private static final int DEFAULT_OFFLINE_START_DELAY = 800;
    private static final int DEFAULT_ONLINE_WAIT_TIME = 1000;
    private static final int MSG_START_OFFLINE_ENGINE = 101;
    private static final int MSG_WAIT_ONLINE_TIMEOUT = 102;
    private static final int OFFLINE_ONLINE_TIME_GAP = 200;
    private IEngineCallback mCallback;
    private Handler mDataHandler;
    private EventHandler mEventHandler;
    private XpOfflineController mOfflineEngine;
    private Bundle mOfflineParams;
    private XpOnlineController mOnlineEngine;
    private Bundle mTxtParams;
    private String mUid;
    private String TAG = "XpDataPriorityHybridEngine";
    private Bundle mOfflineInfo = null;
    private PendingItem mPendingItem = null;
    private volatile long mStartTime = 0;
    private volatile int mSendLatency = 0;
    private volatile int mDataPkgMaxLatency = 0;
    private volatile int mTotalDataPkgSize = 0;
    private int mOfflineDelayStartLatency = DEFAULT_OFFLINE_START_DELAY;
    private int mOnlineWaitTime = 1000;
    private List<DataPriorityParam> mDataPriorityParamList = new ArrayList();
    private final HybridState mIdleState = new IdleState();
    private final HybridState mOnlineDataPendingOfflineNotStartState = new OnlineDataPendingOfflineNotStartState();
    private final HybridState mWaitOnlineDataTillTimeOutOfflineStartedState = new WaitOnlineDataTillTimeOutOfflineStartedState();
    private final HybridState mWaitOnlineDataTillTimeOutOfflineDataReadyState = new WaitOnlineDataTillTimeOutOfflineDataReadyState();
    private final HybridState mWaitOnlineDataTillTimeOutOfflineEndState = new WaitOnlineDataTillTimeOutOfflineEndState();
    private final HybridState mParallelDataPendingState = new ParallelDataPendingState();
    private final HybridState mOnlineOnlyDataPendingState = new OnlineOnlyDataPendingState();
    private final HybridState mOnlineOnlyDataStartedState = new OnlineOnlyDataStartedState();
    private final HybridState mOfflineOnlyDataPendingState = new OfflineOnlyDataPendingState();
    private final HybridState mOfflineOnlyDataStartedState = new OfflineOnlyDataStartedState();
    private final HybridState mOfflineOnlyEndState = new OfflineOnlyEndState();
    private DataWriter mDataWriter = new DataWriter();
    private HybridState mHybridState = this.mIdleState;
    private Bundle mOnlineParams = new Bundle();

    static /* synthetic */ int access$1112(XpDataPriorityHybridEngine x0, int x1) {
        int i = x0.mTotalDataPkgSize + x1;
        x0.mTotalDataPkgSize = i;
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PendingItem {
        public IEngineCallback callback;
        public Bundle params;

        public PendingItem(Bundle bundle, IEngineCallback cb) {
            this.params = bundle;
            this.callback = cb;
        }
    }

    public XpDataPriorityHybridEngine(String channelName, XpOfflineController offlineEndine, XpOnlineController onlineEngine, Handler eventHandler, Handler dataHandler) {
        this.TAG += "_" + channelName;
        this.mEventHandler = new EventHandler(eventHandler.getLooper());
        this.mDataHandler = new Handler(dataHandler.getLooper());
        this.mOfflineEngine = offlineEndine;
        this.mOnlineEngine = onlineEngine;
        this.mOnlineParams.putInt("sampleRate", 24000);
        this.mOnlineParams.putInt("format", 2);
        this.mOnlineParams.putInt("channelCount", 1);
        EventBus.getDefault().register(this);
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IHybridEngine
    public void speak(final Bundle params, final IEngineCallback cb) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.1
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpDataPriorityHybridEngine.this.TAG, "speak at %s", XpDataPriorityHybridEngine.this.mHybridState.getClass().getSimpleName());
                XpDataPriorityHybridEngine.this.mHybridState.speak(params, cb);
            }
        });
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IHybridEngine
    public void stop() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.2
            @Override // java.lang.Runnable
            public void run() {
                if (XpDataPriorityHybridEngine.this.mPendingItem != null) {
                    XpDataPriorityHybridEngine.this.mPendingItem = null;
                    LogUtils.i(XpDataPriorityHybridEngine.this.TAG, "stop: remove pending item");
                    return;
                }
                LogUtils.i(XpDataPriorityHybridEngine.this.TAG, "stop at %s", XpDataPriorityHybridEngine.this.mHybridState.getClass().getSimpleName());
                XpDataPriorityHybridEngine.this.mHybridState.stop();
            }
        });
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IHybridEngine
    public void onHybridEvent(final EventType event, final Object arg) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.3
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpDataPriorityHybridEngine.this.TAG, "onHybridEvent %s at %s", event.name(), XpDataPriorityHybridEngine.this.mHybridState.getClass().getSimpleName());
                if (event != EventType.EVENT_ONLINE_SYNTH_END && event == EventType.EVENT_OFFLINE_DATA_READY) {
                    XpDataPriorityHybridEngine.this.mOfflineParams = (Bundle) arg;
                }
                XpDataPriorityHybridEngine.this.mHybridState.onEvent(event, arg);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setHybridState(HybridState state) {
        PendingItem pendingItem;
        LogUtils.i(this.TAG, "hybrid state change %s to %s", this.mHybridState.getClass().getSimpleName(), state.getClass().getSimpleName());
        this.mHybridState = state;
        HybridState hybridState = this.mHybridState;
        if (hybridState == this.mIdleState && (pendingItem = this.mPendingItem) != null) {
            hybridState.speak(pendingItem.params, this.mPendingItem.callback);
            this.mPendingItem = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class HybridState {
        public HybridState() {
        }

        public void speak(Bundle params, IEngineCallback cb) {
            String str = XpDataPriorityHybridEngine.this.TAG;
            LogUtils.w(str, "Not handled synth speak at " + getClass().getSimpleName());
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.mPendingItem = new PendingItem(params, cb);
        }

        public void stop() {
            String str = XpDataPriorityHybridEngine.this.TAG;
            LogUtils.w(str, "Not handled synth stop at " + getClass().getSimpleName());
        }

        public void onEvent(EventType event, Object arg) {
            LogUtils.w(XpDataPriorityHybridEngine.this.TAG, "Not handled synth event %s at %s", event.name(), getClass().getSimpleName());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setWaitTimeForText(Bundle params) {
        String packageName = params.getString("source", BuildInfoUtils.UNKNOWN);
        String txt = params.getString("txt", "");
        this.mOfflineDelayStartLatency = DEFAULT_OFFLINE_START_DELAY;
        this.mOnlineWaitTime = 1000;
        List<DataPriorityParam> list = this.mDataPriorityParamList;
        if (list != null) {
            for (DataPriorityParam param : list) {
                if (packageName.equals(param.packageName)) {
                    if (param.patternParamList != null) {
                        for (DataPriorityPatternParam patternParam : param.patternParamList) {
                            if (Pattern.matches(patternParam.pattern, txt)) {
                                this.mOfflineDelayStartLatency = patternParam.offlineStartLatency;
                                this.mOnlineWaitTime = patternParam.onlineWaitTime;
                                return;
                            }
                        }
                        return;
                    }
                    return;
                }
            }
        }
    }

    /* loaded from: classes.dex */
    private class IdleState extends HybridState {
        private IdleState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void speak(Bundle params, IEngineCallback cb) {
            int offlineDelay;
            XpDataPriorityHybridEngine.this.mCallback = cb;
            XpDataPriorityHybridEngine.this.mUid = params.getString("uid");
            XpDataPriorityHybridEngine.this.mTxtParams = params;
            XpDataPriorityHybridEngine.this.mStartTime = SystemClock.elapsedRealtime();
            XpDataPriorityHybridEngine.this.mSendLatency = 0;
            XpDataPriorityHybridEngine.this.mDataPkgMaxLatency = 0;
            XpDataPriorityHybridEngine.this.mTotalDataPkgSize = 0;
            XpDataPriorityHybridEngine.this.setWaitTimeForText(params);
            XpDataPriorityHybridEngine.this.mOnlineEngine.speak(XpDataPriorityHybridEngine.this.mTxtParams);
            int onlineWaitTime = params.getInt("onlineWaitTime", -1);
            if (onlineWaitTime < 0) {
                offlineDelay = SystemProperties.getInt("sys.xiaopeng.tts.offline_delay-stable", XpDataPriorityHybridEngine.this.mOfflineDelayStartLatency);
                onlineWaitTime = SystemProperties.getInt("sys.xiaopeng.tts.wait_online-stable", XpDataPriorityHybridEngine.this.mOnlineWaitTime);
            } else {
                offlineDelay = onlineWaitTime - 200;
                if (offlineDelay < 0) {
                    offlineDelay = 0;
                }
            }
            LogUtils.v(XpDataPriorityHybridEngine.this.TAG, "speak offlineDelay " + offlineDelay + " onlineWaitTime " + onlineWaitTime);
            XpDataPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpDataPriorityHybridEngine.MSG_START_OFFLINE_ENGINE, (long) offlineDelay);
            XpDataPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, (long) onlineWaitTime);
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mOnlineDataPendingOfflineNotStartState);
        }
    }

    /* loaded from: classes.dex */
    private class OnlineDataPendingOfflineNotStartState extends HybridState {
        private OnlineDataPendingOfflineNotStartState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
            XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_START_OFFLINE_ENGINE);
            XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
            XpDataPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_START_OFFLINE_ENGINE);
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine.this.mOfflineEngine.speak(XpDataPriorityHybridEngine.this.mTxtParams);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mOfflineOnlyDataPendingState);
            } else if (event == EventType.EVENT_OFFLINE_START_DELAY_TIMEOUT) {
                XpDataPriorityHybridEngine.this.mOfflineEngine.speak(XpDataPriorityHybridEngine.this.mTxtParams);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine2 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine2.setHybridState(xpDataPriorityHybridEngine2.mWaitOnlineDataTillTimeOutOfflineStartedState);
            } else if (event == EventType.EVENT_ONLINE_SYNTH_END) {
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_START_OFFLINE_ENGINE);
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine.this.startOnlineData();
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine3 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine3.setHybridState(xpDataPriorityHybridEngine3.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_BEGIN) {
                int maxWaitTime = ((Integer) arg).intValue();
                int currentDelay = (int) (SystemClock.elapsedRealtime() - XpDataPriorityHybridEngine.this.mStartTime);
                if (maxWaitTime > currentDelay) {
                    XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                    XpDataPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, maxWaitTime - currentDelay);
                }
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class WaitOnlineDataTillTimeOutOfflineStartedState extends HybridState {
        private WaitOnlineDataTillTimeOutOfflineStartedState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
            XpDataPriorityHybridEngine.this.mOfflineEngine.stop();
            XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
            XpDataPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mOfflineOnlyDataPendingState);
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine2 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine2.setHybridState(xpDataPriorityHybridEngine2.mOnlineOnlyDataPendingState);
            } else if (event == EventType.EVENT_WAIT_ONLINE_DATA_TIMEOUT) {
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine3 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine3.setHybridState(xpDataPriorityHybridEngine3.mParallelDataPendingState);
            } else if (event == EventType.EVENT_ONLINE_SYNTH_END) {
                XpDataPriorityHybridEngine.this.mOfflineEngine.stop();
                XpDataPriorityHybridEngine.this.startOnlineData();
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine4 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine4.setHybridState(xpDataPriorityHybridEngine4.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_OFFLINE_DATA_READY) {
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine5 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine5.setHybridState(xpDataPriorityHybridEngine5.mWaitOnlineDataTillTimeOutOfflineDataReadyState);
            } else if (event == EventType.EVENT_ONLINE_BEGIN) {
                int maxWaitTime = ((Integer) arg).intValue();
                int currentDelay = (int) (SystemClock.elapsedRealtime() - XpDataPriorityHybridEngine.this.mStartTime);
                if (maxWaitTime > currentDelay) {
                    XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                    XpDataPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, maxWaitTime - currentDelay);
                }
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class WaitOnlineDataTillTimeOutOfflineDataReadyState extends HybridState {
        private WaitOnlineDataTillTimeOutOfflineDataReadyState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
            XpDataPriorityHybridEngine.this.mOfflineEngine.stop();
            XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
            XpDataPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine.this.startOfflineData();
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mOfflineOnlyDataStartedState);
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine2 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine2.setHybridState(xpDataPriorityHybridEngine2.mOnlineOnlyDataPendingState);
            } else if (event == EventType.EVENT_OFFLINE_END) {
                XpDataPriorityHybridEngine.this.mOfflineInfo = (Bundle) arg;
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine3 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine3.setHybridState(xpDataPriorityHybridEngine3.mWaitOnlineDataTillTimeOutOfflineEndState);
            } else if (event == EventType.EVENT_WAIT_ONLINE_DATA_TIMEOUT) {
                XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
                XpDataPriorityHybridEngine.this.startOfflineData();
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine4 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine4.setHybridState(xpDataPriorityHybridEngine4.mOfflineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_SYNTH_END) {
                XpDataPriorityHybridEngine.this.mOfflineEngine.stop();
                XpDataPriorityHybridEngine.this.startOnlineData();
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine5 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine5.setHybridState(xpDataPriorityHybridEngine5.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_BEGIN) {
                int maxWaitTime = ((Integer) arg).intValue();
                int currentDelay = (int) (SystemClock.elapsedRealtime() - XpDataPriorityHybridEngine.this.mStartTime);
                if (maxWaitTime > currentDelay) {
                    XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                    XpDataPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, maxWaitTime - currentDelay);
                }
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class WaitOnlineDataTillTimeOutOfflineEndState extends HybridState {
        private WaitOnlineDataTillTimeOutOfflineEndState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
            XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
            XpDataPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine.this.mCallback.uploadInfo(XpDataPriorityHybridEngine.this.mOfflineInfo);
                XpDataPriorityHybridEngine.this.startOfflineData();
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mOfflineOnlyEndState);
            } else if (event == EventType.EVENT_WAIT_ONLINE_DATA_TIMEOUT) {
                XpDataPriorityHybridEngine.this.mCallback.uploadInfo(XpDataPriorityHybridEngine.this.mOfflineInfo);
                XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
                XpDataPriorityHybridEngine.this.startOfflineData();
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine2 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine2.setHybridState(xpDataPriorityHybridEngine2.mOfflineOnlyEndState);
            } else if (event == EventType.EVENT_ONLINE_SYNTH_END) {
                XpDataPriorityHybridEngine.this.mOfflineEngine.stop();
                XpDataPriorityHybridEngine.this.startOnlineData();
                XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine3 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine3.setHybridState(xpDataPriorityHybridEngine3.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_BEGIN) {
                int maxWaitTime = ((Integer) arg).intValue();
                int currentDelay = (int) (SystemClock.elapsedRealtime() - XpDataPriorityHybridEngine.this.mStartTime);
                if (maxWaitTime > currentDelay) {
                    XpDataPriorityHybridEngine.this.mEventHandler.removeMessages(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                    XpDataPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, maxWaitTime - currentDelay);
                }
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class ParallelDataPendingState extends HybridState {
        private ParallelDataPendingState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
            XpDataPriorityHybridEngine.this.mOfflineEngine.stop();
            XpDataPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mOfflineOnlyDataPendingState);
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine2 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine2.setHybridState(xpDataPriorityHybridEngine2.mOnlineOnlyDataPendingState);
            } else if (event == EventType.EVENT_ONLINE_SYNTH_END) {
                XpDataPriorityHybridEngine.this.mOfflineEngine.stop();
                XpDataPriorityHybridEngine.this.startOnlineData();
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine3 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine3.setHybridState(xpDataPriorityHybridEngine3.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_OFFLINE_DATA_READY) {
                XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
                XpDataPriorityHybridEngine.this.startOfflineData();
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine4 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine4.setHybridState(xpDataPriorityHybridEngine4.mOfflineOnlyDataStartedState);
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class OnlineOnlyDataPendingState extends HybridState {
        private OnlineOnlyDataPendingState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
            XpDataPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.STOP);
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_SYNTH_END) {
                XpDataPriorityHybridEngine.this.startOnlineData();
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_ERROR) {
                XpDataPriorityHybridEngine.this.mCallback.error();
                XpDataPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.ERROR);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine2 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine2.setHybridState(xpDataPriorityHybridEngine2.mIdleState);
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class OnlineOnlyDataStartedState extends HybridState {
        private boolean mIsDataDone;
        private boolean mIsDataError;
        private boolean mIsEnd;
        private boolean mIsError;
        private boolean mIsStopped;

        private OnlineOnlyDataStartedState() {
            super();
            this.mIsStopped = false;
            this.mIsEnd = false;
            this.mIsError = false;
            this.mIsDataDone = false;
            this.mIsDataError = false;
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.mOnlineEngine.stop();
            XpDataPriorityHybridEngine.this.stopOnlineData();
            this.mIsStopped = true;
            if (this.mIsDataDone || this.mIsDataError) {
                XpDataPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.STOP);
                stateExist();
            }
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_DONE) {
                this.mIsDataDone = true;
            } else if (event == EventType.EVENT_DATA_ERROR) {
                this.mIsDataError = true;
            } else if (event == EventType.EVENT_ONLINE_END) {
                this.mIsEnd = true;
            } else if (event == EventType.EVENT_ONLINE_ERROR) {
                this.mIsError = true;
            } else if (event == EventType.EVENT_UPLOAD_INFO) {
                XpDataPriorityHybridEngine.this.mCallback.uploadInfo((Bundle) arg);
                return;
            } else {
                super.onEvent(event, arg);
            }
            if (this.mIsDataDone || this.mIsDataError) {
                if (this.mIsStopped) {
                    XpDataPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.STOP);
                    stateExist();
                } else if (this.mIsEnd || this.mIsError) {
                    if (!this.mIsDataDone || !this.mIsEnd) {
                        XpDataPriorityHybridEngine.this.mCallback.error();
                        XpDataPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.ERROR);
                    } else {
                        XpDataPriorityHybridEngine.this.mCallback.end();
                        XpDataPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.END);
                    }
                    stateExist();
                }
            }
        }

        private void stateExist() {
            this.mIsStopped = false;
            this.mIsEnd = false;
            this.mIsError = false;
            this.mIsDataDone = false;
            this.mIsDataError = false;
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }
    }

    /* loaded from: classes.dex */
    private class OfflineOnlyDataPendingState extends HybridState {
        private OfflineOnlyDataPendingState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.mOfflineEngine.stop();
            XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_OFFLINE_DATA_READY) {
                XpDataPriorityHybridEngine.this.startOfflineData();
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mOfflineOnlyDataStartedState);
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                XpDataPriorityHybridEngine.this.mCallback.error();
                XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.ERROR);
                XpDataPriorityHybridEngine xpDataPriorityHybridEngine2 = XpDataPriorityHybridEngine.this;
                xpDataPriorityHybridEngine2.setHybridState(xpDataPriorityHybridEngine2.mIdleState);
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class OfflineOnlyDataStartedState extends HybridState {
        private boolean mIsDataDone;
        private boolean mIsDataError;
        private boolean mIsEnd;
        private boolean mIsError;
        private boolean mIsStopped;

        private OfflineOnlyDataStartedState() {
            super();
            this.mIsStopped = false;
            this.mIsEnd = false;
            this.mIsError = false;
            this.mIsDataDone = false;
            this.mIsDataError = false;
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.mOfflineEngine.stop();
            XpDataPriorityHybridEngine.this.stopOfflineData();
            this.mIsStopped = true;
            if (this.mIsDataDone || this.mIsDataError) {
                XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
                stateExist();
            }
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_DONE) {
                this.mIsDataDone = true;
            } else if (event == EventType.EVENT_DATA_ERROR) {
                this.mIsDataError = true;
            } else if (event == EventType.EVENT_OFFLINE_END) {
                XpDataPriorityHybridEngine.this.mCallback.uploadInfo((Bundle) arg);
                this.mIsEnd = true;
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                this.mIsError = true;
            } else {
                super.onEvent(event, arg);
            }
            if (this.mIsDataDone || this.mIsDataError) {
                if (this.mIsStopped) {
                    XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
                    stateExist();
                } else if (this.mIsEnd || this.mIsError) {
                    if (!this.mIsDataDone || !this.mIsEnd) {
                        XpDataPriorityHybridEngine.this.mCallback.error();
                        XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.ERROR);
                    } else {
                        XpDataPriorityHybridEngine.this.mCallback.end();
                        XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.END);
                    }
                    stateExist();
                }
            }
        }

        private void stateExist() {
            this.mIsStopped = false;
            this.mIsEnd = false;
            this.mIsError = false;
            this.mIsDataDone = false;
            this.mIsDataError = false;
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }
    }

    /* loaded from: classes.dex */
    private class OfflineOnlyEndState extends HybridState {
        private boolean mIsDataDone;
        private boolean mIsDataError;
        private boolean mIsStopped;

        private OfflineOnlyEndState() {
            super();
            this.mIsStopped = false;
            this.mIsDataDone = false;
            this.mIsDataError = false;
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void stop() {
            XpDataPriorityHybridEngine.this.stopOfflineData();
            this.mIsStopped = true;
            if (this.mIsDataDone || this.mIsDataError) {
                XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
                stateExist();
            }
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_DONE) {
                this.mIsDataDone = true;
            } else if (event == EventType.EVENT_DATA_ERROR) {
                this.mIsDataError = true;
            } else {
                super.onEvent(event, arg);
            }
            if (this.mIsDataDone || this.mIsDataError) {
                if (this.mIsStopped) {
                    XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
                } else if (this.mIsDataDone) {
                    XpDataPriorityHybridEngine.this.mCallback.end();
                    XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.END);
                } else {
                    XpDataPriorityHybridEngine.this.mCallback.error();
                    XpDataPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.ERROR);
                }
                stateExist();
            }
        }

        private void stateExist() {
            this.mIsStopped = false;
            this.mIsDataDone = false;
            this.mIsDataError = false;
            XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
            xpDataPriorityHybridEngine.setHybridState(xpDataPriorityHybridEngine.mIdleState);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == XpDataPriorityHybridEngine.MSG_START_OFFLINE_ENGINE) {
                XpDataPriorityHybridEngine.this.mHybridState.onEvent(EventType.EVENT_OFFLINE_START_DELAY_TIMEOUT, null);
            } else if (i == XpDataPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT) {
                XpDataPriorityHybridEngine.this.mHybridState.onEvent(EventType.EVENT_WAIT_ONLINE_DATA_TIMEOUT, null);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startOnlineData() {
        this.mCallback.begin(this.mOnlineParams.getInt("sampleRate"), this.mOnlineParams.getInt("format"), this.mOnlineParams.getInt("channelCount"));
        this.mDataWriter.setEngineParams(this.mOnlineEngine, this.mOnlineParams);
        this.mDataHandler.post(this.mDataWriter);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopOnlineData() {
        this.mDataWriter.stop();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startOfflineData() {
        this.mCallback.begin(this.mOfflineParams.getInt("sampleRate"), this.mOfflineParams.getInt("format"), this.mOfflineParams.getInt("channelCount"));
        this.mDataWriter.setEngineParams(this.mOfflineEngine, this.mOfflineParams);
        this.mDataHandler.post(this.mDataWriter);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopOfflineData() {
        this.mDataWriter.stop();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DataWriter implements Runnable {
        private volatile ITtsEngine mEngine = null;
        private volatile Bundle mParams = null;
        private volatile boolean mIsStopped = false;

        public DataWriter() {
        }

        public void setEngineParams(ITtsEngine engine, Bundle params) {
            this.mEngine = engine;
            this.mParams = params;
            this.mIsStopped = false;
        }

        public void stop() {
            this.mIsStopped = true;
        }

        @Override // java.lang.Runnable
        public void run() {
            byte[] dataBuffer;
            LogUtils.v(XpDataPriorityHybridEngine.this.TAG, "readData +++");
            if (this.mEngine == null) {
                return;
            }
            int sampleRate = this.mParams.getInt("sampleRate");
            int bufferSize = AudioTrack.getMinBufferSize(sampleRate, 4, this.mParams.getInt("format"));
            if (bufferSize <= 0) {
                LogUtils.e(XpDataPriorityHybridEngine.this.TAG, "getMinBufferSize error, audioserver may be crash");
                XpDataPriorityHybridEngine.this.onHybridEvent(EventType.EVENT_DATA_ERROR, null);
                return;
            }
            byte[] buffer = new byte[bufferSize];
            boolean isFirstData = true;
            long firstDataPkgRcvTime = 0;
            int multipler = sampleRate / IInputController.KEYCODE_KNOB_WIND_SPD_UP;
            do {
                int size = this.mEngine.getData(buffer);
                if (size > 0) {
                    if (isFirstData) {
                        isFirstData = false;
                        firstDataPkgRcvTime = SystemClock.elapsedRealtime();
                        XpDataPriorityHybridEngine xpDataPriorityHybridEngine = XpDataPriorityHybridEngine.this;
                        xpDataPriorityHybridEngine.mSendLatency = (int) (firstDataPkgRcvTime - xpDataPriorityHybridEngine.mStartTime);
                        XpDataPriorityHybridEngine.this.mTotalDataPkgSize = buffer.length;
                    } else {
                        long time = SystemClock.elapsedRealtime() - firstDataPkgRcvTime;
                        int delay = (int) (time - (XpDataPriorityHybridEngine.this.mTotalDataPkgSize / multipler));
                        if (XpDataPriorityHybridEngine.this.mDataPkgMaxLatency < delay) {
                            XpDataPriorityHybridEngine.this.mDataPkgMaxLatency = delay;
                        }
                        XpDataPriorityHybridEngine.access$1112(XpDataPriorityHybridEngine.this, buffer.length);
                    }
                    if (size < bufferSize) {
                        dataBuffer = new byte[size];
                        System.arraycopy(buffer, 0, dataBuffer, 0, size);
                    } else {
                        dataBuffer = buffer;
                    }
                    XpDataPriorityHybridEngine.this.mCallback.received(dataBuffer);
                }
                if (size < bufferSize) {
                    break;
                }
            } while (!this.mIsStopped);
            XpDataPriorityHybridEngine.this.onHybridEvent(EventType.EVENT_DATA_DONE, null);
            LogUtils.v(XpDataPriorityHybridEngine.this.TAG, "readData ---");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void uploadData(TtsModeType mode, EngineEndType state) {
        long endTime = System.currentTimeMillis();
        HybridUploadData data = new HybridUploadData(mode, this.mSendLatency, this.mDataPkgMaxLatency, this.mTotalDataPkgSize, state, endTime);
        UploadDataModel.getInstance().setHybridData(this.mUid, data);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onConfigChange(final DataPriorityHybridConfig config) {
        String str = this.TAG;
        LogUtils.i(str, "onConfigChange " + config);
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpDataPriorityHybridEngine.4
            @Override // java.lang.Runnable
            public void run() {
                XpDataPriorityHybridEngine.this.mDataPriorityParamList = config.dataPriorityParamList;
            }
        });
    }
}

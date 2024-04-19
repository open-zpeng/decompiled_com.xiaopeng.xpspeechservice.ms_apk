package com.xiaopeng.xpspeechservice.ms.tts.ttsengine;

import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.xiaopeng.lib.framework.moduleinterface.carcontroller.IInputController;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.config.DelayPriorityHybridConfig;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.EngineEndType;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.HybridUploadData;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.TtsModeType;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
/* loaded from: classes.dex */
public class XpLatencyPriorityHybridEngine implements IHybridEngine {
    private static final int MSG_START_OFFLINE_ENGINE = 101;
    private static final int MSG_WAIT_ONLINE_TIMEOUT = 102;
    private static final int OFFLINE_ONLINE_TIME_GAP = 200;
    private IEngineCallback mCallback;
    private Handler mDataHandler;
    private EventHandler mEventHandler;
    private XpOfflineController mOfflineEngine;
    private Bundle mOfflineParams;
    private XpOnlineController mOnlineEngine;
    private Bundle mOnlineParams;
    private Bundle mTxtParams;
    private String mUid;
    private String TAG = "XpLatencyPriorityHybridEngine";
    private Bundle mOfflineInfo = null;
    private PendingItem mPendingItem = null;
    private volatile long mStartTime = 0;
    private volatile int mSendLatency = 0;
    private volatile int mDataPkgMaxLatency = 0;
    private volatile int mTotalDataPkgSize = 0;
    private int mOfflineDelayStartLatency = 300;
    private int mOnlineWaitTime = IInputController.KEYCODE_KNOB_WIND_SPD_UP;
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

    static /* synthetic */ int access$1212(XpLatencyPriorityHybridEngine x0, int x1) {
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

    public XpLatencyPriorityHybridEngine(String channelName, XpOfflineController offlineEndine, XpOnlineController onlineEngine, Handler eventHandler, Handler dataHandler) {
        this.TAG += "_" + channelName;
        this.mEventHandler = new EventHandler(eventHandler.getLooper());
        this.mDataHandler = new Handler(dataHandler.getLooper());
        this.mOfflineEngine = offlineEndine;
        this.mOnlineEngine = onlineEngine;
        EventBus.getDefault().register(this);
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IHybridEngine
    public void speak(final Bundle params, final IEngineCallback cb) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.1
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpLatencyPriorityHybridEngine.this.TAG, "speak at %s", XpLatencyPriorityHybridEngine.this.mHybridState.getClass().getSimpleName());
                XpLatencyPriorityHybridEngine.this.mHybridState.speak(params, cb);
            }
        });
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IHybridEngine
    public void stop() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.2
            @Override // java.lang.Runnable
            public void run() {
                if (XpLatencyPriorityHybridEngine.this.mPendingItem != null) {
                    XpLatencyPriorityHybridEngine.this.mPendingItem = null;
                    LogUtils.i(XpLatencyPriorityHybridEngine.this.TAG, "stop: remove pending item");
                    return;
                }
                LogUtils.i(XpLatencyPriorityHybridEngine.this.TAG, "stop at %s", XpLatencyPriorityHybridEngine.this.mHybridState.getClass().getSimpleName());
                XpLatencyPriorityHybridEngine.this.mHybridState.stop();
            }
        });
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IHybridEngine
    public void onHybridEvent(final EventType event, final Object arg) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.3
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpLatencyPriorityHybridEngine.this.TAG, "onHybridEvent %s at %s", event.name(), XpLatencyPriorityHybridEngine.this.mHybridState.getClass().getSimpleName());
                if (event == EventType.EVENT_ONLINE_DATA_READY) {
                    XpLatencyPriorityHybridEngine.this.mOnlineParams = (Bundle) arg;
                } else if (event == EventType.EVENT_OFFLINE_DATA_READY) {
                    XpLatencyPriorityHybridEngine.this.mOfflineParams = (Bundle) arg;
                }
                XpLatencyPriorityHybridEngine.this.mHybridState.onEvent(event, arg);
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
            String str = XpLatencyPriorityHybridEngine.this.TAG;
            LogUtils.w(str, "Not handled synth speak at " + getClass().getSimpleName());
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.mPendingItem = new PendingItem(params, cb);
        }

        public void stop() {
            String str = XpLatencyPriorityHybridEngine.this.TAG;
            LogUtils.w(str, "Not handled synth stop at " + getClass().getSimpleName());
        }

        public void onEvent(EventType event, Object arg) {
            LogUtils.w(XpLatencyPriorityHybridEngine.this.TAG, "Not handled synth event %s at %s", event.name(), getClass().getSimpleName());
        }
    }

    /* loaded from: classes.dex */
    private class IdleState extends HybridState {
        private IdleState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void speak(Bundle params, IEngineCallback cb) {
            int offlineDelay;
            XpLatencyPriorityHybridEngine.this.mCallback = cb;
            XpLatencyPriorityHybridEngine.this.mUid = params.getString("uid");
            XpLatencyPriorityHybridEngine.this.mTxtParams = params;
            XpLatencyPriorityHybridEngine.this.mStartTime = SystemClock.elapsedRealtime();
            XpLatencyPriorityHybridEngine.this.mSendLatency = 0;
            XpLatencyPriorityHybridEngine.this.mDataPkgMaxLatency = 0;
            XpLatencyPriorityHybridEngine.this.mTotalDataPkgSize = 0;
            XpLatencyPriorityHybridEngine.this.mOnlineEngine.speak(XpLatencyPriorityHybridEngine.this.mTxtParams);
            int onlineWaitTime = params.getInt("onlineWaitTime", -1);
            if (onlineWaitTime < 0) {
                offlineDelay = SystemProperties.getInt("sys.xiaopeng.tts.offline_delay", XpLatencyPriorityHybridEngine.this.mOfflineDelayStartLatency);
                onlineWaitTime = SystemProperties.getInt("sys.xiaopeng.tts.wait_online", XpLatencyPriorityHybridEngine.this.mOnlineWaitTime);
            } else {
                offlineDelay = onlineWaitTime - 200;
                if (offlineDelay < 0) {
                    offlineDelay = 0;
                }
            }
            LogUtils.v(XpLatencyPriorityHybridEngine.this.TAG, "speak offlineDelay " + offlineDelay + " onlineWaitTime " + onlineWaitTime);
            XpLatencyPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpLatencyPriorityHybridEngine.MSG_START_OFFLINE_ENGINE, (long) offlineDelay);
            XpLatencyPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, (long) onlineWaitTime);
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mOnlineDataPendingOfflineNotStartState);
        }
    }

    /* loaded from: classes.dex */
    private class OnlineDataPendingOfflineNotStartState extends HybridState {
        private OnlineDataPendingOfflineNotStartState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
            XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_START_OFFLINE_ENGINE);
            XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
            XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_START_OFFLINE_ENGINE);
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine.this.mOfflineEngine.speak(XpLatencyPriorityHybridEngine.this.mTxtParams);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mOfflineOnlyDataPendingState);
            } else if (event == EventType.EVENT_OFFLINE_START_DELAY_TIMEOUT) {
                XpLatencyPriorityHybridEngine.this.mOfflineEngine.speak(XpLatencyPriorityHybridEngine.this.mTxtParams);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine2 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine2.setHybridState(xpLatencyPriorityHybridEngine2.mWaitOnlineDataTillTimeOutOfflineStartedState);
            } else if (event == EventType.EVENT_ONLINE_DATA_READY) {
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_START_OFFLINE_ENGINE);
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine.this.startOnlineData();
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine3 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine3.setHybridState(xpLatencyPriorityHybridEngine3.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_BEGIN) {
                int maxWaitTime = ((Integer) arg).intValue();
                int currentDelay = (int) (SystemClock.elapsedRealtime() - XpLatencyPriorityHybridEngine.this.mStartTime);
                if (maxWaitTime > currentDelay) {
                    XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                    XpLatencyPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, maxWaitTime - currentDelay);
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

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
            XpLatencyPriorityHybridEngine.this.mOfflineEngine.stop();
            XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
            XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mOfflineOnlyDataPendingState);
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine2 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine2.setHybridState(xpLatencyPriorityHybridEngine2.mOnlineOnlyDataPendingState);
            } else if (event == EventType.EVENT_WAIT_ONLINE_DATA_TIMEOUT) {
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine3 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine3.setHybridState(xpLatencyPriorityHybridEngine3.mParallelDataPendingState);
            } else if (event == EventType.EVENT_ONLINE_DATA_READY) {
                XpLatencyPriorityHybridEngine.this.mOfflineEngine.stop();
                XpLatencyPriorityHybridEngine.this.startOnlineData();
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine4 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine4.setHybridState(xpLatencyPriorityHybridEngine4.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_OFFLINE_DATA_READY) {
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine5 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine5.setHybridState(xpLatencyPriorityHybridEngine5.mWaitOnlineDataTillTimeOutOfflineDataReadyState);
            } else if (event == EventType.EVENT_ONLINE_BEGIN) {
                int maxWaitTime = ((Integer) arg).intValue();
                int currentDelay = (int) (SystemClock.elapsedRealtime() - XpLatencyPriorityHybridEngine.this.mStartTime);
                if (maxWaitTime > currentDelay) {
                    XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                    XpLatencyPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, maxWaitTime - currentDelay);
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

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
            XpLatencyPriorityHybridEngine.this.mOfflineEngine.stop();
            XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
            XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine.this.startOfflineData();
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mOfflineOnlyDataStartedState);
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine2 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine2.setHybridState(xpLatencyPriorityHybridEngine2.mOnlineOnlyDataPendingState);
            } else if (event == EventType.EVENT_OFFLINE_END) {
                XpLatencyPriorityHybridEngine.this.mOfflineInfo = (Bundle) arg;
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine3 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine3.setHybridState(xpLatencyPriorityHybridEngine3.mWaitOnlineDataTillTimeOutOfflineEndState);
            } else if (event == EventType.EVENT_WAIT_ONLINE_DATA_TIMEOUT) {
                XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
                XpLatencyPriorityHybridEngine.this.startOfflineData();
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine4 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine4.setHybridState(xpLatencyPriorityHybridEngine4.mOfflineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_DATA_READY) {
                XpLatencyPriorityHybridEngine.this.mOfflineEngine.stop();
                XpLatencyPriorityHybridEngine.this.startOnlineData();
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine5 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine5.setHybridState(xpLatencyPriorityHybridEngine5.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_BEGIN) {
                int maxWaitTime = ((Integer) arg).intValue();
                int currentDelay = (int) (SystemClock.elapsedRealtime() - XpLatencyPriorityHybridEngine.this.mStartTime);
                if (maxWaitTime > currentDelay) {
                    XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                    XpLatencyPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, maxWaitTime - currentDelay);
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

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
            XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
            XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine.this.mCallback.uploadInfo(XpLatencyPriorityHybridEngine.this.mOfflineInfo);
                XpLatencyPriorityHybridEngine.this.startOfflineData();
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mOfflineOnlyEndState);
            } else if (event == EventType.EVENT_WAIT_ONLINE_DATA_TIMEOUT) {
                XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
                XpLatencyPriorityHybridEngine.this.mCallback.uploadInfo(XpLatencyPriorityHybridEngine.this.mOfflineInfo);
                XpLatencyPriorityHybridEngine.this.startOfflineData();
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine2 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine2.setHybridState(xpLatencyPriorityHybridEngine2.mOfflineOnlyEndState);
            } else if (event == EventType.EVENT_ONLINE_DATA_READY) {
                XpLatencyPriorityHybridEngine.this.mOfflineEngine.stop();
                XpLatencyPriorityHybridEngine.this.startOnlineData();
                XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine3 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine3.setHybridState(xpLatencyPriorityHybridEngine3.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_BEGIN) {
                int maxWaitTime = ((Integer) arg).intValue();
                int currentDelay = (int) (SystemClock.elapsedRealtime() - XpLatencyPriorityHybridEngine.this.mStartTime);
                if (maxWaitTime > currentDelay) {
                    XpLatencyPriorityHybridEngine.this.mEventHandler.removeMessages(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT);
                    XpLatencyPriorityHybridEngine.this.mEventHandler.sendEmptyMessageDelayed(XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT, maxWaitTime - currentDelay);
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

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
            XpLatencyPriorityHybridEngine.this.mOfflineEngine.stop();
            XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.UNSET, EngineEndType.STOP);
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_ERROR) {
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mOfflineOnlyDataPendingState);
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine2 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine2.setHybridState(xpLatencyPriorityHybridEngine2.mOnlineOnlyDataPendingState);
            } else if (event == EventType.EVENT_ONLINE_DATA_READY) {
                XpLatencyPriorityHybridEngine.this.mOfflineEngine.stop();
                XpLatencyPriorityHybridEngine.this.startOnlineData();
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine3 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine3.setHybridState(xpLatencyPriorityHybridEngine3.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_OFFLINE_DATA_READY) {
                XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
                XpLatencyPriorityHybridEngine.this.startOfflineData();
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine4 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine4.setHybridState(xpLatencyPriorityHybridEngine4.mOfflineOnlyDataStartedState);
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

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
            XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.STOP);
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_ONLINE_DATA_READY) {
                XpLatencyPriorityHybridEngine.this.startOnlineData();
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mOnlineOnlyDataStartedState);
            } else if (event == EventType.EVENT_ONLINE_ERROR) {
                XpLatencyPriorityHybridEngine.this.mCallback.error();
                XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.ERROR);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine2 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine2.setHybridState(xpLatencyPriorityHybridEngine2.mIdleState);
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

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.mOnlineEngine.stop();
            XpLatencyPriorityHybridEngine.this.stopOnlineData();
            this.mIsStopped = true;
            if (this.mIsDataDone || this.mIsDataError) {
                XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.STOP);
                stateExist();
            }
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
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
                XpLatencyPriorityHybridEngine.this.mCallback.uploadInfo((Bundle) arg);
                return;
            } else {
                super.onEvent(event, arg);
            }
            if (this.mIsDataDone || this.mIsDataError) {
                if (this.mIsStopped) {
                    XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.STOP);
                    stateExist();
                } else if (this.mIsEnd || this.mIsError) {
                    if (!this.mIsDataDone || !this.mIsEnd) {
                        XpLatencyPriorityHybridEngine.this.mCallback.error();
                        XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.ERROR);
                    } else {
                        XpLatencyPriorityHybridEngine.this.mCallback.end();
                        XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.ONLINE, EngineEndType.END);
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
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
        }
    }

    /* loaded from: classes.dex */
    private class OfflineOnlyDataPendingState extends HybridState {
        private OfflineOnlyDataPendingState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.mOfflineEngine.stop();
            XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_OFFLINE_DATA_READY) {
                XpLatencyPriorityHybridEngine.this.startOfflineData();
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mOfflineOnlyDataStartedState);
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                XpLatencyPriorityHybridEngine.this.mCallback.error();
                XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.ERROR);
                XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine2 = XpLatencyPriorityHybridEngine.this;
                xpLatencyPriorityHybridEngine2.setHybridState(xpLatencyPriorityHybridEngine2.mIdleState);
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

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.mOfflineEngine.stop();
            XpLatencyPriorityHybridEngine.this.stopOfflineData();
            this.mIsStopped = true;
            if (this.mIsDataDone || this.mIsDataError) {
                XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
                stateExist();
            }
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_DONE) {
                this.mIsDataDone = true;
            } else if (event == EventType.EVENT_DATA_ERROR) {
                this.mIsDataError = true;
            } else if (event == EventType.EVENT_OFFLINE_END) {
                XpLatencyPriorityHybridEngine.this.mCallback.uploadInfo((Bundle) arg);
                this.mIsEnd = true;
            } else if (event == EventType.EVENT_OFFLINE_ERROR) {
                this.mIsError = true;
            } else {
                super.onEvent(event, arg);
            }
            if (this.mIsDataDone || this.mIsDataError) {
                if (this.mIsStopped) {
                    XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
                    stateExist();
                } else if (this.mIsEnd || this.mIsError) {
                    if (!this.mIsDataDone || !this.mIsEnd) {
                        XpLatencyPriorityHybridEngine.this.mCallback.error();
                        XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.ERROR);
                    } else {
                        XpLatencyPriorityHybridEngine.this.mCallback.end();
                        XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.END);
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
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
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

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
        public void stop() {
            XpLatencyPriorityHybridEngine.this.stopOfflineData();
            this.mIsStopped = true;
            if (this.mIsDataDone || this.mIsDataError) {
                XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
                stateExist();
            }
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.HybridState
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
                    XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.STOP);
                } else if (this.mIsDataDone) {
                    XpLatencyPriorityHybridEngine.this.mCallback.end();
                    XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.END);
                } else {
                    XpLatencyPriorityHybridEngine.this.mCallback.error();
                    XpLatencyPriorityHybridEngine.this.uploadData(TtsModeType.OFFLINE, EngineEndType.ERROR);
                }
                stateExist();
            }
        }

        private void stateExist() {
            this.mIsStopped = false;
            this.mIsDataDone = false;
            this.mIsDataError = false;
            XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
            xpLatencyPriorityHybridEngine.setHybridState(xpLatencyPriorityHybridEngine.mIdleState);
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
            if (i == XpLatencyPriorityHybridEngine.MSG_START_OFFLINE_ENGINE) {
                XpLatencyPriorityHybridEngine.this.mHybridState.onEvent(EventType.EVENT_OFFLINE_START_DELAY_TIMEOUT, null);
            } else if (i == XpLatencyPriorityHybridEngine.MSG_WAIT_ONLINE_TIMEOUT) {
                XpLatencyPriorityHybridEngine.this.mHybridState.onEvent(EventType.EVENT_WAIT_ONLINE_DATA_TIMEOUT, null);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startOnlineData() {
        long duration = this.mOnlineParams.getLong("durationUs", 0L);
        if (duration != 0) {
            this.mCallback.uploadInfo(this.mOnlineParams);
        }
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
            LogUtils.v(XpLatencyPriorityHybridEngine.this.TAG, "readData +++");
            if (this.mEngine == null) {
                return;
            }
            int sampleRate = this.mParams.getInt("sampleRate");
            int bufferSize = AudioTrack.getMinBufferSize(sampleRate, 4, this.mParams.getInt("format"));
            if (bufferSize <= 0) {
                LogUtils.e(XpLatencyPriorityHybridEngine.this.TAG, "getMinBufferSize error, audioserver may be crash");
                XpLatencyPriorityHybridEngine.this.onHybridEvent(EventType.EVENT_DATA_ERROR, null);
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
                        XpLatencyPriorityHybridEngine xpLatencyPriorityHybridEngine = XpLatencyPriorityHybridEngine.this;
                        xpLatencyPriorityHybridEngine.mSendLatency = (int) (firstDataPkgRcvTime - xpLatencyPriorityHybridEngine.mStartTime);
                        XpLatencyPriorityHybridEngine.this.mTotalDataPkgSize = buffer.length;
                    } else {
                        long time = SystemClock.elapsedRealtime() - firstDataPkgRcvTime;
                        int delay = (int) (time - (XpLatencyPriorityHybridEngine.this.mTotalDataPkgSize / multipler));
                        if (XpLatencyPriorityHybridEngine.this.mDataPkgMaxLatency < delay) {
                            XpLatencyPriorityHybridEngine.this.mDataPkgMaxLatency = delay;
                        }
                        XpLatencyPriorityHybridEngine.access$1212(XpLatencyPriorityHybridEngine.this, buffer.length);
                    }
                    if (size < bufferSize) {
                        dataBuffer = new byte[size];
                        System.arraycopy(buffer, 0, dataBuffer, 0, size);
                    } else {
                        dataBuffer = buffer;
                    }
                    XpLatencyPriorityHybridEngine.this.mCallback.received(dataBuffer);
                }
                if (size < bufferSize) {
                    break;
                }
            } while (!this.mIsStopped);
            XpLatencyPriorityHybridEngine.this.onHybridEvent(EventType.EVENT_DATA_DONE, null);
            LogUtils.v(XpLatencyPriorityHybridEngine.this.TAG, "readData ---");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void uploadData(TtsModeType mode, EngineEndType state) {
        long endTime = System.currentTimeMillis();
        HybridUploadData data = new HybridUploadData(mode, this.mSendLatency, this.mDataPkgMaxLatency, this.mTotalDataPkgSize, state, endTime);
        UploadDataModel.getInstance().setHybridData(this.mUid, data);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onConfigChange(final DelayPriorityHybridConfig config) {
        String str = this.TAG;
        LogUtils.i(str, "onConfigChange " + config);
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpLatencyPriorityHybridEngine.4
            @Override // java.lang.Runnable
            public void run() {
                XpLatencyPriorityHybridEngine.this.mOfflineDelayStartLatency = config.offlineStartLatency;
                XpLatencyPriorityHybridEngine.this.mOnlineWaitTime = config.onlineWaitTime;
            }
        });
    }
}

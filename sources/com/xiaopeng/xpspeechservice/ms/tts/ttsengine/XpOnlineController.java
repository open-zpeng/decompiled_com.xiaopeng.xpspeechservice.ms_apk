package com.xiaopeng.xpspeechservice.ms.tts.ttsengine;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.ttscache.AsyncCacheMaker;
import com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpOnlineEngine;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.EngineEndType;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
/* loaded from: classes.dex */
public class XpOnlineController implements ITtsEngine {
    private static final int INTERVAL_PER_WORD = 500;
    private static final int INTERVAL_STATIC = 500;
    private static final int MAX_TIMEOUT_COUNT = 3;
    private static final int MSG_TIMEOUT = 101;
    private AsyncCacheMaker mAsyncCacheMaker;
    private ITtsEngineCallback mCallback;
    private String mChannelName;
    private DataState mDataState;
    private XpOnlineEngine mEngine;
    private EventHandler mEventHandler;
    private SynthState mSynthState;
    private String TAG = "XpOnlineController";
    private volatile int mTimeOutCnt = 0;
    private Bundle mSpeakItem = null;
    private Bundle mPendingSpeakItem = null;
    private boolean mIsPendingInit = false;
    private boolean mIsPendingShutdown = false;
    private boolean mIsDataReady = false;
    private Bundle mUploadInfo = null;
    private final SynthState mUnInitState = new UnInitState();
    private final SynthState mSynthIdleState = new SynthIdleState();
    private final SynthState mSynthPendingStartState = new SynthPendingStartState();
    private final SynthState mSynthStartInterruptedState = new SynthStartInterruptedState();
    private final SynthState mSynthingState = new SynthingState();
    private final SynthState mSynthStopState = new SynthStopState();
    private final SynthState mSynthEndState = new SynthEndState();
    private final SynthState mSynthErrorState = new SynthErrorState();
    private final SynthState mSynthDoneStopState = new SynthDoneStopState();
    private final SynthState mSynthingDataEndState = new SynthingDataEndState();
    private final SynthState mSynthingDataErrorState = new SynthingDataErrorState();
    private final SynthState mDataDoneSynthStopState = new DataDoneSynthStopState();
    private final DataState mDataIdleState = new DataIdleState();
    private final DataState mDataProcessingState = new DataProcessingState();
    private final DataState mDataStopState = new DataStopState();

    public XpOnlineController(String channelName, ITtsEngineCallback cb, Handler handler) {
        this.mChannelName = channelName;
        this.TAG += "_" + channelName;
        LogUtils.i(this.TAG, "construct");
        this.mCallback = cb;
        this.mEventHandler = new EventHandler(handler.getLooper());
        this.mAsyncCacheMaker = AsyncCacheMaker.getInstance();
        this.mSynthState = this.mUnInitState;
        this.mDataState = this.mDataIdleState;
    }

    public void init() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.1
            @Override // java.lang.Runnable
            public void run() {
                if (XpOnlineController.this.mIsPendingShutdown) {
                    XpOnlineController.this.mIsPendingShutdown = false;
                    LogUtils.i(XpOnlineController.this.TAG, "init: remove pending shutdown");
                    return;
                }
                LogUtils.i(XpOnlineController.this.TAG, "init at %s", XpOnlineController.this.mSynthState.getClass().getSimpleName());
                XpOnlineController.this.mSynthState.init();
            }
        });
    }

    public void shutdown() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.2
            @Override // java.lang.Runnable
            public void run() {
                if (XpOnlineController.this.mIsPendingInit) {
                    XpOnlineController.this.mIsPendingInit = false;
                    LogUtils.i(XpOnlineController.this.TAG, "shutdown: remove pending init");
                    return;
                }
                LogUtils.i(XpOnlineController.this.TAG, "shutdown at %s", XpOnlineController.this.mSynthState.getClass().getSimpleName());
                XpOnlineController.this.mSynthState.shutdown();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initInternal() {
        this.mEngine = new XpOnlineEngine(this.mChannelName, new MyEngineCallback());
        this.mEngine.initEngine();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void shutdownInternal() {
        this.mEngine.shutdown();
        this.mEngine = null;
        this.mPendingSpeakItem = null;
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine
    public void speak(final Bundle bundle) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.3
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpOnlineController.this.TAG, "speak at %s", XpOnlineController.this.mSynthState.getClass().getSimpleName());
                XpOnlineController.this.mSynthState.speak(bundle);
            }
        });
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine
    public void stop() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.4
            @Override // java.lang.Runnable
            public void run() {
                if (XpOnlineController.this.mPendingSpeakItem != null) {
                    XpOnlineController.this.mPendingSpeakItem = null;
                    LogUtils.i(XpOnlineController.this.TAG, "stop: remove pending speak item");
                    return;
                }
                LogUtils.i(XpOnlineController.this.TAG, "stop at %s", XpOnlineController.this.mSynthState.getClass().getSimpleName());
                XpOnlineController.this.mSynthState.stop();
            }
        });
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine
    public int getData(byte[] buffer) {
        return this.mEngine.getData(buffer);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void makeCacheAsync() {
        boolean isNeedCache = this.mSpeakItem.getBoolean("isNeedCache", true);
        if (!isNeedCache) {
            return;
        }
        Bundle item = new Bundle();
        item.putString("txt", this.mSpeakItem.getString("txt", ""));
        item.putString("source", this.mSpeakItem.getString("source", ""));
        this.mAsyncCacheMaker.makeCacheAsync(item);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSynthState(SynthState state) {
        LogUtils.i(this.TAG, "synth state change %s to %s", this.mSynthState.getClass().getSimpleName(), state.getClass().getSimpleName());
        this.mSynthState = state;
        if (this.mSynthState == this.mSynthIdleState) {
            this.mIsDataReady = false;
            this.mUploadInfo = null;
            clearTimeOut();
            if (this.mIsPendingShutdown) {
                this.mIsPendingShutdown = false;
                shutdownInternal();
                this.mSynthState = this.mUnInitState;
                return;
            }
            Bundle bundle = this.mPendingSpeakItem;
            if (bundle != null) {
                this.mSynthIdleState.speak(bundle);
                this.mPendingSpeakItem = null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSynthEvent(EventType event) {
        onSynthEvent(event, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSynthEvent(final EventType event, final Object arg) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.5
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpOnlineController.this.TAG, "onSynthEvent %s at %s", event.name(), XpOnlineController.this.mSynthState.getClass().getSimpleName());
                XpOnlineController.this.mSynthState.onEvent(event, arg);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SynthState {
        public SynthState() {
        }

        public void init() {
            String str = XpOnlineController.this.TAG;
            LogUtils.w(str, "Not handled init at " + getClass().getSimpleName());
            XpOnlineController.this.mIsPendingInit = true;
        }

        public void shutdown() {
            String str = XpOnlineController.this.TAG;
            LogUtils.w(str, "Not handled shutdown at " + getClass().getSimpleName());
            XpOnlineController.this.mIsPendingShutdown = true;
        }

        public void speak(Bundle bundle) {
            String str = XpOnlineController.this.TAG;
            LogUtils.w(str, "Not handled synth speak at " + getClass().getSimpleName());
            XpOnlineController.this.mPendingSpeakItem = bundle;
        }

        public void stop() {
            String str = XpOnlineController.this.TAG;
            LogUtils.w(str, "Not handled synth stop at " + getClass().getSimpleName());
        }

        public void onEvent(EventType event, Object arg) {
            LogUtils.w(XpOnlineController.this.TAG, "Not handled synth event %s at %s", event.name(), getClass().getSimpleName());
        }
    }

    /* loaded from: classes.dex */
    private class UnInitState extends SynthState {
        private UnInitState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void init() {
            XpOnlineController.this.initInternal();
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setSynthState(xpOnlineController.mSynthIdleState);
        }
    }

    /* loaded from: classes.dex */
    private class SynthIdleState extends SynthState {
        private SynthIdleState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void speak(Bundle params) {
            String txt = params.getString("txt");
            LogUtils.i(XpOnlineController.this.TAG, "onSynthesizeText %s length %d", txt, Integer.valueOf(txt.length()));
            UploadDataModel.getInstance().notifyOnlineSynthing(params.getString("uid"));
            XpOnlineController.this.mSpeakItem = params;
            XpOnlineController.this.mEngine.speak(params);
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setSynthState(xpOnlineController.mSynthPendingStartState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void shutdown() {
            XpOnlineController.this.shutdownInternal();
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setSynthState(xpOnlineController.mUnInitState);
        }
    }

    /* loaded from: classes.dex */
    private class SynthPendingStartState extends SynthState {
        private SynthPendingStartState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void stop() {
            XpOnlineController.this.mEngine.stop();
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setSynthState(xpOnlineController.mSynthStartInterruptedState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_SYNTH_BEGIN) {
                int maxWaitTime = ((Integer) arg).intValue();
                if (maxWaitTime > 0) {
                    XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_BEGIN, arg);
                }
                XpOnlineController.this.startData();
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthingState);
            } else if (event == EventType.EVENT_SYNTH_ERROR) {
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_ERROR);
                XpOnlineController.this.mEngine.UploadData((EngineEndType) arg);
                XpOnlineController.this.makeCacheAsync();
                XpOnlineController xpOnlineController2 = XpOnlineController.this;
                xpOnlineController2.setSynthState(xpOnlineController2.mSynthIdleState);
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class SynthStartInterruptedState extends SynthState {
        private SynthStartInterruptedState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_SYNTH_END || event == EventType.EVENT_SYNTH_ERROR) {
                XpOnlineController.this.mEngine.UploadData(EngineEndType.STOP);
                XpOnlineController.this.makeCacheAsync();
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthIdleState);
                return;
            }
            super.onEvent(event, arg);
        }
    }

    /* loaded from: classes.dex */
    private class SynthingState extends SynthState {
        private SynthingState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void stop() {
            XpOnlineController.this.mEngine.stop();
            XpOnlineController.this.stopData();
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setSynthState(xpOnlineController.mSynthStopState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_SYNTH_END) {
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_SYNTH_END);
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthEndState);
            } else if (event == EventType.EVENT_SYNTH_ERROR) {
                XpOnlineController.this.mEngine.UploadData((EngineEndType) arg);
                XpOnlineController xpOnlineController2 = XpOnlineController.this;
                xpOnlineController2.setSynthState(xpOnlineController2.mSynthErrorState);
            } else if (event == EventType.EVENT_DATA_READY) {
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_DATA_READY, arg);
                XpOnlineController.this.mIsDataReady = true;
            } else if (event == EventType.EVENT_DATA_END) {
                XpOnlineController xpOnlineController3 = XpOnlineController.this;
                xpOnlineController3.setSynthState(xpOnlineController3.mSynthingDataEndState);
            } else if (event == EventType.EVENT_DATA_ERROR) {
                XpOnlineController.this.mEngine.stop();
                XpOnlineController xpOnlineController4 = XpOnlineController.this;
                xpOnlineController4.setSynthState(xpOnlineController4.mSynthingDataErrorState);
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class SynthStopState extends SynthState {
        private boolean mIsSynthStopped;
        private boolean mIsWriteStopped;

        private SynthStopState() {
            super();
            this.mIsSynthStopped = false;
            this.mIsWriteStopped = false;
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_SYNTH_END || event == EventType.EVENT_SYNTH_ERROR) {
                this.mIsSynthStopped = true;
            } else if (event == EventType.EVENT_DATA_END) {
                this.mIsWriteStopped = true;
            } else {
                super.onEvent(event, arg);
            }
            if (this.mIsSynthStopped && this.mIsWriteStopped) {
                this.mIsSynthStopped = false;
                this.mIsWriteStopped = false;
                XpOnlineController.this.mEngine.UploadData(EngineEndType.STOP);
                XpOnlineController.this.makeCacheAsync();
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthIdleState);
            }
        }
    }

    /* loaded from: classes.dex */
    private class SynthEndState extends SynthState {
        private SynthEndState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void stop() {
            XpOnlineController.this.stopData();
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setSynthState(xpOnlineController.mSynthDoneStopState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_READY) {
                Bundle params = (Bundle) arg;
                if (XpOnlineController.this.mUploadInfo != null) {
                    params.putAll(XpOnlineController.this.mUploadInfo);
                } else {
                    XpOnlineController.this.mIsDataReady = true;
                }
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_DATA_READY, params);
            } else if (event == EventType.EVENT_DATA_END) {
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_END);
                XpOnlineController.this.mEngine.UploadData(EngineEndType.END);
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthIdleState);
            } else if (event == EventType.EVENT_DATA_ERROR) {
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_ERROR);
                XpOnlineController.this.mEngine.UploadData(EngineEndType.DECODE_ERROR);
                XpOnlineController xpOnlineController2 = XpOnlineController.this;
                xpOnlineController2.setSynthState(xpOnlineController2.mSynthIdleState);
            } else if (event == EventType.EVENT_UPLOAD_INFO) {
                if (XpOnlineController.this.mIsDataReady) {
                    XpOnlineController.this.mCallback.onEvent(event, arg);
                    return;
                }
                XpOnlineController.this.mUploadInfo = (Bundle) arg;
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class SynthErrorState extends SynthState {
        private SynthErrorState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void stop() {
            XpOnlineController.this.stopData();
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setSynthState(xpOnlineController.mSynthDoneStopState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_END || event == EventType.EVENT_DATA_ERROR) {
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_ERROR);
                XpOnlineController.this.makeCacheAsync();
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthIdleState);
                return;
            }
            super.onEvent(event, arg);
        }
    }

    /* loaded from: classes.dex */
    private class SynthDoneStopState extends SynthState {
        private SynthDoneStopState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_END) {
                XpOnlineController.this.mEngine.UploadData(EngineEndType.STOP);
                XpOnlineController.this.makeCacheAsync();
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthIdleState);
                return;
            }
            super.onEvent(event, arg);
        }
    }

    /* loaded from: classes.dex */
    private class SynthingDataEndState extends SynthState {
        private SynthingDataEndState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void stop() {
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setSynthState(xpOnlineController.mDataDoneSynthStopState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_SYNTH_END) {
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_SYNTH_END);
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_END);
                XpOnlineController.this.mEngine.UploadData(EngineEndType.END);
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthIdleState);
            } else if (event == EventType.EVENT_SYNTH_ERROR) {
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_ERROR);
                XpOnlineController.this.mEngine.UploadData((EngineEndType) arg);
                XpOnlineController.this.makeCacheAsync();
                XpOnlineController xpOnlineController2 = XpOnlineController.this;
                xpOnlineController2.setSynthState(xpOnlineController2.mSynthIdleState);
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class SynthingDataErrorState extends SynthState {
        private SynthingDataErrorState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void stop() {
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setSynthState(xpOnlineController.mDataDoneSynthStopState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_SYNTH_END || event == EventType.EVENT_SYNTH_ERROR) {
                XpOnlineController.this.mCallback.onEvent(EventType.EVENT_ONLINE_ERROR);
                XpOnlineController.this.mEngine.UploadData(EngineEndType.DECODE_ERROR);
                XpOnlineController.this.makeCacheAsync();
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthIdleState);
                return;
            }
            super.onEvent(event, arg);
        }
    }

    /* loaded from: classes.dex */
    private class DataDoneSynthStopState extends SynthState {
        private DataDoneSynthStopState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_SYNTH_END || event == EventType.EVENT_SYNTH_ERROR) {
                XpOnlineController.this.mEngine.UploadData(EngineEndType.STOP);
                XpOnlineController.this.makeCacheAsync();
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setSynthState(xpOnlineController.mSynthIdleState);
                return;
            }
            super.onEvent(event, arg);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MyEngineCallback implements ITtsEngineCallback {
        private MyEngineCallback() {
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback
        public void onEvent(EventType event) {
            onEvent(event, null);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback
        public void onEvent(EventType event, Object obj) {
            switch (event) {
                case EVENT_SYNTH_BEGIN:
                case EVENT_SYNTH_END:
                case EVENT_SYNTH_ERROR:
                case EVENT_UPLOAD_INFO:
                    XpOnlineController.this.onSynthEvent(event, obj);
                    return;
                case EVENT_DATA_READY:
                case EVENT_PROCESS_END:
                case EVENT_PROCESS_ERROR:
                    XpOnlineController.this.onDataEvent(event, obj);
                    return;
                default:
                    return;
            }
        }
    }

    private void clearTimeOut() {
        this.mEventHandler.removeMessages(MSG_TIMEOUT);
        this.mTimeOutCnt = 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == XpOnlineController.MSG_TIMEOUT) {
                LogUtils.w(XpOnlineController.this.TAG, "speak timeout");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startData() {
        LogUtils.i(this.TAG, "start data");
        this.mDataState.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopData() {
        LogUtils.i(this.TAG, "stop data");
        this.mDataState.stop();
    }

    private void onDataEvent(EventType event) {
        onDataEvent(event, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDataEvent(final EventType event, final Object arg) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.6
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpOnlineController.this.TAG, "onDataEvent %s at %s", event.name(), XpOnlineController.this.mDataState.getClass().getSimpleName());
                XpOnlineController.this.mDataState.onEvent(event, arg);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDataState(DataState state) {
        LogUtils.v(this.TAG, "data state change %s to %s", this.mDataState.getClass().getSimpleName(), state.getClass().getSimpleName());
        this.mDataState = state;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DataState {
        public DataState() {
        }

        public void start() {
            String str = XpOnlineController.this.TAG;
            LogUtils.w(str, "Not handled data start call at " + getClass().getSimpleName());
        }

        public void stop() {
            String str = XpOnlineController.this.TAG;
            LogUtils.w(str, "Not handled data stop call at " + getClass().getSimpleName());
        }

        public void onEvent(EventType event, Object arg) {
            LogUtils.w(XpOnlineController.this.TAG, "Not handled data event %s at %s", event.name(), getClass().getSimpleName());
        }
    }

    /* loaded from: classes.dex */
    private class DataIdleState extends DataState {
        private DataIdleState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.DataState
        public void start() {
            XpOnlineController.this.mEngine.startProcessor();
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setDataState(xpOnlineController.mDataProcessingState);
        }
    }

    /* loaded from: classes.dex */
    private class DataProcessingState extends DataState {
        private DataProcessingState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.DataState
        public void stop() {
            XpOnlineController.this.mEngine.stopProcessor();
            XpOnlineController xpOnlineController = XpOnlineController.this;
            xpOnlineController.setDataState(xpOnlineController.mDataStopState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.DataState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_READY) {
                XpOnlineController.this.onSynthEvent(EventType.EVENT_DATA_READY, arg);
            } else if (event == EventType.EVENT_PROCESS_END) {
                XpOnlineController.this.onSynthEvent(EventType.EVENT_DATA_END);
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setDataState(xpOnlineController.mDataIdleState);
            } else if (event == EventType.EVENT_PROCESS_ERROR) {
                XpOnlineController.this.onSynthEvent(EventType.EVENT_DATA_ERROR);
                XpOnlineController xpOnlineController2 = XpOnlineController.this;
                xpOnlineController2.setDataState(xpOnlineController2.mDataIdleState);
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class DataStopState extends DataState {
        private DataStopState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOnlineController.DataState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_PROCESS_END || event == EventType.EVENT_PROCESS_ERROR) {
                XpOnlineController.this.onSynthEvent(EventType.EVENT_DATA_END);
                XpOnlineController xpOnlineController = XpOnlineController.this;
                xpOnlineController.setDataState(xpOnlineController.mDataIdleState);
                return;
            }
            super.onEvent(event, arg);
        }
    }
}

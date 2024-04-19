package com.xiaopeng.xpspeechservice.ms.tts.ttsengine;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.ttsengine.msttsengine.XpMsTtsOfflineEngine;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.EngineEndType;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.OfflineUploadData;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
/* loaded from: classes.dex */
public class XpOfflineController implements ITtsEngine {
    private static final int INTERVAL_PER_WORD = 600;
    private static final int INTERVAL_STATIC = 800;
    private static final int MSG_TIMEOUT = 101;
    private static final int STOP_TIMEOUT_INTERVAL = 500;
    private ITtsEngineCallback mCallback;
    private XpMsTtsOfflineEngine mEngine;
    private EventHandler mEventHandler;
    private SynthState mSynthState;
    private String mUid;
    private String TAG = "XpOfflineController";
    private String mResultId = "";
    private Bundle mPendingSpeakItem = null;
    private boolean mIsPendingInit = false;
    private boolean mIsPendingShutdown = false;
    private volatile long mStartSpeakingTime = 0;
    private volatile long mSynthStartTime = 0;
    private volatile int mSynthTime = 0;
    private volatile int mFirstDataLatency = 0;
    private final SynthState mUnInitState = new UnInitState();
    private final SynthState mSynthIdleState = new SynthIdleState();
    private final SynthState mSynthingState = new SynthingState();
    private final SynthState mSynthStopState = new SynthStopState();

    public XpOfflineController(String channelName, ITtsEngineCallback cb, Handler handler) {
        this.TAG += "_" + channelName;
        LogUtils.i(this.TAG, "construct");
        this.mCallback = cb;
        this.mEventHandler = new EventHandler(handler.getLooper());
        this.mSynthState = this.mUnInitState;
        this.mEngine = new XpMsTtsOfflineEngine(channelName, new MyEngineCallback());
    }

    public void init() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.1
            @Override // java.lang.Runnable
            public void run() {
                if (XpOfflineController.this.mIsPendingShutdown) {
                    XpOfflineController.this.mIsPendingShutdown = false;
                    LogUtils.i(XpOfflineController.this.TAG, "init: remove pending shutdown");
                    return;
                }
                LogUtils.i(XpOfflineController.this.TAG, "init at %s", XpOfflineController.this.mSynthState.getClass().getSimpleName());
                XpOfflineController.this.mSynthState.init();
            }
        });
    }

    public void shutdown() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.2
            @Override // java.lang.Runnable
            public void run() {
                if (XpOfflineController.this.mIsPendingInit) {
                    XpOfflineController.this.mIsPendingInit = false;
                    LogUtils.i(XpOfflineController.this.TAG, "shutdown: remove pending init");
                    return;
                }
                LogUtils.i(XpOfflineController.this.TAG, "shutdown at %s", XpOfflineController.this.mSynthState.getClass().getSimpleName());
                XpOfflineController.this.mSynthState.shutdown();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initInternal() {
        this.mEngine.initEngine();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void shutdownInternal() {
        this.mEngine.destroy();
        this.mPendingSpeakItem = null;
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine
    public void speak(final Bundle bundle) {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.3
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpOfflineController.this.TAG, "speak at %s", XpOfflineController.this.mSynthState.getClass().getSimpleName());
                XpOfflineController.this.mSynthState.speak(bundle);
            }
        });
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine
    public void stop() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.4
            @Override // java.lang.Runnable
            public void run() {
                if (XpOfflineController.this.mPendingSpeakItem != null) {
                    XpOfflineController.this.mPendingSpeakItem = null;
                    LogUtils.i(XpOfflineController.this.TAG, "stop: remove pending speak item");
                    return;
                }
                LogUtils.i(XpOfflineController.this.TAG, "stop at %s", XpOfflineController.this.mSynthState.getClass().getSimpleName());
                XpOfflineController.this.mSynthState.stop();
            }
        });
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine
    public int getData(byte[] buffer) {
        return this.mEngine.getData(buffer);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSynthState(SynthState state) {
        LogUtils.i(this.TAG, "synth state change %s to %s", this.mSynthState.getClass().getSimpleName(), state.getClass().getSimpleName());
        this.mSynthState = state;
        if (this.mSynthState == this.mSynthIdleState) {
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

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController$6  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$com$xiaopeng$xpspeechservice$ms$tts$EventType = new int[EventType.values().length];

        static {
            try {
                $SwitchMap$com$xiaopeng$xpspeechservice$ms$tts$EventType[EventType.EVENT_SYNTH_BEGIN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$xiaopeng$xpspeechservice$ms$tts$EventType[EventType.EVENT_DATA_READY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$xiaopeng$xpspeechservice$ms$tts$EventType[EventType.EVENT_SYNTH_END.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$xiaopeng$xpspeechservice$ms$tts$EventType[EventType.EVENT_SYNTH_ERROR.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSynthEvent(final EventType event, final Object arg) {
        int i = AnonymousClass6.$SwitchMap$com$xiaopeng$xpspeechservice$ms$tts$EventType[event.ordinal()];
        if (i == 1) {
            this.mSynthStartTime = SystemClock.elapsedRealtime();
        } else if (i == 2) {
            this.mFirstDataLatency = (int) (SystemClock.elapsedRealtime() - this.mStartSpeakingTime);
        } else if (i == 3 || i == 4) {
            this.mSynthTime = (int) (SystemClock.elapsedRealtime() - this.mSynthStartTime);
        }
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.5
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpOfflineController.this.TAG, "onSynthEvent %s at %s", event.name(), XpOfflineController.this.mSynthState.getClass().getSimpleName());
                String resultId = null;
                int i2 = AnonymousClass6.$SwitchMap$com$xiaopeng$xpspeechservice$ms$tts$EventType[event.ordinal()];
                if (i2 != 1) {
                    if (i2 == 2 || i2 == 3) {
                        resultId = ((Bundle) arg).getString("resultId", "");
                    } else if (i2 == 4) {
                        resultId = (String) arg;
                    }
                    if (!XpOfflineController.this.mResultId.equals(resultId)) {
                        LogUtils.w(XpOfflineController.this.TAG, "resultId not match %s vs %s", resultId, XpOfflineController.this.mResultId);
                        return;
                    } else {
                        XpOfflineController.this.mSynthState.onEvent(event, arg);
                        return;
                    }
                }
                XpOfflineController.this.mResultId = (String) arg;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SynthState {
        public SynthState() {
        }

        public void init() {
            String str = XpOfflineController.this.TAG;
            LogUtils.w(str, "Not handled init at " + getClass().getSimpleName());
            XpOfflineController.this.mIsPendingInit = true;
        }

        public void shutdown() {
            String str = XpOfflineController.this.TAG;
            LogUtils.w(str, "Not handled shutdown at " + getClass().getSimpleName());
            XpOfflineController.this.mIsPendingShutdown = true;
        }

        public void speak(Bundle bundle) {
            String str = XpOfflineController.this.TAG;
            LogUtils.w(str, "Not handled synth speak at " + getClass().getSimpleName());
            XpOfflineController.this.mPendingSpeakItem = bundle;
        }

        public void stop() {
            String str = XpOfflineController.this.TAG;
            LogUtils.w(str, "Not handled synth stop at " + getClass().getSimpleName());
        }

        public void onEvent(EventType event, Object arg) {
            LogUtils.w(XpOfflineController.this.TAG, "Not handled synth event %s at %s", event.name(), getClass().getSimpleName());
        }
    }

    /* loaded from: classes.dex */
    private class UnInitState extends SynthState {
        private UnInitState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.SynthState
        public void init() {
            XpOfflineController.this.initInternal();
            XpOfflineController xpOfflineController = XpOfflineController.this;
            xpOfflineController.setSynthState(xpOfflineController.mSynthIdleState);
        }
    }

    /* loaded from: classes.dex */
    private class SynthIdleState extends SynthState {
        private SynthIdleState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.SynthState
        public void speak(Bundle params) {
            String txt = params.getString("txt");
            LogUtils.i(XpOfflineController.this.TAG, "onSynthesizeText %s length %d", txt, Integer.valueOf(txt.length()));
            XpOfflineController.this.mUid = params.getString("uid");
            UploadDataModel.getInstance().notifyOfflineSynthing(XpOfflineController.this.mUid);
            int rate = params.getInt("rate", 100);
            int timeOut = (((txt.length() * XpOfflineController.INTERVAL_PER_WORD) * 100) / rate) + XpOfflineController.INTERVAL_STATIC;
            XpOfflineController.this.mEventHandler.sendEmptyMessageDelayed(XpOfflineController.MSG_TIMEOUT, timeOut);
            XpOfflineController.this.mFirstDataLatency = 0;
            XpOfflineController.this.mSynthTime = 0;
            XpOfflineController.this.mStartSpeakingTime = SystemClock.elapsedRealtime();
            XpOfflineController.this.mEngine.speak(params);
            XpOfflineController xpOfflineController = XpOfflineController.this;
            xpOfflineController.setSynthState(xpOfflineController.mSynthingState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.SynthState
        public void shutdown() {
            XpOfflineController.this.shutdownInternal();
            XpOfflineController xpOfflineController = XpOfflineController.this;
            xpOfflineController.setSynthState(xpOfflineController.mUnInitState);
        }
    }

    /* loaded from: classes.dex */
    private class SynthingState extends SynthState {
        private SynthingState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.SynthState
        public void stop() {
            XpOfflineController.this.mEngine.stop();
            XpOfflineController.this.mEventHandler.removeMessages(XpOfflineController.MSG_TIMEOUT);
            XpOfflineController.this.mEventHandler.sendEmptyMessageDelayed(XpOfflineController.MSG_TIMEOUT, 500L);
            XpOfflineController xpOfflineController = XpOfflineController.this;
            xpOfflineController.setSynthState(xpOfflineController.mSynthStopState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_READY) {
                XpOfflineController.this.mCallback.onEvent(EventType.EVENT_OFFLINE_DATA_READY, arg);
            } else if (event == EventType.EVENT_SYNTH_ERROR) {
                XpOfflineController.this.mCallback.onEvent(EventType.EVENT_OFFLINE_ERROR);
                XpOfflineController.this.uploadData(EngineEndType.ERROR);
                XpOfflineController xpOfflineController = XpOfflineController.this;
                xpOfflineController.setSynthState(xpOfflineController.mSynthIdleState);
            } else if (event == EventType.EVENT_SYNTH_END) {
                XpOfflineController.this.mCallback.onEvent(EventType.EVENT_OFFLINE_END, arg);
                XpOfflineController.this.uploadData(EngineEndType.END);
                XpOfflineController xpOfflineController2 = XpOfflineController.this;
                xpOfflineController2.setSynthState(xpOfflineController2.mSynthIdleState);
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class SynthStopState extends SynthState {
        private SynthStopState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpOfflineController.SynthState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_SYNTH_END || event == EventType.EVENT_SYNTH_ERROR) {
                XpOfflineController.this.uploadData(EngineEndType.STOP);
                XpOfflineController xpOfflineController = XpOfflineController.this;
                xpOfflineController.setSynthState(xpOfflineController.mSynthIdleState);
                return;
            }
            super.onEvent(event, arg);
        }
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
            XpOfflineController.this.onSynthEvent(event, obj);
        }
    }

    private void clearTimeOut() {
        this.mEventHandler.removeMessages(MSG_TIMEOUT);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void uploadData(EngineEndType state) {
        OfflineUploadData data = new OfflineUploadData(this.mFirstDataLatency, this.mSynthTime, state);
        UploadDataModel.getInstance().setOfflineData(this.mUid, data);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == XpOfflineController.MSG_TIMEOUT) {
                LogUtils.w(XpOfflineController.this.TAG, "tts timeout");
                if (XpOfflineController.this.mSynthState == XpOfflineController.this.mSynthStopState) {
                    XpOfflineController.this.uploadData(EngineEndType.STOP);
                } else {
                    XpOfflineController.this.mCallback.onEvent(EventType.EVENT_OFFLINE_ERROR);
                    XpOfflineController.this.uploadData(EngineEndType.TIMEOUT);
                    if (!XpOfflineController.this.mEngine.isDataStreamEnd()) {
                        XpOfflineController.this.mEngine.reset();
                    }
                }
                XpOfflineController xpOfflineController = XpOfflineController.this;
                xpOfflineController.setSynthState(xpOfflineController.mSynthIdleState);
            }
        }
    }
}

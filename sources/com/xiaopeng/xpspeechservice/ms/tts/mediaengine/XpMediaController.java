package com.xiaopeng.xpspeechservice.ms.tts.mediaengine;

import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import com.xiaopeng.lib.framework.moduleinterface.carcontroller.IInputController;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.EngineEndType;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.MediaUploadData;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.TtsModeType;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
/* loaded from: classes.dex */
public class XpMediaController {
    private IEngineCallback mCallback;
    private String mChannelName;
    private Handler mDataHandler;
    private HandlerThread mDataThread;
    private DataWriter mDataWriter;
    private EventHandler mEventHandler;
    private HandlerThread mEventThread;
    private XpMediaDecoder mMediaDecoder;
    private MediaState mMediaState;
    private volatile TtsModeType mMode;
    private PendingItem mPendingItem;
    private String mUid;
    private String TAG = "XpMediaController";
    private boolean mIsPendingInit = false;
    private boolean mIsPendingShutdown = false;
    private volatile long mStartTime = 0;
    private volatile int mDecodeLatency = 0;
    private volatile int mSendLatency = 0;
    private volatile int mDataPkgMaxLatency = 0;
    private volatile int mTotalDataPkgSize = 0;
    private final MediaState mUnInitState = new UnInitState();
    private final MediaState mMediaIdleState = new MediaIdleState();
    private final MediaState mMediaPendingStartState = new MediaPendingStartState();
    private final MediaState mMediaStartInterruptedState = new MediaStartInterruptedState();
    private final MediaState mMediaProcessingState = new MediaProcessingState();

    static /* synthetic */ int access$1712(XpMediaController x0, int x1) {
        int i = x0.mTotalDataPkgSize + x1;
        x0.mTotalDataPkgSize = i;
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PendingItem {
        public IEngineCallback cb;
        public Object obj;
        public Bundle params;
        public SourceType type;

        public PendingItem(SourceType type, Bundle params, Object obj, IEngineCallback cb) {
            this.type = type;
            this.params = params;
            this.obj = obj;
            this.cb = cb;
        }
    }

    public XpMediaController(String channelName) {
        this.mChannelName = channelName;
        this.TAG += "_" + channelName;
        this.mEventThread = new HandlerThread("MediaEventThread_" + channelName);
        this.mEventThread.start();
        this.mEventHandler = new EventHandler(this.mEventThread.getLooper());
        this.mDataThread = new HandlerThread("MediaDataThread_" + channelName);
        this.mDataThread.start();
        this.mDataHandler = new Handler(this.mDataThread.getLooper());
        this.mMediaState = this.mUnInitState;
    }

    public void init() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.1
            @Override // java.lang.Runnable
            public void run() {
                if (XpMediaController.this.mIsPendingShutdown) {
                    XpMediaController.this.mIsPendingShutdown = false;
                    LogUtils.i(XpMediaController.this.TAG, "init: remove pending shutdown");
                    return;
                }
                LogUtils.i(XpMediaController.this.TAG, "init at %s", XpMediaController.this.mMediaState.getClass().getSimpleName());
                XpMediaController.this.mMediaState.init();
            }
        });
    }

    public void shutdown() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.2
            @Override // java.lang.Runnable
            public void run() {
                if (XpMediaController.this.mIsPendingInit) {
                    XpMediaController.this.mIsPendingInit = false;
                    LogUtils.i(XpMediaController.this.TAG, "shutdown: remove pending init");
                    return;
                }
                LogUtils.i(XpMediaController.this.TAG, "shutdown at %s", XpMediaController.this.mMediaState.getClass().getSimpleName());
                XpMediaController.this.mMediaState.shutdown();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initInternal() {
        this.mDataWriter = new DataWriter();
        this.mMediaDecoder = new XpMediaDecoder(this.mChannelName, new MyEngineCallback());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void shutdownInternal() {
        this.mMediaDecoder.destroy();
        this.mMediaDecoder = null;
        this.mDataWriter = null;
        this.mPendingItem = null;
        this.mCallback = null;
    }

    public void start(final SourceType type, final Bundle params, final Object obj, final IEngineCallback cb) {
        if (type == SourceType.SOURCE_BUFFER) {
            this.mMode = TtsModeType.PROMPT;
        } else if (type == SourceType.SOURCE_PATH) {
            this.mMode = TtsModeType.CACHE;
        } else if (type == SourceType.SOURCE_URI) {
            this.mMode = TtsModeType.HTTP_MEDIA;
        }
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.3
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpMediaController.this.TAG, "speak");
                XpMediaController.this.mMediaState.start(type, params, obj, cb);
            }
        });
    }

    public void stop() {
        this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.4
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.i(XpMediaController.this.TAG, "stop");
                if (XpMediaController.this.mPendingItem != null) {
                    XpMediaController.this.mPendingItem = null;
                    LogUtils.v(XpMediaController.this.TAG, "remove pending item");
                    return;
                }
                XpMediaController.this.mMediaState.stop();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onMediaEvent(EventType event) {
        onMediaEvent(event, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onMediaEvent(final EventType event, final Object arg) {
        if (event == EventType.EVENT_DATA_READY) {
            this.mDecodeLatency = (int) (SystemClock.elapsedRealtime() - this.mStartTime);
        } else if (event == EventType.EVENT_UPLOAD_INFO) {
            this.mCallback.uploadInfo((Bundle) arg);
        } else {
            this.mEventHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.5
                @Override // java.lang.Runnable
                public void run() {
                    LogUtils.i(XpMediaController.this.TAG, "onMediaEvent %s at %s", event.name(), XpMediaController.this.mMediaState.getClass().getSimpleName());
                    XpMediaController.this.mMediaState.onEvent(event, arg);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setMediaState(MediaState state) {
        LogUtils.i(this.TAG, "synth state change %s to %s", this.mMediaState.getClass().getSimpleName(), state.getClass().getSimpleName());
        this.mMediaState = state;
        MediaState mediaState = this.mMediaState;
        MediaState mediaState2 = this.mMediaIdleState;
        if (mediaState == mediaState2) {
            if (this.mIsPendingShutdown) {
                this.mIsPendingShutdown = false;
                shutdownInternal();
                this.mMediaState = this.mUnInitState;
                return;
            }
            PendingItem pendingItem = this.mPendingItem;
            if (pendingItem != null) {
                mediaState2.start(pendingItem.type, this.mPendingItem.params, this.mPendingItem.obj, this.mPendingItem.cb);
                this.mPendingItem = null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MediaState {
        public MediaState() {
        }

        public void init() {
            String str = XpMediaController.this.TAG;
            LogUtils.w(str, "Not handled init call at " + getClass().getSimpleName());
            XpMediaController.this.mIsPendingInit = true;
        }

        public void shutdown() {
            String str = XpMediaController.this.TAG;
            LogUtils.w(str, "Not handled shutdown call at " + getClass().getSimpleName());
            XpMediaController.this.mIsPendingShutdown = true;
        }

        public void start(SourceType type, Bundle params, Object obj, IEngineCallback cb) {
            String str = XpMediaController.this.TAG;
            LogUtils.w(str, "Not handled synth speak call at " + getClass().getSimpleName());
            XpMediaController xpMediaController = XpMediaController.this;
            xpMediaController.mPendingItem = new PendingItem(type, params, obj, cb);
        }

        public void stop() {
            String str = XpMediaController.this.TAG;
            LogUtils.w(str, "Not handled synth stop call at " + getClass().getSimpleName());
        }

        public void onEvent(EventType event, Object arg) {
            LogUtils.w(XpMediaController.this.TAG, "Not handled synth event %s at %s", event.name(), getClass().getSimpleName());
        }
    }

    /* loaded from: classes.dex */
    private class UnInitState extends MediaState {
        private UnInitState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.MediaState
        public void init() {
            XpMediaController.this.initInternal();
            XpMediaController xpMediaController = XpMediaController.this;
            xpMediaController.setMediaState(xpMediaController.mMediaIdleState);
        }
    }

    /* loaded from: classes.dex */
    private class MediaIdleState extends MediaState {
        private MediaIdleState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.MediaState
        public void start(SourceType type, Bundle params, Object obj, IEngineCallback cb) {
            XpMediaController.this.mCallback = cb;
            XpMediaController.this.mUid = params.getString("uid");
            XpMediaController.this.mStartTime = SystemClock.elapsedRealtime();
            XpMediaController.this.mDecodeLatency = 0;
            XpMediaController.this.mSendLatency = 0;
            XpMediaController.this.mDataPkgMaxLatency = 0;
            XpMediaController.this.mTotalDataPkgSize = 0;
            XpMediaController.this.mMediaDecoder.start(type, obj);
            XpMediaController xpMediaController = XpMediaController.this;
            xpMediaController.setMediaState(xpMediaController.mMediaPendingStartState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.MediaState
        public void shutdown() {
            XpMediaController.this.shutdownInternal();
            XpMediaController xpMediaController = XpMediaController.this;
            xpMediaController.setMediaState(xpMediaController.mUnInitState);
        }
    }

    /* loaded from: classes.dex */
    private class MediaPendingStartState extends MediaState {
        private MediaPendingStartState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.MediaState
        public void stop() {
            XpMediaController.this.mMediaDecoder.stop();
            XpMediaController xpMediaController = XpMediaController.this;
            xpMediaController.setMediaState(xpMediaController.mMediaStartInterruptedState);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.MediaState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_PROCESS_BEGIN) {
                Bundle bundle = (Bundle) arg;
                XpMediaController.this.mCallback.begin(bundle.getInt("sampleRate"), bundle.getInt("format"), bundle.getInt("channelCount"));
                XpMediaController.this.mDataWriter.setParams(bundle);
                XpMediaController.this.mDataHandler.post(XpMediaController.this.mDataWriter);
                XpMediaController xpMediaController = XpMediaController.this;
                xpMediaController.setMediaState(xpMediaController.mMediaProcessingState);
            } else if (event == EventType.EVENT_PROCESS_ERROR) {
                XpMediaController.this.mCallback.error();
                XpMediaController.this.uploadData(EngineEndType.ERROR);
                XpMediaController xpMediaController2 = XpMediaController.this;
                xpMediaController2.setMediaState(xpMediaController2.mMediaIdleState);
            } else {
                super.onEvent(event, arg);
            }
        }
    }

    /* loaded from: classes.dex */
    private class MediaStartInterruptedState extends MediaState {
        private MediaStartInterruptedState() {
            super();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.MediaState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_PROCESS_END || event == EventType.EVENT_PROCESS_ERROR) {
                XpMediaController.this.uploadData(EngineEndType.STOP);
                XpMediaController xpMediaController = XpMediaController.this;
                xpMediaController.setMediaState(xpMediaController.mMediaIdleState);
                return;
            }
            super.onEvent(event, arg);
        }
    }

    /* loaded from: classes.dex */
    private class MediaProcessingState extends MediaState {
        private boolean mIsDataDone;
        private boolean mIsDataError;
        private boolean mIsEnd;
        private boolean mIsError;
        private boolean mIsStopped;

        private MediaProcessingState() {
            super();
            this.mIsStopped = false;
            this.mIsEnd = false;
            this.mIsError = false;
            this.mIsDataDone = false;
            this.mIsDataError = false;
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.MediaState
        public void stop() {
            XpMediaController.this.mMediaDecoder.stop();
            XpMediaController.this.mDataWriter.stop();
            this.mIsStopped = true;
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaController.MediaState
        public void onEvent(EventType event, Object arg) {
            if (event == EventType.EVENT_DATA_DONE) {
                this.mIsDataDone = true;
            } else if (event == EventType.EVENT_DATA_ERROR) {
                this.mIsDataError = true;
            } else if (event == EventType.EVENT_PROCESS_END) {
                this.mIsEnd = true;
            } else if (event == EventType.EVENT_PROCESS_ERROR) {
                this.mIsError = true;
            } else {
                super.onEvent(event, arg);
            }
            if (this.mIsDataDone || this.mIsDataError) {
                if (this.mIsEnd || this.mIsError) {
                    if (this.mIsStopped) {
                        XpMediaController.this.uploadData(EngineEndType.STOP);
                    } else if (!this.mIsDataDone || !this.mIsEnd) {
                        XpMediaController.this.mCallback.error();
                        XpMediaController.this.uploadData(EngineEndType.ERROR);
                    } else {
                        XpMediaController.this.mCallback.end();
                        XpMediaController.this.uploadData(EngineEndType.END);
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
            XpMediaController xpMediaController = XpMediaController.this;
            xpMediaController.setMediaState(xpMediaController.mMediaIdleState);
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
            XpMediaController.this.onMediaEvent(event, obj);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DataWriter implements Runnable {
        private volatile Bundle mParams = null;
        private volatile boolean mIsStopped = false;

        public DataWriter() {
        }

        public void setParams(Bundle params) {
            this.mParams = params;
            this.mIsStopped = false;
        }

        public void stop() {
            this.mIsStopped = true;
        }

        @Override // java.lang.Runnable
        public void run() {
            int multipler;
            int multipler2;
            byte[] dataBuffer;
            LogUtils.v(XpMediaController.this.TAG, "readData +++");
            int sampleRate = this.mParams.getInt("sampleRate");
            int format = this.mParams.getInt("format");
            int channelCount = this.mParams.getInt("channelCount");
            int channelConfig = channelCount == 1 ? 4 : 12;
            int bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, format);
            int i = 0;
            LogUtils.v(XpMediaController.this.TAG, "sampleRate %d format %d channelCount %d bufferSize %d", Integer.valueOf(sampleRate), Integer.valueOf(format), Integer.valueOf(channelCount), Integer.valueOf(bufferSize));
            if (bufferSize <= 0) {
                LogUtils.e(XpMediaController.this.TAG, "getMinBufferSize error, audioserver may be crash");
                XpMediaController.this.onMediaEvent(EventType.EVENT_DATA_ERROR);
                return;
            }
            byte[] buffer = new byte[bufferSize];
            boolean isFirstData = true;
            long firstDataPkgRcvTime = 0;
            int multipler3 = sampleRate / IInputController.KEYCODE_KNOB_WIND_SPD_UP;
            while (true) {
                int size = XpMediaController.this.mMediaDecoder.getData(buffer);
                if (size <= 0) {
                    multipler = multipler3;
                    multipler2 = i;
                } else {
                    if (isFirstData) {
                        String str = XpMediaController.this.TAG;
                        LogUtils.v(str, "first data size " + size);
                        isFirstData = false;
                        firstDataPkgRcvTime = SystemClock.elapsedRealtime();
                        XpMediaController xpMediaController = XpMediaController.this;
                        xpMediaController.mSendLatency = (int) (firstDataPkgRcvTime - xpMediaController.mStartTime);
                        XpMediaController.this.mTotalDataPkgSize = buffer.length;
                        multipler = multipler3;
                    } else {
                        long time = SystemClock.elapsedRealtime() - firstDataPkgRcvTime;
                        multipler = multipler3;
                        int delay = (int) (time - (XpMediaController.this.mTotalDataPkgSize / multipler3));
                        if (XpMediaController.this.mDataPkgMaxLatency < delay) {
                            XpMediaController.this.mDataPkgMaxLatency = delay;
                        }
                        XpMediaController.access$1712(XpMediaController.this, buffer.length);
                    }
                    if (size < bufferSize) {
                        dataBuffer = new byte[size];
                        multipler2 = 0;
                        System.arraycopy(buffer, 0, dataBuffer, 0, size);
                    } else {
                        multipler2 = 0;
                        dataBuffer = buffer;
                    }
                    XpMediaController.this.mCallback.received(dataBuffer);
                }
                if (size < bufferSize || this.mIsStopped) {
                    break;
                }
                i = multipler2;
                multipler3 = multipler;
            }
            XpMediaController.this.onMediaEvent(EventType.EVENT_DATA_DONE);
            LogUtils.v(XpMediaController.this.TAG, "readData ---");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void uploadData(EngineEndType state) {
        long endTime = System.currentTimeMillis();
        MediaUploadData data = new MediaUploadData(this.mMode, this.mDecodeLatency, this.mSendLatency, this.mDataPkgMaxLatency, this.mTotalDataPkgSize, state, endTime);
        UploadDataModel.getInstance().setMediaData(this.mUid, data);
    }
}

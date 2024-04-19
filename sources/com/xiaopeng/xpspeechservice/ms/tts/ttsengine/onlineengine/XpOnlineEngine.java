package com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import com.lzy.okgo.model.HttpParams;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import com.xiaopeng.xpspeechservice.ms.bean.CarSpeed;
import com.xiaopeng.xpspeechservice.ms.bean.PowerState;
import com.xiaopeng.xpspeechservice.ms.bean.WsConnectState;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.config.OnlineEngineConfig;
import com.xiaopeng.xpspeechservice.ms.tts.ttscache.XpTtsCache;
import com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IDataCallback;
import com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpMp3Decoder;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.EngineEndType;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.OnlineUploadData;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel;
import com.xiaopeng.xpspeechservice.utils.AudioBuffer;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: classes.dex */
public class XpOnlineEngine {
    private static final int CODE_END = 200;
    private static final int CODE_STOP = 1002;
    private static final int MSG_INIT = 101;
    private static final String MSG_RCV_TYPE_END = "ttsEnd";
    private static final String MSG_RCV_TYPE_START = "ttsStart";
    private static final int MSG_TIMEOUT = 102;
    private static final String MSG_TYPE = "tts";
    private static final String REASON_SOCKET_TIMEOUT = "socket_timeout";
    private static final String REASON_SPEAK_TIMEOUT = "speak_timeout";
    private static final String TYPE_SSML = "ssml";
    private static final String TYPE_TEXT = "text";
    private AudioBuffer mAudioBuffer;
    private volatile ITtsEngineCallback mCallback;
    private String mChannelName;
    private MyTtsDataCallback mDataCallback;
    private MyHandler mHandler;
    private volatile String mJson;
    private volatile String mSourceType;
    private HandlerThread mThread;
    private XpTtsCache mTtsCache;
    private volatile String mTxt;
    private volatile String mUid;
    private XpMp3Decoder mXpMp3Decoder;
    private XpWebSocketHelper mXpWebSocketHelper;
    private String TAG = "XpOnlineEngine";
    private volatile int mTimeOutIntervalMs = 5000;
    private volatile int mDataLengthLimit = 3000;
    private volatile int mPreCacheSize = 200;
    private volatile long mStartTime = 0;
    private boolean mIsFirstData = false;
    private volatile int mFirstDataLatency = 0;
    private long mFirstDataPkgRcvTime = 0;
    private volatile int mTotalDataPkgSize = 0;
    private volatile int mDataPkgMaxLatency = 0;
    private volatile int mTotalPkgTime = 0;
    private volatile int mDecodeLatency = 0;
    private volatile boolean mIsWsConnected = false;
    private volatile boolean mIsDataCompleted = false;
    private volatile boolean mIsShutDown = false;
    private volatile boolean mIsPowerOn = true;
    private volatile int mCarSpeed = 0;

    public XpOnlineEngine(String channelName, ITtsEngineCallback cb) {
        this.mChannelName = channelName;
        this.TAG += "_" + channelName;
        LogUtils.i(this.TAG, "XpOnlineEngine construct");
        this.mCallback = cb;
        this.mDataCallback = new MyTtsDataCallback(cb);
        this.mTtsCache = XpTtsCache.getInstance();
        this.mThread = new HandlerThread("XpOnlineEngine");
        this.mThread.start();
        this.mHandler = new MyHandler(this.mThread.getLooper());
    }

    public void initEngine() {
        LogUtils.d(this.TAG, "initEngine");
        EventBus.getDefault().register(this);
        this.mXpMp3Decoder = new XpMp3Decoder(this.mChannelName, this.mDataCallback, 24000);
        this.mXpWebSocketHelper = new XpWebSocketHelper(this.mChannelName);
        XpWebSocketHelper xpWebSocketHelper = this.mXpWebSocketHelper;
        if (xpWebSocketHelper != null) {
            xpWebSocketHelper.init(new IWebSocketListener() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.XpOnlineEngine.1
                @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.IWebSocketListener
                public void onOpen() {
                    LogUtils.i(XpOnlineEngine.this.TAG, "WebSocketListener onOpen");
                    XpOnlineEngine.this.mIsWsConnected = true;
                    EventBus.getDefault().post(new WsConnectState(1));
                }

                @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.IWebSocketListener
                public void onClosed(int code, String reason) {
                    String str = XpOnlineEngine.this.TAG;
                    LogUtils.i(str, "WebSocketListener onClosed: " + reason);
                    EventBus.getDefault().post(new WsConnectState(0));
                    if (!XpOnlineEngine.this.mIsShutDown && XpOnlineEngine.this.mIsPowerOn) {
                        XpOnlineEngine.this.mIsWsConnected = false;
                        int delay = 5000;
                        delay = (XpOnlineEngine.REASON_SPEAK_TIMEOUT.equals(reason) || XpOnlineEngine.REASON_SOCKET_TIMEOUT.equals(reason)) ? 200 : 200;
                        String str2 = XpOnlineEngine.this.TAG;
                        LogUtils.i(str2, "reconnect delay " + delay);
                        if (XpOnlineEngine.this.mXpWebSocketHelper != null) {
                            XpOnlineEngine.this.mXpWebSocketHelper.connectSocket(delay);
                        }
                    }
                }

                @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.IWebSocketListener
                public void onMessage(String msg) {
                    if (msg != null) {
                        String str = XpOnlineEngine.this.TAG;
                        LogUtils.i(str, "received: " + msg);
                        XpOnlineEngine.this.onMsgRcv(msg);
                        return;
                    }
                    LogUtils.w(XpOnlineEngine.this.TAG, "null response msg received");
                }

                @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.IWebSocketListener
                public void onReceive(byte[] buffer) {
                    XpOnlineEngine.this.onDataRcv(buffer);
                }

                @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.onlineengine.IWebSocketListener
                public void onError(Throwable e) {
                    LogUtils.e(XpOnlineEngine.this.TAG, "onError");
                    EventBus.getDefault().post(new WsConnectState(0));
                    if (!XpOnlineEngine.this.mIsShutDown) {
                        XpOnlineEngine.this.watchDogClear();
                        if (XpOnlineEngine.this.mXpWebSocketHelper.checkUrlChanged()) {
                            LogUtils.i(XpOnlineEngine.this.TAG, "url changed");
                            System.exit(0);
                        }
                        if (XpOnlineEngine.this.mAudioBuffer != null) {
                            XpOnlineEngine.this.mAudioBuffer.writeDone();
                        }
                        if (XpOnlineEngine.this.mCallback != null) {
                            XpOnlineEngine.this.mCallback.onEvent(EventType.EVENT_SYNTH_ERROR, EngineEndType.NETWORK_ERROR);
                        }
                        if (e instanceof SocketTimeoutException) {
                            LogUtils.i(XpOnlineEngine.this.TAG, "socket timeout exception");
                            if (XpOnlineEngine.this.mXpWebSocketHelper != null) {
                                XpOnlineEngine.this.mXpWebSocketHelper.forceCloseWebSocket(XpOnlineEngine.REASON_SOCKET_TIMEOUT);
                            }
                        } else if (e instanceof ConnectException) {
                            LogUtils.i(XpOnlineEngine.this.TAG, "connect exception");
                            if (XpOnlineEngine.this.mXpWebSocketHelper != null) {
                                XpOnlineEngine.this.mXpWebSocketHelper.forceCloseWebSocket("connect exception");
                            }
                        } else if (e instanceof SocketException) {
                            LogUtils.i(XpOnlineEngine.this.TAG, "socket exception");
                            if (XpOnlineEngine.this.mXpWebSocketHelper != null) {
                                XpOnlineEngine.this.mXpWebSocketHelper.forceCloseWebSocket("socket exception");
                            }
                        } else if (e instanceof ProtocolException) {
                            LogUtils.i(XpOnlineEngine.this.TAG, "protocol exception");
                            if (XpOnlineEngine.this.mXpWebSocketHelper != null) {
                                XpOnlineEngine.this.mXpWebSocketHelper.forceCloseWebSocket("protocol exception");
                            }
                        } else {
                            LogUtils.i(XpOnlineEngine.this.TAG, "unknown exception");
                            if (XpOnlineEngine.this.mXpWebSocketHelper != null) {
                                XpOnlineEngine.this.mXpWebSocketHelper.forceCloseWebSocket("unknown exception");
                            }
                        }
                    }
                }
            });
        }
    }

    public void shutdown() {
        LogUtils.v(this.TAG, "shutdown");
        EventBus.getDefault().unregister(this);
        this.mIsShutDown = true;
        XpWebSocketHelper xpWebSocketHelper = this.mXpWebSocketHelper;
        if (xpWebSocketHelper != null) {
            xpWebSocketHelper.shutdown();
        }
        this.mXpMp3Decoder.destroy();
        this.mHandler.removeCallbacksAndMessages(null);
        this.mThread.quitSafely();
        this.mIsWsConnected = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == XpOnlineEngine.MSG_INIT) {
                XpOnlineEngine.this.initEngine();
                LogUtils.i(XpOnlineEngine.this.TAG, "engine init done");
            } else if (i == XpOnlineEngine.MSG_TIMEOUT) {
                LogUtils.e(XpOnlineEngine.this.TAG, "speak timeout");
                XpOnlineEngine.this.reset();
            }
        }
    }

    public void speak(Bundle params) {
        LogUtils.i(this.TAG, "speak");
        this.mSourceType = BuildInfoUtils.UNKNOWN;
        this.mUid = params.getString("uid");
        this.mFirstDataLatency = 0;
        this.mDataPkgMaxLatency = 0;
        this.mDecodeLatency = 0;
        this.mTotalPkgTime = 0;
        this.mTotalDataPkgSize = 0;
        this.mStartTime = SystemClock.elapsedRealtime();
        if (!this.mIsWsConnected) {
            this.mCallback.onEvent(EventType.EVENT_SYNTH_ERROR, EngineEndType.WS_UNCONNECT);
        } else {
            sendData(params);
        }
    }

    private void sendData(Bundle params) {
        boolean isUrlChanged = this.mXpWebSocketHelper.checkUrlChanged();
        if (isUrlChanged) {
            LogUtils.i(this.TAG, "url changed");
            System.exit(0);
        }
        this.mTxt = params.getString("txt");
        String caller = params.getString("source");
        try {
            JSONObject requestMsg = new JSONObject();
            requestMsg.put("msgId", this.mUid);
            requestMsg.put("msgType", MSG_TYPE);
            requestMsg.put("textType", TYPE_TEXT);
            requestMsg.put(TYPE_TEXT, this.mTxt);
            requestMsg.put("caller", caller);
            requestMsg.put("carSpeed", this.mCarSpeed);
            if (this.mTxt.length() <= this.mDataLengthLimit) {
                String str = this.TAG;
                LogUtils.i(str, "sendData " + requestMsg);
                this.mXpWebSocketHelper.sendMessage(requestMsg.toString());
                watchDogReset();
            } else {
                String str2 = this.TAG;
                LogUtils.e(str2, "text too long " + this.mTxt.length());
                this.mCallback.onEvent(EventType.EVENT_SYNTH_ERROR, EngineEndType.TEXT_TOO_LONG);
            }
        } catch (JSONException e) {
            LogUtils.e(this.TAG, "stop msg send error", e);
        }
    }

    public void stop() {
        try {
            JSONObject stopMsg = new JSONObject();
            String stopId = UUID.randomUUID().toString();
            stopMsg.put("msgId", stopId);
            stopMsg.put("msgType", "stop");
            stopMsg.put("interruptMsgId", this.mUid);
            String str = this.TAG;
            LogUtils.i(str, "stop msgId " + stopId);
            this.mXpWebSocketHelper.sendMessage(stopMsg.toString());
        } catch (JSONException e) {
            LogUtils.e(this.TAG, "stop msg send error", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onMsgRcv(String msg) {
        JSONObject msgObject = null;
        try {
            msgObject = new JSONObject(msg);
        } catch (Exception e) {
            LogUtils.e(this.TAG, "onMsgRcv format error", e);
        }
        String msgType = msgObject.optString("msgType");
        if (MSG_RCV_TYPE_START.equals(msgType)) {
            this.mSourceType = msgObject.optString("source");
            this.mIsDataCompleted = false;
            this.mIsFirstData = true;
            this.mAudioBuffer = new AudioBuffer();
            this.mDataCallback.setStream(this.mAudioBuffer);
            int maxWaitTime = msgObject.optInt("maxWaitTime", 0);
            this.mCallback.onEvent(EventType.EVENT_SYNTH_BEGIN, Integer.valueOf(maxWaitTime));
            watchDogReset();
        } else if (MSG_RCV_TYPE_END.equals(msgType)) {
            AudioBuffer audioBuffer = this.mAudioBuffer;
            if (audioBuffer == null) {
                return;
            }
            audioBuffer.writeDone();
            int code = msgObject.optInt("code");
            if (code == 200 || code == 1002) {
                if (code == 200) {
                    this.mIsDataCompleted = true;
                }
                this.mTotalPkgTime = (int) (SystemClock.elapsedRealtime() - this.mStartTime);
                this.mCallback.onEvent(EventType.EVENT_SYNTH_END);
                if (code == 200) {
                    int bufferLen = this.mAudioBuffer.getLength();
                    String str = this.TAG;
                    LogUtils.v(str, "bufferLen " + bufferLen);
                    Bundle info = new Bundle();
                    info.putInt("duration", bufferLen / 6);
                    this.mCallback.onEvent(EventType.EVENT_UPLOAD_INFO, info);
                }
            } else {
                String reason = msgObject.optString("reason");
                String str2 = this.TAG;
                LogUtils.e(str2, "synth error: " + reason);
                this.mCallback.onEvent(EventType.EVENT_SYNTH_ERROR, EngineEndType.SERVER_ERROR);
            }
            watchDogClear();
        } else if ("reviseRequest".equals(msgType)) {
            String text = msgObject.optString(TYPE_TEXT);
            this.mTtsCache.deleteItemFromText(text);
            String msgId = msgObject.optString("msgId");
            String textId = msgObject.optString("textId");
            try {
                JSONObject ackMsg = new JSONObject();
                ackMsg.put("msgId", msgId);
                ackMsg.put("msgType", "reviseResponse");
                ackMsg.put("textId", textId);
                ackMsg.put("ackResult", "ack");
                String str3 = this.TAG;
                LogUtils.i(str3, "ack delete cache of text " + text);
                this.mXpWebSocketHelper.sendMessage(ackMsg.toString());
            } catch (JSONException e2) {
                LogUtils.e(this.TAG, "ack delete cache of text fail", e2);
            }
        } else if ("reviseAllRequest".equals(msgType)) {
            this.mTtsCache.deleteAllItems();
            String msgId2 = msgObject.optString("msgId");
            try {
                JSONObject ackMsg2 = new JSONObject();
                ackMsg2.put("msgId", msgId2);
                ackMsg2.put("msgType", "reviseAllResponse");
                ackMsg2.put("ackResult", "ack");
                LogUtils.i(this.TAG, "ack delete add cache");
                this.mXpWebSocketHelper.sendMessage(ackMsg2.toString());
            } catch (JSONException e3) {
                LogUtils.e(this.TAG, "ack delete add cache fail", e3);
            }
        } else if ("reviseTimeRequest".equals(msgType)) {
            long time = msgObject.optLong("time");
            this.mTtsCache.deleteItemBeforeTime(time);
            String msgId3 = msgObject.optString("msgId");
            try {
                JSONObject ackMsg3 = new JSONObject();
                ackMsg3.put("msgId", msgId3);
                ackMsg3.put("msgType", "reviseTimeResponse");
                ackMsg3.put("ackResult", "ack");
                String str4 = this.TAG;
                LogUtils.i(str4, "ack delete cache before time " + time);
                this.mXpWebSocketHelper.sendMessage(ackMsg3.toString());
            } catch (JSONException e4) {
                LogUtils.e(this.TAG, "ack delete cache before time", e4);
            }
        } else {
            String str5 = this.TAG;
            LogUtils.w(str5, "Unhandled msg: " + msg);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDataRcv(byte[] buffer) {
        if (this.mIsFirstData) {
            this.mIsFirstData = false;
            this.mDataPkgMaxLatency = 0;
            this.mFirstDataLatency = (int) (SystemClock.elapsedRealtime() - this.mStartTime);
            this.mFirstDataPkgRcvTime = SystemClock.elapsedRealtime();
            this.mTotalDataPkgSize = buffer.length;
        } else {
            long time = SystemClock.elapsedRealtime() - this.mFirstDataPkgRcvTime;
            int delay = (int) (time - (this.mTotalDataPkgSize / 6));
            if (this.mDataPkgMaxLatency < delay) {
                this.mDataPkgMaxLatency = delay;
            }
            this.mTotalDataPkgSize += buffer.length;
        }
        this.mAudioBuffer.write(buffer);
        watchDogReset();
    }

    public void startProcessor() {
        this.mXpMp3Decoder.start(this.mPreCacheSize);
    }

    public void stopProcessor() {
        this.mXpMp3Decoder.stop();
    }

    public int getData(byte[] buffer) {
        return this.mXpMp3Decoder.getData(buffer);
    }

    private void watchDogReset() {
        this.mHandler.removeMessages(MSG_TIMEOUT);
        this.mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, this.mTimeOutIntervalMs);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void watchDogClear() {
        this.mHandler.removeMessages(MSG_TIMEOUT);
    }

    public void reset() {
        AudioBuffer audioBuffer = this.mAudioBuffer;
        if (audioBuffer != null) {
            audioBuffer.writeDone();
        }
        if (this.mCallback != null) {
            this.mCallback.onEvent(EventType.EVENT_SYNTH_ERROR, EngineEndType.DATA_PKG_TIMEOUT);
        }
        XpWebSocketHelper xpWebSocketHelper = this.mXpWebSocketHelper;
        if (xpWebSocketHelper != null) {
            xpWebSocketHelper.forceCloseWebSocket(REASON_SPEAK_TIMEOUT);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class MyTtsDataCallback implements IDataCallback {
        private volatile AudioBuffer mAudioBuffer;
        private ITtsEngineCallback mCallback;

        public MyTtsDataCallback(ITtsEngineCallback cb) {
            this.mCallback = cb;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setStream(AudioBuffer buffer) {
            this.mAudioBuffer = buffer;
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IDataCallback
        public void onEvent(EventType event) {
            onEvent(event, null);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IDataCallback
        public void onEvent(EventType event, Bundle bundle) {
            if (event == EventType.EVENT_DATA_READY) {
                XpOnlineEngine.this.mDecodeLatency = (int) (SystemClock.elapsedRealtime() - XpOnlineEngine.this.mStartTime);
            }
            this.mCallback.onEvent(event, bundle);
            if (XpOnlineEngine.this.mIsDataCompleted) {
                if (event == EventType.EVENT_PROCESS_END || event == EventType.EVENT_PROCESS_ERROR) {
                    XpOnlineEngine.this.mIsDataCompleted = false;
                    XpOnlineEngine.this.mTtsCache.makeTtsCache(XpOnlineEngine.this.mTxt, this.mAudioBuffer);
                }
            }
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.IDataCallback
        public int getData(byte[] buffer) {
            return this.mAudioBuffer.read(buffer);
        }
    }

    public void UploadData(EngineEndType state) {
        OnlineUploadData data = new OnlineUploadData(this.mSourceType, this.mUid, this.mFirstDataLatency, this.mDataPkgMaxLatency, this.mDecodeLatency, this.mTotalPkgTime, this.mTotalDataPkgSize, state);
        UploadDataModel.getInstance().setOnlineData(this.mUid, data);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onPowerStateChange(PowerState power) {
        String str = this.TAG;
        LogUtils.i(str, "onPowerStateChange " + power.state);
        if (power.state == 1) {
            this.mIsPowerOn = true;
        } else if (power.state == 0) {
            this.mIsPowerOn = false;
        }
        if (this.mXpWebSocketHelper != null) {
            if (this.mIsPowerOn && !this.mIsShutDown) {
                LogUtils.d(this.TAG, "screen on, reconnect");
                this.mXpWebSocketHelper.connectSocket(200);
                return;
            }
            LogUtils.d(this.TAG, "screen off, disconnect");
            this.mXpWebSocketHelper.closeWebSocket("screen off");
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onConfigChange(OnlineEngineConfig config) {
        String str = this.TAG;
        LogUtils.i(str, "onConfigChange " + config);
        this.mDataLengthLimit = config.dataLengthLimit;
        this.mTimeOutIntervalMs = config.dataTimeOutInterval;
        this.mPreCacheSize = config.preCacheSize;
    }

    @Subscribe(sticky = HttpParams.IS_REPLACE, threadMode = ThreadMode.BACKGROUND)
    public void onCarSpeedChange(CarSpeed carSpeed) {
        this.mCarSpeed = carSpeed.speed;
    }
}

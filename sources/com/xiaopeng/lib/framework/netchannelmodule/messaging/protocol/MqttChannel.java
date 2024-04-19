package com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.MessagingException;
import com.xiaopeng.lib.framework.netchannelmodule.common.BackgroundHandler;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.events.EventSender;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.exception.MessagingExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.statistic.Counter;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.NetUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.io.EOFException;
import java.net.SocketException;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;
/* loaded from: classes.dex */
public class MqttChannel {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String CLASS_NAME = MqttChannel.class.getSimpleName();
    private static final String TAG = "NetChannel-MqttChannel";
    private Application mApplicationContext;
    private volatile AbstractChannelProfile mChannelProfile;
    private Counter mCounter;
    private EventSender mEventSender;
    private volatile ExtremePingSender mExtremePingSender;
    private volatile BackgroundHandler mHandler;
    private MqttClientPersistence mMemoryPersistence;
    private volatile MqttAndroidClient mMqttAndroidClient;
    private MqttCallbackImpl mMqttCallback;
    private MqttConnectOptions mMqttConnectOptions;
    private volatile Scheduler mScheduler;
    private volatile SchedulerClient mSchedulerClient;
    private boolean mSendOutAllLogs;

    public static MqttChannel getInstance() {
        return MqttChannelHolder.INSTANCE;
    }

    public static AbstractChannelProfile getCurrentChannelProfile() {
        return getInstance().mChannelProfile;
    }

    public synchronized void init(Application application, final AbstractChannelProfile profile) throws MessagingException {
        if (this.mApplicationContext != null) {
            this.mHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        MqttChannel.this.reInitMqtt(profile);
                    } catch (Exception e) {
                    }
                }
            });
            return;
        }
        this.mApplicationContext = application;
        this.mChannelProfile = profile;
        this.mSendOutAllLogs = profile.sendOutAllLogs();
        LoggerFactory.setLogger(LoggerImpl.class.getName());
        this.mHandler = new BackgroundHandler(CLASS_NAME);
        this.mMqttCallback = new MqttCallbackImpl();
        this.mEventSender = new EventSender(profile.channel());
        this.mExtremePingSender = new ExtremePingSender();
        this.mSchedulerClient = new SchedulerClient();
        this.mScheduler = new Scheduler(this.mSchedulerClient, this.mHandler);
        SystemStatusMonitor.getInstance().registerBroadcastReceiver(application);
        if (BuildInfoUtils.isEngVersion()) {
            DebugCommandMonitor.getInstance().registerBroadcastReceiver();
        }
        this.mCounter = new Counter(this.mApplicationContext, this.mHandler, this.mChannelProfile.needToCollectData());
        initMqttConnection();
    }

    public boolean sendoutAllLogs() {
        return this.mSendOutAllLogs;
    }

    public synchronized boolean isConnected() {
        if (this.mScheduler == null) {
            return false;
        }
        return this.mScheduler.connected();
    }

    public void networkChanged(final boolean connected) {
        this.mHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.2
            @Override // java.lang.Runnable
            public void run() {
                if (MqttChannel.this.mScheduler == null) {
                    return;
                }
                if (connected) {
                    MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.NETWORK_CHANGED);
                    if (MqttChannel.this.mCounter != null) {
                        MqttChannel.this.mCounter.commitForcibly();
                        return;
                    }
                    return;
                }
                MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTED);
            }
        });
    }

    public void disconnectedDueToException() {
        disconnectedDueToException(null);
    }

    public void disconnectedDueToException(@Nullable Throwable throwable) {
        if (realNetworkException(throwable)) {
            this.mScheduler.resetFail();
        }
        this.mSchedulerClient.disconnectedDueToException(throwable);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public synchronized void requestToDisconnect() {
        logInternal("requestToDisconnect");
        if (isConnected()) {
            this.mHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.3
                @Override // java.lang.Runnable
                public void run() {
                    MqttChannel.this.disconnectConnection();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public synchronized void requestToConnect() {
        logInternal("requestToConnect");
        if (isConnected()) {
            return;
        }
        this.mHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.4
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (MqttChannel.this.networkAvailable()) {
                        MqttChannel.this.connectMqtt();
                    } else {
                        LogUtils.d("NetChannel-MqttChannel", "Network is not available, delay to try...");
                    }
                } catch (Exception e) {
                    MqttChannel.this.log(e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reInitMqttWithLastProfile() throws MessagingException {
        reInitMqtt(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void reInitMqtt(@Nullable AbstractChannelProfile profile) throws MessagingException {
        if (profile == null) {
            profile = this.mChannelProfile;
        }
        closeCurrentConnection();
        this.mEventSender.changeChannel(profile.channel());
        this.mScheduler.reset();
        initMqttConnection();
    }

    private synchronized void closeCurrentConnection() {
        if (this.mMqttAndroidClient == null) {
            return;
        }
        try {
            this.mHandler.clearRunnables();
            this.mSchedulerClient.stopBackgroundReconnection();
            this.mSchedulerClient.stopReopenAction();
            this.mScheduler.reset();
            this.mMqttAndroidClient.unregisterResources();
            this.mMqttAndroidClient.disconnectConnection((IMqttActionListener) null);
        } catch (Exception ex) {
            log("Failed to disconnectConnection current connection!!!");
            ex.printStackTrace();
        }
        this.mMqttAndroidClient = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disconnectConnection() {
        if (this.mScheduler == null || !this.mScheduler.connected()) {
            return;
        }
        this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTING);
        try {
            this.mMqttAndroidClient.disconnectConnection((IMqttActionListener) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTED);
        try {
            if (this.mMemoryPersistence != null) {
                this.mMemoryPersistence.close();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void initMqttConnection() throws MessagingException {
        synchronized (this) {
            initMqttClient();
        }
        this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTED);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean connectMqtt() throws MessagingException {
        if (this.mMqttAndroidClient == null || this.mMqttConnectOptions == null) {
            this.mScheduler.increaseFail();
            log("mMqttAndroidClient == null || mMqttConnectOptions == null, return...");
            return false;
        } else if (!this.mScheduler.allowToConnect()) {
            this.mScheduler.increaseFail();
            log("already connected or connecting! State:" + this.mScheduler.state());
            return false;
        } else {
            try {
                log(this.mMqttConnectOptions.toString());
                this.mScheduler.moveTo(Scheduler.STATE.CONNECTING);
                this.mMqttAndroidClient.connect(this.mMqttConnectOptions, this.mApplicationContext, new IMqttActionListener() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.5
                    public void onSuccess(IMqttToken asyncActionToken) {
                        MqttChannel mqttChannel = MqttChannel.this;
                        mqttChannel.logInternal("onSuccess asyncActionToken=" + asyncActionToken);
                        try {
                            if (MqttChannel.this.mChannelProfile.canPublish()) {
                                MqttChannel.this.mMqttAndroidClient.setBufferOpts(MqttChannel.this.mChannelProfile.buildBufferOptions());
                            }
                            MqttChannel.this.mScheduler.resetFail();
                            MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.CONNECTED);
                            MqttChannel.this.mEventSender.postConnected("");
                        } catch (Exception e) {
                            MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTED);
                        }
                    }

                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (exception instanceof MqttException) {
                            if (4 == ((MqttException) exception).getReasonCode()) {
                                MqttChannel.this.mSchedulerClient.stopBackgroundReconnection();
                            }
                            MqttChannel.this.mEventSender.postDisconnected(exception);
                        }
                        MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTED);
                        LogUtils.d("NetChannel-MqttChannel", "connectMqtt failed! release connection resources in time");
                        try {
                            MqttChannel.this.mMqttAndroidClient.unregisterResources();
                            MqttChannel.this.mMqttAndroidClient.disconnectConnection((IMqttActionListener) null);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        if (exception != null) {
                            if (exception.getCause() != null && (exception.getCause() instanceof SocketException) && exception.toString().indexOf("EBADF") > 0) {
                                MqttChannel mqttChannel = MqttChannel.this;
                                mqttChannel.log("onFailure: Kill the process due to exception:" + exception);
                                System.exit(0);
                            }
                            if (MqttChannel.this.realNetworkException(exception.getCause())) {
                                MqttChannel.this.mScheduler.resetFail();
                            } else {
                                MqttChannel.this.mScheduler.increaseFail();
                            }
                        }
                        MqttChannel mqttChannel2 = MqttChannel.this;
                        mqttChannel2.log("onFailure asyncActionToken=" + asyncActionToken + " exception=" + exception);
                    }
                });
                return true;
            } catch (Exception e) {
                this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTED);
                log(e.toString());
                e.printStackTrace();
                throw new MessagingExceptionImpl(18);
            }
        }
    }

    private void initMqttClient() throws MessagingException {
        if (this.mMqttAndroidClient == null) {
            initAndroidClientSuccess();
        }
        this.mMqttConnectOptions = this.mChannelProfile.buildConnectOptions();
    }

    private boolean initAndroidClientSuccess() throws MessagingException {
        LogUtils.i("NetChannel-MqttChannel", "initAndroidClientSuccess mMqttAndroidClient = " + this.mMqttAndroidClient);
        String url = this.mChannelProfile.serverUrl();
        if (TextUtils.isEmpty(url)) {
            Counter counter = this.mCounter;
            if (counter != null) {
                counter.increaseInitFailedCount();
            }
            throw new MessagingExceptionImpl(17);
        }
        String clientId = this.mChannelProfile.clientId();
        if (this.mMemoryPersistence == null) {
            this.mMemoryPersistence = this.mChannelProfile.mqttClientPersistence();
        }
        try {
            this.mMemoryPersistence.open(clientId, url);
        } catch (MqttPersistenceException ex) {
            logInternal(ex.toString());
            this.mMemoryPersistence = null;
        }
        MqttClientPersistence mqttClientPersistence = this.mMemoryPersistence;
        if (mqttClientPersistence instanceof MemoryPersistenceProxy) {
            ((MemoryPersistenceProxy) mqttClientPersistence).setTraceEnabled(this.mChannelProfile.enableTrace());
        }
        this.mMqttAndroidClient = new MqttAndroidClient(this.mApplicationContext, url, clientId, this.mMemoryPersistence);
        this.mMqttAndroidClient.setCallback(this.mMqttCallback);
        this.mMqttAndroidClient.setTraceEnabled(this.mChannelProfile.enableTrace());
        this.mMqttAndroidClient.setTraceCallback(new MqttTraceHandler() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.6
            public void traceDebug(String source, String message) {
                MqttChannel mqttChannel = MqttChannel.this;
                mqttChannel.logInternal("traceDebug_source--> " + source + ", message --> " + message);
            }

            public void traceError(String source, String message) {
                MqttChannel mqttChannel = MqttChannel.this;
                mqttChannel.log("traceError_source--> " + source + ", message --> " + message);
            }

            public void traceException(String source, String message, Exception e) {
                MqttChannel mqttChannel = MqttChannel.this;
                mqttChannel.log("traceException_source--> " + source + ", message --> " + message);
            }
        });
        return true;
    }

    public synchronized void subscribe(final String topic) throws MessagingException {
        if (this.mMqttAndroidClient == null) {
            log("subscribe mMqttAndroidClient not init.");
            throw new MessagingExceptionImpl(17);
        }
        this.mHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.7
            @Override // java.lang.Runnable
            public void run() {
                MqttChannel.this.subscribeInternal(topic);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void subscribeInternal(final String topic) {
        if (!this.mScheduler.allowToSubscribe()) {
            return;
        }
        try {
            this.mMqttAndroidClient.subscribe(topic, this.mChannelProfile.defaultQoSLevel(), (Object) null, new IMqttActionListener() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.8
                public void onSuccess(IMqttToken asyncActionToken) {
                    MqttChannel mqttChannel = MqttChannel.this;
                    mqttChannel.logInternal("subscribe topicName onSuccess topicName=" + topic);
                    MqttChannel.this.mEventSender.postSubscribed(topic);
                    MqttChannel.this.mScheduler.resetFail();
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    MqttChannel.this.mEventSender.postUnsubscribed();
                    MqttChannel.this.mScheduler.increaseFail();
                    MqttChannel.this.disconnectedDueToException(exception);
                    MqttChannel mqttChannel = MqttChannel.this;
                    mqttChannel.logInternal("subscribeToTopic topicName onFailure topicName=" + topic);
                    if (exception != null) {
                        MqttChannel.this.logInternal(exception.toString());
                        exception.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log(e.toString());
        }
    }

    public long publishMessage(String topic, String msg) throws MessagingException {
        return publishMessage(topic, msg.getBytes(), this.mChannelProfile.defaultQoSLevel());
    }

    public long publishMessage(String topic, byte[] msg) throws MessagingException {
        return publishMessage(topic, msg, this.mChannelProfile.defaultQoSLevel());
    }

    public long publishMessage(String topic, byte[] msg, int qosLevel) throws MessagingException {
        logInternal("publishMessage topic=" + topic + " msg.length =" + msg.length);
        long messageId = 0;
        if (this.mMqttAndroidClient == null) {
            logInternal("publishMessage mMqttAndroidClient not init.");
            throw new MessagingExceptionImpl(17);
        }
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(msg);
        mqttMessage.setQos(qosLevel);
        synchronized (this) {
            try {
                if (this.mCounter != null) {
                    this.mCounter.increaseTryingToPublish();
                }
                messageId = this.mMqttAndroidClient.publish(topic, mqttMessage, (Object) null, new IMqttActionListener() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.9
                    public void onSuccess(IMqttToken asyncActionToken) {
                    }

                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        String logMessage;
                        MqttChannel.this.mScheduler.increaseFail();
                        if (MqttChannel.this.mCounter != null) {
                            MqttChannel.this.mCounter.increasePublishFailureCount();
                        }
                        if (exception instanceof MqttException) {
                            MqttChannel.this.mEventSender.postDeliveryFailure(asyncActionToken.getMessageId(), ((MqttException) exception).getReasonCode());
                            logMessage = "Failed to publish, exception (A):" + ((MqttException) exception);
                            MqttChannel.this.logInternal(logMessage);
                        } else {
                            logMessage = "Failed to publish, exception (B):" + exception;
                            MqttChannel.this.logInternal(logMessage);
                            MqttChannel.this.mEventSender.postDeliveryFailure(asyncActionToken.getMessageId(), 0);
                        }
                        MqttChannel.this.mEventSender.postTracingLog(logMessage);
                    }
                }).getMessageId();
            } catch (Exception e) {
                if (this.mCounter != null) {
                    this.mCounter.increasePublishFailureCount();
                }
                if (e instanceof MqttException) {
                    this.mEventSender.postDeliveryFailure(0L, e.getReasonCode());
                } else {
                    this.mEventSender.postDeliveryFailure(0L, 0);
                }
            }
        }
        return messageId;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean realNetworkException(@Nullable Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        log("Found exception:" + throwable.toString());
        return throwable instanceof EOFException;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void logInternal(String message) {
        if (BuildInfoUtils.isUserVersion()) {
            return;
        }
        log(message);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void log(String message) {
        LogUtils.i("NetChannel-MqttChannel", message);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class MqttChannelHolder {
        private static final MqttChannel INSTANCE = new MqttChannel();

        private MqttChannelHolder() {
        }
    }

    /* loaded from: classes.dex */
    public class MqttCallbackImpl implements MqttCallbackExtended {
        public MqttCallbackImpl() {
        }

        public void connectComplete(boolean reconnect, String serverURI) {
            if (MqttChannel.this.mMqttAndroidClient != null) {
                if (MqttChannel.this.mMqttAndroidClient.isConnected()) {
                    MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.CONNECTED);
                } else {
                    MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTED);
                }
                MqttChannel.this.mEventSender.postConnected(serverURI);
                MqttChannel mqttChannel = MqttChannel.this;
                mqttChannel.logInternal("connectComplete reconnect=" + reconnect);
            }
        }

        public void connectionLost(Throwable cause) {
            MqttChannel mqttChannel = MqttChannel.this;
            mqttChannel.log("Connection lost due to " + cause);
            MqttChannel.this.disconnectedDueToException(cause);
        }

        public void messageArrived(String topic, MqttMessage message) {
            LogUtils.d("NetChannel-MqttChannel", "messageArrived: topic = " + topic);
            MqttChannel.this.mEventSender.postReceivedMessage(topic, message.getPayload());
            MqttChannel.this.mScheduler.resetFail();
            if (MqttChannel.this.mCounter != null) {
                MqttChannel.this.mCounter.increaseReceivedCount();
            }
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
            if (BuildInfoUtils.isUserVersion()) {
                MqttChannel.this.mEventSender.postDeliveryCompleted(token.getMessageId());
            } else {
                try {
                    MqttChannel.this.mEventSender.postDeliveryCompleted(token.getMessageId(), token.getMessage().getPayload());
                } catch (Exception ex) {
                    LogUtils.d("NetChannel-MqttChannel", "Delivered completely, but met exception while getting message:" + ex);
                }
            }
            MqttChannel.this.mScheduler.resetFail();
            long payloadSize = 0;
            try {
                if (token.getMessage() != null && token.getMessage().getPayload() != null) {
                    payloadSize = token.getMessage().getPayload().length;
                }
            } catch (Exception e) {
            }
            if (MqttChannel.this.mCounter != null) {
                MqttChannel.this.mCounter.increasePublishedCountWithSize(payloadSize);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ExtremePingSender {
        private static final int EXTREME_PING_MAX_INTERVAL = 80000;
        private static final int EXTREME_PING_MIN_INTERVAL = 10000;
        private static final int EXTREME_PING_STABLE_THRESHOLD = 20;
        private static final int EXTREME_PING_TIMEOUT = 8000;
        private int mActiveInterval;
        private int mActiveRecoverDelay;
        private Runnable mExceptionHandlingRunnable;
        private Runnable mExtremePingRunnable;
        private IMqttActionListener mPingActionListener;
        private int mSucceedCount;

        private ExtremePingSender() {
            this.mActiveInterval = EXTREME_PING_MIN_INTERVAL;
            this.mActiveRecoverDelay = EXTREME_PING_TIMEOUT;
            this.mPingActionListener = new IMqttActionListener() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.ExtremePingSender.1
                public void onSuccess(IMqttToken asyncActionToken) {
                    ExtremePingSender.access$1808(ExtremePingSender.this);
                    MqttChannel.this.mHandler.removeCallbacks(ExtremePingSender.this.mExceptionHandlingRunnable);
                    if (ExtremePingSender.this.mSucceedCount > 20) {
                        ExtremePingSender.this.mActiveInterval = ExtremePingSender.EXTREME_PING_MAX_INTERVAL;
                        ExtremePingSender.this.mActiveRecoverDelay = 20000;
                        ExtremePingSender.this.mSucceedCount = 0;
                    }
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    ExtremePingSender.this.mSucceedCount = 0;
                    ExtremePingSender.this.mActiveInterval = ExtremePingSender.EXTREME_PING_MIN_INTERVAL;
                    ExtremePingSender.this.mActiveRecoverDelay = ExtremePingSender.EXTREME_PING_TIMEOUT;
                }
            };
            this.mExtremePingRunnable = new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.ExtremePingSender.2
                @Override // java.lang.Runnable
                public void run() {
                    if (MqttChannel.this.isConnected()) {
                        try {
                            if (ExtremePingSender.this.sendExtremePing(ExtremePingSender.this.mPingActionListener)) {
                                MqttChannel.this.mHandler.postDelayed(ExtremePingSender.this.mExceptionHandlingRunnable, ExtremePingSender.this.mActiveRecoverDelay);
                            }
                        } catch (MessagingException ex) {
                            MqttChannel mqttChannel = MqttChannel.this;
                            mqttChannel.logInternal("Failed to send extreme ping:" + ex);
                        }
                    }
                    MqttChannel.this.mHandler.postDelayed(ExtremePingSender.this.mExtremePingRunnable, ExtremePingSender.this.mActiveInterval);
                }
            };
        }

        static /* synthetic */ int access$1808(ExtremePingSender x0) {
            int i = x0.mSucceedCount;
            x0.mSucceedCount = i + 1;
            return i;
        }

        public void startExtremePingSender(@NonNull Runnable exceptionHandlingRunnable) {
            if (MqttChannel.this.mChannelProfile == null || !MqttChannel.this.mChannelProfile.enableExtremePing()) {
                return;
            }
            this.mSucceedCount = 0;
            this.mExceptionHandlingRunnable = exceptionHandlingRunnable;
            this.mActiveInterval = EXTREME_PING_MIN_INTERVAL;
            this.mActiveRecoverDelay = EXTREME_PING_TIMEOUT;
            MqttChannel.this.mHandler.removeCallbacks(this.mExtremePingRunnable);
            MqttChannel.this.mHandler.postDelayed(this.mExtremePingRunnable, 10000L);
            MqttChannel.this.logInternal("start ExtremePingSender");
        }

        public void stopExtremePingSender() {
            MqttChannel.this.mHandler.removeCallbacks(this.mExtremePingRunnable);
            MqttChannel.this.mHandler.removeCallbacks(this.mExceptionHandlingRunnable);
            MqttChannel.this.logInternal("stop ExtremePingSender");
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean sendExtremePing(IMqttActionListener listener) throws MessagingException {
            boolean published = false;
            if (!MqttChannel.this.isConnected()) {
                return false;
            }
            synchronized (this) {
                try {
                    MqttChannel.this.mMqttAndroidClient.checkPing(listener);
                    published = true;
                } catch (Exception e) {
                    listener.onFailure((IMqttToken) null, e);
                }
            }
            return published;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SchedulerClient implements Scheduler.Client {
        private static final int REINIT_INTERVAL = 20000;
        private Runnable mConnectAction;
        private Runnable mReInitAction;
        private int mRepeatPeriod;
        private Runnable mUnexpectedHandlingAction;

        private SchedulerClient() {
            this.mReInitAction = new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.SchedulerClient.1
                @Override // java.lang.Runnable
                public void run() {
                    boolean networkAvailable = MqttChannel.this.networkAvailable();
                    MqttChannel mqttChannel = MqttChannel.this;
                    mqttChannel.log("mReInitAction runs! netAvailable:" + networkAvailable);
                    if (networkAvailable) {
                        SchedulerClient.this.requestToReInitInternal();
                    }
                }
            };
            this.mConnectAction = new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.SchedulerClient.2
                @Override // java.lang.Runnable
                public void run() {
                    if (!MqttChannel.this.networkAvailable()) {
                        MqttChannel.this.log("Wifi or System APN is not available, delay to try...");
                        MqttChannel.this.mHandler.postDelayed(SchedulerClient.this.mConnectAction, SchedulerClient.this.mRepeatPeriod);
                        return;
                    }
                    boolean startedConnecting = false;
                    try {
                        if (MqttChannel.this.mMqttAndroidClient != null) {
                            startedConnecting = MqttChannel.this.connectMqtt();
                        } else {
                            SchedulerClient.this.mRepeatPeriod = SchedulerClient.REINIT_INTERVAL;
                            SchedulerClient.this.requestToReInitInternal();
                        }
                    } catch (Exception e) {
                        startedConnecting = false;
                        MqttChannel.this.log(e.toString());
                        e.printStackTrace();
                    }
                    if (startedConnecting) {
                        MqttChannel.this.log("Connecting...");
                    }
                    MqttChannel.this.mHandler.postDelayed(SchedulerClient.this.mConnectAction, SchedulerClient.this.mRepeatPeriod);
                }
            };
            this.mUnexpectedHandlingAction = new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.SchedulerClient.3
                @Override // java.lang.Runnable
                public void run() {
                    if (MqttChannel.this.mMqttAndroidClient != null) {
                        MqttChannel.this.mExtremePingSender.stopExtremePingSender();
                        LogUtils.d("NetChannel-MqttChannel", "mUnexpectedHandlingAction: connected state = " + MqttChannel.this.mScheduler.connected() + ", " + MqttChannel.this.mMqttAndroidClient.isConnected());
                        if (MqttChannel.this.mScheduler.connected() || MqttChannel.this.mMqttAndroidClient.isConnected()) {
                            MqttChannel.this.disconnectConnection();
                        } else if (MqttChannel.this.mScheduler != null) {
                            MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTED);
                            MqttChannel.this.mScheduler.increaseFailTwice();
                        }
                    }
                }
            };
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void requestToReInitInternal() {
            if (TextUtils.isEmpty(MqttChannel.this.mChannelProfile.serverUrl())) {
                MqttChannel.this.log("No valid connection options!");
                return;
            }
            MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.INITIALIZING);
            try {
                MqttChannel.this.reInitMqttWithLastProfile();
            } catch (Exception e) {
                MqttChannel.this.mScheduler.moveTo(Scheduler.STATE.DISCONNECTED);
                e.printStackTrace();
            }
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.Client
        public void scheduleReopen() {
            stopBackgroundReconnection();
            stopExtremePingSender();
            MqttChannel.this.mHandler.postDelayed(this.mReInitAction, 1000L);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.Client
        public void disconnect() {
            MqttChannel.this.mHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel.SchedulerClient.4
                @Override // java.lang.Runnable
                public void run() {
                    MqttChannel.this.disconnectConnection();
                }
            });
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.Client
        public void startExtremePingSender() {
            MqttChannel.this.mExtremePingSender.startExtremePingSender(this.mUnexpectedHandlingAction);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.Client
        public void stopExtremePingSender() {
            MqttChannel.this.mExtremePingSender.stopExtremePingSender();
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.Client
        public void disconnectedDueToException(Throwable throwable) {
            MqttChannel mqttChannel = MqttChannel.this;
            mqttChannel.logInternal("disconnectedDueToException " + throwable);
            MqttChannel.this.mHandler.removeCallbacks(this.mUnexpectedHandlingAction);
            MqttChannel.this.mHandler.post(this.mUnexpectedHandlingAction);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.Client
        public void startBackgroundReconnection(int delayAndPeriod) {
            this.mRepeatPeriod = delayAndPeriod;
            MqttChannel.this.mHandler.removeCallbacks(this.mConnectAction);
            MqttChannel.this.mExtremePingSender.stopExtremePingSender();
            MqttChannel.this.mHandler.postDelayed(this.mConnectAction, delayAndPeriod);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.Client
        public void stopBackgroundReconnection() {
            MqttChannel.this.mHandler.removeCallbacks(this.mConnectAction);
            MqttChannel.this.mHandler.removeCallbacks(this.mUnexpectedHandlingAction);
            stopReopenAction();
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.Client
        public void stopReopenAction() {
            MqttChannel.this.mHandler.removeCallbacks(this.mReInitAction);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.Client
        public void startConnectImmediately(int delayAndPeriod) {
            this.mRepeatPeriod = delayAndPeriod;
            MqttChannel.this.mHandler.removeCallbacks(this.mConnectAction);
            MqttChannel.this.mHandler.post(this.mConnectAction);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean networkAvailable() {
        if (NetUtils.isNetworkAvailable(this.mApplicationContext)) {
            return !NetUtils.isMobileNetwork(this.mApplicationContext) || NetUtils.isSystemApnReady();
        }
        return false;
    }
}

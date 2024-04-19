package com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol;

import android.support.annotation.VisibleForTesting;
import com.xiaopeng.lib.framework.netchannelmodule.common.BackgroundHandler;
import com.xiaopeng.lib.framework.netchannelmodule.common.ContextNetStatusProvider;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.events.EventSender;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.config.RemoteControlConfig;
@VisibleForTesting
/* loaded from: classes.dex */
public class Scheduler {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int RECONNECTION_LONG_TIME_FAILED_CNT = 60;
    private static final int RECONNECTION_MAX_DELAY = 10000;
    private static final int RECONNECTION_MIN_DELAY = 2000;
    private static final int RECONNECTION_NORMAL_DELAY = 2000;
    private static final String TAG = "NetChannel-Scheduler";
    private static final int THRESHOLD_FAILED_COUNT_TO_DISCONNECT = 20;
    private static final int THRESHOLD_FAILED_COUNT_TO_REINIT = 50;
    private volatile Client mClient;
    private volatile STATE mLastState;
    private BackgroundHandler mMqttHandler;
    private volatile int mConnectedNetworkType = 0;
    private volatile STATE mState = STATE.INITIALIZING;
    private volatile int mContinuousConnectionFailed = 0;
    private volatile int mReconnectionDelay = RemoteControlConfig.SERVICE_TYPE_CARDIAGNOSIS_RUN_SH;
    private volatile int mFailedCount = 0;

    /* loaded from: classes.dex */
    public interface Client {
        void disconnect();

        void disconnectedDueToException(Throwable th);

        void scheduleReopen();

        void startBackgroundReconnection(int i);

        void startConnectImmediately(int i);

        void startExtremePingSender();

        void stopBackgroundReconnection();

        void stopExtremePingSender();

        void stopReopenAction();
    }

    /* loaded from: classes.dex */
    public enum STATE {
        INITIALIZING,
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        NETWORK_CHANGED
    }

    public Scheduler(Client client, BackgroundHandler handler) {
        this.mClient = client;
        this.mMqttHandler = handler;
    }

    public synchronized void moveTo(final STATE state) {
        String message = "[MQTTSTATUS] Move from state:" + this.mState + " to " + state;
        LogUtils.d(TAG, message);
        this.mLastState = this.mState;
        this.mState = state;
        if (state != STATE.INITIALIZING && state != STATE.DISCONNECTING) {
            this.mMqttHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.1
                @Override // java.lang.Runnable
                public void run() {
                    Scheduler scheduler = Scheduler.this;
                    scheduler.scheduleAction(scheduler.mLastState, state);
                }
            });
            if (MqttChannel.getInstance().sendoutAllLogs()) {
                EventSender.getCurrent().postTracingLog(message);
            }
        }
    }

    public synchronized void increaseFail() {
        this.mFailedCount++;
        this.mMqttHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.2
            @Override // java.lang.Runnable
            public void run() {
                Scheduler.this.doCheckFailedCount();
            }
        });
    }

    public synchronized void increaseFailTwice() {
        this.mFailedCount += 2;
        this.mMqttHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler.3
            @Override // java.lang.Runnable
            public void run() {
                Scheduler.this.doCheckFailedCount();
            }
        });
    }

    public synchronized void resetFail() {
        if (this.mFailedCount > 0) {
            this.mClient.stopReopenAction();
        }
        this.mFailedCount = 0;
    }

    public synchronized void reset() {
        this.mState = STATE.DISCONNECTED;
        this.mClient.stopBackgroundReconnection();
        this.mClient.stopExtremePingSender();
        if (this.mFailedCount > 0) {
            this.mClient.stopReopenAction();
        }
        this.mFailedCount = 0;
    }

    public synchronized boolean allowToConnect() {
        boolean z;
        if (this.mState != STATE.DISCONNECTED) {
            z = this.mState == STATE.NETWORK_CHANGED;
        }
        return z;
    }

    public synchronized boolean allowToSubscribe() {
        return this.mState == STATE.CONNECTED;
    }

    public synchronized boolean connected() {
        return this.mState == STATE.CONNECTED;
    }

    @VisibleForTesting
    public STATE state() {
        return this.mState;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void doCheckFailedCount() {
        LogUtils.d(TAG, "Failed:" + this.mFailedCount);
        if (this.mFailedCount > THRESHOLD_FAILED_COUNT_TO_REINIT) {
            this.mFailedCount = 0;
            this.mClient.scheduleReopen();
            return;
        }
        if (this.mFailedCount % 20 == 0) {
            if (this.mState != STATE.CONNECTING && this.mState != STATE.INITIALIZING) {
                LogUtils.d(TAG, "Try to disconnect the connection.");
                this.mClient.disconnect();
            }
            LogUtils.d(TAG, "Try to reopen the connection.");
            this.mClient.scheduleReopen();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.Scheduler$4  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$xiaopeng$lib$framework$netchannelmodule$messaging$protocol$Scheduler$STATE = new int[STATE.values().length];

        static {
            try {
                $SwitchMap$com$xiaopeng$lib$framework$netchannelmodule$messaging$protocol$Scheduler$STATE[STATE.DISCONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$xiaopeng$lib$framework$netchannelmodule$messaging$protocol$Scheduler$STATE[STATE.CONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$xiaopeng$lib$framework$netchannelmodule$messaging$protocol$Scheduler$STATE[STATE.NETWORK_CHANGED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$xiaopeng$lib$framework$netchannelmodule$messaging$protocol$Scheduler$STATE[STATE.CONNECTING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$xiaopeng$lib$framework$netchannelmodule$messaging$protocol$Scheduler$STATE[STATE.INITIALIZING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$xiaopeng$lib$framework$netchannelmodule$messaging$protocol$Scheduler$STATE[STATE.DISCONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void scheduleAction(STATE oldState, STATE state) {
        int i = AnonymousClass4.$SwitchMap$com$xiaopeng$lib$framework$netchannelmodule$messaging$protocol$Scheduler$STATE[state.ordinal()];
        if (i == 1) {
            this.mClient.stopBackgroundReconnection();
            this.mClient.stopExtremePingSender();
            this.mConnectedNetworkType = 0;
            if (oldState == STATE.INITIALIZING) {
                this.mClient.startConnectImmediately(RemoteControlConfig.SERVICE_TYPE_CARDIAGNOSIS_RUN_SH);
                this.mReconnectionDelay = RemoteControlConfig.SERVICE_TYPE_CARDIAGNOSIS_RUN_SH;
            } else {
                this.mContinuousConnectionFailed++;
                if (this.mContinuousConnectionFailed > RECONNECTION_LONG_TIME_FAILED_CNT) {
                    this.mReconnectionDelay = RECONNECTION_MAX_DELAY;
                }
                this.mClient.startBackgroundReconnection(this.mReconnectionDelay);
            }
        } else if (i == 2) {
            this.mContinuousConnectionFailed = 0;
            this.mClient.stopBackgroundReconnection();
            this.mClient.startExtremePingSender();
            this.mReconnectionDelay = RemoteControlConfig.SERVICE_TYPE_CARDIAGNOSIS_RUN_SH;
            this.mConnectedNetworkType = getNetworkType();
        } else if (i == 3) {
            if (oldState == STATE.CONNECTED) {
                int netType = getNetworkType();
                if (netType == this.mConnectedNetworkType) {
                    this.mState = STATE.CONNECTED;
                    LogUtils.d(TAG, "Skip the network change event cause of same network type!");
                }
            }
            if (oldState == STATE.CONNECTED) {
                this.mClient.scheduleReopen();
            } else if (oldState == STATE.DISCONNECTED) {
                this.mClient.stopBackgroundReconnection();
                this.mClient.startConnectImmediately(RemoteControlConfig.SERVICE_TYPE_CARDIAGNOSIS_RUN_SH);
            }
            this.mReconnectionDelay = RemoteControlConfig.SERVICE_TYPE_CARDIAGNOSIS_RUN_SH;
        } else if (i == 4) {
            resetFail();
            this.mReconnectionDelay = RemoteControlConfig.SERVICE_TYPE_CARDIAGNOSIS_RUN_SH;
            this.mClient.stopBackgroundReconnection();
            this.mClient.startBackgroundReconnection(this.mReconnectionDelay);
        }
    }

    private int getNetworkType() {
        int result = ContextNetStatusProvider.getNetType();
        ContextNetStatusProvider.getNetStrength(result);
        return result;
    }
}

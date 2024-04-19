package com.xiaopeng.lib.framework.netchannelmodule.messaging.events;

import android.support.annotation.Nullable;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.MessagingException;
import com.xiaopeng.lib.framework.netchannelmodule.common.BackgroundHandler;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
/* loaded from: classes.dex */
public class EventSender {
    private static final String THREAD_NAME = "EventSender";
    private static BackgroundHandler mHandler;
    private static volatile EventSender sCurrentSender = null;
    private IMessaging.CHANNEL mChannel;

    public static EventSender getCurrent() {
        return sCurrentSender;
    }

    public EventSender(IMessaging.CHANNEL channel) {
        this.mChannel = channel;
        sCurrentSender = this;
        mHandler = new BackgroundHandler(THREAD_NAME);
    }

    public void changeChannel(IMessaging.CHANNEL channel) {
        this.mChannel = channel;
    }

    public void postDeliveryCompleted(long messageId, @Nullable byte[] message) {
        ReportingEvent event = new ReportingEvent(IEvent.TYPE.DELIVERY_COMPLETED).channel(this.mChannel).messageId(messageId).message(message);
        post(event);
    }

    public void postDeliveryCompleted(long messageId) {
        ReportingEvent event = new ReportingEvent(IEvent.TYPE.DELIVERY_COMPLETED).channel(this.mChannel).messageId(messageId);
        post(event);
    }

    public void postDeliveryFailure(long messageId, int reasonCode) {
        ReportingEvent event = new ReportingEvent(IEvent.TYPE.DELIVERY_FAILURE).channel(this.mChannel).messageId(messageId).reasonCode(reasonCode);
        post(event);
    }

    public void postReceivedMessage(String topic, byte[] message) {
        ReceivedMessageEvent event = new ReceivedMessageEvent(IEvent.TYPE.RECEIVED_MESSAGE).channel(this.mChannel).topic(topic).message(message);
        postAtFront(event);
    }

    public void postConnected(String serverUrl) {
        ConnectionEvent event = new ConnectionEvent(IEvent.TYPE.CONNECTED).channel(this.mChannel).serverUri(serverUrl);
        postAtFront(event);
    }

    public void postDisconnected(@Nullable Throwable cause) {
        int reasonCode = 0;
        int protocolReasonCode = 0;
        String errMsg = null;
        if (cause != null) {
            errMsg = cause.getMessage();
            if (cause instanceof MqttException) {
                reasonCode = 1;
                protocolReasonCode = ((MqttException) cause).getReasonCode();
            } else if (cause instanceof MessagingException) {
                reasonCode = ((MessagingException) cause).getReasonCode();
            }
        }
        ConnectionEvent event = new ConnectionEvent(IEvent.TYPE.DISCONNECTED).channel(this.mChannel).reasonCode(reasonCode).serverUri(errMsg == null ? "" : errMsg).protocolReasonCode(protocolReasonCode);
        postAtFront(event);
    }

    public void postSubscribed(String topic) {
        SubscribeEvent event = new SubscribeEvent(IEvent.TYPE.SUBSCRIBED).channel(this.mChannel).topic(topic);
        post(event);
    }

    public void postUnsubscribed() {
        SubscribeEvent event = new SubscribeEvent(IEvent.TYPE.UNSUBSCRIBED).channel(this.mChannel);
        post(event);
    }

    public void postTracingLog(String message) {
        TracingEvent event = new TracingEvent().channel(this.mChannel).message(message);
        post(event);
    }

    public void close() {
        mHandler.stop();
    }

    private void post(final IEvent event) {
        mHandler.post(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.events.EventSender.1
            @Override // java.lang.Runnable
            public void run() {
                EventBus.getDefault().post(event);
            }
        });
    }

    private void postAtFront(final IEvent event) {
        mHandler.postAtFront(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.events.EventSender.2
            @Override // java.lang.Runnable
            public void run() {
                EventBus.getDefault().post(event);
            }
        });
    }
}

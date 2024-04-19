package com.xiaopeng.lib.framework.netchannelmodule.messaging;

import android.app.Application;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.exception.MessagingExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.ProfileFactory;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MqttChannel;
/* loaded from: classes.dex */
public class MessagingImpl implements IMessaging {
    public static final String TAG = "MessagingImpl";
    private AbstractChannelProfile mChannelProfile;

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging
    public void initChannelWithContext(IMessaging.CHANNEL channel, Application application) throws Exception {
        GlobalConfig.setApplicationContext(application);
        Module.register(DataLogModuleEntry.class, new DataLogModuleEntry(application));
        this.mChannelProfile = ProfileFactory.channelProfile(channel);
        MqttChannel.getInstance().init(application, this.mChannelProfile);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging
    public boolean available() {
        if (GlobalConfig.getApplicationContext() == null) {
            return false;
        }
        return MqttChannel.getInstance().isConnected();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging
    public void subscribe(String topic) throws Exception {
        if (GlobalConfig.getApplicationContext() == null) {
            throw new MessagingExceptionImpl(17);
        }
        if (!this.mChannelProfile.canSubscribe()) {
            throw new MessagingExceptionImpl(33);
        }
        MqttChannel.getInstance().subscribe(topic);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging
    public long publish(String topic, String message) throws Exception {
        if (GlobalConfig.getApplicationContext() == null) {
            throw new MessagingExceptionImpl(17);
        }
        if (!this.mChannelProfile.canPublish()) {
            throw new MessagingExceptionImpl(32);
        }
        return MqttChannel.getInstance().publishMessage(topic, message);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging
    public long publish(String topic, byte[] message) throws Exception {
        if (GlobalConfig.getApplicationContext() == null) {
            throw new MessagingExceptionImpl(17);
        }
        if (!this.mChannelProfile.canPublish()) {
            throw new MessagingExceptionImpl(32);
        }
        return MqttChannel.getInstance().publishMessage(topic, message);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging
    public long publish(String topic, String message, IMessaging.QOS qosLevel) throws Exception {
        if (GlobalConfig.getApplicationContext() == null) {
            throw new MessagingExceptionImpl(17);
        }
        if (!this.mChannelProfile.canPublish()) {
            throw new MessagingExceptionImpl(32);
        }
        return MqttChannel.getInstance().publishMessage(topic, message.getBytes(), qosLevel.value());
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging
    public long publish(String topic, byte[] message, IMessaging.QOS qosLevel) throws Exception {
        if (GlobalConfig.getApplicationContext() == null) {
            throw new MessagingExceptionImpl(17);
        }
        if (!this.mChannelProfile.canPublish()) {
            throw new MessagingExceptionImpl(32);
        }
        return MqttChannel.getInstance().publishMessage(topic, message, qosLevel.value());
    }
}

package com.xiaopeng.lib.framework.netchannelmodule.messaging.profile;

import android.os.SystemProperties;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
/* loaded from: classes.dex */
public final class CommunicationChannelProfile extends AbstractChannelProfile {
    private static final boolean FUNCTION_PUBLISH = true;
    private static final boolean FUNCTION_SUBSCRIBE = true;

    public CommunicationChannelProfile() {
        super(true, true);
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile
    @NonNull
    public String logTag() {
        return "PahoBizLogger";
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile
    @NonNull
    public String serverUrl() {
        String url = decode(SystemProperties.get(SYSTEM_PROPERTY_MQTT_COMM_URL));
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        return resolveWithDns("ssl://" + url);
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile
    public String clientId() {
        String decodedClientId;
        String clientId = SystemProperties.get(SYSTEM_PROPERTY_MQTT_COMM_ID);
        if (TextUtils.isEmpty(clientId)) {
            decodedClientId = super.clientId();
        } else {
            decodedClientId = decode(clientId);
        }
        return decodedClientId + ":" + BuildInfoUtils.getSystemVersion();
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile
    public IMessaging.CHANNEL channel() {
        return IMessaging.CHANNEL.COMMUNICATION;
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile
    public MqttConnectOptions buildConnectOptions() {
        MqttConnectOptions options = super.buildConnectOptions();
        options.setSocketFactory(GlobalConfig.getSocketFactory());
        options.setUserName(getMQTTUserName());
        options.setPassword(getMQTTPassword().toCharArray());
        return options;
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile
    public boolean enableExtremePing() {
        return true;
    }

    private String getMQTTUserName() {
        String userName = SystemProperties.get(SYSTEM_PROPERTY_MQTT_COMM_USER);
        if (TextUtils.isEmpty(userName)) {
            return decode(SystemProperties.get(SYSTEM_PROPERTY_MQTT_USER));
        }
        return decode(userName);
    }

    private String getMQTTPassword() {
        String password = SystemProperties.get(SYSTEM_PROPERTY_MQTT_COMM_PASS);
        if (TextUtils.isEmpty(password)) {
            return decode(SystemProperties.get(SYSTEM_PROPERTY_MQTT_PASS));
        }
        return decode(password);
    }
}

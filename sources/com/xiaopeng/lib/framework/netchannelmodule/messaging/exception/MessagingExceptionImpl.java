package com.xiaopeng.lib.framework.netchannelmodule.messaging.exception;

import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.MessagingException;
import org.eclipse.paho.client.mqttv3.MqttException;
/* loaded from: classes.dex */
public class MessagingExceptionImpl extends MessagingException {
    private static final long serialVersionUID = 100;
    private int mProtocolReasonCode;
    private int mReasonCode;

    public MessagingExceptionImpl(int reasonCode) {
        super("");
        this.mReasonCode = reasonCode;
        this.mProtocolReasonCode = 0;
    }

    public MessagingExceptionImpl(MqttException exception) {
        super(exception.getMessage());
        this.mReasonCode = 1;
        this.mProtocolReasonCode = exception.getReasonCode();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.MessagingException
    public int getReasonCode() {
        return this.mReasonCode;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.MessagingException
    public int getProtocolReasonCode() {
        return this.mProtocolReasonCode;
    }
}

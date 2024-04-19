package com.xiaopeng.lib.framework.netchannelmodule.messaging.events;

import android.support.annotation.Nullable;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging;
/* loaded from: classes.dex */
public class ReportingEvent implements IEvent {
    private IMessaging.CHANNEL mChannel;
    private byte[] mMessageBody;
    private long mMessageId;
    private int mProtocolReasonCode;
    private int mReasonCode;
    private IEvent.TYPE mType;

    public ReportingEvent(IEvent.TYPE eventType) {
        this.mType = eventType;
    }

    public ReportingEvent reasonCode(int code) {
        this.mReasonCode = code;
        return this;
    }

    public ReportingEvent messageId(long id) {
        this.mMessageId = id;
        return this;
    }

    public ReportingEvent message(byte[] body) {
        this.mMessageBody = body;
        return this;
    }

    public ReportingEvent protocolReasonCode(int code) {
        this.mProtocolReasonCode = code;
        return this;
    }

    public ReportingEvent channel(IMessaging.CHANNEL channel) {
        this.mChannel = channel;
        return this;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    public IMessaging.CHANNEL channel() {
        return this.mChannel;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    public IEvent.TYPE type() {
        return this.mType;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    public int reasonCode() {
        return this.mReasonCode;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    public long messageId() {
        return this.mMessageId;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    @Nullable
    public String messageTopic() {
        return null;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    @Nullable
    public byte[] messageContent() {
        return this.mMessageBody;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    public int protocolReasonCode() {
        return this.mProtocolReasonCode;
    }
}

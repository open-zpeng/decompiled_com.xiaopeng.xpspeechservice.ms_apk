package com.xiaopeng.lib.framework.netchannelmodule.messaging.events;

import android.support.annotation.Nullable;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging;
/* loaded from: classes.dex */
public class ConnectionEvent implements IEvent {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private IMessaging.CHANNEL mChannel;
    private int mProtocolReasonCode = 0;
    private int mReasonCode;
    private byte[] mServerUri;
    private IEvent.TYPE mType;

    public ConnectionEvent(IEvent.TYPE eventType) {
        this.mType = eventType;
    }

    public ConnectionEvent reasonCode(int code) {
        this.mReasonCode = code;
        return this;
    }

    public ConnectionEvent protocolReasonCode(int code) {
        this.mProtocolReasonCode = code;
        return this;
    }

    public ConnectionEvent channel(IMessaging.CHANNEL channel) {
        this.mChannel = channel;
        return this;
    }

    public ConnectionEvent serverUri(String uri) {
        this.mServerUri = uri.getBytes();
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
        return 0L;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    @Nullable
    public String messageTopic() {
        return null;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    @Nullable
    public byte[] messageContent() {
        return this.mServerUri;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IEvent
    public int protocolReasonCode() {
        return this.mProtocolReasonCode;
    }
}

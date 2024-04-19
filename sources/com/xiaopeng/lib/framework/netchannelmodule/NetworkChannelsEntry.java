package com.xiaopeng.lib.framework.netchannelmodule;

import com.xiaopeng.lib.framework.module.IModuleEntry;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IRxWebSocket;
import com.xiaopeng.lib.framework.netchannelmodule.http.HttpImpl;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.MessagingImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.RemoteAwsStorageImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.RemoteOssStorageImpl;
import com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketImpl;
import com.xiaopeng.lib.utils.info.DeviceInfoUtils;
/* loaded from: classes.dex */
public class NetworkChannelsEntry implements IModuleEntry {
    private final Creator<? extends IRemoteStorage> mRemoteStorageChannel;
    private final Creator<MessagingImpl> mMessagingChannel = new Creator<>(MessagingImpl.class);
    private final Creator<HttpImpl> mHttpChannel = new Creator<>(HttpImpl.class);
    private final Creator<RxWebSocketImpl> mWebSocketChannel = new Creator<>(RxWebSocketImpl.class);

    public NetworkChannelsEntry() {
        if (DeviceInfoUtils.isInternationalVer()) {
            this.mRemoteStorageChannel = new Creator<>(RemoteAwsStorageImpl.class);
        } else {
            this.mRemoteStorageChannel = new Creator<>(RemoteOssStorageImpl.class);
        }
    }

    @Override // com.xiaopeng.lib.framework.module.IModuleEntry
    public Object get(Class interfaceClass) {
        if (IMessaging.class == interfaceClass) {
            return this.mMessagingChannel.get(this);
        }
        if (IRemoteStorage.class == interfaceClass) {
            return this.mRemoteStorageChannel.get(this);
        }
        if (IHttp.class == interfaceClass) {
            return this.mHttpChannel.get(this);
        }
        if (IRxWebSocket.class == interfaceClass) {
            return this.mWebSocketChannel.get(this);
        }
        return null;
    }

    /* loaded from: classes.dex */
    private static class Creator<T> {
        private Class<T> mClassType;
        private volatile T mInstance;

        public Creator(Class<T> classz) {
            this.mClassType = classz;
        }

        public T get(Object lockObject) {
            if (this.mInstance == null) {
                synchronized (lockObject) {
                    if (this.mInstance == null) {
                        try {
                            this.mInstance = this.mClassType.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to initialize the channel.");
                        }
                    }
                }
            }
            return this.mInstance;
        }
    }
}

package com.xiaopeng.lib.framework.configuration;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.xiaopeng.lib.framework.configuration.IConfigurationServiceCallback;
/* loaded from: classes.dex */
public interface IConfigurationServiceInterface extends IInterface {
    String getConfiguration(String str, String str2) throws RemoteException;

    void subscribe(String str, String str2, int i, IConfigurationServiceCallback iConfigurationServiceCallback) throws RemoteException;

    void unsubscribe(String str) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements IConfigurationServiceInterface {
        private static final String DESCRIPTOR = "com.xiaopeng.lib.framework.configuration.IConfigurationServiceInterface";
        static final int TRANSACTION_getConfiguration = 3;
        static final int TRANSACTION_subscribe = 1;
        static final int TRANSACTION_unsubscribe = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConfigurationServiceInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IConfigurationServiceInterface)) {
                return (IConfigurationServiceInterface) iin;
            }
            return new Proxy(obj);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                String _arg1 = data.readString();
                int _arg2 = data.readInt();
                IConfigurationServiceCallback _arg3 = IConfigurationServiceCallback.Stub.asInterface(data.readStrongBinder());
                subscribe(_arg0, _arg1, _arg2, _arg3);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                unsubscribe(_arg02);
                reply.writeNoException();
                return true;
            } else if (code != 3) {
                if (code == 1598968902) {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                return super.onTransact(code, data, reply, flags);
            } else {
                data.enforceInterface(DESCRIPTOR);
                String _arg03 = data.readString();
                String _arg12 = data.readString();
                String _result = getConfiguration(_arg03, _arg12);
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            }
        }

        /* loaded from: classes.dex */
        private static class Proxy implements IConfigurationServiceInterface {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.xiaopeng.lib.framework.configuration.IConfigurationServiceInterface
            public void subscribe(String appID, String appVersion, int appVersionCode, IConfigurationServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appID);
                    _data.writeString(appVersion);
                    _data.writeInt(appVersionCode);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.xiaopeng.lib.framework.configuration.IConfigurationServiceInterface
            public void unsubscribe(String appID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appID);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.xiaopeng.lib.framework.configuration.IConfigurationServiceInterface
            public String getConfiguration(String appID, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appID);
                    _data.writeString(key);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}

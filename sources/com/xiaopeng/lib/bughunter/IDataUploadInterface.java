package com.xiaopeng.lib.bughunter;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
/* loaded from: classes.dex */
public interface IDataUploadInterface extends IInterface {
    void uploadCan(String str) throws RemoteException;

    void uploadFiles(List<String> list) throws RemoteException;

    void uploadFilesWithParam(List<String> list, String str) throws RemoteException;

    void uploadLog(String str) throws RemoteException;

    void uploadLogImmediately(String str, String str2) throws RemoteException;

    void uploadLogOrigin(String str, String str2) throws RemoteException;

    void uploadSystemLog(String str) throws RemoteException;

    /* loaded from: classes.dex */
    public static abstract class Stub extends Binder implements IDataUploadInterface {
        private static final String DESCRIPTOR = "com.xiaopeng.lib.bughunter.IDataUploadInterface";
        static final int TRANSACTION_uploadCan = 3;
        static final int TRANSACTION_uploadFiles = 5;
        static final int TRANSACTION_uploadFilesWithParam = 7;
        static final int TRANSACTION_uploadLog = 1;
        static final int TRANSACTION_uploadLogImmediately = 4;
        static final int TRANSACTION_uploadLogOrigin = 2;
        static final int TRANSACTION_uploadSystemLog = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDataUploadInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IDataUploadInterface)) {
                return (IDataUploadInterface) iin;
            }
            return new Proxy(obj);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1598968902) {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    uploadLog(_arg0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    String _arg1 = data.readString();
                    uploadLogOrigin(_arg02, _arg1);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg03 = data.readString();
                    uploadCan(_arg03);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg04 = data.readString();
                    String _arg12 = data.readString();
                    uploadLogImmediately(_arg04, _arg12);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    List<String> _arg05 = data.createStringArrayList();
                    uploadFiles(_arg05);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg06 = data.readString();
                    uploadSystemLog(_arg06);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    List<String> _arg07 = data.createStringArrayList();
                    String _arg13 = data.readString();
                    uploadFilesWithParam(_arg07, _arg13);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* loaded from: classes.dex */
        private static class Proxy implements IDataUploadInterface {
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

            @Override // com.xiaopeng.lib.bughunter.IDataUploadInterface
            public void uploadLog(String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(data);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xiaopeng.lib.bughunter.IDataUploadInterface
            public void uploadLogOrigin(String eventName, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(eventName);
                    _data.writeString(data);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xiaopeng.lib.bughunter.IDataUploadInterface
            public void uploadCan(String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(data);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xiaopeng.lib.bughunter.IDataUploadInterface
            public void uploadLogImmediately(String eventName, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(eventName);
                    _data.writeString(data);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xiaopeng.lib.bughunter.IDataUploadInterface
            public void uploadFiles(List<String> filePaths) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(filePaths);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xiaopeng.lib.bughunter.IDataUploadInterface
            public void uploadSystemLog(String ossUrl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ossUrl);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.xiaopeng.lib.bughunter.IDataUploadInterface
            public void uploadFilesWithParam(List<String> filePaths, String params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(filePaths);
                    _data.writeString(params);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}

package com.xiaopeng.lib.framework.netchannelmodule.http.traffic.plain;

import android.os.Build;
import com.xiaopeng.lib.utils.LogUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;
/* loaded from: classes.dex */
public class CountingPlainSocketInstrument implements SocketImplFactory {
    private static Constructor mPlainSocketImplConstruct;
    private static Constructor mSocksSocketImplConstruct;
    private boolean mIsServer;

    public CountingPlainSocketInstrument(boolean isServer) {
        this.mIsServer = isServer;
    }

    public static synchronized void initialize() {
        synchronized (CountingPlainSocketInstrument.class) {
            try {
                Class clazz = Class.forName("java.net.PlainSocketImpl");
                mPlainSocketImplConstruct = clazz.getDeclaredConstructor(new Class[0]);
                mPlainSocketImplConstruct.setAccessible(true);
                Socket.setSocketImplFactory(new CountingPlainSocketInstrument(false));
            } catch (Exception e) {
                LogUtils.w(e.toString());
            }
            if (!isKitkat()) {
                try {
                    Class clazz2 = Class.forName("java.net.SocksSocketImpl");
                    mSocksSocketImplConstruct = clazz2.getDeclaredConstructor(new Class[0]);
                    mSocksSocketImplConstruct.setAccessible(true);
                    ServerSocket.setSocketFactory(new CountingPlainSocketInstrument(true));
                } catch (Exception e2) {
                    LogUtils.w(e2.toString());
                }
            }
        }
    }

    private static boolean isKitkat() {
        return Build.VERSION.SDK_INT == 19;
    }

    public static synchronized void reset() {
        synchronized (CountingPlainSocketInstrument.class) {
            try {
                Field field = Socket.class.getDeclaredField("factory");
                field.setAccessible(true);
                field.set(null, null);
            } catch (Exception e) {
                LogUtils.w(e.toString());
            }
            if (!isKitkat()) {
                try {
                    Field field2 = ServerSocket.class.getDeclaredField("factory");
                    field2.setAccessible(true);
                    field2.set(null, null);
                } catch (Exception e2) {
                    LogUtils.w(e2.toString());
                }
            }
        }
    }

    @Override // java.net.SocketImplFactory
    public SocketImpl createSocketImpl() {
        try {
            if (isKitkat()) {
                return new HookPlainSocketKitKatImpl((SocketImpl) mPlainSocketImplConstruct.newInstance(new Object[0]));
            }
            return new HookPlainSocketPImpl((SocketImpl) (this.mIsServer ? mSocksSocketImplConstruct : mPlainSocketImplConstruct).newInstance(new Object[0]));
        } catch (Exception e) {
            if (!isKitkat() && this.mIsServer) {
                Constructor constructor = mSocksSocketImplConstruct;
                if (constructor != null) {
                    try {
                        return (SocketImpl) constructor.newInstance(new Object[0]);
                    } catch (Exception e1) {
                        LogUtils.w(e1.toString());
                        LogUtils.w(e.toString());
                        return new EmptySocketImpl();
                    }
                }
            } else {
                Constructor constructor2 = mPlainSocketImplConstruct;
                if (constructor2 != null) {
                    try {
                        return (SocketImpl) constructor2.newInstance(new Object[0]);
                    } catch (Exception e12) {
                        LogUtils.w(e12.toString());
                        LogUtils.w(e.toString());
                        return new EmptySocketImpl();
                    }
                }
            }
            LogUtils.w(e.toString());
            return new EmptySocketImpl();
        }
    }
}

package com.xiaopeng.lib.framework.netchannelmodule.http.traffic.plain;

import com.lzy.okgo.model.HttpHeaders;
import com.xiaopeng.lib.framework.netchannelmodule.http.statistic.SocketCounter;
import com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingInputStream;
import com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingOutputStream;
import com.xiaopeng.lib.framework.netchannelmodule.http.traffic.ICollector;
import com.xiaopeng.lib.utils.LogUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.util.HashMap;
import java.util.Map;
/* loaded from: classes.dex */
public class HookPlainSocketPImpl extends SocketImpl {
    private static final String TAG = "SocketImplHook";
    private static Field mFieldAddress;
    private static Field mFieldFd;
    private static Field mFieldLocalPort;
    private static Field mFieldPort;
    private static Class mSuperClass;
    private static Map<String, Method> methodMap = new HashMap();
    private ICollector mCollector = new ICollector() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.traffic.plain.HookPlainSocketPImpl.1
        @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.ICollector
        public String getDomain() {
            return HookPlainSocketPImpl.this.mDomain;
        }
    };
    private SocketCounter mCounter = SocketCounter.getInstance();
    private SocketImpl mDelegate;
    private String mDomain;

    static {
        try {
            mSuperClass = Class.forName("java.net.AbstractPlainSocketImpl");
            mFieldAddress = SocketImpl.class.getDeclaredField("address");
            mFieldPort = SocketImpl.class.getDeclaredField("port");
            mFieldLocalPort = SocketImpl.class.getDeclaredField("localport");
            mFieldFd = SocketImpl.class.getDeclaredField("fd");
            mFieldAddress.setAccessible(true);
            mFieldPort.setAccessible(true);
            mFieldFd.setAccessible(true);
            mFieldLocalPort.setAccessible(true);
        } catch (Exception e) {
            LogUtils.d(TAG, e);
        }
    }

    private static Method getMethod(String tag, Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method m = methodMap.get(tag);
        if (m == null) {
            try {
                m = clazz.getDeclaredMethod(name, parameterTypes);
                m.setAccessible(true);
                methodMap.put(tag, m);
                return m;
            } catch (Exception e) {
                LogUtils.d(TAG, e);
                return m;
            }
        }
        return m;
    }

    public HookPlainSocketPImpl(SocketImpl delegate) {
        this.mDelegate = delegate;
    }

    @Override // java.net.SocketImpl
    protected int getPort() {
        return ((Integer) getValue(mFieldPort, this.mDelegate, Integer.valueOf(this.port))).intValue();
    }

    @Override // java.net.SocketImpl
    protected InetAddress getInetAddress() {
        return (InetAddress) getValue(mFieldAddress, this.mDelegate, this.address);
    }

    @Override // java.net.SocketImpl
    protected int getLocalPort() {
        return ((Integer) getValue(mFieldLocalPort, this.mDelegate, Integer.valueOf(this.localport))).intValue();
    }

    @Override // java.net.SocketImpl
    protected FileDescriptor getFileDescriptor() {
        return (FileDescriptor) getValue(mFieldFd, this.mDelegate, this.fd);
    }

    private void syncFd() {
        this.fd = (FileDescriptor) getValue(mFieldFd, this.mDelegate, this.fd);
    }

    public void syncFromLocalToDelegate() {
        setValue(mFieldFd, this.mDelegate, this.fd);
        setValue(mFieldAddress, this.mDelegate, this.address);
        setValue(mFieldPort, this.mDelegate, Integer.valueOf(this.port));
        setValue(mFieldLocalPort, this.mDelegate, Integer.valueOf(this.localport));
    }

    @Override // java.net.SocketImpl
    protected void accept(SocketImpl newSocket) throws IOException {
        if (newSocket instanceof HookPlainSocketPImpl) {
            ((HookPlainSocketPImpl) newSocket).syncFromLocalToDelegate();
        }
        Method method = getMethod("accept", mSuperClass, "accept", SocketImpl.class);
        invoke(method, true, this.mDelegate, newSocket);
    }

    @Override // java.net.SocketImpl
    protected void shutdownInput() throws IOException {
        Method method = getMethod("shutdownInput", mSuperClass, "shutdownInput", new Class[0]);
        invoke(method, true, this.mDelegate, new Object[0]);
    }

    @Override // java.net.SocketImpl
    protected void shutdownOutput() throws IOException {
        Method method = getMethod("shutdownOutput", mSuperClass, "shutdownOutput", new Class[0]);
        invoke(method, true, this.mDelegate, new Object[0]);
    }

    @Override // java.net.SocketImpl
    protected int available() throws IOException {
        Method method = getMethod("available", mSuperClass, "available", new Class[0]);
        Integer result = (Integer) invoke(method, false, this.mDelegate, new Object[0]);
        if (result == null) {
            return -1;
        }
        return result.intValue();
    }

    @Override // java.net.SocketImpl
    protected void bind(InetAddress address, int port) throws IOException {
        Method method = getMethod("bind", mSuperClass, "bind", InetAddress.class, Integer.TYPE);
        invoke(method, true, this.mDelegate, address, Integer.valueOf(port));
    }

    @Override // java.net.SocketImpl
    protected void close() throws IOException {
        Method method = getMethod(HttpHeaders.HEAD_VALUE_CONNECTION_CLOSE, mSuperClass, HttpHeaders.HEAD_VALUE_CONNECTION_CLOSE, new Class[0]);
        invoke(method, true, this.mDelegate, new Object[0]);
    }

    @Override // java.net.SocketImpl
    protected void connect(String host, int port) throws IOException {
        LogUtils.d(TAG, "host=" + host + ",port=" + port);
        StringBuilder sb = new StringBuilder();
        sb.append(host);
        sb.append(":");
        sb.append(port);
        this.mDomain = sb.toString();
        Method method = getMethod("connect0", mSuperClass, "connect", String.class, Integer.TYPE);
        invoke(method, true, this.mDelegate, host, Integer.valueOf(port));
        this.mCounter.increaseRequest(this.mDomain, 0L);
    }

    @Override // java.net.SocketImpl
    protected void connect(InetAddress address, int port) throws IOException {
        LogUtils.d(TAG, "InetAddr=" + address + ", port=" + port);
        StringBuilder sb = new StringBuilder();
        sb.append(address.getHostName());
        sb.append(":");
        sb.append(port);
        this.mDomain = sb.toString();
        Method method = getMethod("connect1", mSuperClass, "connect", InetAddress.class, Integer.TYPE);
        invoke(method, true, this.mDelegate, address, Integer.valueOf(port));
        this.mCounter.increaseRequest(this.mDomain, 0L);
    }

    @Override // java.net.SocketImpl
    protected void connect(SocketAddress remoteAddr, int timeout) throws IOException {
        LogUtils.d(TAG, "SocketAddr=" + remoteAddr);
        this.mDomain = remoteAddr + "";
        Method method = getMethod("connect2", mSuperClass, "connect", SocketAddress.class, Integer.TYPE);
        invoke(method, true, this.mDelegate, remoteAddr, Integer.valueOf(timeout));
        this.mCounter.increaseRequest(this.mDomain, 0L);
    }

    @Override // java.net.SocketImpl
    protected void create(boolean isStreaming) throws IOException {
        Method method = getMethod("create", mSuperClass, "create", Boolean.TYPE);
        invoke(method, true, this.mDelegate, Boolean.valueOf(isStreaming));
        syncFd();
    }

    @Override // java.net.SocketImpl
    protected InputStream getInputStream() throws IOException {
        Method method = getMethod("getInputStream", mSuperClass, "getInputStream", new Class[0]);
        InputStream stream = (InputStream) invoke(method, false, this.mDelegate, new Object[0]);
        SocketCounter.getInstance().increaseSucceedWithSize(this.mDomain, 0L);
        if (stream == null) {
            return null;
        }
        return new CountingInputStream(this.mCollector, stream).setStatisticCounter(this.mCounter);
    }

    @Override // java.net.SocketImpl
    protected OutputStream getOutputStream() throws IOException {
        Method method = getMethod("getOutputStream", mSuperClass, "getOutputStream", new Class[0]);
        OutputStream stream = (OutputStream) invoke(method, false, this.mDelegate, new Object[0]);
        if (stream == null) {
            return null;
        }
        return new CountingOutputStream(this.mCollector, stream).setStatisticCounter(this.mCounter);
    }

    @Override // java.net.SocketImpl
    protected void listen(int backlog) throws IOException {
        Method method = getMethod("listen", mSuperClass, "listen", Integer.TYPE);
        invoke(method, true, this.mDelegate, Integer.valueOf(backlog));
    }

    @Override // java.net.SocketImpl
    protected void sendUrgentData(int value) throws IOException {
        Method method = getMethod("sendUrgentData", mSuperClass, "sendUrgentData", Integer.TYPE);
        invoke(method, true, this.mDelegate, Integer.valueOf(value));
    }

    @Override // java.net.SocketOptions
    public Object getOption(int optID) throws SocketException {
        return this.mDelegate.getOption(optID);
    }

    @Override // java.net.SocketOptions
    public void setOption(int optID, Object val) throws SocketException {
        syncFd();
        this.mDelegate.setOption(optID, val);
    }

    private <T> T getValue(Field field, Object target, T defaultV) {
        if (field != null) {
            try {
                return (T) field.get(target);
            } catch (Exception e) {
                LogUtils.d(TAG, e);
            }
        }
        return defaultV;
    }

    private <T> void setValue(Field field, Object target, Object value) {
        if (field != null) {
            try {
                field.set(target, value);
            } catch (Exception e) {
                LogUtils.d(TAG, e);
            }
        }
    }

    private <T> T invoke(Method method, boolean emptyReturn, Object target, Object... param) throws IOException {
        if (method == null) {
            return null;
        }
        try {
            if (emptyReturn) {
                method.invoke(target, param);
                return null;
            }
            return (T) method.invoke(target, param);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            if (throwable instanceof IOException) {
                throw ((IOException) throwable);
            }
            throw new IOException(e.toString());
        }
    }
}

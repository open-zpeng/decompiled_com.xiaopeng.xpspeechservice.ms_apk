package com.xiaopeng.lib.framework.netchannelmodule.http.traffic.plain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
/* loaded from: classes.dex */
public class EmptySocketImpl extends SocketImpl {
    @Override // java.net.SocketImpl
    protected void accept(SocketImpl newSocket) throws IOException {
    }

    @Override // java.net.SocketImpl
    protected int available() throws IOException {
        return 0;
    }

    @Override // java.net.SocketImpl
    protected void bind(InetAddress address, int port) throws IOException {
    }

    @Override // java.net.SocketImpl
    protected void close() throws IOException {
    }

    @Override // java.net.SocketImpl
    protected void connect(String host, int port) throws IOException {
    }

    @Override // java.net.SocketImpl
    protected void connect(InetAddress address, int port) throws IOException {
    }

    @Override // java.net.SocketImpl
    protected void create(boolean isStreaming) throws IOException {
    }

    @Override // java.net.SocketImpl
    protected InputStream getInputStream() throws IOException {
        return null;
    }

    @Override // java.net.SocketImpl
    protected OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override // java.net.SocketImpl
    protected void listen(int backlog) throws IOException {
    }

    @Override // java.net.SocketImpl
    protected void connect(SocketAddress remoteAddr, int timeout) throws IOException {
    }

    @Override // java.net.SocketImpl
    protected void sendUrgentData(int value) throws IOException {
    }

    @Override // java.net.SocketOptions
    public Object getOption(int optID) throws SocketException {
        return null;
    }

    @Override // java.net.SocketOptions
    public void setOption(int optID, Object val) throws SocketException {
    }
}

package com.xiaopeng.lib.framework.netchannelmodule.http.traffic.tls;

import android.support.annotation.NonNull;
import com.xiaopeng.lib.framework.netchannelmodule.http.statistic.SocketCounter;
import com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingInputStream;
import com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingOutputStream;
import com.xiaopeng.lib.framework.netchannelmodule.http.traffic.ICollector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
/* loaded from: classes.dex */
public class HookTLSKitkatSocket {
    private static Constructor mInputConstruct;
    private static Field mInputField;
    private static Constructor mOutputConstruct;
    private static Field mOutputField;

    public static Socket createSocket(Socket socket, final String domain) throws Exception {
        if (!(socket instanceof SSLSocket)) {
            return socket;
        }
        if (mInputConstruct == null || mOutputConstruct == null) {
            mInputConstruct = Class.forName("com.android.org.conscrypt.OpenSSLSocketImpl$SSLInputStream").getDeclaredConstructors()[0];
            mOutputConstruct = Class.forName("com.android.org.conscrypt.OpenSSLSocketImpl$SSLOutputStream").getDeclaredConstructors()[0];
            mInputField = Class.forName("com.android.org.conscrypt.OpenSSLSocketImpl").getDeclaredField("is");
            mOutputField = Class.forName("com.android.org.conscrypt.OpenSSLSocketImpl").getDeclaredField("os");
            mInputConstruct.setAccessible(true);
            mOutputConstruct.setAccessible(true);
            mInputField.setAccessible(true);
            mOutputField.setAccessible(true);
        }
        SSLSocket sslSocket = (SSLSocket) socket;
        ICollector collector = new ICollector() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.traffic.tls.HookTLSKitkatSocket.1
            @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.ICollector
            public String getDomain() {
                return domain;
            }
        };
        KitKatTLSInputStream i = new KitKatTLSInputStream(collector, sslSocket);
        KitKatTLSOutputStream o = new KitKatTLSOutputStream(collector, sslSocket);
        i.setStatisticCounter(SocketCounter.getInstance());
        o.setStatisticCounter(SocketCounter.getInstance());
        mInputField.set(socket, i);
        mOutputField.set(socket, o);
        return sslSocket;
    }

    /* loaded from: classes.dex */
    private static class KitKatTLSInputStream extends CountingInputStream {
        private SSLSocket socket;

        public KitKatTLSInputStream(@NonNull ICollector collector, @NonNull SSLSocket sslSocket) throws IOException {
            super(collector, null);
            this.socket = sslSocket;
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingInputStream, java.io.FilterInputStream, java.io.InputStream
        public int read(byte[] b, int off, int len) throws IOException {
            checkInputStream();
            increaseSuccess();
            return super.read(b, off, len);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingInputStream, java.io.FilterInputStream, java.io.InputStream
        public int read() throws IOException {
            checkInputStream();
            increaseSuccess();
            return super.read();
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingInputStream, java.io.FilterInputStream, java.io.InputStream
        public int read(byte[] b) throws IOException {
            checkInputStream();
            increaseSuccess();
            return super.read(b);
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public int available() throws IOException {
            checkInputStream();
            return super.available();
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public synchronized void reset() throws IOException {
            checkInputStream();
            super.reset();
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public long skip(long byteCount) throws IOException {
            checkInputStream();
            return super.skip(byteCount);
        }

        public void checkInputStream() throws IOException {
            if (this.in == null) {
                try {
                    this.in = (InputStream) HookTLSKitkatSocket.mInputConstruct.newInstance(this.socket);
                } catch (Exception e) {
                    Throwable c = e.getCause();
                    throw new IOException(c == null ? e : c);
                }
            }
        }

        public void increaseSuccess() {
            this.mCounter.increaseSucceedWithSize(this.mCollector.getDomain(), 0L);
        }
    }

    /* loaded from: classes.dex */
    private static class KitKatTLSOutputStream extends CountingOutputStream {
        private SSLSocket socket;

        public KitKatTLSOutputStream(@NonNull ICollector collector, @NonNull SSLSocket sslSocket) throws IOException {
            super(collector, null);
            this.socket = sslSocket;
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingOutputStream, java.io.FilterOutputStream, java.io.OutputStream
        public void write(int b) throws IOException {
            checkInputStream();
            increaseSuccess();
            super.write(b);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingOutputStream, java.io.FilterOutputStream, java.io.OutputStream
        public void write(byte[] b) throws IOException {
            checkInputStream();
            increaseSuccess();
            super.write(b);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingOutputStream, java.io.FilterOutputStream, java.io.OutputStream
        public void write(byte[] b, int off, int len) throws IOException {
            checkInputStream();
            increaseSuccess();
            super.write(b, off, len);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingOutputStream, java.io.FilterOutputStream, java.io.OutputStream, java.io.Flushable
        public void flush() throws IOException {
            checkInputStream();
            super.flush();
        }

        public void checkInputStream() throws IOException {
            if (this.out == null) {
                try {
                    this.out = (OutputStream) HookTLSKitkatSocket.mOutputConstruct.newInstance(this.socket);
                } catch (Exception e) {
                    Throwable c = e.getCause();
                    throw new IOException(c == null ? e : c);
                }
            }
        }

        public void increaseSuccess() {
            this.mCounter.increaseSucceedWithSize(this.mCollector.getDomain(), 0L);
        }
    }
}

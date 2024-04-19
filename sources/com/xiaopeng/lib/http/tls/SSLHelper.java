package com.xiaopeng.lib.http.tls;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import com.xiaopeng.lib.security.xmartv1.XmartV1Constants;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.TlsVersion;
/* loaded from: classes.dex */
public class SSLHelper {
    private static ISSLSocketWrapper mWrapper;
    private static final boolean sUseLocalCertFile;

    /* loaded from: classes.dex */
    public interface ISSLSocketWrapper {
        Socket createSocket(Socket socket, String str) throws IOException;
    }

    static {
        sUseLocalCertFile = Build.VERSION.SDK_INT == 19;
    }

    public static List<ConnectionSpec> getConnectionSpecs() {
        ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2).cipherSuites(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256).build();
        List<ConnectionSpec> specs = new ArrayList<>();
        specs.add(cs);
        specs.add(ConnectionSpec.COMPATIBLE_TLS);
        specs.add(ConnectionSpec.CLEARTEXT);
        return specs;
    }

    public static SSLSocketFactory getTLS2SocketFactory(Context context) {
        if (sUseLocalCertFile) {
            return LocalFileCert.getTLS2SocketFactory(context);
        }
        return KeyStoreCert.getTLS2SocketFactory();
    }

    public static X509TrustManager getX509TrustManager(Context context) {
        if (sUseLocalCertFile) {
            return LocalFileCert.getX509TrustManager(context);
        }
        return KeyStoreCert.getX509TrustManager();
    }

    /* loaded from: classes.dex */
    public static class TLS2SocketFactory extends SSLSocketFactory {
        private final String[] TLS_V12_ONLY = {XmartV1Constants.TLS_REVISION_1_2};
        final SSLSocketFactory delegate;

        public TLS2SocketFactory(SSLSocketFactory base) {
            this.delegate = base;
        }

        @Override // javax.net.ssl.SSLSocketFactory
        public String[] getDefaultCipherSuites() {
            return this.delegate.getDefaultCipherSuites();
        }

        @Override // javax.net.ssl.SSLSocketFactory
        public String[] getSupportedCipherSuites() {
            return this.delegate.getSupportedCipherSuites();
        }

        @Override // javax.net.ssl.SSLSocketFactory
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            Socket createSocket = this.delegate.createSocket(s, host, port, autoClose);
            return patch(createSocket, host + ":" + port);
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(String host, int port) throws IOException {
            Socket createSocket = this.delegate.createSocket(host, port);
            return patch(createSocket, host + ":" + port);
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            Socket createSocket = this.delegate.createSocket(host, port, localHost, localPort);
            return patch(createSocket, host + ":" + port);
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(InetAddress host, int port) throws IOException {
            StringBuilder sb;
            if (host == null) {
                sb = new StringBuilder();
            } else {
                sb = new StringBuilder();
                sb.append(host.getHostName());
            }
            sb.append(":");
            sb.append(port);
            String h = sb.toString();
            return patch(this.delegate.createSocket(host, port), h);
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            StringBuilder sb;
            if (address == null) {
                sb = new StringBuilder();
            } else {
                sb = new StringBuilder();
                sb.append(address.getHostName());
            }
            sb.append(":");
            sb.append(port);
            String h = sb.toString();
            return patch(this.delegate.createSocket(address, port, localAddress, localPort), h);
        }

        private Socket patch(Socket s, String domain) throws IOException {
            if (SSLHelper.mWrapper != null) {
                s = SSLHelper.mWrapper.createSocket(s, domain);
            }
            if (s instanceof SSLSocket) {
                ((SSLSocket) s).setEnabledProtocols(this.TLS_V12_ONLY);
            }
            return s;
        }
    }

    public static void setSSLSocketWrapper(@NonNull ISSLSocketWrapper wrapper) {
        mWrapper = wrapper;
    }
}

package com.xiaopeng.lib.http.tls;

import android.os.Build;
import android.util.Log;
import com.xiaopeng.lib.http.tls.SSLHelper;
import com.xiaopeng.lib.security.xmartv1.XmartV1Constants;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
/* loaded from: classes.dex */
public class KeyStoreCert {
    private static final String CA_CERT_PATH = "/system/etc/index_kstp.html";
    private static final String KEYSTORE_BKS = "BKS";
    private static final String KEY_STORE_CACERT_PASSWORD = "chengzi";
    private static final String TAG = "KeyStoreCert";

    public static SSLSocketFactory getTLS2SocketFactory() {
        CompositeX509TrustManager trustManager;
        try {
            SSLContext sslContext = SSLContext.getInstance(XmartV1Constants.TLS_REVISION_1_2);
            KeyStore tks = KeyStore.getInstance(XmartV1Constants.KEY_STORE_TYPE);
            tks.load(null);
            if (Build.VERSION.SDK_INT > 28) {
                trustManager = new CompositeX509TrustManager();
            } else {
                trustManager = new CompositeX509TrustManager(tks);
            }
            TrustManager[] ts = {trustManager};
            KeyManager[] ks = {new CompositeX509KeyManager(tks)};
            sslContext.init(ks, ts, null);
            return new SSLHelper.TLS2SocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static X509TrustManager getX509TrustManager() {
        try {
            if (Build.VERSION.SDK_INT > 28) {
                CompositeX509TrustManager trustManager = new CompositeX509TrustManager();
                Log.i(TAG, "getX509TrustManager from  Android  Q ");
                return trustManager;
            }
            Log.i(TAG, "getX509TrustManager from  Android  P ");
            KeyStore tks = KeyStore.getInstance(XmartV1Constants.KEY_STORE_TYPE);
            tks.load(null);
            return new CompositeX509TrustManager(tks);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* loaded from: classes.dex */
    private static class CompositeX509TrustManager implements X509TrustManager {
        private List<X509Certificate> certificates;
        private boolean hasValidCert;
        private KeyStore keyStore;
        private final List<X509TrustManager> trustManagers;

        public CompositeX509TrustManager(KeyStore keyStore) {
            this.certificates = null;
            this.hasValidCert = false;
            this.keyStore = null;
            Log.i(KeyStoreCert.TAG, "init Android P  CompositeX509TrustManager ");
            this.trustManagers = new ArrayList();
            this.keyStore = keyStore;
            try {
                KeyStore newKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
                newKeystore.load(null);
                String[] cas = {"ca", "client", "server", "alias"};
                for (String c : cas) {
                    try {
                        Certificate certificate = keyStore.getCertificate(c);
                        if (certificate instanceof X509Certificate) {
                            newKeystore.setCertificateEntry(c, certificate);
                        }
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    }
                }
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                tmf.init(newKeystore);
                TrustManager[] trustManagers = tmf.getTrustManagers();
                if (trustManagers != null) {
                    for (TrustManager trustManager : trustManagers) {
                        if (trustManager instanceof X509TrustManager) {
                            this.trustManagers.add((X509TrustManager) trustManager);
                        }
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        public CompositeX509TrustManager() {
            this.certificates = null;
            this.hasValidCert = false;
            this.keyStore = null;
            Log.i(KeyStoreCert.TAG, "init Android Q  CompositeX509TrustManager ");
            this.trustManagers = new ArrayList();
            this.certificates = new ArrayList();
            try {
                KeyStore tks = KeyStore.getInstance(KeyStoreCert.KEYSTORE_BKS);
                InputStream tsIn = new FileInputStream(KeyStoreCert.CA_CERT_PATH);
                tks.load(tsIn, KeyStoreCert.KEY_STORE_CACERT_PASSWORD.toCharArray());
                tsIn.close();
                Enumeration<String> aliases = tks.aliases();
                while (aliases.hasMoreElements()) {
                    Certificate certificate = tks.getCertificate(aliases.nextElement());
                    if (certificate instanceof X509Certificate) {
                        this.certificates.add((X509Certificate) certificate);
                        this.hasValidCert = true;
                    }
                }
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                tmf.init(tks);
                TrustManager[] trustManagers = tmf.getTrustManagers();
                if (trustManagers != null) {
                    for (TrustManager trustManager : trustManagers) {
                        if (trustManager instanceof X509TrustManager) {
                            this.trustManagers.add((X509TrustManager) trustManager);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (X509TrustManager trustManager : this.trustManagers) {
                try {
                    trustManager.checkClientTrusted(chain, authType);
                    return;
                } catch (CertificateException e) {
                }
            }
            throw new CertificateException("None of the TrustManagers trust this certificate chain");
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (X509TrustManager trustManager : this.trustManagers) {
                try {
                    trustManager.checkServerTrusted(chain, authType);
                    return;
                } catch (CertificateException e) {
                }
            }
            throw new CertificateException("None of the TrustManagers trust this certificate chain");
        }

        @Override // javax.net.ssl.X509TrustManager
        public X509Certificate[] getAcceptedIssuers() {
            if (Build.VERSION.SDK_INT > 28) {
                Log.i(KeyStoreCert.TAG, "getAcceptedIssuers from ANdroid Q ");
                return (X509Certificate[]) this.certificates.toArray(new X509Certificate[0]);
            }
            Log.i(KeyStoreCert.TAG, "getAcceptedIssuers from ANdroid P ");
            List<X509Certificate> certificates = new ArrayList<>();
            String[] cas = {"ca", "client", "server", "alias"};
            for (String c : cas) {
                try {
                    Certificate certificate = this.keyStore.getCertificate(c);
                    if (certificate instanceof X509Certificate) {
                        certificates.add((X509Certificate) certificate);
                    }
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                }
            }
            return (X509Certificate[]) certificates.toArray(new X509Certificate[0]);
        }

        public boolean isHasValidCert() {
            return this.hasValidCert;
        }
    }

    /* loaded from: classes.dex */
    private static class CompositeX509KeyManager implements X509KeyManager {
        private static final String ALIAS_client = "client";
        private static final String ALIAS_server = "server";
        private Certificate cert;
        private final KeyStore keyStore;
        private PrivateKey privateKey;

        public CompositeX509KeyManager(KeyStore keyStore) {
            this.keyStore = keyStore;
        }

        @Override // javax.net.ssl.X509KeyManager
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return new String[]{ALIAS_client};
        }

        @Override // javax.net.ssl.X509KeyManager
        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            return ALIAS_client;
        }

        @Override // javax.net.ssl.X509KeyManager
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return new String[]{ALIAS_server};
        }

        @Override // javax.net.ssl.X509KeyManager
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            return ALIAS_server;
        }

        @Override // javax.net.ssl.X509KeyManager
        public X509Certificate[] getCertificateChain(String alias) {
            X509Certificate cert = null;
            if (0 == 0) {
                try {
                    cert = this.keyStore.getCertificate(ALIAS_client);
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                }
            }
            return new X509Certificate[]{cert};
        }

        @Override // javax.net.ssl.X509KeyManager
        public PrivateKey getPrivateKey(String alias) {
            if (0 != 0) {
                return null;
            }
            try {
                PrivateKey privateKey = (PrivateKey) this.keyStore.getKey(ALIAS_client, null);
                return privateKey;
            } catch (KeyStoreException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchAlgorithmException e2) {
                e2.printStackTrace();
                return null;
            } catch (UnrecoverableKeyException e3) {
                e3.printStackTrace();
                return null;
            }
        }
    }
}

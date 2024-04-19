package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss;

import android.app.Application;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.Callback;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
import com.xiaopeng.lib.framework.netchannelmodule.R;
import com.xiaopeng.lib.framework.netchannelmodule.common.util.EncryptionUtil;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception.StorageExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic.StorageCounter;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever;
import com.xiaopeng.lib.security.xmartv1.XmartV1Constants;
import com.xiaopeng.lib.utils.LogUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
/* loaded from: classes.dex */
public abstract class BaseOssTask implements Runnable {
    private static final int CONNECTION_TIMEOUT = 60000;
    private static final int ERROR_FORBIDDEN = 403;
    private static final int MAX_CONCURRENT_REQUEST = 2;
    private static final int MAX_ERROR_RETRY = 3;
    private static final int SOCKET_TIMEOUT = 60000;
    private static final String TAG = "NetChannel-BaseOssTask";
    private static final String XIAOPENG_PRIVATE_DOMAIN = "oss.xiaopeng.com";
    private static OSS sOssClient;
    private Application mApplication;
    private final Bucket mBucket;
    private Callback mCallback;
    protected Map<String, String> mCallbackParams;
    protected boolean mCertified;
    protected String mLocalFilePath;
    protected long mLocalFileSize;
    private String mModuleName;
    private String mRemoteFolder;
    protected String mRemoteObjectKey;
    protected String mRemoteUrl;
    private static final byte[] KEY = {123, 54, 90, 56, 23, 74, 89, 71, 55, 78, 65, 40, 100};
    private static final byte[] ENCRYPT_PASSWORD = {3, 95, 59, 87, 103, 47, 55, 32, 88, 61, 50};

    abstract void performRealTask();

    public BaseOssTask(@NonNull Bucket bucket) {
        this.mBucket = bucket;
    }

    public BaseOssTask application(@NonNull Application application) {
        this.mApplication = application;
        return this;
    }

    public BaseOssTask module(@NonNull String name) throws IllegalArgumentException {
        if (!TextUtils.isEmpty(this.mRemoteFolder)) {
            throw new IllegalArgumentException("Remote folder has been assigned.");
        }
        this.mModuleName = name;
        return this;
    }

    public BaseOssTask remoteFolder(@NonNull String name) throws IllegalArgumentException {
        if (!TextUtils.isEmpty(this.mModuleName)) {
            throw new IllegalArgumentException("Module name has been assigned.");
        }
        this.mRemoteFolder = name;
        return this;
    }

    public BaseOssTask filePath(@NonNull String filePath) {
        this.mLocalFilePath = filePath;
        return this;
    }

    public BaseOssTask callback(@NonNull Callback callback) {
        this.mCallback = callback;
        return this;
    }

    public BaseOssTask remoteCallbackParams(Map<String, String> remoteCallbackParams) {
        this.mCallbackParams = remoteCallbackParams;
        return this;
    }

    public BaseOssTask needCertified(boolean openCertification) {
        this.mCertified = openCertification;
        return this;
    }

    public BaseOssTask build() throws StorageException {
        StringBuilder sb;
        String url;
        if (TextUtils.isEmpty(this.mRemoteFolder) && TextUtils.isEmpty(this.mModuleName)) {
            throw new StorageExceptionImpl(3);
        }
        String str = this.mRemoteFolder;
        if (str == null) {
            this.mRemoteObjectKey = this.mBucket.generateObjectKey(this.mModuleName);
        } else {
            this.mRemoteObjectKey = str;
        }
        if (this.mCertified) {
            sb = new StringBuilder();
            url = this.mBucket.getPrivateUrl();
        } else {
            sb = new StringBuilder();
            url = this.mBucket.getUrl();
        }
        sb.append(url);
        sb.append(this.mRemoteObjectKey);
        this.mRemoteUrl = sb.toString();
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void doSuccess() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onSuccess(this.mRemoteUrl, this.mLocalFilePath);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void doFailure(@NonNull StorageException exception) {
        LogUtils.d(TAG, "Failed! Reason is-->" + exception.getMessage());
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onFailure(this.mRemoteUrl, this.mLocalFilePath, exception);
        }
        if (exception.getReasonCode() == ERROR_FORBIDDEN) {
            TokenRetriever.getInstance().clearToken();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String bucketRootName() {
        return this.mBucket.getRootName();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public long bucketMaxObjectSize() {
        return this.mBucket.getMaxObjectSize();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public synchronized OSS createOssClient(@NonNull String accessKeyId, @NonNull String accessKeySecret, @NonNull String securityToken) {
        OSSStsTokenCredentialProvider oSSStsTokenCredentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);
        if (sOssClient == null) {
            ClientConfiguration configuration = new ClientConfiguration();
            configuration.setConnectionTimeout(60000);
            configuration.setSocketTimeout(60000);
            configuration.setMaxConcurrentRequest(2);
            configuration.setMaxErrorRetry(3);
            try {
                try {
                    try {
                        if (this.mCertified) {
                            configuration.setOkHttpClient(getCustomOkHttpClient());
                            sOssClient = new OSSClient(this.mApplication, this.mBucket.getPrivateUrl(), oSSStsTokenCredentialProvider, configuration);
                        } else {
                            sOssClient = new OSSClient(this.mApplication, Bucket.END_POINT, oSSStsTokenCredentialProvider, configuration);
                        }
                    } catch (KeyStoreException e) {
                        LogUtils.e(TAG, "createOssClient failed, KeyStoreException:" + e.getMessage());
                    } catch (CertificateException e2) {
                        LogUtils.e(TAG, "createOssClient failed, CertificateException:" + e2.getMessage());
                    }
                } catch (IOException e3) {
                    LogUtils.e(TAG, "createOssClient failed, IOException:" + e3.getMessage());
                } catch (KeyManagementException e4) {
                    LogUtils.e(TAG, "createOssClient failed, KeyManagementException:" + e4.getMessage());
                }
            } catch (NoSuchAlgorithmException e5) {
                LogUtils.e(TAG, "createOssClient failed, NoSuchAlgorithmException:" + e5.getMessage());
            } catch (UnrecoverableKeyException e6) {
                LogUtils.e(TAG, "createOssClient failed, UnrecoverableKeyException:" + e6.getMessage());
            }
        } else {
            sOssClient.updateCredentialProvider(oSSStsTokenCredentialProvider);
        }
        return sOssClient;
    }

    private OkHttpClient getCustomOkHttpClient() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {
        byte[] decryptData = EncryptionUtil.decrypt(ENCRYPT_PASSWORD, KEY);
        String password = new String(decryptData, StandardCharsets.UTF_8);
        InputStream pfxStream = this.mApplication.getResources().openRawResource(R.raw.client);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(pfxStream, password.toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        InputStream caInput = new BufferedInputStream(this.mApplication.getResources().openRawResource(R.raw.ca));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> caCertificates = cf.generateCertificates(caInput);
        if (caCertificates.isEmpty()) {
            throw new IllegalArgumentException("The CA certificates are empty");
        }
        KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        caKeyStore.load(null, null);
        int index = 0;
        for (Certificate caCertificate : caCertificates) {
            int index2 = index + 1;
            String certificateAlias = Integer.toString(index);
            caKeyStore.setCertificateEntry(certificateAlias, caCertificate);
            index = index2;
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(caKeyStore);
        SSLContext sslContext = SSLContext.getInstance(XmartV1Constants.TLS_REVISION_1_2);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        OkHttpClient client = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0]).hostnameVerifier(new HostnameVerifier() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.BaseOssTask.1
            @Override // javax.net.ssl.HostnameVerifier
            public boolean verify(String hostname, SSLSession session) {
                if (!TextUtils.isEmpty(hostname) && hostname.endsWith(BaseOssTask.XIAOPENG_PRIVATE_DOMAIN)) {
                    return true;
                }
                return false;
            }
        }).build();
        return client;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mCallback.onStart(this.mRemoteUrl, this.mLocalFilePath);
        performRealTask();
        StorageCounter.getInstance().increaseRequestCount();
    }
}

package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.lzy.okgo.OkGo;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.Callback;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception.StorageExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic.StorageCounter;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever;
import com.xiaopeng.lib.http.tls.KeyStoreCert;
import com.xiaopeng.lib.utils.DateUtils;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.SystemPropertyUtil;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.X509TrustManager;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
/* loaded from: classes.dex */
public abstract class BaseAwsTask implements Runnable {
    protected static final String CAR_APP_SECRET = "B638C588DCAD7C1A43E6FB";
    protected static final int CONNECTION_TIMEOUT = 60000;
    protected static final String DOWNLOAD_HOST = "https://fra-xp-log.s3.eu-central-1.amazonaws.com";
    protected static final int ERROR_FORBIDDEN = 403;
    protected static final int MAX_CONCURRENT_REQUEST = 2;
    protected static final int SOCKET_TIMEOUT = 100000;
    private static final String TAG = "NetChannel-BaseAwsTask";
    private String mBucket;
    private Callback mCallback;
    protected Map<String, String> mCallbackParams;
    private OkHttpClient mHttpClient;
    protected String mLocalFilePath;
    protected long mLocalFileSize;
    private String mModuleName;
    private String mRemoteFolder;
    protected String mRemoteObjectKey;
    protected String mRemoteUrl;

    abstract void performRealTask();

    public BaseAwsTask(@NonNull String bucket) {
        this.mBucket = bucket;
    }

    public BaseAwsTask module(@NonNull String name) throws IllegalArgumentException {
        if (!TextUtils.isEmpty(this.mRemoteFolder)) {
            throw new IllegalArgumentException("Remote folder has been assigned.");
        }
        this.mModuleName = name;
        return this;
    }

    public BaseAwsTask remoteFolder(@NonNull String name) throws IllegalArgumentException {
        if (!TextUtils.isEmpty(this.mModuleName)) {
            throw new IllegalArgumentException("Module name has been assigned.");
        }
        this.mRemoteFolder = name;
        return this;
    }

    public BaseAwsTask filePath(@NonNull String filePath) {
        this.mLocalFilePath = filePath;
        return this;
    }

    public BaseAwsTask callback(@NonNull Callback callback) {
        this.mCallback = callback;
        return this;
    }

    public BaseAwsTask remoteCallbackParams(Map<String, String> remoteCallbackParams) {
        this.mCallbackParams = remoteCallbackParams;
        return this;
    }

    public BaseAwsTask build() throws StorageException {
        if (TextUtils.isEmpty(this.mRemoteFolder) && TextUtils.isEmpty(this.mModuleName)) {
            throw new StorageExceptionImpl(3);
        }
        String str = this.mRemoteFolder;
        if (str == null) {
            this.mRemoteObjectKey = generateObjectKey(this.mModuleName);
        } else {
            this.mRemoteFolder = str.replace("//", "/");
            if (this.mRemoteFolder.startsWith("/")) {
                this.mRemoteFolder = this.mRemoteFolder.substring(1);
            }
            if (this.mRemoteFolder.startsWith(this.mBucket)) {
                this.mRemoteObjectKey = this.mRemoteFolder;
            } else {
                this.mRemoteObjectKey = this.mBucket + "/" + this.mRemoteFolder;
            }
        }
        this.mRemoteObjectKey = this.mRemoteObjectKey.trim();
        this.mRemoteUrl = "https://fra-xp-log.s3.eu-central-1.amazonaws.com/" + this.mRemoteObjectKey;
        return this;
    }

    public String generateObjectKey(String moduleName) {
        long timeStamp = System.currentTimeMillis();
        return this.mBucket + "/" + moduleName + "/" + BuildInfoUtils.getSystemVersion() + "/" + DateUtils.formatDate7(timeStamp) + "/" + SystemPropertyUtil.getVehicleId() + "/" + timeStamp + ".zip";
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
    public synchronized OkHttpClient createAwsUploadClient() {
        if (this.mHttpClient == null) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(2);
            this.mHttpClient = new OkHttpClient().newBuilder().connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.SECONDS).readTimeout(100000L, TimeUnit.SECONDS).writeTimeout(100000L, TimeUnit.SECONDS).sslSocketFactory(KeyStoreCert.getTLS2SocketFactory(), new X509TrustManager() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.BaseAwsTask.1
                @Override // javax.net.ssl.X509TrustManager
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override // javax.net.ssl.X509TrustManager
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override // javax.net.ssl.X509TrustManager
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }).dispatcher(dispatcher).build();
        }
        return this.mHttpClient;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mCallback.onStart(this.mRemoteUrl, this.mLocalFilePath);
        performRealTask();
        StorageCounter.getInstance().increaseRequestCount();
    }
}

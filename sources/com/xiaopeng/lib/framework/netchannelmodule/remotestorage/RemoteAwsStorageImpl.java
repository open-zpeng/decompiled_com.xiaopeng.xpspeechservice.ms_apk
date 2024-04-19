package com.xiaopeng.lib.framework.netchannelmodule.remotestorage;

import android.app.Application;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.Callback;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.BaseAwsTask;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.SimpleAwsUploadTask;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception.StorageExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.Bucket;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic.StorageCounter;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.util.Map;
/* loaded from: classes.dex */
public class RemoteAwsStorageImpl implements IRemoteStorage {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private Application mApplication;

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void initWithCategoryAndContext(Application application) {
        initWithContext(application);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void initWithContext(Application application) {
        if (this.mApplication != null) {
            return;
        }
        this.mApplication = application;
        GlobalConfig.setApplicationContext(this.mApplication);
        StorageCounter.getInstance().init(this.mApplication);
        Module.register(DataLogModuleEntry.class, new DataLogModuleEntry(this.mApplication));
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    @Deprecated
    public void needCertified(boolean openCertification) {
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void uploadWithCallback(IRemoteStorage.CATEGORY category, String module, String file, Callback callback) throws Exception {
        doPreCheck();
        Bucket bucket = Bucket.get(category);
        BaseAwsTask task = createUploadTask(bucket.getRootName()).module(module).filePath(file).callback(callback).build();
        ThreadUtils.postBackground(task);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void uploadWithCallback(IRemoteStorage.CATEGORY category, String module, String file, Callback callback, Map<String, String> remoteCallbackParams) throws Exception {
        doPreCheck();
        Bucket bucket = Bucket.get(category);
        BaseAwsTask task = createUploadTask(bucket.getRootName()).module(module).filePath(file).callback(callback).remoteCallbackParams(remoteCallbackParams).build();
        ThreadUtils.postBackground(task);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void uploadWithPathAndCallback(String bucketName, String remoteFolder, String file, Callback callback) throws Exception {
        doPreCheck();
        BaseAwsTask task = createUploadTask(bucketName).remoteFolder(remoteFolder).filePath(file).callback(callback).build();
        ThreadUtils.postBackground(task);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void uploadWithPathAndCallback(String bucketName, String remoteFolder, String file, Callback callback, Map<String, String> remoteCallbackParams) throws Exception {
        doPreCheck();
        BaseAwsTask task = createUploadTask(bucketName).remoteFolder(remoteFolder).filePath(file).callback(callback).remoteCallbackParams(remoteCallbackParams).build();
        ThreadUtils.postBackground(task);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void appendWithPathAndCallback(String bucketName, String remoteFolder, byte[] uploadContent, Callback callback) throws Exception {
        callback.onFailure(bucketName, remoteFolder, new StorageExceptionImpl(StorageException.REASON_UPLOAD_ERROR));
    }

    private BaseAwsTask createUploadTask(String bucket) {
        return new SimpleAwsUploadTask(bucket);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void downloadWithPathAndCallback(String bucketName, String remoteFolder, String localFile, Callback callback) {
        callback.onFailure(bucketName, remoteFolder, new StorageExceptionImpl(StorageException.REASON_DOWNLOAD_ERROR));
    }

    private void doPreCheck() throws StorageException {
        if (this.mApplication == null) {
            throw new StorageExceptionImpl(1);
        }
        if (StorageCounter.getInstance().isExceedTrafficQuota()) {
            StorageCounter.getInstance().increaseRejectCount();
            throw new StorageExceptionImpl(StorageException.REASON_EXCEED_TRAFFIC_QUOTA);
        }
    }
}

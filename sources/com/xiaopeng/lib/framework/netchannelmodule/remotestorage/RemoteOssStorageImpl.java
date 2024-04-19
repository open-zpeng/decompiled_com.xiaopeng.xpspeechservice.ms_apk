package com.xiaopeng.lib.framework.netchannelmodule.remotestorage;

import android.app.Application;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.Callback;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception.StorageExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.AppendableTask;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.BaseOssTask;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.Bucket;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.DownloadTask;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.SimpleUploadTask;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic.StorageCounter;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.util.Map;
/* loaded from: classes.dex */
public class RemoteOssStorageImpl implements IRemoteStorage {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private Application mApplication;
    private boolean mCertified;

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
    public void needCertified(boolean openCertification) {
        this.mCertified = openCertification;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void uploadWithCallback(IRemoteStorage.CATEGORY category, String module, String file, Callback callback) throws Exception {
        doPreCheck();
        Bucket bucket = Bucket.get(category);
        BaseOssTask task = createUploadTask(bucket).application(this.mApplication).module(module).filePath(file).callback(callback).needCertified(this.mCertified).build();
        ThreadUtils.postBackground(task);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void uploadWithCallback(IRemoteStorage.CATEGORY category, String module, String file, Callback callback, Map<String, String> remoteCallbackParams) throws Exception {
        doPreCheck();
        Bucket bucket = Bucket.get(category);
        BaseOssTask task = createUploadTask(bucket).application(this.mApplication).module(module).filePath(file).callback(callback).needCertified(this.mCertified).remoteCallbackParams(remoteCallbackParams).build();
        ThreadUtils.postBackground(task);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void uploadWithPathAndCallback(String bucketName, String remoteFolder, String file, Callback callback) throws Exception {
        doPreCheck();
        Bucket bucket = new Bucket(bucketName);
        BaseOssTask task = createUploadTask(bucket).application(this.mApplication).remoteFolder(remoteFolder).filePath(file).callback(callback).needCertified(this.mCertified).build();
        ThreadUtils.postBackground(task);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void uploadWithPathAndCallback(String bucketName, String remoteFolder, String file, Callback callback, Map<String, String> remoteCallbackParams) throws Exception {
        doPreCheck();
        Bucket bucket = new Bucket(bucketName);
        BaseOssTask task = createUploadTask(bucket).application(this.mApplication).remoteFolder(remoteFolder).filePath(file).callback(callback).needCertified(this.mCertified).remoteCallbackParams(remoteCallbackParams).build();
        ThreadUtils.postBackground(task);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void appendWithPathAndCallback(String bucketName, String remoteFolder, byte[] uploadContent, Callback callback) throws Exception {
        doPreCheck();
        Bucket bucket = new Bucket(bucketName);
        BaseOssTask task = new AppendableTask(bucket).application(this.mApplication).remoteFolder(remoteFolder).needCertified(this.mCertified).callback(callback);
        ((AppendableTask) task).append(uploadContent);
        ThreadUtils.postBackground(task.build());
    }

    private BaseOssTask createUploadTask(Bucket bucket) {
        return new SimpleUploadTask(bucket);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage
    public void downloadWithPathAndCallback(String bucketName, String remoteFolder, String localFile, Callback callback) throws Exception {
        doPreCheck();
        Bucket bucket = new Bucket(bucketName);
        BaseOssTask task = new DownloadTask(bucket).application(this.mApplication).remoteFolder(remoteFolder).filePath(localFile).callback(callback).needCertified(this.mCertified).build();
        ThreadUtils.postBackground(task);
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

package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss;

import android.os.Environment;
import android.support.annotation.NonNull;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception.StorageExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic.StorageCounter;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.Token;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.util.LogFileCleaner;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.io.File;
/* loaded from: classes.dex */
public class ResumableUploadTask extends BaseOssTask {
    private static final String TAG = "NetChannel-ResumableUploadTask";
    private final String mOssTaskLogFolder;
    private long mUploadedSize;

    public ResumableUploadTask(Bucket bucket) {
        super(bucket);
        this.mUploadedSize = 0L;
        this.mOssTaskLogFolder = initRecordDir();
        LogFileCleaner.getInstance().setLogFileFolder(this.mOssTaskLogFolder);
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.BaseOssTask
    public void performRealTask() {
        TokenRetriever.getInstance().getTokenWithCallback(new FutureTaskCallback());
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.BaseOssTask
    public ResumableUploadTask build() throws StorageException {
        super.build();
        File file = new File(this.mLocalFilePath);
        if (!file.exists()) {
            throw new StorageExceptionImpl(513);
        }
        this.mLocalFileSize = file.length();
        return this;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void executeUploadTask(@NonNull Token token) throws StorageException {
        final OSS oss = createOssClient(token.accessKeyId(), token.acessKeySecret(), token.securityToken());
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.ResumableUploadTask.1
            @Override // java.lang.Runnable
            public void run() {
                ResumableUploadTask.this.upload2OssByResumable(oss);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void upload2OssByResumable(OSS oss) {
        ResumableUploadRequest request = new ResumableUploadRequest(bucketRootName(), this.mRemoteObjectKey, this.mLocalFilePath, initRecordDir());
        if (this.mCallbackParams != null) {
            request.setCallbackParam(this.mCallbackParams);
        }
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.ResumableUploadTask.2
            public void onProgress(ResumableUploadRequest request2, long currentSize, long totalSize) {
                ResumableUploadTask.this.mUploadedSize += currentSize;
                LogUtils.d(ResumableUploadTask.TAG, "Uploading " + request2.getUploadFilePath() + ", current size " + currentSize);
            }
        });
        oss.asyncResumableUpload(request, new OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.ResumableUploadTask.3
            public void onSuccess(ResumableUploadRequest request2, ResumableUploadResult result) {
                LogUtils.d(ResumableUploadTask.TAG, "upload " + ResumableUploadTask.this.mLocalFilePath + " success!");
                if (!StorageCounter.isInternationVersion()) {
                    IDataLog dataLogService = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
                    dataLogService.sendStatData(dataLogService.buildStat().setEventName(GlobalConfig.EVENT_NAME_SUCCESS).setProperty("pack", GlobalConfig.getApplicationSimpleName()).setProperty("method", "resumable").setProperty("localPath", ResumableUploadTask.this.mLocalFilePath).setProperty("localSize", Long.valueOf(ResumableUploadTask.this.mLocalFileSize)).setProperty("uploadPath", ResumableUploadTask.this.mRemoteObjectKey).setProperty("uploadSize", Long.valueOf(ResumableUploadTask.this.mUploadedSize)).setProperty("uploadId", request2.getUploadId()).setProperty("requestId", result.getRequestId()).build());
                }
                ResumableUploadTask.this.doSuccess();
                StorageCounter.getInstance().increaseSucceedWithSize(ResumableUploadTask.this.mLocalFileSize);
            }

            public void onFailure(ResumableUploadRequest request2, ClientException clientException, ServiceException serviceException) {
                String failReason = "clientException:" + clientException + " serviceException:" + serviceException;
                if (!StorageCounter.isInternationVersion()) {
                    IDataLog dataLogService = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
                    dataLogService.sendStatData(dataLogService.buildStat().setEventName(GlobalConfig.EVENT_NAME_FAIL).setProperty("pack", GlobalConfig.getApplicationSimpleName()).setProperty("method", "resumable").setProperty("localPath", ResumableUploadTask.this.mLocalFilePath).setProperty("localSize", Long.valueOf(ResumableUploadTask.this.mLocalFileSize)).setProperty("uploadPath", ResumableUploadTask.this.mRemoteObjectKey).setProperty("uploadSize", Long.valueOf(ResumableUploadTask.this.mUploadedSize)).setProperty("failReason", failReason).setProperty("uploadId", request2.getUploadId()).build());
                }
                if (clientException != null) {
                    ResumableUploadTask.this.doFailure(new StorageExceptionImpl(StorageException.REASON_UPLOAD_ERROR, failReason));
                    StorageCounter.getInstance().increaseFailureWithCode(String.valueOf((int) StorageException.REASON_UPLOAD_ERROR), ResumableUploadTask.this.mUploadedSize);
                } else if (serviceException != null) {
                    int code = serviceException.getStatusCode();
                    ResumableUploadTask.this.doFailure(new StorageExceptionImpl(code, failReason));
                    StorageCounter.getInstance().increaseFailureWithCode(serviceException.getErrorCode(), ResumableUploadTask.this.mUploadedSize);
                }
                LogUtils.d(ResumableUploadTask.TAG, "Uploaded " + ResumableUploadTask.this.mUploadedSize);
                LogFileCleaner.getInstance().cleanLogAsNeeded();
            }
        });
    }

    private String initRecordDir() {
        String recordDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Log/oss_record/";
        File recordDir = new File(recordDirPath);
        if (!recordDir.exists()) {
            recordDir.mkdirs();
        }
        return recordDirPath;
    }

    /* loaded from: classes.dex */
    private class FutureTaskCallback implements TokenRetriever.IRetrievingCallback {
        private FutureTaskCallback() {
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever.IRetrievingCallback
        public void onFailure(StorageException exception) {
            ResumableUploadTask.this.doFailure(exception);
            StorageCounter.getInstance().increaseFailureWithCode(String.valueOf((int) StorageException.REASON_GET_TOKEN_ERROR), 0L);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever.IRetrievingCallback
        public void onSuccess(@NonNull Token token) {
            try {
                ResumableUploadTask.this.executeUploadTask(token);
            } catch (StorageException exception) {
                ResumableUploadTask.this.doFailure(exception);
                StorageCounter.getInstance().increaseFailureWithCode(String.valueOf(exception.getReasonCode()), 0L);
            }
        }
    }
}

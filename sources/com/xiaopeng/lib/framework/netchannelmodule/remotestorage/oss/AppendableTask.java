package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss;

import android.os.Looper;
import android.support.annotation.NonNull;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception.StorageExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic.StorageCounter;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.Token;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import com.xiaopeng.lib.utils.info.DeviceInfoUtils;
/* loaded from: classes.dex */
public class AppendableTask extends BaseOssTask {
    private static int STATUS_OK = 200;
    private static final String TAG = "NetChannel-AppendableTask";
    private byte[] mUploadContent;

    public AppendableTask(Bucket bucket) {
        super(bucket);
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.BaseOssTask
    public void performRealTask() {
        TokenRetriever.getInstance().getTokenWithCallback(new FutureTaskCallback());
    }

    public AppendableTask append(byte[] data) throws StorageException {
        if (data == null || data.length == 0) {
            throw new StorageExceptionImpl(4);
        }
        this.mUploadContent = data;
        return this;
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.BaseOssTask
    public BaseOssTask build() throws StorageException {
        byte[] bArr = this.mUploadContent;
        if (bArr == null || bArr.length == 0) {
            throw new StorageExceptionImpl(4);
        }
        return super.build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void executeAppendTask(@NonNull Token token) {
        final OSS oss = createOssClient(token.accessKeyId(), token.acessKeySecret(), token.securityToken());
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.AppendableTask.1
            @Override // java.lang.Runnable
            public void run() {
                AppendableTask.this.appendToOssObject(oss);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void appendToOssObject(OSS oss) {
        if (BuildInfoUtils.isDebuggableVersion() && Thread.currentThread() == Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Not allow to run in main thread.");
        }
        try {
            long position = tryToGetExistingObjectLength(oss);
            AppendObjectRequest append = new AppendObjectRequest(bucketRootName(), this.mRemoteObjectKey, this.mUploadContent);
            append.setPosition(position);
            AppendObjectResult result = oss.appendObject(append);
            if (STATUS_OK == result.getStatusCode()) {
                boolean international = DeviceInfoUtils.isInternationalVer();
                if (!international) {
                    IDataLog dataLogService = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
                    dataLogService.sendStatData(dataLogService.buildStat().setEventName(GlobalConfig.EVENT_NAME_SUCCESS).setProperty("pack", GlobalConfig.getApplicationSimpleName()).setProperty("method", "append").setProperty("localSize", Integer.valueOf(this.mUploadContent.length)).setProperty("uploadPath", this.mRemoteObjectKey).setProperty("requestId", result.getRequestId()).build());
                }
                doSuccess();
                StorageCounter.getInstance().increaseSucceedWithSize(this.mUploadContent.length);
                return;
            }
            String errorCode = String.valueOf(result.getStatusCode());
            boolean international2 = DeviceInfoUtils.isInternationalVer();
            if (!international2) {
                IDataLog dataLogService2 = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
                dataLogService2.sendStatData(dataLogService2.buildStat().setEventName(GlobalConfig.EVENT_NAME_FAIL).setProperty("pack", GlobalConfig.getApplicationSimpleName()).setProperty("method", "append").setProperty("localSize", Integer.valueOf(this.mUploadContent.length)).setProperty("uploadPath", this.mRemoteObjectKey).setProperty("failReason", errorCode).setProperty("uploadId", (String) null).build());
            }
            LogUtils.e(TAG, "append error!", errorCode);
            doFailure(new StorageExceptionImpl(StorageException.REASON_UPLOAD_ERROR, errorCode));
            StorageCounter.getInstance().increaseFailureWithCode(errorCode, 0L);
        } catch (Exception e) {
            if (!StorageCounter.isInternationVersion()) {
                IDataLog dataLogService3 = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
                dataLogService3.sendStatData(dataLogService3.buildStat().setEventName(GlobalConfig.EVENT_NAME_FAIL).setProperty("pack", GlobalConfig.getApplicationSimpleName()).setProperty("method", "append").setProperty("localPath", this.mLocalFilePath).setProperty("localSize", Integer.valueOf(this.mUploadContent.length)).setProperty("uploadPath", this.mRemoteObjectKey).setProperty("failReason", e.toString()).setProperty("uploadId", (String) null).build());
            }
            LogUtils.e(TAG, "append error!", e);
            doFailure(new StorageExceptionImpl(StorageException.REASON_UPLOAD_ERROR, e.toString()));
            StorageCounter.getInstance().increaseFailureWithCode(e.getMessage(), 0L);
        }
    }

    private long tryToGetExistingObjectLength(OSS oss) {
        try {
            HeadObjectRequest head = new HeadObjectRequest(bucketRootName(), this.mRemoteObjectKey);
            HeadObjectResult result = oss.headObject(head);
            if (STATUS_OK != result.getStatusCode()) {
                return 0L;
            }
            long size = result.getMetadata().getContentLength();
            return size;
        } catch (Exception e) {
            return 0L;
        }
    }

    /* loaded from: classes.dex */
    private class FutureTaskCallback implements TokenRetriever.IRetrievingCallback {
        private FutureTaskCallback() {
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever.IRetrievingCallback
        public void onFailure(StorageException exception) {
            AppendableTask.this.doFailure(exception);
            StorageCounter.getInstance().increaseFailureWithCode(String.valueOf((int) StorageException.REASON_GET_TOKEN_ERROR), 0L);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever.IRetrievingCallback
        public void onSuccess(@NonNull Token token) {
            AppendableTask.this.executeAppendTask(token);
        }
    }
}

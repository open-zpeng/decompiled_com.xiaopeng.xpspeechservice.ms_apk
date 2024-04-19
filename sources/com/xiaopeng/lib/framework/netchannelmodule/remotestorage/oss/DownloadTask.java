package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception.StorageExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.statistic.StorageCounter;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.Token;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever;
import com.xiaopeng.lib.utils.FileUtils;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: classes.dex */
public class DownloadTask extends BaseOssTask {
    private final int SUCCESS_CODE;
    private final String TAG;
    private final int UNKNOWN_LENGTH;

    public DownloadTask(Bucket bucket) {
        super(bucket);
        this.TAG = "NetChannel-DownloadTask";
        this.SUCCESS_CODE = 0;
        this.UNKNOWN_LENGTH = -1;
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.BaseOssTask
    public DownloadTask build() throws StorageException {
        super.build();
        this.mLocalFileSize = 0L;
        return this;
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.BaseOssTask
    public void performRealTask() {
        TokenRetriever.getInstance().getTokenWithCallback(new FutureTaskCallback());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void downloadFile(OSS oss) {
        oss.asyncGetObject(new GetObjectRequest(bucketRootName(), this.mRemoteObjectKey), new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.DownloadTask.1
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                int errorCode;
                String reason = "";
                try {
                    DownloadTask.this.createFileAsNeeded();
                    errorCode = DownloadTask.this.writeContentToFile(result);
                } catch (Exception exception) {
                    errorCode = 515;
                    reason = "Failed to write file:" + DownloadTask.this.mLocalFilePath + ", \n" + exception;
                    LogUtils.e("NetChannel-DownloadTask", reason);
                }
                if (errorCode == 0) {
                    LogUtils.e("Succeed to download file to :" + DownloadTask.this.mLocalFilePath + ", size:" + DownloadTask.this.mLocalFileSize);
                    DownloadTask.this.doSuccess();
                    StorageCounter.getInstance().increaseSucceedWithSize(DownloadTask.this.mLocalFileSize);
                    return;
                }
                FileUtils.deleteFile(DownloadTask.this.mLocalFilePath);
                DownloadTask.this.doFailure(new StorageExceptionImpl(errorCode, reason));
            }

            public void onFailure(GetObjectRequest request, ClientException clientException, ServiceException serviceException) {
                String failReason = "clientException:" + clientException + " serviceException:" + serviceException;
                int code = StorageException.REASON_DOWNLOAD_ERROR;
                if (serviceException != null) {
                    code = serviceException.getStatusCode();
                }
                DownloadTask.this.doFailure(new StorageExceptionImpl(code, failReason));
                StorageCounter.getInstance().increaseFailureWithCode(serviceException.getErrorCode(), 0L);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int writeContentToFile(GetObjectResult result) {
        int errorCode = 0;
        File localFile = new File(this.mLocalFilePath);
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            try {
                byte[] contents = new byte[4096];
                inputStream = result.getObjectContent();
                outputStream = new FileOutputStream(localFile, false);
                while (true) {
                    int readLength = inputStream.read(contents, 0, contents.length);
                    if (readLength <= 0) {
                        break;
                    }
                    outputStream.write(contents, 0, readLength);
                    this.mLocalFileSize += readLength;
                }
                outputStream.flush();
            } catch (IOException exception) {
                errorCode = StorageException.REASON_DOWNLOAD_ERROR;
                LogUtils.e("NetChannel-DownloadTask", "Failed to download file:" + exception);
            }
            if (result.getContentLength() != -1 && this.mLocalFileSize != result.getContentLength()) {
                LogUtils.d("NetChannel-DownloadTask", "Not download completed. Expected:" + result.getContentLength() + ", but actual:" + this.mLocalFileSize);
                return StorageException.REASON_DOWNLOAD_INCOMPLETE;
            }
            return errorCode;
        } finally {
            closeStream(inputStream);
            closeStream(outputStream);
        }
    }

    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                LogUtils.e("NetChannel-DownloadTask", "Failed to close stream: " + e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void createFileAsNeeded() throws IOException {
        File localFile = new File(this.mLocalFilePath);
        if (localFile.exists()) {
            FileUtils.deleteFile(this.mLocalFilePath);
        }
        File parent = localFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        localFile.createNewFile();
    }

    /* loaded from: classes.dex */
    private class FutureTaskCallback implements TokenRetriever.IRetrievingCallback {
        private FutureTaskCallback() {
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever.IRetrievingCallback
        public void onFailure(StorageException exception) {
            DownloadTask.this.doFailure(exception);
            StorageCounter.getInstance().increaseFailureWithCode(String.valueOf((int) StorageException.REASON_GET_TOKEN_ERROR), 0L);
        }

        @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever.IRetrievingCallback
        public void onSuccess(Token token) {
            final OSS oss = DownloadTask.this.createOssClient(token.accessKeyId(), token.acessKeySecret(), token.securityToken());
            ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.DownloadTask.FutureTaskCallback.1
                @Override // java.lang.Runnable
                public void run() {
                    DownloadTask.this.downloadFile(oss);
                }
            });
        }
    }
}

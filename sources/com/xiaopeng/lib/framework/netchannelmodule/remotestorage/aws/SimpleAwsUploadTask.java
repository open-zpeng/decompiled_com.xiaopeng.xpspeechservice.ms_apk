package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lzy.okgo.model.Progress;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.Callback;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
import com.xiaopeng.lib.framework.netchannelmodule.NetworkChannelsEntry;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.UploadInfo;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception.StorageExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.util.EncryptUtil;
import com.xiaopeng.lib.http.CommonUtils;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.io.File;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.json.JSONObject;
import org.litepal.util.Const;
/* loaded from: classes.dex */
public class SimpleAwsUploadTask extends BaseAwsTask {
    private static final long BUFFER_SIZE = 1024;
    private static final String KEY_CALLBACK_BODY = "callbackBody";
    private static final String KEY_CALLBACK_URL = "callbackUrl";
    private static final String PRE_SIGN_URL = "https://fra-callback-api.xiaopeng.com/xmartFileData";
    private static final String TAG = "NetChannel-SimpleAwsUploadTask";
    private static final String XMART_OSSFILE_URL = "https://fra-bd-callback.xiaopeng.com/oss/xmartData/xmartOssFile";

    public SimpleAwsUploadTask(String bucket) {
        super(bucket);
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.BaseAwsTask
    public void performRealTask() {
        JsonObject jsonObject = wrapBody();
        sendByIHttp(jsonObject);
    }

    private JsonObject wrapBody() {
        String file = this.mRemoteObjectKey;
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign = createSign(file, timestamp);
        UploadInfo uploadInfo = new UploadInfo.Builder().file(file).timestamp(timestamp).sign(sign).build();
        UpdateUploadInfo(uploadInfo);
        JsonObject jsonObject = wrapJsonBody(uploadInfo);
        return jsonObject;
    }

    private void UpdateUploadInfo(UploadInfo uploadInfo) {
        if (this.mCallbackParams == null || this.mCallbackParams.isEmpty() || this.mCallbackParams.get(KEY_CALLBACK_BODY) == null || !XMART_OSSFILE_URL.equals(this.mCallbackParams.get(KEY_CALLBACK_URL))) {
            return;
        }
        try {
            UploadInfo callbackUploadInfo = (UploadInfo) new Gson().fromJson(this.mCallbackParams.get(KEY_CALLBACK_BODY), (Class<Object>) UploadInfo.class);
            if (!TextUtils.isEmpty(callbackUploadInfo.model)) {
                uploadInfo.app_id = callbackUploadInfo.app_id;
            }
            if (!TextUtils.isEmpty(callbackUploadInfo.model)) {
                uploadInfo.model = callbackUploadInfo.model;
            }
            if (!TextUtils.isEmpty(callbackUploadInfo.type)) {
                uploadInfo.type = callbackUploadInfo.type;
            }
            if (!TextUtils.isEmpty(callbackUploadInfo.v)) {
                uploadInfo.v = callbackUploadInfo.v;
            }
            if (uploadInfo.file != null && uploadInfo.file.startsWith("fra-xp-log/")) {
                uploadInfo.file = uploadInfo.file.substring(11);
            }
        } catch (Exception exception) {
            LogUtils.e(TAG, exception);
        }
    }

    private JsonObject wrapJsonBody(UploadInfo uploadInfo) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("app_id", uploadInfo.app_id);
        jsonObject.addProperty("file", uploadInfo.file);
        jsonObject.addProperty("model", uploadInfo.model);
        jsonObject.addProperty(Const.TableSchema.COLUMN_TYPE, uploadInfo.type);
        jsonObject.addProperty("v", uploadInfo.v);
        jsonObject.addProperty("timestamp", uploadInfo.timestamp);
        jsonObject.addProperty("sign", uploadInfo.sign);
        if (this.mCallbackParams != null) {
            JSONObject jsonObj = new JSONObject(this.mCallbackParams);
            jsonObject.addProperty("call_back", jsonObj.toString());
        }
        return jsonObject;
    }

    private void sendByIHttp(JsonObject jsonObject) {
        IHttp http = (IHttp) Module.get(NetworkChannelsEntry.class).get(IHttp.class);
        http.bizHelper().post(PRE_SIGN_URL, jsonObject.toString()).build().execute(new Callback() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.SimpleAwsUploadTask.1
            @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.Callback
            public void onSuccess(IResponse iResponse) {
                try {
                    String responseStr = iResponse.body();
                    JSONObject resObj = new JSONObject(responseStr);
                    String url = resObj.getString(Progress.URL);
                    if (url != null && url.length() > 0) {
                        SimpleAwsUploadTask.this.executeUploadTask(url.replace("https://", "http://"));
                    }
                } catch (Exception exception) {
                    SimpleAwsUploadTask.this.doFailure(new StorageExceptionImpl(StorageException.REASON_GET_TOKEN_ERROR, exception.getMessage()));
                }
            }

            @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.Callback
            public void onFailure(IResponse iResponse) {
                SimpleAwsUploadTask.this.doFailure(new StorageExceptionImpl(StorageException.REASON_GET_TOKEN_ERROR, iResponse.getException().getMessage()));
            }
        });
    }

    private String createSign(String file, String timestamp) {
        String appString = "file" + file + Const.TableSchema.COLUMN_TYPE + UploadInfo.UPLOAD_TYPE + "v" + UploadInfo.VERSION;
        String sign = EncryptUtil.MD5("XP-Appid" + timestamp + appString + CommonUtils.CAR_APP_SEC).toLowerCase();
        return sign;
    }

    @Override // com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.BaseAwsTask
    public SimpleAwsUploadTask build() throws StorageException {
        super.build();
        File file = new File(this.mLocalFilePath);
        if (!file.exists()) {
            throw new StorageExceptionImpl(513);
        }
        this.mLocalFileSize = file.length();
        return this;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void executeUploadTask(final String url) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.SimpleAwsUploadTask.2
            @Override // java.lang.Runnable
            public void run() {
                SimpleAwsUploadTask.this.upload2AwsByNormal(url);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void upload2AwsByNormal(String urlStr) {
        File file = new File(this.mLocalFilePath);
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody body = createProgressRequestBody(mediaType, file);
        Request request = new Request.Builder().url(urlStr).put(body).build();
        OkHttpClient httpClient = createAwsUploadClient();
        try {
            httpClient.newCall(request).execute();
            doSuccess();
        } catch (Exception e) {
            LogUtils.e(TAG, "upload error!", e);
            doFailure(new StorageExceptionImpl(StorageException.REASON_UPLOAD_ERROR, e.getMessage()));
        }
    }

    public RequestBody createProgressRequestBody(final MediaType contentType, final File file) {
        return new RequestBody() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws.SimpleAwsUploadTask.3
            @Override // okhttp3.RequestBody
            public MediaType contentType() {
                return contentType;
            }

            @Override // okhttp3.RequestBody
            public long contentLength() {
                return file.length();
            }

            @Override // okhttp3.RequestBody
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                Source source = Okio.source(file);
                Buffer buf = new Buffer();
                while (true) {
                    long bytes = source.read(buf, 1024L);
                    if (bytes != -1) {
                        bufferedSink.write(buf, bytes);
                    } else {
                        return;
                    }
                }
            }
        };
    }
}

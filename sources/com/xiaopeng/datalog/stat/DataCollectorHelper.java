package com.xiaopeng.datalog.stat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.datalog.StatEvent;
import com.xiaopeng.datalog.bean.LogEvent;
import com.xiaopeng.datalog.helper.DataBackupHelper;
import com.xiaopeng.lib.bughunter.IDataUploadInterface;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.utils.DateUtils;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.SystemPropertyUtil;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.lib.utils.ZipUtils;
import com.xiaopeng.lib.utils.crypt.AESUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
/* loaded from: classes.dex */
public class DataCollectorHelper {
    public static final String BUCKET_AND_ENDPOINT;
    public static final String BUCKET_NAME;
    private static final int CHECK_CONNECTION_DELAY = 10000;
    private static final boolean DEBUG = false;
    private static final int DUMP_CACHE_DELAY = 60000;
    private static final String LOG_PATH = "/data/Log/log0/";
    private static final int MAX_NUM_PER_ZIP_DATA_LOCAL = 20;
    private static final int MESSAGE_CHECK_CONNECTION = 3;
    private static final int MESSAGE_DUMP_CAN = 1;
    private static final int MESSAGE_DUMP_CDU = 2;
    private static final String SEPARATOR = "#";
    private static final String TAG = "DataCollectorHelper";
    private static volatile DataCollectorHelper mInstance;
    private Context mContext;
    private IDataUploadInterface remoteService;
    private final List<String> mCanDataCache = new CopyOnWriteArrayList();
    private final List<Map<String, Object>> mCduDataCache = new CopyOnWriteArrayList();
    private Gson mGson = new Gson();
    private Handler mHandler = new Handler(ThreadUtils.getLooper(0), new Handler.Callback() { // from class: com.xiaopeng.datalog.stat.DataCollectorHelper.1
        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                LogUtils.v(DataCollectorHelper.TAG, "handleMessage MESSAGE_DUMP_CAN");
                DataCollectorHelper.this.dumpCan2File();
            } else if (i == 2) {
                LogUtils.d(DataCollectorHelper.TAG, "handleMessage MESSAGE_DUMP_CDU");
                DataCollectorHelper.this.dumpCdu2File();
            } else if (i == 3) {
                LogUtils.d(DataCollectorHelper.TAG, "handleMessage MESSAGE_CHECK_CONNECTION");
                if (DataCollectorHelper.this.remoteService == null) {
                    DataCollectorHelper.this.bindService();
                }
            }
            return true;
        }
    });
    private ServiceConnection mConnection = new ServiceConnection() { // from class: com.xiaopeng.datalog.stat.DataCollectorHelper.2
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtils.d(DataCollectorHelper.TAG, "onServiceConnected");
            DataCollectorHelper.this.remoteService = IDataUploadInterface.Stub.asInterface(iBinder);
            IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() { // from class: com.xiaopeng.datalog.stat.DataCollectorHelper.2.1
                @Override // android.os.IBinder.DeathRecipient
                public void binderDied() {
                    if (DataCollectorHelper.this.remoteService != null) {
                        DataCollectorHelper.this.remoteService.asBinder().unlinkToDeath(this, 0);
                        DataCollectorHelper.this.remoteService = null;
                        DataCollectorHelper.this.bindService();
                    }
                }
            };
            try {
                if (DataCollectorHelper.this.remoteService != null) {
                    DataCollectorHelper.this.remoteService.asBinder().linkToDeath(deathRecipient, 0);
                }
            } catch (RemoteException mE) {
                LogUtils.w(DataCollectorHelper.TAG, "RemoteException occurs when reLink to Service, exception:", mE);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.d(DataCollectorHelper.TAG, "onServiceDisconnected");
            DataCollectorHelper.this.remoteService = null;
            DataCollectorHelper.this.bindService();
        }
    };

    static {
        BUCKET_NAME = BuildInfoUtils.isLanVersion() ? "xp-log-local" : "xp-log";
        BUCKET_AND_ENDPOINT = "http://" + BUCKET_NAME + ".oss-cn-hangzhou.aliyuncs.com/";
    }

    private DataCollectorHelper() {
    }

    public static DataCollectorHelper getInstance() {
        if (mInstance == null) {
            synchronized (DataCollectorHelper.class) {
                if (mInstance == null) {
                    mInstance = new DataCollectorHelper();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context;
        bindService();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void bindService() {
        Intent intent = new Intent("com.xiaopeng.service.DATA_SERVICE");
        intent.setPackage("com.xiaopeng.data.uploader");
        this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        this.mContext.bindServiceAsUser(intent, this.mConnection, 1, UserHandle.CURRENT);
        if (!this.mHandler.hasMessages(3)) {
            this.mHandler.sendEmptyMessageDelayed(3, 10000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isConnected() {
        return this.remoteService != null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void uploadCdu(String data) {
        boolean success = false;
        IDataUploadInterface iDataUploadInterface = this.remoteService;
        if (iDataUploadInterface != null) {
            try {
                iDataUploadInterface.uploadLog(data);
                if (this.mCduDataCache.size() > 0) {
                    dumpCdu2File();
                }
                success = true;
            } catch (RemoteException e) {
                LogUtils.w(TAG, "uploadCdu error!", e);
            }
        }
        if (!success) {
            Map<String, Object> dataMap = (Map) this.mGson.fromJson(data, new TypeToken<Map<String, Object>>() { // from class: com.xiaopeng.datalog.stat.DataCollectorHelper.3
            }.getType());
            uploadCduLogInternal(dataMap);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void uploadLogOrigin(String eventName, String data) {
        boolean success = false;
        IDataUploadInterface iDataUploadInterface = this.remoteService;
        if (iDataUploadInterface != null) {
            try {
                iDataUploadInterface.uploadLogOrigin(eventName, data);
                if (this.mCduDataCache.size() > 0) {
                    dumpCdu2File();
                }
                success = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (!success) {
            IDataLog dataLog = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
            String jsonData = dataLog.buildStat().setEventName(eventName).setProperty(CacheEntity.DATA, data).build().toJson();
            Map<String, Object> dataMap = (Map) this.mGson.fromJson(jsonData, new TypeToken<Map<String, Object>>() { // from class: com.xiaopeng.datalog.stat.DataCollectorHelper.4
            }.getType());
            uploadCduLogInternal(dataMap);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void uploadCan(String data) {
        boolean success = false;
        IDataUploadInterface iDataUploadInterface = this.remoteService;
        if (iDataUploadInterface != null) {
            try {
                iDataUploadInterface.uploadCan(data);
                if (this.mCanDataCache.size() > 0) {
                    dumpCan2File();
                }
                success = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (!success) {
            this.mCanDataCache.add(data);
            if (this.mCanDataCache.size() >= 20) {
                dumpCan2File();
            } else if (!this.mHandler.hasMessages(1)) {
                this.mHandler.sendEmptyMessageDelayed(1, OkGo.DEFAULT_MILLISECONDS);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dumpCan2File() {
        if (this.mCanDataCache.isEmpty()) {
            LogUtils.d(TAG, "mCanDataCache.isEmpty(), return!");
            return;
        }
        String canDataZipJson = buildCanDataZipJson();
        String message = ZipUtils.compressForGzipAndBase64NoWrap(canDataZipJson);
        DataBackupHelper.getInstance().updateFile(DataBackupHelper.CAN_TAG, message, this.mContext);
        this.mHandler.removeMessages(1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dumpCdu2File() {
        if (this.mCduDataCache.isEmpty()) {
            LogUtils.d(TAG, "mCduDataCache.isEmpty(), return!");
        }
        String cduDataZipJson = buildCduDataZipJson();
        String message = ZipUtils.compressForGzipAndBase64NoWrap(cduDataZipJson);
        DataBackupHelper.getInstance().updateFile(DataBackupHelper.CDU_TAG, message, this.mContext);
        this.mHandler.removeMessages(2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void uploadLogImmediately(String eventName, String data) {
        boolean success = false;
        IDataUploadInterface iDataUploadInterface = this.remoteService;
        if (iDataUploadInterface != null) {
            try {
                iDataUploadInterface.uploadLogImmediately(eventName, data);
                success = true;
            } catch (RemoteException e) {
                LogUtils.w(TAG, "uploadLogImmediately error!", e);
            }
        }
        if (!success) {
            LogUtils.w(TAG, "uploadLogImmediately fail and ignore!");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void uploadCduWithFiles(final StatEvent statEvent, final List<String> filePaths) {
        final String[] uploadInfos = generateUploadInfos();
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.datalog.stat.DataCollectorHelper.5
            @Override // java.lang.Runnable
            public void run() {
                DataCollectorHelper.this.zipAndEncrypt(uploadInfos[1], filePaths);
                statEvent.put("address", uploadInfos[0]);
                String dataJson = statEvent.toJson();
                List<String> filePathList = new ArrayList<>();
                filePathList.add(uploadInfos[1].replace(".zip", "_en.zip"));
                DataCollectorHelper.this.uploadCdu(dataJson);
                DataCollectorHelper.this.uploadFiles(filePathList);
            }
        });
    }

    private String[] generateUploadInfos() {
        long timeStamp = System.currentTimeMillis();
        String objectKeyDir = BUCKET_NAME + "/log/" + BuildInfoUtils.getSystemVersion() + "/" + DateUtils.formatDate7(timeStamp) + "/" + SystemPropertyUtil.getVehicleId();
        String address = generateRemoteUrl(timeStamp, objectKeyDir);
        String dstFilePath = generateFilePath(timeStamp, objectKeyDir);
        return new String[]{address, dstFilePath};
    }

    private String generateRemoteUrl(long timeStamp, String objectKeyDir) {
        String objectKey = objectKeyDir.substring(objectKeyDir.indexOf("/") + 1) + "/" + timeStamp + "_en.zip";
        return BUCKET_AND_ENDPOINT + objectKey;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void zipAndEncrypt(String dstFilePath, List<String> filePaths) {
        File dstZipFile = null;
        try {
            dstZipFile = ZipUtils.zipMultiFiles(dstFilePath, filePaths);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File encryptFile = new File(dstFilePath.replace(".zip", "_en.zip"));
        boolean encryptResult = AESUtils.encrypt(dstZipFile, encryptFile, "@!chxpzi#0109$+/");
        deleteLocalFile(dstZipFile, encryptResult);
    }

    @NonNull
    private String generateFilePath(long timeStamp, String objectKeyDir) {
        String dstFileDirPath = "/sdcard/Log/upload-zip/" + objectKeyDir;
        File dstFileDir = new File(dstFileDirPath);
        if (!dstFileDir.exists()) {
            dstFileDir.mkdirs();
        }
        return dstFileDirPath + "/" + timeStamp + ".zip";
    }

    private void deleteLocalFile(File dstZipFile, boolean encryptResult) {
        if (encryptResult) {
            try {
                dstZipFile.delete();
                System.gc();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    private String generateAllFilePathString(List<String> filePaths) {
        StringBuilder allFilePathStr = new StringBuilder();
        int len = filePaths.size();
        for (int i = 0; i < len; i++) {
            if (i < len - 1) {
                allFilePathStr.append(filePaths.get(i));
                allFilePathStr.append(SEPARATOR);
            } else {
                allFilePathStr.append(filePaths.get(i));
            }
        }
        return allFilePathStr.toString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void uploadFiles(List<String> fileList) {
        IDataUploadInterface iDataUploadInterface = this.remoteService;
        if (iDataUploadInterface != null) {
            try {
                iDataUploadInterface.uploadFiles(fileList);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    private String buildCanDataZipJson() {
        LogEvent event = LogEvent.create(LogEvent.RefType.CAN);
        try {
            event.setV(Integer.valueOf(SystemPropertyUtil.getDBCVersion()).intValue());
        } catch (Exception e) {
            LogUtils.w(TAG, "parse dbcVersion error!", e);
            event.setV(0);
        }
        JsonObject canData = new JsonObject();
        canData.addProperty(CacheEntity.DATA, this.mGson.toJson(this.mCanDataCache));
        this.mCanDataCache.clear();
        ArrayList<Object> msg = new ArrayList<>();
        msg.add(canData);
        event.setMsg(msg);
        String canDataZipJson = this.mGson.toJson(event);
        msg.clear();
        return canDataZipJson;
    }

    private void uploadCduLogInternal(Map<String, Object> data) {
        this.mCduDataCache.add(data);
        if (this.mCduDataCache.size() >= 20) {
            dumpCdu2File();
        } else if (!this.mHandler.hasMessages(2)) {
            this.mHandler.sendEmptyMessageDelayed(2, OkGo.DEFAULT_MILLISECONDS);
        }
    }

    private String buildCduDataZipJson() {
        LogEvent event = LogEvent.create(LogEvent.RefType.CDU);
        try {
            event.setV(Integer.valueOf(SystemPropertyUtil.getDBCVersion()).intValue());
        } catch (Exception e) {
            LogUtils.w(TAG, "parse dbcVersion error!", e);
            event.setV(0);
        }
        List msg = new ArrayList(this.mCduDataCache);
        this.mCduDataCache.clear();
        event.setMsg(msg);
        String cduDataZipJson = this.mGson.toJson(event);
        return cduDataZipJson;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public String uploadRecentSystemLog() {
        String[] uploadInfos = generateUploadInfos();
        final String remoteUrl = uploadInfos[0];
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.datalog.stat.DataCollectorHelper.6
            @Override // java.lang.Runnable
            public void run() {
                if (DataCollectorHelper.this.isConnected()) {
                    DataCollectorHelper.this.internalUploadRecentSystemLog(remoteUrl);
                } else {
                    ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.datalog.stat.DataCollectorHelper.6.1
                        @Override // java.lang.Runnable
                        public void run() {
                            DataCollectorHelper.this.internalUploadRecentSystemLog(remoteUrl);
                        }
                    }, 1000L);
                }
            }
        });
        return remoteUrl;
    }

    void internalUploadRecentSystemLog(String ossUrl) {
        if (this.remoteService != null) {
            try {
                LogUtils.i(TAG, "internalUploadRecentSystemLog " + ossUrl);
                this.remoteService.uploadSystemLog(ossUrl);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}

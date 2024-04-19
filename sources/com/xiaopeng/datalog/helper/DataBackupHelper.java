package com.xiaopeng.datalog.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.utils.DateUtils;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.MD5Utils;
import com.xiaopeng.lib.utils.SystemPropertyUtil;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.lib.utils.info.DeviceInfoUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class DataBackupHelper {
    public static final String CAN_TAG = "candata";
    public static final String CDU_TAG = "cdudata";
    private static final String DATA_FILE_DIR = "/sdcard/Log/";
    private static final long EXPIRE_TIME_THRESHOLD = 604800000;
    private static final long MAX_FILE_LEN = 10485760;
    private static final int MAX_RETRY_COUNT = 5;
    private static final int OK_CODE = 200;
    private static final String TAG = "DataBackupHelper";
    public static final int TYPE_CAN = 1;
    public static final int TYPE_CDU = 2;
    private static volatile DataBackupHelper mInstance;

    private DataBackupHelper() {
    }

    public static DataBackupHelper getInstance() {
        if (mInstance == null) {
            synchronized (DataBackupHelper.class) {
                if (mInstance == null) {
                    mInstance = new DataBackupHelper();
                }
            }
        }
        return mInstance;
    }

    public void updateFile(final String tag, final String data, final Context context) {
        ThreadUtils.post(0, new Runnable() { // from class: com.xiaopeng.datalog.helper.DataBackupHelper.1
            @Override // java.lang.Runnable
            public void run() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("vehicleId", String.valueOf(SystemPropertyUtil.getVehicleId()));
                jsonObject.addProperty("message", data);
                jsonObject.addProperty("md5", MD5Utils.getMD5(data));
                Gson gson = new Gson();
                String content = gson.toJson((JsonElement) jsonObject);
                byte[] contentBytes = content.getBytes();
                DataBackupHelper.this.initDir(tag, context);
                DataBackupHelper.this.deleteExpiredFile(tag, context);
                DataBackupHelper.this.writeFile(tag, contentBytes, context);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initDir(String tag, Context context) {
        File datafileDir = new File(DATA_FILE_DIR + tag + "/" + context.getPackageName());
        if (!datafileDir.exists()) {
            datafileDir.mkdirs();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deleteExpiredFile(final String tag, Context context) {
        File canFileDir = new File(DATA_FILE_DIR + tag + "/" + context.getPackageName());
        if (!canFileDir.exists()) {
            canFileDir.mkdirs();
        }
        String[] fileNames = canFileDir.list(new FilenameFilter() { // from class: com.xiaopeng.datalog.helper.DataBackupHelper.2
            @Override // java.io.FilenameFilter
            public boolean accept(File dir, String filename) {
                return filename.contains(tag) && filename.endsWith(".log");
            }
        });
        if (fileNames == null) {
            return;
        }
        Arrays.sort(fileNames);
        List<String> deleteFileNames = new ArrayList<>();
        for (String fileName : fileNames) {
            if (isExpiredFile(fileName)) {
                deleteFileNames.add(fileName);
                boolean isDeleted = new File(DATA_FILE_DIR + tag + "/" + context.getPackageName() + "/" + fileName).delete();
                StringBuilder sb = new StringBuilder();
                sb.append("file: ");
                sb.append(fileName);
                sb.append(" is expired, deleted ");
                sb.append(isDeleted);
                LogUtils.d(TAG, sb.toString());
            }
        }
        if (deleteFileNames.size() > 0) {
            sendExpireInfo(deleteFileNames, fileNames);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void writeFile(String tag, byte[] data, Context context) {
        File currentFile = getCurrentFile(tag, context);
        String zipFilePath = generateZipFilePath(currentFile.getPath());
        File zipFile = new File(zipFilePath);
        if (zipFile.exists()) {
            LogUtils.i(TAG, "zipFile exists, delete .zip and write .log");
            zipFile.delete();
        }
        RandomAccessFile randomFile = null;
        try {
            try {
                try {
                    randomFile = new RandomAccessFile(currentFile, "rw");
                    long fileLength = randomFile.length();
                    randomFile.seek(fileLength);
                    randomFile.write(data);
                    randomFile.writeBytes("\n");
                    randomFile.getFD().sync();
                    LogUtils.i(TAG, "write " + currentFile + " success, data.length:" + data.length);
                    randomFile.close();
                } catch (Throwable th) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (Exception e) {
                        }
                    }
                    throw th;
                }
            } catch (Exception e2) {
                LogUtils.w(TAG, "write file fail!", e2);
                if (randomFile != null) {
                    randomFile.close();
                }
            }
        } catch (Exception e3) {
        }
    }

    private boolean isExpiredFile(String fileName) {
        int indexEnd = fileName.lastIndexOf("_");
        int indexBegin = indexEnd - 8;
        String dateStr = fileName.substring(indexBegin, indexEnd) + "_000000";
        long timeStamp = System.currentTimeMillis();
        String currentDateStr = DateUtils.formatDate10(timeStamp);
        String finalCurrentDateStr = currentDateStr.substring(0, 8) + "_000000";
        long currentDateStamp = DateUtils.dateToStamp(finalCurrentDateStr);
        long fileDateStamp = DateUtils.dateToStamp(dateStr);
        if (currentDateStamp - fileDateStamp > EXPIRE_TIME_THRESHOLD) {
            LogUtils.d(TAG, fileName + " EXPIRED!");
            return true;
        }
        return false;
    }

    private File getCurrentFile(final String tag, Context context) {
        File dataFileDir = new File(DATA_FILE_DIR + tag + "/" + context.getPackageName());
        File[] files = dataFileDir.listFiles(new FilenameFilter() { // from class: com.xiaopeng.datalog.helper.DataBackupHelper.3
            @Override // java.io.FilenameFilter
            public boolean accept(File dir, String filename) {
                return filename.contains(tag) && filename.contains(".log");
            }
        });
        if (files == null || files.length == 0) {
            LogUtils.d(TAG, "create a new File");
            return createNewFile(tag, context);
        }
        Arrays.sort(files);
        File currentFile = files[files.length - 1];
        if (!hasTodayFile(currentFile) || currentFile.length() >= 10485760) {
            return createNewFile(tag, context);
        }
        return currentFile;
    }

    private boolean hasTodayFile(File currentFile) {
        String currentFileName = currentFile.getName();
        String dateStr = DateUtils.formatDate10(System.currentTimeMillis());
        int index = dateStr.indexOf("_");
        String dayStr = dateStr.substring(0, index);
        return currentFileName.contains(dayStr);
    }

    @NonNull
    private File createNewFile(String tag, Context context) {
        String dateStr = DateUtils.formatDate10(System.currentTimeMillis());
        return new File(DATA_FILE_DIR + tag + "/" + context.getPackageName() + "/" + tag + "_" + dateStr + ".log");
    }

    private String generateZipFilePath(String filePath) {
        return filePath.replace(".log", ".zip");
    }

    private void sendExpireInfo(List<String> deleteFileNames, String[] fileNames) {
        String newExpireFile;
        int size = deleteFileNames.size();
        String oldExpireFile = getTimeFromFile(deleteFileNames.get(0));
        if (fileNames.length > size) {
            newExpireFile = getTimeFromFile(fileNames[size]);
        } else {
            newExpireFile = getTimeFromFile(deleteFileNames.get(size - 1));
        }
        boolean international = DeviceInfoUtils.isInternationalVer();
        if (!international) {
            IDataLog dataLogService = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
            dataLogService.sendStatData(dataLogService.buildStat().setEventName("data_expire").setProperty("startTime", oldExpireFile).setProperty("endTime", newExpireFile).build());
        }
    }

    private String getTimeFromFile(String fileName) {
        int index = fileName.lastIndexOf("_");
        int indexBegin = index - 8;
        int indexEnd = fileName.lastIndexOf(".");
        return fileName.substring(indexBegin, indexEnd);
    }
}

package com.xiaopeng.lib.utils.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import com.tencent.mars.xlog.Xlog;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import com.xiaopeng.lib.utils.log.XLoggerFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/* loaded from: classes.dex */
public class XLogger implements LogUtils.Logger, Handler.Callback {
    private static final int CACHE_DAYS = 0;
    private static final int MAX_INCREASE_SIZE_ONE_MIN = 131072;
    private static final int MESSAGE_FLUSH = 1;
    private static final String PUBLIC_KEY = "43b835098ac59e6a265edfd3b4aa53ba18e67c9503e88f74253d433f36cf854e3a6f77a92d25454284bcf57913bd91abadfa062520cb8662f6740eaeca23b573";
    private static final String TAG = "XLogger";
    private List<String> mAlreadyChmodList;
    private final String mFilePath;
    private final String mFilePrefix;
    private final int mFlushInterval;
    private Handler mHandler = new Handler(ThreadUtils.getLooper(0), this);
    private final int mLevel;
    private long mLogLengthPerMin;
    private long mMainThreadId;
    private final long mMaxSingleSize;
    private final long mMaxTotalSize;
    private final int mMode;
    private final String mPackageName;
    private int mPid;
    private Xlog mXlog;

    /* JADX INFO: Access modifiers changed from: package-private */
    public XLogger(XLoggerFactory.XLoggerBuilder builder) throws XLogLoadLibraryException {
        this.mPackageName = builder.context.getPackageName();
        this.mFilePath = builder.filePath;
        this.mFilePrefix = builder.filePrefix;
        this.mMode = builder.mode;
        this.mLevel = builder.level;
        this.mFlushInterval = builder.flushInterval;
        this.mMaxTotalSize = builder.maxTotalSize;
        this.mMaxSingleSize = builder.maxSingleSize;
        init(builder.context);
    }

    private void init(Context context) throws XLogLoadLibraryException {
        try {
            System.loadLibrary("c++_shared");
            System.loadLibrary("marsxlog");
            this.mMainThreadId = context.getMainLooper().getThread().getId();
            this.mPid = Process.myPid();
            String cachePath = context.getFilesDir() + "/mXlog";
            Xlog.appenderOpen(this.mLevel, this.mMode, cachePath, this.mFilePath, this.mFilePrefix, 0, PUBLIC_KEY);
            Xlog.setConsoleLogOpen(true);
            Xlog.setMaxFileSize(this.mMaxSingleSize);
            this.mXlog = new Xlog();
            this.mHandler.sendEmptyMessageDelayed(1, this.mFlushInterval);
        } catch (Throwable throwable) {
            throw new XLogLoadLibraryException(throwable);
        }
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        if (msg.what == 1) {
            this.mHandler.sendEmptyMessageDelayed(1, this.mFlushInterval);
            long increaseLength = this.mLogLengthPerMin;
            this.mLogLengthPerMin = 0L;
            if (!BuildInfoUtils.isDebuggableVersion() || !BuildInfoUtils.getFullSystemVersion().endsWith("DEV")) {
            }
            this.mXlog.appenderFlush(true);
            cleanLogPath();
            if (increaseLength > 131072) {
                LogUtils.e(TAG, "##Error##[LogExplosion]## increaseLength > 128k, packageName:" + this.mPackageName);
            }
        }
        return true;
    }

    @SuppressLint({"SetWorldReadable"})
    private void cleanLogPath() {
        File logDir = new File(this.mFilePath);
        if (!logDir.exists() || !logDir.isDirectory()) {
            LogUtils.d(TAG, "!logDir.exists() || !logDir.isDirectory(), return!");
            return;
        }
        if (this.mAlreadyChmodList == null) {
            this.mAlreadyChmodList = new ArrayList();
            LogUtils.d(TAG, "logDir.setReadable(true, false), path:" + logDir.getPath());
            logDir.setReadable(true, false);
            logDir.setWritable(true, false);
            logDir.setExecutable(true, false);
        }
        File[] logFiles = logDir.listFiles();
        if (logFiles == null || logFiles.length == 0) {
            LogUtils.d(TAG, "logFiles == null || logFiles.length == 0, return!");
            return;
        }
        long fileSizeSum = 0;
        for (File f : logFiles) {
            fileSizeSum += f.length();
            if (!this.mAlreadyChmodList.contains(f.getName())) {
                boolean chmodResult = f.setReadable(true, false);
                LogUtils.d(TAG, "logDir.setReadable(true, false), path:" + f.getPath() + " chmodResult:" + (chmodResult & f.setWritable(true, false) & f.setExecutable(true, false)));
                this.mAlreadyChmodList.add(f.getName());
            }
        }
        if (this.mAlreadyChmodList.size() > 0 && this.mAlreadyChmodList.size() > logFiles.length) {
            LogUtils.d(TAG, "delete first mAlreadyChmodList element:" + this.mAlreadyChmodList.get(0));
            this.mAlreadyChmodList.remove(0);
        }
        if (fileSizeSum < this.mMaxTotalSize) {
            return;
        }
        List<File> logFileList = Arrays.asList(logFiles);
        Collections.sort(logFileList, new Comparator<File>() { // from class: com.xiaopeng.lib.utils.log.XLogger.1
            @Override // java.util.Comparator
            public int compare(File lhs, File rhs) {
                return (int) (lhs.lastModified() - rhs.lastModified());
            }
        });
        for (int index = 0; fileSizeSum > this.mMaxTotalSize && index <= logFiles.length; index++) {
            LogUtils.d(TAG, "fileSizeSum > " + this.mMaxTotalSize + ", delete " + logFiles[index].getPath());
            fileSizeSum -= logFiles[index].length();
            boolean result = logFiles[index].delete();
            LogUtils.d(TAG, "delete result:" + result);
            if (!result) {
                LogUtils.e(TAG, "delete file fail, reset to DefaultLogger");
                LogUtils.setLogger(new LogUtils.DefaultLogger());
                return;
            }
        }
    }

    @Override // com.xiaopeng.lib.utils.LogUtils.Logger
    public void logByLevel(int type, String msg, String TAG2, String fileName) {
        this.mLogLengthPerMin += msg.length();
        long tid = Thread.currentThread().getId();
        if (type == 2) {
            this.mXlog.logV(TAG2, fileName, (String) null, 0, this.mPid, tid, this.mMainThreadId, msg);
        } else if (type == 3) {
            this.mXlog.logD(TAG2, fileName, (String) null, 0, this.mPid, tid, this.mMainThreadId, msg);
        } else if (type == 4) {
            this.mXlog.logI(TAG2, fileName, (String) null, 0, this.mPid, tid, this.mMainThreadId, msg);
        } else if (type == 5) {
            this.mXlog.logW(TAG2, fileName, (String) null, 0, this.mPid, tid, this.mMainThreadId, msg);
        } else if (type == 6) {
            this.mXlog.logE(TAG2, fileName, (String) null, 0, this.mPid, tid, this.mMainThreadId, msg);
        }
    }
}

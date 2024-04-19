package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.util;

import com.xiaopeng.lib.utils.FileUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.io.File;
import java.util.concurrent.atomic.AtomicLong;
/* loaded from: classes.dex */
public class LogFileCleaner {
    private static final int CLEANING_THRESHOLD = 512;
    private static final long FILE_EXPIRED_TIME = 604800000;
    private static final int MAX_DELETED_FILES_EACH_TIME = 20;
    private static final long ONE_DAY_MILLS = 86400000;
    private CleaningTask mCleaningTask;
    private AtomicLong mLastCleaningDate;
    private String mLogFileFolder;

    public static LogFileCleaner getInstance() {
        return Holder.INSTANCE;
    }

    private LogFileCleaner() {
        this.mLastCleaningDate = new AtomicLong(0L);
    }

    public void cleanLogAsNeeded() {
        if (this.mLogFileFolder == null) {
            return;
        }
        long currentDate = System.currentTimeMillis() / ONE_DAY_MILLS;
        if (this.mLastCleaningDate.get() > currentDate) {
            return;
        }
        if (this.mCleaningTask == null) {
            this.mCleaningTask = new CleaningTask();
        }
        ThreadUtils.postBackground(this.mCleaningTask);
        this.mLastCleaningDate.set(currentDate);
    }

    public void setLogFileFolder(String folder) {
        this.mLogFileFolder = folder;
    }

    /* loaded from: classes.dex */
    private final class CleaningTask implements Runnable {
        private CleaningTask() {
        }

        @Override // java.lang.Runnable
        public void run() {
            File[] files;
            File folder = new File(LogFileCleaner.this.mLogFileFolder);
            if (!folder.exists() || !folder.isDirectory() || (files = folder.listFiles()) == null || files.length <= 512) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            int count = 0;
            for (File fileItem : files) {
                if (currentTime - fileItem.lastModified() > LogFileCleaner.FILE_EXPIRED_TIME) {
                    FileUtils.deleteFile(fileItem.getAbsolutePath());
                    count++;
                }
                if (count > 20) {
                    return;
                }
            }
        }
    }

    /* loaded from: classes.dex */
    private static final class Holder {
        private static final LogFileCleaner INSTANCE = new LogFileCleaner();

        private Holder() {
        }
    }
}

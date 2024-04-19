package com.xiaopeng.lib.utils.log;

import android.content.Context;
import com.xiaopeng.lib.utils.FileUtils;
import com.xiaopeng.lib.utils.LogUtils;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class XLoggerFactory {
    private static final List<String> PACKAGE_WHITE_LIST = Arrays.asList("com.xiaopeng.data.uploader", "com.xiaopeng.device.communication");

    public static XLogger createXLoggerDefault(Context context) {
        checkWhiteList(context);
        return new XLoggerBuilder(context).build();
    }

    private static void checkWhiteList(Context context) {
        String packageName = context.getPackageName();
        if (!PACKAGE_WHITE_LIST.contains(packageName)) {
            throw new IllegalArgumentException("不好意思，你的包名没有登记，还不能使用xlog，请联系dengzr@xiaopeng.com");
        }
    }

    /* loaded from: classes.dex */
    public static class XLoggerBuilder {
        Context context;
        String filePath;
        String filePrefix;
        int flushInterval;
        int level;
        long maxSingleSize;
        long maxTotalSize;
        int mode;

        private XLoggerBuilder(Context context) {
            this.mode = 0;
            this.level = 1;
            this.flushInterval = 60000;
            this.maxTotalSize = 5242880L;
            this.maxSingleSize = FileUtils.SIZE_1MB;
            this.context = context;
        }

        public XLoggerBuilder level(int level) {
            this.level = level;
            return this;
        }

        public XLoggerBuilder mode(int mode) {
            this.mode = mode;
            return this;
        }

        public XLoggerBuilder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public XLoggerBuilder filePrefix(String filePrefix) {
            this.filePrefix = filePrefix;
            return this;
        }

        public XLoggerBuilder flushInterval(int interval) {
            this.flushInterval = interval;
            return this;
        }

        public XLoggerBuilder maxTotalSize(long size) {
            this.maxTotalSize = size;
            return this;
        }

        public XLoggerBuilder maxSingleSize(long size) {
            this.maxSingleSize = size;
            return this;
        }

        public XLogger build() {
            if (this.maxTotalSize <= 0) {
                throw new IllegalArgumentException("Illegal argument, maxTotalSize <= 0");
            }
            if (this.flushInterval < 60) {
                throw new IllegalArgumentException("Illegal argument, flushInterval < 60!");
            }
            if (this.filePath == null) {
                this.filePath = "/data/Log/log0/" + this.context.getPackageName();
            }
            if (this.filePrefix == null) {
                String packageName = this.context.getPackageName();
                this.filePrefix = packageName.substring(packageName.lastIndexOf(46) + 1);
            }
            try {
                return new XLogger(this);
            } catch (XLogLoadLibraryException e) {
                LogUtils.w("XLogger", "Create XLogger fail!", e);
                return null;
            }
        }

        public String toString() {
            return "XLoggerBuilder{context=" + this.context + ", filePath='" + this.filePath + "', filePrefix='" + this.filePrefix + "', mode=" + this.mode + ", level=" + this.level + ", flushInterval=" + this.flushInterval + ", maxTotalSize=" + this.maxTotalSize + '}';
        }
    }
}

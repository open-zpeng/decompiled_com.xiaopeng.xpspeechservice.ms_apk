package com.xiaopeng.xpspeechservice.utils;

import android.os.SystemProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
/* loaded from: classes.dex */
public class XpDumpData {
    private static final String DEFAULT_PATH = "/sdcard/tts/tts.dump";
    private static final String TAG = "XpDumpData";
    private FileOutputStream mFos;
    private final boolean mIsEnable;
    private String mPath;

    public XpDumpData(boolean isEnable) {
        this(DEFAULT_PATH, isEnable);
    }

    public XpDumpData(String path, boolean isEnable) {
        this.mFos = null;
        this.mIsEnable = isEnable;
        this.mPath = path;
    }

    public void start() {
        boolean enable = SystemProperties.getBoolean("sys.xiaopeng.tts.dump", false);
        if (!enable && !this.mIsEnable) {
            return;
        }
        LogUtils.v(TAG, "tts dump enabled, start to dump");
        try {
            File file = new File(this.mPath);
            if (file.exists()) {
                file.delete();
            }
            this.mFos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dumpData(byte[] buffer) {
        try {
            if (this.mFos != null) {
                this.mFos.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dumpData(byte[] buffer, int offset, int size) {
        try {
            if (this.mFos != null) {
                this.mFos.write(buffer, offset, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void end() {
        try {
            if (this.mFos != null) {
                this.mFos.close();
                this.mFos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.xiaopeng.xpspeechservice.utils;

import android.util.Log;
/* loaded from: classes.dex */
public class LogUtils {
    public static int LOG_LEVEL = 0;
    private static final String TAG = "XpSpeech_";
    public static Logger sLogger = new DefaultLogger();
    public static boolean sLogEnable = true;

    /* loaded from: classes.dex */
    public interface Logger {
        void logByLevel(int i, String str, String str2);

        void logByLevel(int i, String str, String str2, Throwable th);
    }

    static {
        LOG_LEVEL = 2;
        if (XpSysUtils.isUserBuild()) {
            LOG_LEVEL = 3;
        }
    }

    public static boolean isLogEnable() {
        return sLogEnable;
    }

    public static void setLogEnable(boolean enable) {
        sLogEnable = enable;
    }

    public static void setLogLevel(int logLevel) {
        LOG_LEVEL = logLevel;
    }

    public static int getLogLevel() {
        return LOG_LEVEL;
    }

    public static void setLogger(Logger logger) {
        sLogger = logger;
    }

    public static boolean isLogLevelEnabled(int logLevel) {
        return LOG_LEVEL <= logLevel && isLogEnable();
    }

    public static void v(String msg) {
        if (isLogLevelEnabled(2)) {
            doLog(2, TAG, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (!isLogLevelEnabled(2)) {
            return;
        }
        doLog(2, TAG + tag, msg);
    }

    public static void v(String tag, String format, Object... v) {
        if (!isLogLevelEnabled(2)) {
            return;
        }
        String msg = String.format(format, v);
        doLog(2, TAG + tag, msg);
    }

    public static void v(String tag, String msg, Throwable t) {
        if (!isLogLevelEnabled(2)) {
            return;
        }
        doLog(2, TAG + tag, msg, t);
    }

    public static void v(String tag, Throwable t) {
        if (!isLogLevelEnabled(2)) {
            return;
        }
        doLog(2, TAG + tag, "Exception occurs at ", t);
    }

    public static void d(String msg) {
        if (isLogLevelEnabled(3)) {
            doLog(3, TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (!isLogLevelEnabled(3)) {
            return;
        }
        doLog(3, TAG + tag, msg);
    }

    public static void d(String tag, String format, Object... v) {
        if (!isLogLevelEnabled(3)) {
            return;
        }
        String msg = String.format(format, v);
        doLog(3, TAG + tag, msg);
    }

    public static void d(String tag, String msg, Throwable t) {
        if (!isLogLevelEnabled(3)) {
            return;
        }
        doLog(3, TAG + tag, msg, t);
    }

    public static void d(String tag, Throwable t) {
        if (!isLogLevelEnabled(3)) {
            return;
        }
        doLog(3, TAG + tag, "Exception occurs at ", t);
    }

    public static void i(String msg) {
        if (isLogLevelEnabled(4)) {
            doLog(4, TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (!isLogLevelEnabled(4)) {
            return;
        }
        doLog(4, TAG + tag, msg);
    }

    public static void i(String tag, String format, Object... v) {
        if (!isLogLevelEnabled(4)) {
            return;
        }
        String msg = String.format(format, v);
        doLog(4, TAG + tag, msg);
    }

    public static void i(String tag, String msg, Throwable t) {
        if (!isLogLevelEnabled(4)) {
            return;
        }
        doLog(4, TAG + tag, msg, t);
    }

    public static void i(String tag, Throwable t) {
        if (!isLogLevelEnabled(4)) {
            return;
        }
        doLog(4, TAG + tag, "Exception occurs at ", t);
    }

    public static void w(String msg) {
        if (isLogLevelEnabled(5)) {
            doLog(5, TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (!isLogLevelEnabled(5)) {
            return;
        }
        doLog(5, TAG + tag, msg);
    }

    public static void w(String tag, String format, Object... v) {
        if (!isLogLevelEnabled(5)) {
            return;
        }
        String msg = String.format(format, v);
        doLog(5, TAG + tag, msg);
    }

    public static void w(String tag, String msg, Throwable t) {
        if (!isLogLevelEnabled(5)) {
            return;
        }
        doLog(5, TAG + tag, msg, t);
    }

    public static void w(String tag, Throwable t) {
        if (!isLogLevelEnabled(5)) {
            return;
        }
        doLog(5, TAG + tag, "Exception occurs at ", t);
    }

    public static void e(String msg) {
        if (isLogLevelEnabled(6)) {
            doLog(6, TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (!isLogLevelEnabled(6)) {
            return;
        }
        doLog(6, TAG + tag, msg);
    }

    public static void e(String tag, String format, Object... v) {
        if (!isLogLevelEnabled(6)) {
            return;
        }
        String msg = String.format(format, v);
        doLog(6, TAG + tag, msg);
    }

    public static void e(String tag, String msg, Throwable t) {
        if (!isLogLevelEnabled(6)) {
            return;
        }
        doLog(6, TAG + tag, msg, t);
    }

    public static void e(String tag, Throwable t) {
        if (!isLogLevelEnabled(6)) {
            return;
        }
        doLog(6, TAG + tag, "Exception occurs at ", t);
    }

    private static void doLog(int logLevel, String tag, String msg) {
        sLogger.logByLevel(logLevel, tag, msg);
    }

    private static void doLog(int logLevel, String tag, String msg, Throwable t) {
        sLogger.logByLevel(logLevel, tag, msg, t);
    }

    /* loaded from: classes.dex */
    private static class DefaultLogger implements Logger {
        private DefaultLogger() {
        }

        @Override // com.xiaopeng.xpspeechservice.utils.LogUtils.Logger
        public void logByLevel(int type, String tag, String msg) {
            if (type == 2) {
                Log.v(tag, msg);
            } else if (type == 3) {
                Log.d(tag, msg);
            } else if (type == 4) {
                Log.i(tag, msg);
            } else if (type == 5) {
                Log.w(tag, msg);
            } else if (type == 6) {
                Log.e(tag, msg);
            }
        }

        @Override // com.xiaopeng.xpspeechservice.utils.LogUtils.Logger
        public void logByLevel(int type, String tag, String msg, Throwable t) {
            if (type == 2) {
                Log.v(tag, msg, t);
            } else if (type == 3) {
                Log.d(tag, msg, t);
            } else if (type == 4) {
                Log.i(tag, msg, t);
            } else if (type == 5) {
                Log.w(tag, msg, t);
            } else if (type == 6) {
                Log.e(tag, msg, t);
            }
        }
    }
}

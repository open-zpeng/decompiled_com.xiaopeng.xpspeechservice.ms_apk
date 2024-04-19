package com.xiaopeng.xpspeechservice.utils;

import android.os.Handler;
import android.os.Looper;
/* loaded from: classes.dex */
public class WorkHandler extends Handler {
    public WorkHandler(Looper looper) {
        super(looper);
    }

    public void optPost(Runnable runnable) {
        if (getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            post(runnable);
        }
    }

    public void optPostDelayed(Runnable runnable, long delayMillis) {
        if (getLooper() == Looper.myLooper() && delayMillis == 0) {
            runnable.run();
        } else {
            postDelayed(runnable, delayMillis);
        }
    }
}

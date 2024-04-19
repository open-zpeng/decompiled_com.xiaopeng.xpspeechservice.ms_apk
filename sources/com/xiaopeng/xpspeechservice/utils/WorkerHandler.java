package com.xiaopeng.xpspeechservice.utils;

import android.os.Handler;
import android.os.Looper;
/* loaded from: classes.dex */
public class WorkerHandler extends Handler {
    public WorkerHandler(Looper looper) {
        super(looper);
    }

    public void optPost(Runnable runnable) {
        if (getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            super.post(runnable);
        }
    }

    public void optPostDelay(Runnable runnable, long delayMillis) {
        if (getLooper() == Looper.myLooper() && delayMillis == 0) {
            runnable.run();
        } else {
            super.postDelayed(runnable, delayMillis);
        }
    }
}

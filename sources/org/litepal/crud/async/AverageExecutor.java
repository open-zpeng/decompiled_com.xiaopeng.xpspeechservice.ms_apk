package org.litepal.crud.async;

import org.litepal.crud.callback.AverageCallback;
/* loaded from: classes.dex */
public class AverageExecutor extends AsyncExecutor {
    private AverageCallback cb;

    public void listen(AverageCallback callback) {
        this.cb = callback;
        execute();
    }

    public AverageCallback getListener() {
        return this.cb;
    }
}

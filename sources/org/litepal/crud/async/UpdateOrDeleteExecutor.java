package org.litepal.crud.async;

import org.litepal.crud.callback.UpdateOrDeleteCallback;
/* loaded from: classes.dex */
public class UpdateOrDeleteExecutor extends AsyncExecutor {
    private UpdateOrDeleteCallback cb;

    public void listen(UpdateOrDeleteCallback callback) {
        this.cb = callback;
        execute();
    }

    public UpdateOrDeleteCallback getListener() {
        return this.cb;
    }
}

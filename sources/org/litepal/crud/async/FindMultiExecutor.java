package org.litepal.crud.async;

import org.litepal.crud.callback.FindMultiCallback;
/* loaded from: classes.dex */
public class FindMultiExecutor extends AsyncExecutor {
    private FindMultiCallback cb;

    public void listen(FindMultiCallback callback) {
        this.cb = callback;
        execute();
    }

    public FindMultiCallback getListener() {
        return this.cb;
    }
}

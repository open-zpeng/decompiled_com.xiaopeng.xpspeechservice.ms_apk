package org.litepal.crud.async;

import org.litepal.crud.callback.FindCallback;
/* loaded from: classes.dex */
public class FindExecutor extends AsyncExecutor {
    private FindCallback cb;

    public void listen(FindCallback callback) {
        this.cb = callback;
        execute();
    }

    public FindCallback getListener() {
        return this.cb;
    }
}

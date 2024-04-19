package org.litepal.crud.async;
/* loaded from: classes.dex */
public abstract class AsyncExecutor {
    private Runnable pendingTask;

    public void submit(Runnable task) {
        this.pendingTask = task;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void execute() {
        Runnable runnable = this.pendingTask;
        if (runnable != null) {
            new Thread(runnable).start();
        }
    }
}

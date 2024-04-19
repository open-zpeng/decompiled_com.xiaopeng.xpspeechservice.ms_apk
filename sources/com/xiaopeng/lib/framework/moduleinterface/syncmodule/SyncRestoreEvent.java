package com.xiaopeng.lib.framework.moduleinterface.syncmodule;

import android.support.annotation.NonNull;
import java.util.List;
/* loaded from: classes.dex */
public final class SyncRestoreEvent {
    public List<SyncData> list;
    public long uid;

    public SyncRestoreEvent(long uid, @NonNull List<SyncData> list) {
        this.uid = uid;
        this.list = list;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SyncRestoreEvent { uid:");
        sb.append(this.uid);
        sb.append("; list size:");
        List<SyncData> list = this.list;
        sb.append(list == null ? 0 : list.size());
        sb.append("; }");
        return sb.toString();
    }
}

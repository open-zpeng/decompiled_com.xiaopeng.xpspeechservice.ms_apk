package com.xiaopeng.system.delegate.module;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.systemdelegate.ISystemDelegate;
import com.xiaopeng.lib.utils.LogUtils;
/* loaded from: classes.dex */
public class SystemDelegateService implements ISystemDelegate {
    private static final String TAG = "SystemDelegateService";
    private Context mContext;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SystemDelegateService(Context context) {
        this.mContext = context;
        Module.register(SystemDelegateModuleEntry.class, new SystemDelegateModuleEntry(context));
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.systemdelegate.ISystemDelegate
    @Nullable
    public String getCertificate() throws RemoteException {
        LogUtils.d(TAG, "start getCertificate!");
        Uri uri = Uri.parse(SystemDelegateConstants.URI_SSL_CERT);
        Cursor cursor = this.mContext.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String cert = cursor.getString(0);
                    LogUtils.w(TAG, "query result success");
                    cursor.close();
                    return cert;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        LogUtils.w(TAG, "cursor is empty!");
        return null;
    }
}

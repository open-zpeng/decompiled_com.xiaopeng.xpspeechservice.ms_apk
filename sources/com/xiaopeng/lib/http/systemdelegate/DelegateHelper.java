package com.xiaopeng.lib.http.systemdelegate;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.xiaopeng.lib.http.IrdetoUtils;
/* loaded from: classes.dex */
public class DelegateHelper {
    private static final String CALLING_DATA = "data";
    private static final String CALLING_RESULT = "result";
    private static final String CONTENT_PATH_IRDETO_TOKEN_AC = "buildTokenDataAC";
    private static final String CONTENT_PATH_IRDETO_TOKEN_ALL = "buildTokenDataAll";
    private static final Uri DELEGATE_CONTENT_URI = Uri.parse("content://com.xiaopeng.system.delegate");

    public static String callBuildTokenDataThroughSystemDelegate(Context context, String[] ids, byte[] data) {
        String method;
        if (context == null) {
            return null;
        }
        ContentResolver resolver = context.getContentResolver();
        if (ids.equals(IrdetoUtils.TOKEN_ALL)) {
            method = CONTENT_PATH_IRDETO_TOKEN_ALL;
        } else {
            method = CONTENT_PATH_IRDETO_TOKEN_AC;
        }
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        Bundle bundle2 = resolver.call(DELEGATE_CONTENT_URI, method, (String) null, bundle);
        if (bundle2 == null) {
            return null;
        }
        return bundle2.getString(CALLING_RESULT);
    }
}

package com.xiaopeng.lib.utils.view;

import android.content.Context;
import android.widget.Toast;
/* loaded from: classes.dex */
public class ToastUtils {
    private static Toast mToast;

    public static void showToast(Context context, String msg) {
        Toast toast;
        if (context != null && (toast = getToast(context, msg)) != null) {
            toast.show();
        }
    }

    public static void showToast(Context context, int resourceId) {
        Toast toast;
        if (context != null && (toast = getToast(context, context.getResources().getString(resourceId))) != null) {
            toast.show();
        }
    }

    public static Toast getToast(Context context, String msg) {
        Toast toast = mToast;
        if (toast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), msg, 0);
        } else {
            toast.setText(msg);
        }
        return mToast;
    }
}

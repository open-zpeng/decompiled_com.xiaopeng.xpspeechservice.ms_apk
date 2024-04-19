package com.xiaopeng.lib.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
/* loaded from: classes.dex */
class NetUtils {
    NetUtils() {
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivity != null && (info = connectivity.getActiveNetworkInfo()) != null && info.isAvailable() && info.isConnected()) {
            return true;
        }
        return false;
    }
}

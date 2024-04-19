package com.xiaopeng.lib.framework.netchannelmodule.common;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import com.xiaopeng.lib.framework.moduleinterface.carcontroller.IRadioController;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.NetUtils;
/* loaded from: classes.dex */
public class ContextNetStatusProvider extends ContentProvider {
    public static final String TAG = "NetChannel-MqttChannel";
    private static Context mContext;
    private static MyPhoneStateListener mPhoneStateListener;

    public static Context getApplicationContext() {
        return mContext;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        Context ctx = getContext();
        if (ctx != null) {
            mContext = ctx.getApplicationContext();
            if (mPhoneStateListener == null) {
                mPhoneStateListener = new MyPhoneStateListener();
                TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService("phone");
                if (telephonyManager != null) {
                    telephonyManager.listen(mPhoneStateListener, IRadioController.TEF663x_PCHANNEL);
                }
            }
        }
        SharePrefsANRFix.fix();
        return true;
    }

    @Override // android.content.ContentProvider
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override // android.content.ContentProvider
    @Nullable
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override // android.content.ContentProvider
    @Nullable
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override // android.content.ContentProvider
    @Nullable
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Override // android.content.ContentProvider
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class MyPhoneStateListener extends PhoneStateListener {
        public int signalStrengthValue;

        private MyPhoneStateListener() {
        }

        @Override // android.telephony.PhoneStateListener
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99) {
                    this.signalStrengthValue = (signalStrength.getGsmSignalStrength() * 2) - 113;
                    return;
                } else {
                    this.signalStrengthValue = signalStrength.getGsmSignalStrength();
                    return;
                }
            }
            this.signalStrengthValue = signalStrength.getCdmaDbm();
        }
    }

    private static int getWifiRssi(Context context) {
        WifiInfo wifiInfo;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
        if (wifiManager != null && (wifiInfo = wifiManager.getConnectionInfo()) != null) {
            return wifiInfo.getRssi();
        }
        return 0;
    }

    public static int getNetStrength(int type) {
        Context context = mContext;
        if (context == null) {
            return 0;
        }
        if (type == 10) {
            int rssi = getWifiRssi(context);
            LogUtils.d(TAG, "current net type is wifi, rssi is " + rssi);
            return rssi;
        }
        MyPhoneStateListener myPhoneStateListener = mPhoneStateListener;
        if (myPhoneStateListener != null) {
            int dbm = myPhoneStateListener.signalStrengthValue;
            LogUtils.d(TAG, "current net type is mobile, dbm is " + dbm);
            return dbm;
        }
        LogUtils.d(TAG, "not init or not wifi, signal strength is 0");
        return 0;
    }

    public static int getNetType() {
        Context context = mContext;
        if (context != null) {
            return NetUtils.getNetworkType(context);
        }
        return 0;
    }

    public static int getNetStrength() {
        int type = getNetType();
        return getNetStrength(type);
    }
}

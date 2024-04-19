package com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.utils.ThreadUtils;
/* loaded from: classes.dex */
public class SystemStatusMonitor {
    static final String TAG = "SystemStatusMonitor";
    private static final int TYPE_NONE = -1;
    private BroadcastReceiver mBroadcastReceiver;

    /* loaded from: classes.dex */
    private static final class Holder {
        private static final SystemStatusMonitor INSTANCE = new SystemStatusMonitor();

        private Holder() {
        }
    }

    public static SystemStatusMonitor getInstance() {
        return Holder.INSTANCE;
    }

    public void registerBroadcastReceiver(Context context) {
        if (this.mBroadcastReceiver == null) {
            this.mBroadcastReceiver = new BroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction("android.intent.action.SCREEN_ON");
            context.registerReceiver(this.mBroadcastReceiver, filter);
        }
    }

    /* loaded from: classes.dex */
    private class BroadcastReceiver extends android.content.BroadcastReceiver {
        private BroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                onNetworkChanged();
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                MqttChannel.getInstance().disconnectedDueToException(new Exception("awake from sleep"));
            }
        }

        @SuppressLint({"Wakelock"})
        private void onNetworkChanged() {
            ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.SystemStatusMonitor.BroadcastReceiver.1
                private int mOldNetType = -1;

                @Override // java.lang.Runnable
                public void run() {
                    PowerManager pm = (PowerManager) GlobalConfig.getApplicationContext().getSystemService("power");
                    boolean connected = true;
                    PowerManager.WakeLock wl = pm.newWakeLock(1, "MQTT");
                    wl.acquire();
                    ConnectivityManager cm = (ConnectivityManager) GlobalConfig.getApplicationContext().getSystemService("connectivity");
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    if (networkInfo != null) {
                        int netType = networkInfo.getType();
                        if (this.mOldNetType != netType) {
                            if (!networkInfo.isAvailable() || !networkInfo.isConnected()) {
                                connected = false;
                            }
                            MqttChannel.getInstance().networkChanged(connected);
                        }
                        this.mOldNetType = netType;
                    } else {
                        this.mOldNetType = -1;
                        MqttChannel.getInstance().networkChanged(false);
                    }
                    wl.release();
                }
            });
        }
    }
}

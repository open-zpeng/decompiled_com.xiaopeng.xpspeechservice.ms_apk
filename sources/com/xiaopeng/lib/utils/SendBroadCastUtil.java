package com.xiaopeng.lib.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
/* loaded from: classes.dex */
public class SendBroadCastUtil {
    private static final String TAG = "SendBroadCastUtil";
    private static SendBroadCastUtil mUtil;
    private WeakReference<Context> mContext;

    private SendBroadCastUtil(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    public static synchronized SendBroadCastUtil getInstance(Context context) {
        SendBroadCastUtil sendBroadCastUtil;
        synchronized (SendBroadCastUtil.class) {
            if (mUtil == null || !mUtil.isContextAlive()) {
                mUtil = new SendBroadCastUtil(context);
            }
            sendBroadCastUtil = mUtil;
        }
        return sendBroadCastUtil;
    }

    private boolean isContextAlive() {
        WeakReference<Context> weakReference = this.mContext;
        if (weakReference != null && weakReference.get() != null) {
            return true;
        }
        return false;
    }

    public void sendBroadCast(String action) {
        LogUtils.i(TAG, "action:" + action);
        Intent intent = new Intent();
        intent.setAction(action);
        Context context = this.mContext.get();
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void sendBroadCast(String action, String name, int data) {
        LogUtils.i(TAG, "action:" + action + ",data[key:" + name + ",value:" + data + "]");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(name, data);
        Context context = this.mContext.get();
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void sendBroadCast(String action, String name, String data) {
        LogUtils.i(TAG, "action:" + action + ",data[key:" + name + ",value:" + data + "]");
        Intent intent = new Intent();
        intent.setAction(action);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(data)) {
            intent.putExtra(name, data);
        }
        Context context = this.mContext.get();
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void sendBroadCast(String action, Bundle bundle) {
        LogUtils.i(TAG, "action:" + action);
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        Context context = this.mContext.get();
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void sendBroadCast(final String action, final String name, final String data, long delay) {
        LogUtils.i(TAG, "延迟" + delay + "毫秒广播");
        new Timer().schedule(new TimerTask() { // from class: com.xiaopeng.lib.utils.SendBroadCastUtil.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                SendBroadCastUtil.this.sendBroadCast(action, name, data);
            }
        }, delay);
    }
}

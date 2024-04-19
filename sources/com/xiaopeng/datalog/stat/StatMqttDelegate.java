package com.xiaopeng.datalog.stat;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.xiaopeng.datalog.StatEvent;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEvent;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class StatMqttDelegate extends AbstractStatDelegate implements Handler.Callback {
    private static final int MSG_UPLOAD_CAN = 1;
    private static final int MSG_UPLOAD_CDU = 2;
    private static final int MSG_UPLOAD_CDU_IMMEDIATELY = 3;
    private static final int MSG_UPLOAD_CDU_WITH_FILES = 4;
    private static final int MSG_UPLOAD_FILES = 5;
    private static final int MSG_UPLOAD_LOG_ORIGIN = 6;
    private static final String TAG = "StatMqttDelegate";
    private Handler handler;

    public StatMqttDelegate(Context context) {
        super(context);
        DataCollectorHelper.getInstance().init(context);
        HandlerThread handlerThread = new HandlerThread(TAG, 10);
        handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper(), this);
    }

    @Override // com.xiaopeng.datalog.stat.AbstractStatDelegate
    public void uploadCdu(final IStatEvent event) {
        ThreadUtils.post(0, new Runnable() { // from class: com.xiaopeng.datalog.stat.StatMqttDelegate.1
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.d(StatMqttDelegate.TAG, "uploadCdu stat:" + event.toString());
                StatMqttDelegate.this.uploadCdu(event.toJson());
            }
        });
    }

    @Override // com.xiaopeng.datalog.stat.AbstractStatDelegate
    public void uploadCdu(final IMoleEvent event) {
        ThreadUtils.post(0, new Runnable() { // from class: com.xiaopeng.datalog.stat.StatMqttDelegate.2
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.d(StatMqttDelegate.TAG, "uploadCdu mole:" + event.toString());
                StatMqttDelegate.this.uploadCdu(event.toJson());
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void uploadCdu(String event) {
        if (!DataCollectorHelper.getInstance().isConnected()) {
            Message message = Message.obtain(this.handler, 2, event);
            this.handler.sendMessageDelayed(message, 1000L);
            return;
        }
        Message message2 = Message.obtain(this.handler, 2, event);
        this.handler.sendMessage(message2);
    }

    @Override // com.xiaopeng.datalog.stat.AbstractStatDelegate
    public void uploadCan(String data) {
        if (!DataCollectorHelper.getInstance().isConnected()) {
            Message message = Message.obtain(this.handler, 1, data);
            this.handler.sendMessageDelayed(message, 1000L);
            return;
        }
        Message message2 = Message.obtain(this.handler, 1, data);
        this.handler.sendMessage(message2);
    }

    @Override // com.xiaopeng.datalog.stat.AbstractStatDelegate
    public void uploadLogImmediately(String eventName, String data) {
        List<String> args = Arrays.asList(eventName, data);
        if (!DataCollectorHelper.getInstance().isConnected()) {
            Message message = Message.obtain(this.handler, 3, args);
            this.handler.sendMessageDelayed(message, 1000L);
            return;
        }
        Message message2 = Message.obtain(this.handler, 3, args);
        this.handler.sendMessage(message2);
    }

    @Override // com.xiaopeng.datalog.stat.AbstractStatDelegate
    public void uploadLogOrigin(String eventName, String data) {
        List<String> args = Arrays.asList(eventName, data);
        if (!DataCollectorHelper.getInstance().isConnected()) {
            Message message = Message.obtain(this.handler, 6, args);
            this.handler.sendMessageDelayed(message, 1000L);
            return;
        }
        Message message2 = Message.obtain(this.handler, 6, args);
        this.handler.sendMessage(message2);
    }

    @Override // com.xiaopeng.datalog.stat.AbstractStatDelegate
    public void uploadCduWithFiles(IStatEvent event, List<String> filePaths) {
        StatEventWithFiles args = new StatEventWithFiles((StatEvent) event, filePaths);
        if (!DataCollectorHelper.getInstance().isConnected()) {
            Message message = Message.obtain(this.handler, 4, args);
            this.handler.sendMessageDelayed(message, 1000L);
            return;
        }
        Message message2 = Message.obtain(this.handler, 4, args);
        this.handler.sendMessage(message2);
    }

    @Override // com.xiaopeng.datalog.stat.AbstractStatDelegate
    public void uploadFiles(List<String> filePaths) {
        if (!DataCollectorHelper.getInstance().isConnected()) {
            Message message = Message.obtain(this.handler, 5, filePaths);
            this.handler.sendMessageDelayed(message, 1000L);
            return;
        }
        Message message2 = Message.obtain(this.handler, 5, filePaths);
        this.handler.sendMessage(message2);
    }

    @Override // com.xiaopeng.datalog.stat.AbstractStatDelegate
    public String uploadRecentSystemLog() {
        return DataCollectorHelper.getInstance().uploadRecentSystemLog();
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                String data = (String) msg.obj;
                DataCollectorHelper.getInstance().uploadCan(data);
                break;
            case 2:
                String data2 = (String) msg.obj;
                DataCollectorHelper.getInstance().uploadCdu(data2);
                break;
            case 3:
                List<String> args = (List) msg.obj;
                String eventName = args.get(0);
                String data3 = args.get(1);
                DataCollectorHelper.getInstance().uploadLogImmediately(eventName, data3);
                break;
            case 4:
                StatEventWithFiles args2 = (StatEventWithFiles) msg.obj;
                DataCollectorHelper.getInstance().uploadCduWithFiles(args2.event, args2.filePaths);
                break;
            case 5:
                List<String> fileList = (List) msg.obj;
                DataCollectorHelper.getInstance().uploadFiles(fileList);
                break;
            case 6:
                List<String> args3 = (List) msg.obj;
                String eventName2 = args3.get(0);
                String data4 = args3.get(1);
                DataCollectorHelper.getInstance().uploadLogOrigin(eventName2, data4);
                break;
        }
        return true;
    }

    /* loaded from: classes.dex */
    private class StatEventWithFiles {
        StatEvent event;
        List<String> filePaths;

        public StatEventWithFiles(StatEvent event, List<String> filePaths) {
            this.event = event;
            this.filePaths = filePaths;
        }
    }
}

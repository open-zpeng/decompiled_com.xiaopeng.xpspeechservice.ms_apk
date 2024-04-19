package com.xiaopeng.xpspeechservice.ms.tts.uploaddata;

import com.google.gson.Gson;
import com.xiaopeng.datalog.DataLogModuleEntry;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.util.HashMap;
/* loaded from: classes.dex */
public class UploadDataModel {
    private static final boolean DEBUG = false;
    private static final String TAG = "UploadDataModel";
    private static final String TTS_BUTTON_ID = "B001";
    private static final String TTS_DATA_KEY = "data";
    private static final String TTS_EVENT_NAME = "tts_data";
    private static final String TTS_PAGE_ID = "P10067";
    private IDataLog mDataLogService;
    private HashMap<String, UploadItem> mUploadItemMap;

    /* loaded from: classes.dex */
    private class UploadItem {
        public UploadDataBean data;
        public boolean isHybridDone = false;
        public boolean isOfflineSynthing = false;
        public boolean isOnlineSynthing = false;

        public UploadItem(UploadDataBean data) {
            this.data = data;
        }
    }

    private UploadDataModel() {
        this.mDataLogService = (IDataLog) Module.get(DataLogModuleEntry.class).get(IDataLog.class);
        this.mUploadItemMap = new HashMap<>();
    }

    /* loaded from: classes.dex */
    private static class SingleHolder {
        private static UploadDataModel instance = new UploadDataModel();

        private SingleHolder() {
        }
    }

    public static UploadDataModel getInstance() {
        return SingleHolder.instance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void uploadTtsData(UploadDataBean data) {
        String jsonData = new Gson().toJson(data);
        LogUtils.i(TAG, jsonData);
        IDataLog iDataLog = this.mDataLogService;
        iDataLog.sendStatData(iDataLog.buildMoleEvent().setEvent(TTS_EVENT_NAME).setPageId(TTS_PAGE_ID).setButtonId(TTS_BUTTON_ID).setProperty("data", jsonData).build());
    }

    public void setSrcData(final String uid, final SrcUploadData data) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel.1
            @Override // java.lang.Runnable
            public void run() {
                UploadDataBean uploadData = new UploadDataBean();
                uploadData.setText(data.text);
                uploadData.setSource(data.source);
                uploadData.setStartTime(data.startTime);
                UploadDataModel.this.mUploadItemMap.put(uid, new UploadItem(uploadData));
            }
        });
    }

    public void notifyOfflineSynthing(final String uid) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel.2
            @Override // java.lang.Runnable
            public void run() {
                UploadItem item = (UploadItem) UploadDataModel.this.mUploadItemMap.get(uid);
                if (item != null) {
                    item.isOfflineSynthing = true;
                }
            }
        });
    }

    public void setOfflineData(final String uid, final OfflineUploadData data) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel.3
            @Override // java.lang.Runnable
            public void run() {
                UploadItem item = (UploadItem) UploadDataModel.this.mUploadItemMap.get(uid);
                if (item != null) {
                    UploadDataBean uploadData = item.data;
                    uploadData.setOfflineFirstDataLatency(data.offlineFirstDataLatency);
                    uploadData.setOfflineSynthTime(data.offlineSynthTime);
                    uploadData.setOfflineState(data.state.getDesc());
                    if (item.isHybridDone && !item.isOnlineSynthing) {
                        UploadDataModel.this.uploadTtsData(uploadData);
                        UploadDataModel.this.mUploadItemMap.remove(uid);
                        return;
                    }
                    item.isOfflineSynthing = false;
                }
            }
        });
    }

    public void notifyOnlineSynthing(final String uid) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel.4
            @Override // java.lang.Runnable
            public void run() {
                UploadItem item = (UploadItem) UploadDataModel.this.mUploadItemMap.get(uid);
                if (item != null) {
                    item.isOnlineSynthing = true;
                }
            }
        });
    }

    public void setOnlineData(final String uid, final OnlineUploadData data) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel.5
            @Override // java.lang.Runnable
            public void run() {
                UploadItem item = (UploadItem) UploadDataModel.this.mUploadItemMap.get(uid);
                if (item != null) {
                    UploadDataBean uploadData = item.data;
                    uploadData.setOnlineDataType(data.onlineDataType);
                    uploadData.setOnlineMsgId(data.onlineMsgId);
                    uploadData.setOnlineFirstDataLatency(data.onlineFirstDataLatency);
                    uploadData.setOnlineDataMaxLatency(data.onlineDataMaxLatency);
                    uploadData.setOnlineSynthTime(data.onlineSynthTime);
                    uploadData.setOnlineDecodeLatency(data.onlineDecodeLatency);
                    uploadData.setOnlineDataLength(data.onlineDataLength);
                    uploadData.setOnlineState(data.state.getDesc());
                    if (item.isHybridDone && !item.isOfflineSynthing) {
                        UploadDataModel.this.uploadTtsData(uploadData);
                        UploadDataModel.this.mUploadItemMap.remove(uid);
                        return;
                    }
                    item.isOnlineSynthing = false;
                }
            }
        });
    }

    public void setMediaData(final String uid, final MediaUploadData data) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel.6
            @Override // java.lang.Runnable
            public void run() {
                UploadItem item = (UploadItem) UploadDataModel.this.mUploadItemMap.get(uid);
                if (item != null) {
                    UploadDataBean uploadData = item.data;
                    uploadData.setMode(data.mode.getDesc());
                    uploadData.setMediaDecodeLatency(data.mediaDecodeLatency);
                    uploadData.setMediaFirstFrameLatency(data.mediaFirstFrameLatency);
                    uploadData.setFirstFrameLatency(data.mediaFirstFrameLatency);
                    uploadData.setDataMaxLatency(data.dataMaxLatency);
                    uploadData.setDataLength(data.dataLength);
                    uploadData.setMediaState(data.state.getDesc());
                    uploadData.setState(data.state.getDesc());
                    uploadData.setEndTime(data.endTime);
                    UploadDataModel.this.uploadTtsData(uploadData);
                    UploadDataModel.this.mUploadItemMap.remove(uid);
                }
            }
        });
    }

    public void setHybridData(final String uid, final HybridUploadData data) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel.7
            @Override // java.lang.Runnable
            public void run() {
                UploadItem item = (UploadItem) UploadDataModel.this.mUploadItemMap.get(uid);
                if (item != null) {
                    UploadDataBean uploadData = item.data;
                    uploadData.setMode(data.mode.getDesc());
                    uploadData.setFirstFrameLatency(data.firstFrameLatency);
                    uploadData.setDataMaxLatency(data.dataMaxLatency);
                    uploadData.setDataLength(data.dataLength);
                    uploadData.setState(data.state.getDesc());
                    uploadData.setEndTime(data.endTime);
                    if (!item.isOfflineSynthing && !item.isOnlineSynthing) {
                        UploadDataModel.this.uploadTtsData(uploadData);
                        UploadDataModel.this.mUploadItemMap.remove(uid);
                        return;
                    }
                    item.isHybridDone = true;
                }
            }
        });
    }
}

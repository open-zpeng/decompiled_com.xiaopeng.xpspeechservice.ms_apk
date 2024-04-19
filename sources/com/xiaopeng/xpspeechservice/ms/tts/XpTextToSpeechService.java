package com.xiaopeng.xpspeechservice.ms.tts;

import android.content.Intent;
import android.os.Bundle;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import com.xiaopeng.speech.tts.XpSynthesisCallback;
import com.xiaopeng.speech.tts.XpSynthesisRequest;
import com.xiaopeng.speech.tts.XpTextToSpeechServiceBase;
import com.xiaopeng.xpspeechservice.ms.EnvMonitor;
import com.xiaopeng.xpspeechservice.ms.tts.config.ConfigModel;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.SrcUploadData;
import com.xiaopeng.xpspeechservice.ms.tts.uploaddata.UploadDataModel;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.util.Locale;
/* loaded from: classes.dex */
public class XpTextToSpeechService extends XpTextToSpeechServiceBase {
    private static final String TAG = "XpTextToSpeechService";
    private volatile String[] mCurrentLanguage = null;
    private XpTtsEngine mXpTtsEngine;

    @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase, android.app.Service
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG, "onCreate 20221010");
        EnvMonitor.getInstance();
        this.mXpTtsEngine = XpTtsEngine.getInstance();
        this.mXpTtsEngine.init(getChannelIdList());
        UploadDataModel.getInstance();
        ConfigModel.getInstance();
        afterOnCreate();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1;
    }

    @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase, android.app.Service
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG, "onDestroy +++");
        this.mXpTtsEngine.shutdown();
        this.mXpTtsEngine = null;
        LogUtils.i(TAG, "onDestroy ---");
    }

    @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase
    protected int onIsLanguageAvailable(String lang, String country, String variant) {
        if (Locale.SIMPLIFIED_CHINESE.getISO3Language().equals(lang) || Locale.US.getISO3Language().equals(lang)) {
            if (Locale.SIMPLIFIED_CHINESE.getISO3Country().equals(country) || Locale.US.getISO3Country().equals(country)) {
                return 1;
            }
            return 0;
        }
        return -2;
    }

    @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase
    protected String[] onGetLanguage() {
        return this.mCurrentLanguage;
    }

    @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase
    protected int onLoadLanguage(String lang, String country, String variant) {
        this.mCurrentLanguage = new String[]{lang, country, ""};
        return onIsLanguageAvailable(lang, country, variant);
    }

    @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase
    protected void onSynthesizeText(XpSynthesisRequest request, XpSynthesisCallback cb) {
        String txt = request.getCharSequenceText().toString();
        Bundle params = request.getParams();
        String uid = params.getString("uid", "");
        LogUtils.i(TAG, "onSynthesizeText %s uid %s", txt, uid);
        String source = params.getString("source", BuildInfoUtils.UNKNOWN);
        long startTime = System.currentTimeMillis();
        SrcUploadData data = new SrcUploadData(txt, source, startTime);
        UploadDataModel.getInstance().setSrcData(uid, data);
        IEngineCallback callback = new MyEngineCallback(cb);
        this.mXpTtsEngine.speak(params, callback);
    }

    @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase
    protected void onStop(Bundle params) {
        String uid = params.getString("uid", "");
        String channelName = getChannelNameForId(params.getInt("channel", -1));
        LogUtils.i(TAG, "onStop uid %s on channel %s", uid, channelName);
        this.mXpTtsEngine.stop(params);
    }

    @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase
    protected void setViceBtEnable(boolean on) {
        LogUtils.i(TAG, "setViceBtEnable " + on);
        this.mXpTtsEngine.setViceBtEnable(on);
    }

    /* loaded from: classes.dex */
    private class MyEngineCallback implements IEngineCallback {
        private XpSynthesisCallback mCallback;

        public MyEngineCallback(XpSynthesisCallback cb) {
            this.mCallback = cb;
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback
        public void begin(int sampleRate, int format, int channelCount) {
            LogUtils.i(XpTextToSpeechService.TAG, "begin %d %d %d", Integer.valueOf(sampleRate), Integer.valueOf(format), Integer.valueOf(channelCount));
            this.mCallback.start(sampleRate, format, channelCount);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback
        public void received(byte[] audioData) {
            if (audioData.length != 0) {
                this.mCallback.audioAvailable(audioData, 0, audioData.length);
            }
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback
        public void end() {
            LogUtils.i(XpTextToSpeechService.TAG, "end");
            this.mCallback.done();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback
        public void error() {
            LogUtils.i(XpTextToSpeechService.TAG, "error");
            this.mCallback.error();
            this.mCallback.done();
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback
        public void uploadInfo(Bundle info) {
            this.mCallback.uploadInfo(info);
        }
    }
}

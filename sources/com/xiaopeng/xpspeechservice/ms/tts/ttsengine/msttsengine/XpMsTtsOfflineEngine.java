package com.xiaopeng.xpspeechservice.ms.tts.ttsengine.msttsengine;

import android.os.Bundle;
import com.android.internal.util.FastXmlSerializer;
import com.microsoft.cognitiveservices.speech.AudioDataStream;
import com.microsoft.cognitiveservices.speech.EmbeddedSpeechConfig;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.StreamStatus;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.util.EventHandler;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import com.xiaopeng.xpspeechservice.utils.XpSysUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
/* loaded from: classes.dex */
public class XpMsTtsOfflineEngine implements ITtsEngine {
    private static final String CLOUD_VOICE_NAME = "zh-CN-XiaoxiaoNeural";
    private static final int SAMPLE_RATE_OFFLINE = 16000;
    private static final String SSML_STRING = "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\"zh-CN\"><voice name=\"zh-CN-XiaoxiaoNeural\"><prosody rate=\"%s\"><![CDATA[%s]]></prosody></voice></speak>";
    private static final String SYNTH_BACKEND = "offline";
    private static final String serviceRegion = "chinaeast2";
    private static final String speechSubscriptionKey = "64677d2f93834318a4dd0e524e14c1a0";
    private static final String warmUpOfflineTxt = "小鹏汽车是一家专注于智能出行的新势力企业";
    private ITtsEngineCallback mCallback;
    private SpeechConfig mSpeechConfig;
    private volatile AudioDataStream mStream;
    private SpeechSynthesisResult mSynthResult;
    private SpeechSynthesizer mSynthesizer;
    private String TAG = "XpMsTtsOfflineEngine";
    private volatile boolean mIsFirstData = false;
    private volatile int mDataLength = 0;

    public XpMsTtsOfflineEngine(String channelName, ITtsEngineCallback cb) {
        this.TAG += "_" + channelName;
        LogUtils.i(this.TAG, "XpMsTtsOfflineEngine construct");
        this.mCallback = cb;
    }

    public void initEngine() {
        LogUtils.i(this.TAG, "initEngine");
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
        if (new File("/system/etc/speech/XiaoxiaoDeviceV4").exists()) {
            EmbeddedSpeechConfig.fromPath("/system/etc/speech/XiaoxiaoDeviceV4");
            speechConfig.setProperty(PropertyId.SpeechServiceConnection_SynthOfflineDataPath, "/system/etc/speech/XiaoxiaoDeviceV4");
        } else {
            LogUtils.e(this.TAG, "offline data not exists");
        }
        speechConfig.setProperty(PropertyId.SpeechServiceConnection_SynthBackend, SYNTH_BACKEND);
        speechConfig.setProperty("SPEECH-SynthBackendSwitchingPolicy", "force_offline");
        speechConfig.setProperty("SPEECH-SynthOfflineIgnoreFormatCheckAndResampling", "true");
        if (XpSysUtils.isDevBuild()) {
            String logName = "mstts.log";
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                logName = String.format("mstts_%s.log", sdf.format(new Date()));
            } catch (Exception e) {
                LogUtils.e(this.TAG, "date format error", e);
            }
            boolean isCreatePathSuccess = true;
            File logPath = new File("/sdcard/tts");
            if (!logPath.exists()) {
                isCreatePathSuccess = logPath.mkdir();
            }
            if (isCreatePathSuccess) {
                File logFile = new File("/sdcard/tts", logName);
                speechConfig.setProperty(PropertyId.Speech_LogFilename, logFile.getAbsolutePath());
                String str = this.TAG;
                LogUtils.v(str, "log file /sdcard/tts/" + logName);
            }
        }
        SpeechSynthesisOutputFormat format = SpeechSynthesisOutputFormat.Raw16Khz16BitMonoPcm;
        speechConfig.setSpeechSynthesisOutputFormat(format);
        this.mSynthesizer = new SpeechSynthesizer(speechConfig, (AudioConfig) null);
        doWarmUp();
        this.mSynthesizer.SynthesisStarted.addEventListener(new EventHandler() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.msttsengine.-$$Lambda$XpMsTtsOfflineEngine$68kRJ6JRMzfiUdaIy-7HiBRaDSA
            @Override // com.microsoft.cognitiveservices.speech.util.EventHandler
            public final void onEvent(Object obj, Object obj2) {
                XpMsTtsOfflineEngine.this.lambda$initEngine$0$XpMsTtsOfflineEngine(obj, (SpeechSynthesisEventArgs) obj2);
            }
        });
        this.mSynthesizer.Synthesizing.addEventListener(new EventHandler() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.msttsengine.-$$Lambda$XpMsTtsOfflineEngine$Z1BU9yGglJXfRp2vBMQXlupQd8g
            @Override // com.microsoft.cognitiveservices.speech.util.EventHandler
            public final void onEvent(Object obj, Object obj2) {
                XpMsTtsOfflineEngine.this.lambda$initEngine$1$XpMsTtsOfflineEngine(obj, (SpeechSynthesisEventArgs) obj2);
            }
        });
        this.mSynthesizer.SynthesisCompleted.addEventListener(new EventHandler() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.msttsengine.-$$Lambda$XpMsTtsOfflineEngine$33rFEwULcwmyavN6JeLcInuuJIQ
            @Override // com.microsoft.cognitiveservices.speech.util.EventHandler
            public final void onEvent(Object obj, Object obj2) {
                XpMsTtsOfflineEngine.this.lambda$initEngine$2$XpMsTtsOfflineEngine(obj, (SpeechSynthesisEventArgs) obj2);
            }
        });
        this.mSynthesizer.SynthesisCanceled.addEventListener(new EventHandler() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttsengine.msttsengine.-$$Lambda$XpMsTtsOfflineEngine$0e8LocfAZ0EZta98ON9luSmtkik
            @Override // com.microsoft.cognitiveservices.speech.util.EventHandler
            public final void onEvent(Object obj, Object obj2) {
                XpMsTtsOfflineEngine.this.lambda$initEngine$3$XpMsTtsOfflineEngine(obj, (SpeechSynthesisEventArgs) obj2);
            }
        });
        this.mSpeechConfig = speechConfig;
    }

    public /* synthetic */ void lambda$initEngine$0$XpMsTtsOfflineEngine(Object o, SpeechSynthesisEventArgs e) {
        SpeechSynthesisResult result = e.getResult();
        String resultId = result.getResultId();
        result.close();
        String str = this.TAG;
        LogUtils.i(str, "Synthesis started resultId " + resultId);
        this.mCallback.onEvent(EventType.EVENT_SYNTH_BEGIN, resultId);
        e.close();
    }

    public /* synthetic */ void lambda$initEngine$1$XpMsTtsOfflineEngine(Object o, SpeechSynthesisEventArgs e) {
        SpeechSynthesisResult result = e.getResult();
        this.mDataLength += result.getAudioData().length;
        if (this.mIsFirstData) {
            this.mIsFirstData = false;
            String resultId = result.getResultId();
            LogUtils.i(this.TAG, "Synthesis first data resultId " + resultId);
            Bundle bundle = new Bundle();
            bundle.putInt("sampleRate", SAMPLE_RATE_OFFLINE);
            bundle.putInt("format", 2);
            bundle.putInt("channelCount", 1);
            bundle.putString("resultId", resultId);
            this.mCallback.onEvent(EventType.EVENT_DATA_READY, bundle);
        }
        result.close();
        e.close();
    }

    public /* synthetic */ void lambda$initEngine$2$XpMsTtsOfflineEngine(Object o, SpeechSynthesisEventArgs e) {
        SpeechSynthesisResult result = e.getResult();
        String resultId = result.getResultId();
        result.close();
        e.close();
        String str = this.TAG;
        LogUtils.i(str, "Synthesis completed resultId " + resultId);
        String str2 = this.TAG;
        LogUtils.v(str2, "bufferLen " + this.mDataLength);
        Bundle bundle = new Bundle();
        bundle.putInt("duration", this.mDataLength / 32);
        bundle.putString("resultId", resultId);
        this.mCallback.onEvent(EventType.EVENT_SYNTH_END, bundle);
    }

    public /* synthetic */ void lambda$initEngine$3$XpMsTtsOfflineEngine(Object o, SpeechSynthesisEventArgs e) {
        SpeechSynthesisResult result = e.getResult();
        String resultId = result.getResultId();
        SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(result);
        result.close();
        e.close();
        LogUtils.w(this.TAG, "Synthesis canceled resultId %s %s", resultId, cancellation.toString());
        this.mCallback.onEvent(EventType.EVENT_SYNTH_ERROR, resultId);
    }

    private void doWarmUp() {
        LogUtils.i(this.TAG, "doWarmUp +++");
        try {
            this.mSynthesizer.SpeakText(warmUpOfflineTxt);
        } catch (Exception e) {
            LogUtils.e(this.TAG, "doWarmUp error", e);
        }
        LogUtils.i(this.TAG, "doWarmUp ---");
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine
    public void speak(Bundle params) {
        String ssml;
        LogUtils.i(this.TAG, "speak");
        String txt = params.getString("txt");
        try {
            StringWriter writer = new StringWriter();
            FastXmlSerializer xml = new FastXmlSerializer();
            xml.setOutput(writer);
            xml.startTag((String) null, "speak");
            xml.attribute((String) null, "xmlns", "http://www.w3.org/2001/10/synthesis");
            xml.attribute("xmlns", "mstts", "http://www.w3.org/2001/mstts");
            xml.attribute("xmlns", "emo", "http://www.w3.org/2009/10/emotionml");
            xml.attribute((String) null, "version", "1.0");
            xml.attribute("xml", "lang", "zh-CN");
            xml.startTag((String) null, "voice");
            xml.attribute((String) null, "name", CLOUD_VOICE_NAME);
            xml.text(txt);
            xml.endTag((String) null, "voice");
            xml.endTag((String) null, "speak");
            xml.flush();
            ssml = writer.toString().replaceAll(">\n", ">");
        } catch (IOException e) {
            LogUtils.e(this.TAG, "build xml error", e);
            ssml = String.format(SSML_STRING, "1.0", txt);
        }
        LogUtils.v(this.TAG, ssml);
        this.mIsFirstData = true;
        this.mDataLength = 0;
        this.mSynthResult = this.mSynthesizer.StartSpeakingSsml(ssml);
        this.mStream = AudioDataStream.fromResult(this.mSynthResult);
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine
    public void stop() {
        LogUtils.v(this.TAG, "stop");
        this.mSynthesizer.StopSpeakingAsync();
    }

    public void destroy() {
        LogUtils.i(this.TAG, "destroy");
        SpeechSynthesisResult speechSynthesisResult = this.mSynthResult;
        if (speechSynthesisResult != null) {
            speechSynthesisResult.close();
        }
        SpeechSynthesizer speechSynthesizer = this.mSynthesizer;
        if (speechSynthesizer != null) {
            speechSynthesizer.close();
        }
        SpeechConfig speechConfig = this.mSpeechConfig;
        if (speechConfig != null) {
            speechConfig.close();
        }
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.ttsengine.ITtsEngine
    public int getData(byte[] buffer) {
        return (int) this.mStream.readData(buffer);
    }

    public boolean isDataStreamEnd() {
        if (this.mStream.getStatus() == StreamStatus.Canceled) {
            LogUtils.v(this.TAG, "stream status Canceled");
            return true;
        } else if (this.mStream.getStatus() == StreamStatus.AllData) {
            LogUtils.v(this.TAG, "stream status AllData");
            return true;
        } else {
            return false;
        }
    }

    public void reset() {
        LogUtils.i(this.TAG, "reset");
        destroy();
        initEngine();
    }
}

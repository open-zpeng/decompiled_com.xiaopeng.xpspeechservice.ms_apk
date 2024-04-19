package com.microsoft.msttsengine;

import android.content.Context;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.analytics.AnalyticsTransmissionTarget;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
/* loaded from: classes.dex */
public class SpeechSynthesizer {
    private AnalyticsTransmissionTarget libTarget;
    private Synthesizer synthesizer;
    private SpeechSynthesizerConfig synthesizerConfig;
    private ArrayList<VoiceInfo> voices = new ArrayList<>();
    private VoiceInfo selectedVoice = null;
    private String ANALYTICS_SECRET = "2448e23d2fd74160ad920d1046ad2e7d-1c36d59a-4196-472a-9a44-8dd0a866e74a-7586";

    public SpeechSynthesizer(Context context, SpeechSynthesizerConfig config) {
        this.synthesizerConfig = config;
        AppCenter.startFromLibrary(context, new Class[]{Analytics.class});
        this.libTarget = Analytics.getTransmissionTarget(this.ANALYTICS_SECRET);
        this.libTarget.trackEvent("newSpeechSynthesizer");
        InitSynthesizer();
    }

    public void InitSynthesizer() {
        this.synthesizer = new Synthesizer();
        this.synthesizer.init();
        InstallVoices();
        this.libTarget.trackEvent("InitSynthesizer");
    }

    public void InstallVoices() {
        try {
            this.synthesizer.installVoices(this.synthesizerConfig.GetModelPath());
        } catch (MsttsException e) {
            Log.e("Error", "voice installation error");
        }
        SetTTSVoice();
        this.libTarget.trackEvent("InstallVoices");
    }

    public void SetTTSVoice() {
        this.voices = this.synthesizer.getInstalledVoices();
        if (!this.voices.isEmpty()) {
            int i = 0;
            while (true) {
                if (i >= this.voices.size()) {
                    break;
                } else if (!this.voices.get(i).strVoiceName.toLowerCase().contains(this.synthesizerConfig.GetModelLanguage().toLowerCase())) {
                    i++;
                } else {
                    this.selectedVoice = this.voices.get(i);
                    break;
                }
            }
            this.libTarget.trackEvent("SetTTSVoice");
            return;
        }
        throw new MsttsException("SpeechSynthesizer SetTTSVoice failed", MsttsException.TTSERR_UNIT_NAME_NOT_FOUND);
    }

    public void StartSpeechSynthesisPlaybackAsync(String s) {
        StartSpeechSynthesisPlaybackAsync(s, null);
    }

    public void StartSpeechSynthesisPlaybackAsync(String s, Runnable callback) {
        this.synthesizer.setReceiveWave(new TtsCallback() { // from class: com.microsoft.msttsengine.SpeechSynthesizer.1
            public byte[] waveData = null;

            @Override // com.microsoft.msttsengine.TtsCallback
            public int receiveWave(long responceHandle, byte[] data, int size) {
                byte[] bArr = this.waveData;
                if (bArr == null) {
                    byte[] tmp = new byte[data.length];
                    System.arraycopy(data, 0, tmp, 0, data.length);
                    this.waveData = tmp;
                } else {
                    byte[] tmp2 = new byte[bArr.length + data.length];
                    System.arraycopy(bArr, 0, tmp2, 0, bArr.length);
                    System.arraycopy(data, 0, tmp2, this.waveData.length, data.length);
                    this.waveData = tmp2;
                }
                return 0;
            }

            @Override // com.microsoft.msttsengine.TtsCallback
            public byte[] getWaveData() {
                return this.waveData;
            }
        });
        VoiceInfo voiceInfo = this.selectedVoice;
        if (voiceInfo != null) {
            this.synthesizer.setVoice(voiceInfo);
        }
        this.synthesizer.setOutput();
        this.synthesizer.speak(s, 0);
        playSound(this.synthesizer.getWaveData(), callback);
        Map<String, String> properties = new HashMap<>();
        properties.put("VoiceName", this.selectedVoice.strVoiceName);
        this.libTarget.trackEvent("StartSpeechSynthesisPlaybackAsync", properties);
    }

    private void playSound(final byte[] sound, final Runnable callback) {
        if (sound == null || sound.length == 0) {
            return;
        }
        AsyncTask.execute(new Runnable() { // from class: com.microsoft.msttsengine.SpeechSynthesizer.2
            @Override // java.lang.Runnable
            public void run() {
                AudioTrack audioTrack = new AudioTrack(3, 16000, 2, 2, AudioTrack.getMinBufferSize(16000, 2, 2), 1);
                if (audioTrack.getState() == 1) {
                    audioTrack.play();
                    byte[] bArr = sound;
                    audioTrack.write(bArr, 0, bArr.length);
                }
                Runnable runnable = callback;
                if (runnable != null) {
                    runnable.run();
                }
                audioTrack.stop();
                audioTrack.release();
            }
        });
    }

    private ArrayList<String> getLocalVoices() {
        File dir = new File(this.synthesizerConfig.GetModelPath());
        if (dir.list() != null) {
            return new ArrayList<>(Arrays.asList(dir.list(new FilenameFilter() { // from class: com.microsoft.msttsengine.SpeechSynthesizer.3
                @Override // java.io.FilenameFilter
                public boolean accept(File file, String s) {
                    return new File(file, s).isDirectory();
                }
            })));
        }
        return new ArrayList<>();
    }
}

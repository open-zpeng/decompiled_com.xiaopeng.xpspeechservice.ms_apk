package com.xiaopeng.speech.tts;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.speech.tts.ITextToSpeechCallback;
import android.speech.tts.ITextToSpeechService;
import android.speech.tts.Voice;
import java.util.List;
/* loaded from: classes.dex */
public class ITextToSpeechServiceWrapper extends ITextToSpeechService.Stub {
    private XpTextToSpeechServiceBase mService;

    public ITextToSpeechServiceWrapper(XpTextToSpeechServiceBase service) {
        this.mService = service;
    }

    public void close() {
        this.mService = null;
    }

    public int speak(IBinder caller, CharSequence text, int queueMode, Bundle params, String utteranceId) {
        return this.mService.speak(caller, text, queueMode, params, utteranceId);
    }

    public int synthesizeToFileDescriptor(IBinder caller, CharSequence text, ParcelFileDescriptor fileDescriptor, Bundle params, String utteranceId) {
        return -1;
    }

    public int playAudio(IBinder caller, Uri audioUri, int queueMode, Bundle params, String utteranceId) {
        return -1;
    }

    public int playSilence(IBinder caller, long duration, int queueMode, String utteranceId) {
        return -1;
    }

    public boolean isSpeaking() {
        return this.mService.isSpeaking();
    }

    public boolean isSpeakingByChannel(int channel) {
        return this.mService.isSpeaking(channel);
    }

    public int stop(IBinder caller, String utteranceId) {
        return this.mService.stop(caller, utteranceId);
    }

    public int stopByChannel(IBinder caller, int[] channelList) {
        return this.mService.stop(caller, channelList);
    }

    public String[] getLanguage() {
        return this.mService.getLanguage();
    }

    public String[] getClientDefaultLanguage() {
        return this.mService.getClientDefaultLanguage();
    }

    public int isLanguageAvailable(String lang, String country, String variant) {
        return this.mService.isLanguageAvailable(lang, country, variant);
    }

    public String[] getFeaturesForLanguage(String lang, String country, String variant) {
        return this.mService.getFeaturesForLanguage(lang, country, variant);
    }

    public int loadLanguage(IBinder caller, String lang, String country, String variant) {
        return this.mService.loadLanguage(caller, lang, country, variant);
    }

    public List<Voice> getVoices() {
        return this.mService.getVoices();
    }

    public int loadVoice(IBinder caller, String voiceName) {
        return this.mService.loadVoice(caller, voiceName);
    }

    public String getDefaultVoiceNameFor(String lang, String country, String variant) {
        return this.mService.getDefaultVoiceNameFor(lang, country, variant);
    }

    public void setCallback(IBinder caller, ITextToSpeechCallback cb) {
        this.mService.setCallback(caller, cb);
    }

    public int setSoloMode(IBinder caller, int channel, boolean on) {
        return this.mService.setSoloMode(caller, channel, on);
    }

    public void setListenTtsStateChanged(IBinder caller) {
        this.mService.setListenTtsStateChanged(caller);
    }
}

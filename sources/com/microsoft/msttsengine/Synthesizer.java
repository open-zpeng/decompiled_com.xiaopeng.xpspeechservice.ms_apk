package com.microsoft.msttsengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/* loaded from: classes.dex */
public class Synthesizer {
    TtsCallback callback;
    private long synthesizerHandle;

    /* loaded from: classes.dex */
    public static class ContentType {
        public static final int PLAIN_TEXT = 0;
        public static final int SSML_TEXT = 1;
    }

    private native void nativeCloseHandler(long j);

    private native long nativeCreateHandler();

    private native void nativeGetInstalledVoices(long j, ArrayList<VoiceInfo> arrayList);

    private native WaveFormatEx nativeGetOuputFormat(long j);

    private native int nativeInstallVoices(long j, String str);

    private native int nativeSetDefaultVoice(long j);

    private native int nativeSetOutput(long j, Synthesizer synthesizer);

    private native int nativeSetVoice(long j, VoiceInfo voiceInfo);

    private native int nativeSpeak(long j, String str, int i);

    private native int nativeStop(long j);

    static {
        System.loadLibrary("MSTTSEngine");
    }

    public void setReceiveWave(TtsCallback callback) {
        this.callback = callback;
    }

    private int receiveWave(long responseHandle, byte[] data, int size) {
        return this.callback.receiveWave(responseHandle, data, size);
    }

    public byte[] getWaveData() {
        return this.callback.getWaveData();
    }

    public void init() throws MsttsException {
        this.synthesizerHandle = nativeCreateHandler();
        long j = this.synthesizerHandle;
        if (j < 0) {
            throw new MsttsException(String.format("Initialization failed, error code:%d", Long.valueOf(j)), (int) this.synthesizerHandle);
        }
    }

    public void speak(String jstrContent, int eContentType) throws MsttsException {
        int errorCode = nativeSpeak(this.synthesizerHandle, jstrContent, eContentType);
        if (errorCode != 0) {
            throw new MsttsException(String.format("speak failed, error code: %d", Integer.valueOf(errorCode)), errorCode);
        }
    }

    public void setOutput() throws MsttsException {
        int errorCode = nativeSetOutput(this.synthesizerHandle, this);
        if (errorCode != 0) {
            throw new MsttsException(String.format("Set output failed, error code:%d", Integer.valueOf(errorCode)), errorCode);
        }
    }

    public void closeSynthesizer() {
        nativeCloseHandler(this.synthesizerHandle);
    }

    public void installVoices(String voicePath) throws MsttsException {
        int errorCode = nativeInstallVoices(this.synthesizerHandle, voicePath);
        if (errorCode < 0) {
            throw new MsttsException(String.format("Install voice failed, error code:%d", Integer.valueOf(errorCode)), errorCode);
        }
    }

    public ArrayList<VoiceInfo> getInstalledVoices() {
        ArrayList<VoiceInfo> list = new ArrayList<>();
        nativeGetInstalledVoices(this.synthesizerHandle, list);
        return list;
    }

    public void setVoice(VoiceInfo voice) throws MsttsException {
        int errorCode = nativeSetVoice(this.synthesizerHandle, voice);
        if (errorCode < 0) {
            throw new MsttsException(String.format("Set voice failed, error code:%d", Integer.valueOf(errorCode)), errorCode);
        }
    }

    public WaveFormatEx getOutputFormat() {
        Map<String, String> map = new HashMap<>();
        map.put("Action", "getOutputFormat");
        return nativeGetOuputFormat(this.synthesizerHandle);
    }

    public void stop() throws MsttsException {
        int errorCode = nativeStop(this.synthesizerHandle);
        if (errorCode < 0) {
            throw new MsttsException(String.format("stop failed, error code:%d", Integer.valueOf(errorCode)), errorCode);
        }
    }
}

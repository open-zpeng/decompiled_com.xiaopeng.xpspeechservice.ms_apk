package com.microsoft.msttsengine;
/* loaded from: classes.dex */
public interface TtsCallback {
    byte[] getWaveData();

    int receiveWave(long j, byte[] bArr, int i);
}

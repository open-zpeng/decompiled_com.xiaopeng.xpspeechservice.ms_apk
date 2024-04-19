package com.microsoft.cognitiveservices.speech.audio;
/* loaded from: classes.dex */
public class MicrophoneCoordinates {
    private int X;
    private int Y;
    private int Z;

    public MicrophoneCoordinates(int i, int i2, int i3) {
        this.X = i;
        this.Y = i2;
        this.Z = i3;
    }

    public MicrophoneCoordinates(MicrophoneCoordinates microphoneCoordinates) {
        this.X = microphoneCoordinates.X;
        this.Y = microphoneCoordinates.Y;
        this.Z = microphoneCoordinates.Z;
    }

    public int getX() {
        return this.X;
    }

    public int getY() {
        return this.Y;
    }

    public int getZ() {
        return this.Z;
    }
}

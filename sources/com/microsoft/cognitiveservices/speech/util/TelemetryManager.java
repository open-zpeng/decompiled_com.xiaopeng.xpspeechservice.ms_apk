package com.microsoft.cognitiveservices.speech.util;
/* loaded from: classes.dex */
public class TelemetryManager {
    private static volatile TelemetryManager singleton;
    private HttpClient client = new HttpClient();

    private TelemetryManager() {
    }

    public static TelemetryManager getSingleton() {
        if (singleton == null) {
            synchronized (TelemetryManager.class) {
                if (singleton == null) {
                    try {
                        System.loadLibrary("Microsoft.CognitiveServices.Speech.extension.telemetry");
                    } catch (Exception e) {
                        System.loadLibrary("Microsoft.CognitiveServices.Speech.extension.telemetry");
                    }
                    singleton = new TelemetryManager();
                }
            }
        }
        return singleton;
    }
}

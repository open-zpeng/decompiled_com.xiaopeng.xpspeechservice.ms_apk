package com.xiaopeng.xpspeechservice.ms.tts.mediaengine;

import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback;
import java.lang.ref.WeakReference;
/* loaded from: classes.dex */
public class XpFFmpeg {
    private static final String TAG = "XpFFmpeg";
    private IFFmpegDataCallback mDataCallback;
    private ITtsEngineCallback mInfoCallback;
    private long mNativeContext;

    private static native void native_init();

    private final native void native_setup(Object obj);

    public final native int native_decodeMp3File(String str);

    public final native int native_decodeOpusBuffer(byte[] bArr, int i);

    public final native void native_stop();

    static {
        System.loadLibrary(TAG);
        native_init();
    }

    public XpFFmpeg(IFFmpegDataCallback dataCallback, ITtsEngineCallback infoCallback) {
        this.mDataCallback = dataCallback;
        this.mInfoCallback = infoCallback;
        native_setup(new WeakReference(this));
    }

    private static void postDataFromNative(Object obj, byte[] buffer, int size) {
        XpFFmpeg xpFFmpeg = (XpFFmpeg) ((WeakReference) obj).get();
        if (xpFFmpeg != null) {
            xpFFmpeg.mDataCallback.dataCallback(buffer, size);
        }
    }

    private static void postInfoFromNative(Object obj, int type, long duration) {
        XpFFmpeg xpFFmpeg = (XpFFmpeg) ((WeakReference) obj).get();
        if (xpFFmpeg != null) {
            xpFFmpeg.mInfoCallback.onEvent(EventType.values()[type], Long.valueOf(duration));
        }
    }
}

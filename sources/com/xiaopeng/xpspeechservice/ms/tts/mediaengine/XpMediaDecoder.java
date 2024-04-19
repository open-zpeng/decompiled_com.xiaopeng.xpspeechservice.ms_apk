package com.xiaopeng.xpspeechservice.ms.tts.mediaengine;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import com.xiaopeng.xpspeechservice.ms.SpeechApp;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback;
import com.xiaopeng.xpspeechservice.utils.AudioBuffer;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Map;
/* loaded from: classes.dex */
public class XpMediaDecoder {
    private static final int MSG_END = 2;
    private static final int MSG_ERROR = 3;
    private static final int MSG_START = 0;
    private static final int MSG_STOP = 1;
    private String TAG;
    private ITtsEngineCallback mCallback;
    private DecodeHandler mDecodeHandler;
    private HandlerThread mDecodeThread;
    private volatile boolean mIsEnd;
    private boolean mIsFirstData;
    private volatile boolean mIsStopped;
    private MediaCodec mMediaCodec;
    private MediaExtractor mMediaExtractor;
    private MyCallback mMyCallback;
    private volatile AudioBuffer mOutputBuffer;
    private volatile SourceType mType;
    private XpFFmpeg mXpFFmpeg;

    /* loaded from: classes.dex */
    private class PlayItem {
        public Object obj;
        public SourceType type;

        public PlayItem(SourceType type, Object obj) {
            this.type = type;
            this.obj = obj;
        }
    }

    public XpMediaDecoder(String channelName, ITtsEngineCallback cb) {
        this(channelName, cb, null);
    }

    public XpMediaDecoder(String channelName, ITtsEngineCallback cb, Handler handler) {
        Looper looper;
        this.TAG = "XpMediaDecoder";
        this.mIsStopped = false;
        this.mIsEnd = true;
        this.mIsFirstData = false;
        this.mDecodeThread = null;
        this.TAG += "_" + channelName;
        LogUtils.v(this.TAG, "construct");
        if (handler != null) {
            looper = handler.getLooper();
        } else {
            this.mDecodeThread = new HandlerThread("MediaDecode_" + channelName);
            this.mDecodeThread.start();
            looper = this.mDecodeThread.getLooper();
        }
        this.mDecodeHandler = new DecodeHandler(looper);
        this.mCallback = cb;
        this.mMyCallback = new MyCallback();
        this.mXpFFmpeg = new XpFFmpeg(new MyDataCallback(), new MyInfoCallback());
    }

    public void start(SourceType type, Object obj) {
        String str = this.TAG;
        LogUtils.i(str, "start " + type.name());
        PlayItem item = new PlayItem(type, obj);
        DecodeHandler decodeHandler = this.mDecodeHandler;
        decodeHandler.sendMessage(decodeHandler.obtainMessage(0, item));
    }

    public void stop() {
        LogUtils.i(this.TAG, "stop");
        this.mIsStopped = true;
        this.mDecodeHandler.sendEmptyMessage(1);
    }

    public int getData(byte[] buffer) {
        return this.mOutputBuffer.read(buffer);
    }

    /* loaded from: classes.dex */
    private class MyCallback extends MediaCodec.Callback {
        private MyCallback() {
        }

        @Override // android.media.MediaCodec.Callback
        public void onInputBufferAvailable(MediaCodec mc, int inputBufferId) {
            try {
                if (XpMediaDecoder.this.mIsStopped) {
                    return;
                }
                ByteBuffer inputBuffer = mc.getInputBuffer(inputBufferId);
                int size = XpMediaDecoder.this.mMediaExtractor.readSampleData(inputBuffer, 0);
                if (size > 0) {
                    mc.queueInputBuffer(inputBufferId, 0, size, XpMediaDecoder.this.mMediaExtractor.getSampleTime(), 0);
                    XpMediaDecoder.this.mMediaExtractor.advance();
                } else {
                    mc.queueInputBuffer(inputBufferId, 0, 0, 0L, 4);
                }
            } catch (Exception e) {
                LogUtils.e(XpMediaDecoder.this.TAG, "onInputBufferAvailable fail", e);
                XpMediaDecoder.this.error();
            }
        }

        @Override // android.media.MediaCodec.Callback
        public void onOutputBufferAvailable(MediaCodec mc, int outputBufferId, MediaCodec.BufferInfo info) {
            try {
                try {
                } catch (Exception e) {
                    LogUtils.e(XpMediaDecoder.this.TAG, "onOutputBufferAvailable fail", e);
                    XpMediaDecoder.this.error();
                }
                if (XpMediaDecoder.this.mIsStopped) {
                    return;
                }
                if ((info.flags & 2) != 0) {
                    LogUtils.v(XpMediaDecoder.this.TAG, "decode BUFFER_FLAG_CODEC_CONFIG");
                    return;
                }
                if (info.size > 0) {
                    if (XpMediaDecoder.this.mIsFirstData) {
                        XpMediaDecoder.this.mIsFirstData = false;
                        LogUtils.v(XpMediaDecoder.this.TAG, "first decoded buffer output");
                        XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_DATA_READY);
                    }
                    ByteBuffer outputBuffer = mc.getOutputBuffer(outputBufferId);
                    byte[] byteArry = new byte[info.size];
                    outputBuffer.get(byteArry);
                    XpMediaDecoder.this.mOutputBuffer.write(byteArry);
                }
                if ((info.flags & 4) != 0) {
                    LogUtils.v(XpMediaDecoder.this.TAG, "last buffer");
                    XpMediaDecoder.this.mOutputBuffer.writeDone();
                    XpMediaDecoder.this.mDecodeHandler.sendEmptyMessage(2);
                    XpMediaDecoder.this.mIsEnd = true;
                }
            } finally {
                mc.releaseOutputBuffer(outputBufferId, false);
            }
        }

        @Override // android.media.MediaCodec.Callback
        public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) {
            Bundle bundle = new Bundle();
            bundle.putInt("sampleRate", format.getInteger("sample-rate", 16000));
            bundle.putInt("format", format.getInteger("pcm-encoding", 2));
            bundle.putInt("channelCount", format.getInteger("channel-count", 1));
            XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_BEGIN, bundle);
        }

        @Override // android.media.MediaCodec.Callback
        public void onError(MediaCodec codec, MediaCodec.CodecException e) {
            LogUtils.e(XpMediaDecoder.this.TAG, "codec error", e);
            XpMediaDecoder.this.error();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DecodeHandler extends Handler {
        public DecodeHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i != 1) {
                    if (i == 2) {
                        LogUtils.i(XpMediaDecoder.this.TAG, "handleMessage MSG_END");
                        XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_END);
                        XpMediaDecoder.this.clean();
                        return;
                    } else if (i == 3) {
                        LogUtils.i(XpMediaDecoder.this.TAG, "handleMessage MSG_ERROR");
                        XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_ERROR);
                        if (XpMediaDecoder.this.mType == SourceType.SOURCE_URI) {
                            XpMediaDecoder.this.clean();
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                }
                LogUtils.i(XpMediaDecoder.this.TAG, "handleMessage MSG_STOP");
                if (XpMediaDecoder.this.mIsEnd) {
                    LogUtils.d(XpMediaDecoder.this.TAG, "decode already end");
                    return;
                }
                if (XpMediaDecoder.this.mType == SourceType.SOURCE_URI) {
                    XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_END);
                    XpMediaDecoder.this.mOutputBuffer.writeDone();
                    try {
                        XpMediaDecoder.this.mMediaCodec.stop();
                    } catch (Exception e) {
                        LogUtils.v(XpMediaDecoder.this.TAG, "stop fail", e);
                    }
                    XpMediaDecoder.this.clean();
                } else {
                    XpMediaDecoder.this.mXpFFmpeg.native_stop();
                }
                XpMediaDecoder.this.mIsEnd = true;
                return;
            }
            LogUtils.v(XpMediaDecoder.this.TAG, "handleMessage MSG_START");
            try {
                PlayItem item = (PlayItem) msg.obj;
                SourceType type = item.type;
                XpMediaDecoder.this.mType = type;
                if (type == SourceType.SOURCE_URI) {
                    Uri uri = (Uri) item.obj;
                    String str = XpMediaDecoder.this.TAG;
                    LogUtils.v(str, "start uri " + uri);
                    XpMediaDecoder.this.mOutputBuffer = new AudioBuffer();
                    XpMediaDecoder.this.mMediaExtractor = new MediaExtractor();
                    XpMediaDecoder.this.mMediaExtractor.setDataSource(SpeechApp.getContext(), uri, (Map<String, String>) null);
                    XpMediaDecoder.this.mMediaExtractor.selectTrack(0);
                    MediaFormat format = XpMediaDecoder.this.mMediaExtractor.getTrackFormat(0);
                    long durationUs = format.getLong("durationUs", 0L);
                    if (durationUs != 0) {
                        Bundle info = new Bundle();
                        info.putInt("duration", (int) (durationUs / 1000));
                        XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_UPLOAD_INFO, info);
                    }
                    XpMediaDecoder.this.mMediaCodec = MediaCodec.createDecoderByType(format.getString("mime"));
                    XpMediaDecoder.this.mMediaCodec.setCallback(XpMediaDecoder.this.mMyCallback, XpMediaDecoder.this.mDecodeHandler);
                    XpMediaDecoder.this.mMediaCodec.configure(format, (Surface) null, (MediaCrypto) null, 0);
                    XpMediaDecoder.this.mMediaCodec.start();
                    XpMediaDecoder.this.mIsFirstData = true;
                    XpMediaDecoder.this.mIsStopped = false;
                } else if (type == SourceType.SOURCE_BUFFER) {
                    LogUtils.v(XpMediaDecoder.this.TAG, "start buffer");
                    byte[] buffer = (byte[]) item.obj;
                    XpMediaDecoder.this.mOutputBuffer = new AudioBuffer(buffer.length * 12);
                    Bundle bundle = new Bundle();
                    bundle.putInt("sampleRate", 48000);
                    bundle.putInt("format", 2);
                    bundle.putInt("channelCount", 1);
                    XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_BEGIN, bundle);
                    int ret = XpMediaDecoder.this.mXpFFmpeg.native_decodeOpusBuffer(buffer, buffer.length);
                    if (ret < 0) {
                        XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_ERROR);
                        XpMediaDecoder.this.mOutputBuffer.writeDone();
                    }
                } else if (type != SourceType.SOURCE_PATH) {
                    LogUtils.e(XpMediaDecoder.this.TAG, "unknown source type");
                    return;
                } else {
                    String path = (String) item.obj;
                    File file = new File(path);
                    XpMediaDecoder.this.mOutputBuffer = new AudioBuffer(((int) file.length()) * 8);
                    String str2 = XpMediaDecoder.this.TAG;
                    LogUtils.v(str2, "start path " + path);
                    Bundle bundle2 = new Bundle();
                    bundle2.putInt("sampleRate", 24000);
                    bundle2.putInt("format", 2);
                    bundle2.putInt("channelCount", 1);
                    XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_BEGIN, bundle2);
                    int ret2 = XpMediaDecoder.this.mXpFFmpeg.native_decodeMp3File(path);
                    if (ret2 < 0) {
                        XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_ERROR);
                        XpMediaDecoder.this.mOutputBuffer.writeDone();
                        if (file.exists()) {
                            String str3 = XpMediaDecoder.this.TAG;
                            LogUtils.e(str3, "delete cache " + path);
                            file.delete();
                        }
                    }
                }
                XpMediaDecoder.this.mIsEnd = false;
            } catch (Exception e2) {
                LogUtils.e(XpMediaDecoder.this.TAG, "start fail", e2);
                XpMediaDecoder.this.error();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void error() {
        this.mDecodeHandler.sendEmptyMessage(3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clean() {
        LogUtils.v(this.TAG, "clean");
        try {
            this.mMediaCodec.release();
            this.mMediaCodec = null;
            this.mMediaExtractor.release();
            this.mMediaExtractor = null;
        } catch (Exception e) {
            LogUtils.v(this.TAG, "release media decoder error", e);
        }
    }

    public void destroy() {
        LogUtils.v(this.TAG, "destroy");
        HandlerThread handlerThread = this.mDecodeThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
    }

    /* loaded from: classes.dex */
    private class MyDataCallback implements IFFmpegDataCallback {
        private volatile boolean mIsFirst = true;

        public MyDataCallback() {
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.mediaengine.IFFmpegDataCallback
        public void dataCallback(byte[] buffer, int size) {
            if (size <= 0) {
                XpMediaDecoder.this.mIsEnd = true;
                XpMediaDecoder.this.mOutputBuffer.writeDone();
                if (size == 0) {
                    LogUtils.v(XpMediaDecoder.this.TAG, "dataCallback end data");
                    XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_END);
                } else {
                    LogUtils.e(XpMediaDecoder.this.TAG, "dataCallback error");
                    XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_ERROR);
                }
                this.mIsFirst = true;
                return;
            }
            if (this.mIsFirst) {
                this.mIsFirst = false;
                LogUtils.v(XpMediaDecoder.this.TAG, "dataCallback first data");
                XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_DATA_READY);
            }
            XpMediaDecoder.this.mOutputBuffer.write(buffer, size);
        }
    }

    /* loaded from: classes.dex */
    private class MyInfoCallback implements ITtsEngineCallback {
        private MyInfoCallback() {
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback
        public void onEvent(EventType event) {
            onEvent(event, null);
        }

        @Override // com.xiaopeng.xpspeechservice.ms.tts.ITtsEngineCallback
        public void onEvent(EventType event, Object obj) {
            if (event == EventType.EVENT_INFO_DURATION) {
                int duration = (int) (((Long) obj).longValue() / 1000);
                String str = XpMediaDecoder.this.TAG;
                LogUtils.v(str, "onEvent info duration " + duration);
                Bundle info = new Bundle();
                info.putInt("duration", duration);
                XpMediaDecoder.this.mCallback.onEvent(EventType.EVENT_UPLOAD_INFO, info);
            }
        }
    }
}

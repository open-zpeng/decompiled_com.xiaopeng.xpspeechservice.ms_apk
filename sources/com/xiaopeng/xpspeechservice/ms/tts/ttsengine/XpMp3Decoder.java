package com.xiaopeng.xpspeechservice.ms.tts.ttsengine;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import com.xiaopeng.xpspeechservice.ms.tts.EventType;
import com.xiaopeng.xpspeechservice.utils.AudioBuffer;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import com.xiaopeng.xpspeechservice.utils.XpDumpData;
import java.nio.ByteBuffer;
/* loaded from: classes.dex */
public class XpMp3Decoder {
    private static final String MIME_MP3 = "audio/mpeg";
    private static final int MSG_DESTROY = 4;
    private static final int MSG_END = 2;
    private static final int MSG_ERROR = 3;
    private static final int MSG_START = 0;
    private static final int MSG_STOP = 1;
    private static final int mBufferSize = 144;
    private final Bundle mBundle;
    private IDataCallback mCallback;
    private DecodeHandler mDecodeHandler;
    private HandlerThread mDecodeThread;
    private MediaFormat mFormat;
    private boolean mIsEmptyBuffer;
    private MediaCodec mMediaCodec;
    private MyCallback mMyCallback;
    private volatile AudioBuffer mOutputBuffer;
    private int mSampleRate;
    private XpDumpData mXpDumpData;
    private String TAG = "XpMp3Decoder";
    private volatile boolean mIsStopped = false;
    private boolean mIsEnd = false;

    public XpMp3Decoder(String channelName, IDataCallback callback, int sampleRate) {
        this.mSampleRate = 24000;
        this.TAG += "_" + channelName;
        this.mCallback = callback;
        this.mSampleRate = sampleRate;
        this.mFormat = MediaFormat.createAudioFormat(MIME_MP3, this.mSampleRate, 1);
        LogUtils.v(this.TAG, "construct");
        this.mDecodeThread = new HandlerThread("Mp3Decoder");
        this.mDecodeThread.start();
        this.mDecodeHandler = new DecodeHandler(this.mDecodeThread.getLooper());
        this.mMyCallback = new MyCallback();
        try {
            this.mMediaCodec = MediaCodec.createDecoderByType(MIME_MP3);
            this.mMediaCodec.setCallback(this.mMyCallback, this.mDecodeHandler);
            this.mMediaCodec.configure(this.mFormat, (Surface) null, (MediaCrypto) null, 0);
        } catch (Exception e) {
            LogUtils.e(this.TAG, "create exception", e);
        }
        this.mXpDumpData = new XpDumpData(false);
        this.mBundle = new Bundle();
        this.mBundle.putInt("sampleRate", this.mSampleRate);
        this.mBundle.putInt("format", 2);
        this.mBundle.putInt("channelCount", 1);
    }

    public void start(int preCacheSize) {
        LogUtils.v(this.TAG, "start");
        Message msg = this.mDecodeHandler.obtainMessage(0, preCacheSize, 0);
        this.mDecodeHandler.sendMessage(msg);
    }

    public void stop() {
        LogUtils.v(this.TAG, "stop");
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
            byte[] dataBuffer;
            try {
                if (XpMp3Decoder.this.mIsStopped) {
                    return;
                }
                ByteBuffer inputBuffer = mc.getInputBuffer(inputBufferId);
                byte[] buffer = new byte[XpMp3Decoder.mBufferSize];
                int size = XpMp3Decoder.this.mCallback.getData(buffer);
                if (size > 0) {
                    if (size < XpMp3Decoder.mBufferSize) {
                        dataBuffer = new byte[size];
                        System.arraycopy(buffer, 0, dataBuffer, 0, size);
                    } else {
                        dataBuffer = buffer;
                    }
                    XpMp3Decoder.this.mXpDumpData.dumpData(dataBuffer);
                    inputBuffer.put(dataBuffer);
                    mc.queueInputBuffer(inputBufferId, 0, size, 0L, 0);
                    return;
                }
                mc.queueInputBuffer(inputBufferId, 0, 0, 0L, 4);
            } catch (Exception e) {
                LogUtils.e(XpMp3Decoder.this.TAG, "onInputBufferAvailable fail", e);
                XpMp3Decoder.this.error();
            }
        }

        @Override // android.media.MediaCodec.Callback
        public void onOutputBufferAvailable(MediaCodec mc, int outputBufferId, MediaCodec.BufferInfo info) {
            try {
                try {
                    try {
                        if (XpMp3Decoder.this.mIsStopped) {
                            try {
                                mc.releaseOutputBuffer(outputBufferId, false);
                            } catch (Exception e) {
                                LogUtils.e(XpMp3Decoder.this.TAG, "releaseOutputBuffer fail", e);
                            }
                        } else if ((info.flags & 2) != 0) {
                            LogUtils.v(XpMp3Decoder.this.TAG, "decode BUFFER_FLAG_CODEC_CONFIG");
                            try {
                                mc.releaseOutputBuffer(outputBufferId, false);
                            } catch (Exception e2) {
                                LogUtils.e(XpMp3Decoder.this.TAG, "releaseOutputBuffer fail", e2);
                            }
                        } else {
                            if (info.size > 0) {
                                ByteBuffer outputBuffer = mc.getOutputBuffer(outputBufferId);
                                byte[] byteArry = new byte[info.size];
                                outputBuffer.get(byteArry);
                                XpMp3Decoder.this.mOutputBuffer.write(byteArry);
                                if (XpMp3Decoder.this.mIsEmptyBuffer) {
                                    XpMp3Decoder.this.mIsEmptyBuffer = false;
                                    XpMp3Decoder.this.mCallback.onEvent(EventType.EVENT_DATA_READY, XpMp3Decoder.this.mBundle);
                                }
                            }
                            if ((info.flags & 4) != 0) {
                                LogUtils.v(XpMp3Decoder.this.TAG, "last buffer");
                                XpMp3Decoder.this.mXpDumpData.end();
                                XpMp3Decoder.this.mOutputBuffer.writeDone();
                                XpMp3Decoder.this.mDecodeHandler.sendEmptyMessage(2);
                                XpMp3Decoder.this.mIsEnd = true;
                            }
                            mc.releaseOutputBuffer(outputBufferId, false);
                        }
                    } catch (Exception e3) {
                        LogUtils.e(XpMp3Decoder.this.TAG, "releaseOutputBuffer fail", e3);
                    }
                } catch (Exception e4) {
                    LogUtils.e(XpMp3Decoder.this.TAG, "onOutputBufferAvailable fail", e4);
                    XpMp3Decoder.this.error();
                    mc.releaseOutputBuffer(outputBufferId, false);
                }
            } catch (Throwable th) {
                try {
                    mc.releaseOutputBuffer(outputBufferId, false);
                } catch (Exception e5) {
                    LogUtils.e(XpMp3Decoder.this.TAG, "releaseOutputBuffer fail", e5);
                }
                throw th;
            }
        }

        @Override // android.media.MediaCodec.Callback
        public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) {
        }

        @Override // android.media.MediaCodec.Callback
        public void onError(MediaCodec codec, MediaCodec.CodecException e) {
            LogUtils.e(XpMp3Decoder.this.TAG, "codec error", e);
            XpMp3Decoder.this.error();
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
            if (i == 0) {
                LogUtils.v(XpMp3Decoder.this.TAG, "handleMessage MSG_START");
                XpMp3Decoder.this.mOutputBuffer = new AudioBuffer();
                XpMp3Decoder.this.mOutputBuffer.setPreCacheSize(msg.arg1 * 48);
                XpMp3Decoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_BEGIN, XpMp3Decoder.this.mBundle);
                XpMp3Decoder.this.mXpDumpData.start();
                XpMp3Decoder.this.mIsEmptyBuffer = true;
                XpMp3Decoder.this.mIsStopped = false;
                XpMp3Decoder.this.mIsEnd = false;
                try {
                    if (XpMp3Decoder.this.mMediaCodec == null) {
                        XpMp3Decoder.this.mMediaCodec = MediaCodec.createDecoderByType(XpMp3Decoder.MIME_MP3);
                        XpMp3Decoder.this.mMediaCodec.setCallback(XpMp3Decoder.this.mMyCallback, XpMp3Decoder.this.mDecodeHandler);
                        XpMp3Decoder.this.mMediaCodec.configure(XpMp3Decoder.this.mFormat, (Surface) null, (MediaCrypto) null, 0);
                    }
                    XpMp3Decoder.this.mMediaCodec.start();
                } catch (Exception e) {
                    LogUtils.v(XpMp3Decoder.this.TAG, "mediacodec start fail", e);
                    XpMp3Decoder.this.error();
                }
            } else if (i == 1) {
                LogUtils.v(XpMp3Decoder.this.TAG, "handleMessage MSG_STOP");
                if (XpMp3Decoder.this.mIsEnd) {
                    LogUtils.d(XpMp3Decoder.this.TAG, "decode already end");
                    return;
                }
                XpMp3Decoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_END);
                XpMp3Decoder.this.mOutputBuffer.writeDone();
                try {
                    XpMp3Decoder.this.mMediaCodec.stop();
                    XpMp3Decoder.this.mMediaCodec.setCallback(XpMp3Decoder.this.mMyCallback, XpMp3Decoder.this.mDecodeHandler);
                    XpMp3Decoder.this.mMediaCodec.configure(XpMp3Decoder.this.mFormat, (Surface) null, (MediaCrypto) null, 0);
                } catch (Exception e2) {
                    LogUtils.v(XpMp3Decoder.this.TAG, "mediacodec prepare for next fail", e2);
                    XpMp3Decoder.this.error(false);
                }
            } else if (i == 2) {
                LogUtils.v(XpMp3Decoder.this.TAG, "handleMessage MSG_END");
                XpMp3Decoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_END);
                try {
                    XpMp3Decoder.this.mMediaCodec.flush();
                } catch (Exception e3) {
                    LogUtils.e(XpMp3Decoder.this.TAG, "mediacodec flush fail", e3);
                    XpMp3Decoder.this.error(false);
                }
            } else if (i != 3) {
                if (i == 4) {
                    LogUtils.v(XpMp3Decoder.this.TAG, "handleMessage MSG_DESTROY");
                    try {
                        XpMp3Decoder.this.mMediaCodec.release();
                    } catch (Exception e4) {
                        LogUtils.e(XpMp3Decoder.this.TAG, "media codec release error", e4);
                    }
                }
            } else {
                LogUtils.v(XpMp3Decoder.this.TAG, "handleMessage MSG_ERROR");
                if (XpMp3Decoder.this.mOutputBuffer != null) {
                    XpMp3Decoder.this.mOutputBuffer.writeDone();
                }
                boolean isNeedCallback = ((Boolean) msg.obj).booleanValue();
                if (isNeedCallback) {
                    XpMp3Decoder.this.mCallback.onEvent(EventType.EVENT_PROCESS_ERROR);
                }
                try {
                    XpMp3Decoder.this.mMediaCodec.reset();
                    XpMp3Decoder.this.mMediaCodec.setCallback(XpMp3Decoder.this.mMyCallback, XpMp3Decoder.this.mDecodeHandler);
                    XpMp3Decoder.this.mMediaCodec.configure(XpMp3Decoder.this.mFormat, (Surface) null, (MediaCrypto) null, 0);
                } catch (Exception e5) {
                    LogUtils.e(XpMp3Decoder.this.TAG, "recovery from error fail", e5);
                    XpMp3Decoder.this.mMediaCodec = null;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void error() {
        error(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void error(boolean isNeedCallback) {
        DecodeHandler decodeHandler = this.mDecodeHandler;
        decodeHandler.sendMessage(decodeHandler.obtainMessage(3, Boolean.valueOf(isNeedCallback)));
    }

    public void reset() {
        error(false);
    }

    public void destroy() {
        LogUtils.v(this.TAG, "destroy");
        this.mDecodeHandler.sendEmptyMessage(4);
        this.mDecodeThread.quitSafely();
    }
}

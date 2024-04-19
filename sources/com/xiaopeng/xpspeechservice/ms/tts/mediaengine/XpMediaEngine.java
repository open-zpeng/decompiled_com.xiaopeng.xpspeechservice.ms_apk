package com.xiaopeng.xpspeechservice.ms.tts.mediaengine;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.danikula.videocache.HttpProxyCacheServer;
import com.xiaopeng.xpspeechservice.ms.tts.IEngine;
import com.xiaopeng.xpspeechservice.ms.tts.IEngineCallback;
import com.xiaopeng.xpspeechservice.ms.tts.ttscache.XpTtsCache;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
/* loaded from: classes.dex */
public class XpMediaEngine implements IEngine {
    private static final int MSG_INIT = 101;
    private String TAG = "XpMediaEngine";
    private XpTtsCache mCacheMaker;
    private PromptLoader mPromptFile;
    private HttpProxyCacheServer mProxyCacheServer;
    private XpMediaController mXpMediaController;

    public XpMediaEngine(String channelName) {
        this.TAG += "_" + channelName;
        LogUtils.i(this.TAG, "construct");
        this.mProxyCacheServer = ProxyFactory.getProxy();
        this.mXpMediaController = new XpMediaController(channelName);
    }

    public void init() {
        LogUtils.i(this.TAG, "initEngine");
        this.mXpMediaController.init();
        this.mPromptFile = PromptLoader.getInstance();
        this.mCacheMaker = XpTtsCache.getInstance();
    }

    public void shutdown() {
        LogUtils.i(this.TAG, "shutdown");
        this.mXpMediaController.shutdown();
        this.mPromptFile = null;
        this.mCacheMaker = null;
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.IEngine
    public int speak(Bundle params, IEngineCallback cb) {
        String txt = params.getString("txt");
        try {
            if (isTxtUrl(txt)) {
                String str = this.TAG;
                LogUtils.v(str, "isCached " + this.mProxyCacheServer.isCached(txt));
                String url = this.mProxyCacheServer.getProxyUrl(txt);
                String str2 = this.TAG;
                LogUtils.v(str2, "getProxyUrl " + url);
                Uri uri = Uri.parse(url);
                this.mXpMediaController.start(SourceType.SOURCE_URI, params, uri, cb);
                return 0;
            }
            boolean isPromptEnable = SystemProperties.getBoolean("sys.xiaopeng.tts.prompt_enable", true);
            if (isPromptEnable) {
                byte[] buffer = this.mPromptFile.getBytes(txt);
                if (buffer != null) {
                    LogUtils.v(this.TAG, "prompt match");
                    this.mXpMediaController.start(SourceType.SOURCE_BUFFER, params, buffer, cb);
                    return 0;
                }
            } else {
                LogUtils.v(this.TAG, "prompt disabled");
            }
            boolean isCacheEnable = SystemProperties.getBoolean("sys.xiaopeng.tts.localcache_enable", true);
            if (isCacheEnable) {
                String cachePath = this.mCacheMaker.getTtsCachePath(txt);
                if (cachePath != null) {
                    LogUtils.v(this.TAG, "cache match");
                    this.mXpMediaController.start(SourceType.SOURCE_PATH, params, cachePath, cb);
                    return 0;
                }
                return -1;
            }
            LogUtils.v(this.TAG, "cache disabled");
            return -1;
        } catch (Exception e) {
            String str3 = this.TAG;
            LogUtils.e(str3, "speak fail " + e);
            return -1;
        }
    }

    @Override // com.xiaopeng.xpspeechservice.ms.tts.IEngine
    public void stop() {
        LogUtils.i(this.TAG, "stop");
        this.mXpMediaController.stop();
    }

    private boolean isTxtUrl(String text) {
        return !TextUtils.isEmpty(text) && (text.startsWith("http://") || text.startsWith("https://"));
    }
}

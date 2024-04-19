package com.xiaopeng.xpspeechservice.ms.tts.mediaengine;

import com.danikula.videocache.HttpProxyCacheServer;
import com.xiaopeng.xpspeechservice.ms.SpeechApp;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.io.File;
/* loaded from: classes.dex */
public class ProxyFactory {
    private static final long CACHE_MAX_SIZE = 134217728;
    private static final File CACHE_PATH = SpeechApp.getContext().getCacheDir();
    private static final String TAG = "ProxyFactory";

    private ProxyFactory() {
        LogUtils.d(TAG, "ProxyFactory construct");
    }

    /* loaded from: classes.dex */
    private static class SingleHolder {
        private static final HttpProxyCacheServer INSTANCE = new HttpProxyCacheServer.Builder(SpeechApp.getContext()).maxCacheSize(ProxyFactory.CACHE_MAX_SIZE).cacheDirectory(new File(ProxyFactory.CACHE_PATH, "audio-cache")).build();

        private SingleHolder() {
        }
    }

    public static HttpProxyCacheServer getProxy() {
        return SingleHolder.INSTANCE;
    }
}

package com.xiaopeng.xpspeechservice.utils;

import android.os.Bundle;
import android.util.Pools;
/* loaded from: classes.dex */
public class BundlePool {
    private static final Pools.SynchronizedPool<Bundle> sPool = new Pools.SynchronizedPool<>(10);

    public static Bundle obtain() {
        Bundle bundle = (Bundle) sPool.acquire();
        if (bundle != null) {
            bundle.clear();
            return bundle;
        }
        return new Bundle();
    }

    public static void release(Bundle bundle) {
        sPool.release(bundle);
    }
}

package com.xiaopeng.xpspeechservice.ms.tts.ttscache;

import android.content.ContentValues;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import com.lzy.okgo.model.Progress;
import com.xiaopeng.lib.utils.SharedPreferencesUtils;
import com.xiaopeng.xpspeechservice.ms.SpeechApp;
import com.xiaopeng.xpspeechservice.ms.tts.config.TtsCacheConfig;
import com.xiaopeng.xpspeechservice.utils.AudioBuffer;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;
/* loaded from: classes.dex */
public class XpTtsCache {
    private static final String APP_CACHE_PATH = SpeechApp.getContext().getCacheDir().getAbsolutePath() + "/ttsCache";
    private static final String MNT_CACHE_PATH = "/mnt/tts/cache";
    private static final String TAG = "XpTtsCache";
    private static final String TTS_PATH_MNT = "/mnt/tts";
    private CacheHandler mCacheHandler;
    private HandlerThread mCacheThread;
    private volatile long mExpiredTime;
    private final Lock mLock;
    private long mMaxCacheSize;
    private volatile long mTotalSize;
    private final String mTtsCachePath;
    private volatile long mValidPeriod;

    static /* synthetic */ long access$314(XpTtsCache x0, long x1) {
        long j = x0.mTotalSize + x1;
        x0.mTotalSize = j;
        return j;
    }

    static /* synthetic */ long access$322(XpTtsCache x0, long x1) {
        long j = x0.mTotalSize - x1;
        x0.mTotalSize = j;
        return j;
    }

    /* loaded from: classes.dex */
    private static class SingleHolder {
        private static XpTtsCache instance = new XpTtsCache();

        private SingleHolder() {
        }
    }

    public static XpTtsCache getInstance() {
        return SingleHolder.instance;
    }

    private XpTtsCache() {
        this.mTotalSize = 0L;
        this.mLock = new ReentrantLock(true);
        this.mMaxCacheSize = 734003200L;
        this.mValidPeriod = 3628800000L;
        this.mExpiredTime = 0L;
        this.mCacheThread = new HandlerThread("cacheThread");
        this.mCacheThread.start();
        this.mCacheHandler = new CacheHandler(this.mCacheThread.getLooper());
        this.mTtsCachePath = getTtsCachePath();
        SharedPreferencesUtils spu = SharedPreferencesUtils.getInstance(SpeechApp.getContext());
        boolean isV4Model = spu.getBoolean("isV4Model", false);
        if (!isV4Model) {
            LogUtils.i(TAG, "model update V3 to V4, delete cache");
            spu.putBoolean("isV4Model", true);
            LitePal.deleteAll(TtsCacheItem.class, new String[0]);
        }
        this.mTotalSize = ((Integer) LitePal.sum(TtsCacheItem.class, "size", Integer.TYPE)).intValue();
        if (this.mTotalSize == 0) {
            clearCacheFile();
        }
        EventBus.getDefault().register(this);
    }

    private String getTtsCachePath() {
        String path = APP_CACHE_PATH;
        String model = SystemProperties.get("ro.product.model", "");
        if ("D55".equals(model) && new File(TTS_PATH_MNT).exists()) {
            path = MNT_CACHE_PATH;
        }
        LogUtils.v(TAG, "tts cache path " + path);
        File cacheDir = new File(path);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        return path;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearCacheFile() {
        LogUtils.v(TAG, "clearCacheFile");
        try {
            File path = new File(this.mTtsCachePath);
            File[] files = path.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    f.delete();
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "clear cache file fail", e);
        }
    }

    public void makeTtsCache(String txt, AudioBuffer audioBuffer) {
        byte[] buffer = audioBuffer.getWholeBuffer();
        makeTtsCache(txt, buffer);
    }

    public void makeTtsCache(final String txt, final byte[] buffer) {
        boolean isCacheEnable = SystemProperties.getBoolean("sys.xiaopeng.tts.localcache_enable", true);
        if (!isCacheEnable) {
            return;
        }
        this.mCacheHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.XpTtsCache.1
            @Override // java.lang.Runnable
            public void run() {
                if (buffer.length != 0) {
                    long maxCacheSize = SystemProperties.getLong("sys.xiaopeng.tts.cache_size", XpTtsCache.this.mMaxCacheSize);
                    while (true) {
                        if (XpTtsCache.this.mTotalSize + buffer.length <= maxCacheSize) {
                            break;
                        }
                        List<TtsCacheItem> itemList = LitePal.select("id", Progress.FILE_NAME, "size").order("LastTime asc").limit(1).find(TtsCacheItem.class);
                        if (itemList.size() > 0) {
                            XpTtsCache.this.deleteItem(itemList.get(0));
                        } else {
                            LogUtils.e(XpTtsCache.TAG, "find oldest cache item fail");
                            break;
                        }
                    }
                    List<TtsCacheItem> itemList2 = LitePal.select("id", Progress.FILE_NAME, "size").where("text = ?", txt).limit(1).find(TtsCacheItem.class);
                    if (itemList2.size() > 0) {
                        TtsCacheItem item = itemList2.get(0);
                        String fileName = item.getFileName();
                        int size = item.getSize();
                        XpTtsCache.access$322(XpTtsCache.this, size);
                        LitePal.delete(TtsCacheItem.class, item.getId());
                        LogUtils.w(XpTtsCache.TAG, "cache already exists, drop old one " + fileName);
                        File file = new File(XpTtsCache.this.mTtsCachePath, fileName);
                        if (file.exists()) {
                            file.delete();
                        } else {
                            LogUtils.w(XpTtsCache.TAG, "file %s not exists", fileName);
                        }
                    }
                    String fileName2 = UUID.randomUUID().toString() + ".mp3";
                    int ret = XpTtsCache.this.writeBufferToFile(fileName2, buffer);
                    if (ret == 0) {
                        XpTtsCache.access$314(XpTtsCache.this, buffer.length);
                        TtsCacheItem item2 = new TtsCacheItem();
                        item2.setFileName(fileName2);
                        item2.setSize(buffer.length);
                        item2.setText(txt);
                        long currentTime = System.currentTimeMillis();
                        item2.setTime(currentTime);
                        item2.setLastTime(currentTime);
                        item2.save();
                        LogUtils.v(XpTtsCache.TAG, "add to db size %d txt %s", Integer.valueOf(buffer.length), txt);
                    }
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int writeBufferToFile(String fileName, byte[] buffer) {
        try {
            FileOutputStream fos = new FileOutputStream(this.mTtsCachePath + "/" + fileName);
            fos.write(buffer);
            fos.close();
            return 0;
        } catch (Exception e) {
            LogUtils.e(TAG, "write buffer to file fail", e);
            return -1;
        }
    }

    public String getTtsCachePath(String txt) {
        this.mLock.lock();
        try {
            try {
                List<TtsCacheItem> itemList = LitePal.select("id", Progress.FILE_NAME, "time", "size").where("text = ?", txt).limit(1).find(TtsCacheItem.class);
                if (itemList.size() > 0) {
                    LogUtils.v(TAG, "cache record found for %s", txt);
                    TtsCacheItem item = itemList.get(0);
                    if (isItemExpired(item)) {
                        return null;
                    }
                    ContentValues values = new ContentValues();
                    values.put("lastTime", Long.valueOf(System.currentTimeMillis()));
                    LitePal.update(TtsCacheItem.class, values, item.getId());
                    String path = this.mTtsCachePath + "/" + item.getFileName();
                    File cacheFile = new File(path);
                    if (cacheFile.exists()) {
                        return path;
                    }
                    LogUtils.w(TAG, "cache file not exists");
                    LitePal.delete(TtsCacheItem.class, item.getId());
                    this.mTotalSize -= item.getSize();
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "getTtsCachePath fail", e);
            }
            return null;
        } finally {
            this.mLock.unlock();
        }
    }

    public void deleteItemFromText(final String txt) {
        this.mCacheHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.XpTtsCache.2
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.v(XpTtsCache.TAG, "deleteItemFromText " + txt);
                if (txt == null) {
                    return;
                }
                List<TtsCacheItem> itemList = LitePal.select("id", Progress.FILE_NAME, "size").where("text = ?", txt).limit(1).find(TtsCacheItem.class);
                if (itemList.size() > 0) {
                    TtsCacheItem item = itemList.get(0);
                    XpTtsCache.this.deleteItem(item);
                    return;
                }
                LogUtils.w(XpTtsCache.TAG, "did Not find text to delete: " + txt);
            }
        });
    }

    public void deleteAllItems() {
        this.mCacheHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.XpTtsCache.3
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.v(XpTtsCache.TAG, "deleteAllItems");
                XpTtsCache.this.mLock.lock();
                try {
                    try {
                        LitePal.deleteAll(TtsCacheItem.class, new String[0]);
                    } catch (Exception e) {
                        LogUtils.e(XpTtsCache.TAG, "deleteAllItem fail", e);
                    }
                    XpTtsCache.this.clearCacheFile();
                } finally {
                    XpTtsCache.this.mLock.unlock();
                }
            }
        });
    }

    public void deleteItemBeforeTime(final long expiredTime) {
        this.mCacheHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.XpTtsCache.4
            @Override // java.lang.Runnable
            public void run() {
                LogUtils.v(XpTtsCache.TAG, "deleteItemBeforeTime");
                XpTtsCache.this.mLock.lock();
                try {
                    try {
                        List<TtsCacheItem> itemList = LitePal.select("id", Progress.FILE_NAME, "size").where("time < ?", String.valueOf(expiredTime)).find(TtsCacheItem.class);
                        for (TtsCacheItem item : itemList) {
                            XpTtsCache.this.deleteItem(item);
                        }
                    } catch (Exception e) {
                        LogUtils.e(XpTtsCache.TAG, "deleteAllItem fail", e);
                    }
                } finally {
                    XpTtsCache.this.mLock.unlock();
                }
            }
        });
    }

    private boolean isItemExpired(TtsCacheItem item) {
        if (this.mValidPeriod > 0) {
            long period = System.currentTimeMillis() - item.getTime();
            if (period > this.mValidPeriod) {
                LogUtils.v(TAG, "oops! this cache period %d exceed %d", Long.valueOf(period), Long.valueOf(this.mValidPeriod));
                deleteItem(item);
                return true;
            }
        }
        if (this.mExpiredTime <= 0 || item.getTime() >= this.mExpiredTime) {
            return false;
        }
        LogUtils.v(TAG, "oops! cache time %d expired %d", Long.valueOf(item.getTime()), Long.valueOf(this.mExpiredTime));
        deleteItem(item);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deleteItem(TtsCacheItem item) {
        String fileName = item.getFileName();
        int size = item.getSize();
        this.mTotalSize -= size;
        LitePal.delete(TtsCacheItem.class, item.getId());
        LogUtils.v(TAG, "delete " + fileName);
        File file = new File(this.mTtsCachePath, fileName);
        if (file.exists()) {
            file.delete();
        } else {
            LogUtils.w(TAG, "file %s not exists", fileName);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CacheHandler extends Handler {
        public CacheHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onConfigChange(final TtsCacheConfig config) {
        LogUtils.i(TAG, "onConfigChange " + config);
        this.mCacheHandler.post(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.ttscache.XpTtsCache.5
            @Override // java.lang.Runnable
            public void run() {
                XpTtsCache.this.mMaxCacheSize = config.maxCacheSize;
                XpTtsCache.this.mValidPeriod = config.validPeriod;
                XpTtsCache.this.mExpiredTime = config.expiredTime;
            }
        });
    }
}

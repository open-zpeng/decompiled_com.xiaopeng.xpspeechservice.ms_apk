package com.xiaopeng.xpspeechservice.ms.tts.mediaengine;

import android.os.SystemClock;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
/* loaded from: classes.dex */
public class PromptLoader {
    private static final String DYNAMIC_FILE = "/system/etc/speech/xp_tts_ms_dynamic.dat";
    private static final String STATIC_FILE = "/system/etc/speech/xp_tts_ms_static.dat";
    private static final String TAG = "PromptLoader";
    private List<PromptConfig> mPromptConfigs;

    /* loaded from: classes.dex */
    private static class SingleHolder {
        private static PromptLoader instance = new PromptLoader();

        private SingleHolder() {
        }
    }

    public static PromptLoader getInstance() {
        return SingleHolder.instance;
    }

    private PromptLoader() {
        this.mPromptConfigs = new CopyOnWriteArrayList();
        loadStaticFile();
        loadDynamicFile();
    }

    private void loadStaticFile() {
        loadData(STATIC_FILE);
    }

    private void loadDynamicFile() {
        ThreadUtils.postNormal(new Runnable() { // from class: com.xiaopeng.xpspeechservice.ms.tts.mediaengine.PromptLoader.1
            @Override // java.lang.Runnable
            public void run() {
                PromptLoader.this.loadData(PromptLoader.DYNAMIC_FILE);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loadData(String path) {
        try {
            InputStream in = new FileInputStream(path);
            long startTime = SystemClock.uptimeMillis();
            byte[] headLengthArr = new byte[4];
            in.read(headLengthArr, 0, 4);
            int headLength = ByteUtils.bytes2Int(headLengthArr);
            PromptConfig config = new PromptConfig();
            config.path = path;
            config.headLength = headLength;
            byte[] headConfigByteArr = new byte[headLength];
            in.read(headConfigByteArr);
            String json = new String(headConfigByteArr, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            HashMap<String, FileLocationBean> fileLocMap = (HashMap) gson.fromJson(json, new TypeToken<HashMap<String, FileLocationBean>>() { // from class: com.xiaopeng.xpspeechservice.ms.tts.mediaengine.PromptLoader.2
            }.getType());
            config.map = fileLocMap;
            this.mPromptConfigs.add(config);
            long duration = SystemClock.uptimeMillis() - startTime;
            LogUtils.i(TAG, "loadData size %d takes %dms", Integer.valueOf(fileLocMap.size()), Long.valueOf(duration));
            $closeResource(null, in);
        } catch (Exception e) {
            LogUtils.e(TAG, "loadData new fail", e);
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 == null) {
            x1.close();
            return;
        }
        try {
            x1.close();
        } catch (Throwable th) {
            x0.addSuppressed(th);
        }
    }

    public byte[] getBytes(String text) {
        if (this.mPromptConfigs.isEmpty()) {
            LogUtils.v(TAG, "prompt data not loaded");
            return null;
        }
        PromptConfig config = null;
        FileLocationBean fileLocation = null;
        Iterator<PromptConfig> it = this.mPromptConfigs.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PromptConfig c = it.next();
            if (c.map != null) {
                FileLocationBean fileLocation2 = c.map.get(text);
                fileLocation = fileLocation2;
                if (fileLocation != null) {
                    config = c;
                    break;
                }
            }
        }
        if (config == null) {
            return null;
        }
        long pos = config.headLength + 4 + fileLocation.pos;
        byte[] buffer = new byte[(int) fileLocation.length];
        try {
            RandomAccessFile file = new RandomAccessFile(config.path, "r");
            file.seek(pos);
            file.read(buffer);
            $closeResource(null, file);
            return buffer;
        } catch (Exception e) {
            LogUtils.e(TAG, "getBytes fail", e);
            return null;
        }
    }
}

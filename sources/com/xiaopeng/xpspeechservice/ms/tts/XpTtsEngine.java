package com.xiaopeng.xpspeechservice.ms.tts;

import android.os.Bundle;
import com.xiaopeng.speech.tts.XpTextToSpeechServiceBase;
import com.xiaopeng.xpspeechservice.ms.tts.mediaengine.XpMediaEngine;
import com.xiaopeng.xpspeechservice.ms.tts.ttsengine.XpHybridEngine;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.util.List;
/* loaded from: classes.dex */
public class XpTtsEngine {
    private static final String TAG = "XpTtsEngine";
    ComboEngine[] mEngineList;

    /* loaded from: classes.dex */
    private class ComboEngine {
        public XpHybridEngine hybridEninge;
        public int id;
        public XpMediaEngine mediaEngine;
        public IEngine currentEngine = null;
        public boolean isOn = false;

        public ComboEngine(int id, XpHybridEngine hybridEninge, XpMediaEngine mediaEngine) {
            this.id = id;
            this.hybridEninge = hybridEninge;
            this.mediaEngine = mediaEngine;
        }
    }

    /* loaded from: classes.dex */
    private static class SingleHolder {
        private static final XpTtsEngine INSTANCE = new XpTtsEngine();

        private SingleHolder() {
        }
    }

    public static XpTtsEngine getInstance() {
        return SingleHolder.INSTANCE;
    }

    private XpTtsEngine() {
        this.mEngineList = new ComboEngine[3];
        LogUtils.d(TAG, "XpTtsEngine v20220520");
    }

    public void init(List<Integer> list) {
        LogUtils.i(TAG, "init");
        for (Integer id : list) {
            ComboEngine engine = this.mEngineList[id.intValue()];
            if (engine != null) {
                if (id.intValue() != 2) {
                    engine.hybridEninge.init();
                    engine.mediaEngine.init();
                }
            } else {
                String channelName = XpTextToSpeechServiceBase.getChannelNameForId(id.intValue());
                LogUtils.d(TAG, "init engine for channel " + channelName);
                XpHybridEngine hybridEninge = new XpHybridEngine(channelName);
                XpMediaEngine mediaEngine = new XpMediaEngine(channelName);
                if (id.intValue() != 2) {
                    hybridEninge.init();
                    mediaEngine.init();
                }
                this.mEngineList[id.intValue()] = new ComboEngine(id.intValue(), hybridEninge, mediaEngine);
            }
        }
    }

    public void shutdown() {
        ComboEngine[] comboEngineArr;
        LogUtils.i(TAG, "shutdown");
        for (ComboEngine engine : this.mEngineList) {
            if (engine != null) {
                engine.hybridEninge.shutdown();
                engine.mediaEngine.shutdown();
                engine.isOn = false;
            }
        }
    }

    public void speak(Bundle params, IEngineCallback cb) {
        int id = params.getInt("channel", 0);
        String channelName = XpTextToSpeechServiceBase.getChannelNameForId(id);
        LogUtils.i(TAG, "speak on channel " + channelName);
        ComboEngine engine = this.mEngineList[id];
        if (engine == null) {
            LogUtils.e(TAG, "channel %s is null", channelName);
            cb.error();
        } else if (id == 2 && !engine.isOn) {
            LogUtils.w(TAG, "channel vice bt is shutdown");
            cb.error();
        } else if (engine.mediaEngine.speak(params, cb) == 0) {
            engine.currentEngine = engine.mediaEngine;
        } else {
            engine.hybridEninge.speak(params, cb);
            engine.currentEngine = engine.hybridEninge;
        }
    }

    public void stop(Bundle params) {
        int id = params.getInt("channel", 0);
        String channelName = XpTextToSpeechServiceBase.getChannelNameForId(id);
        LogUtils.i(TAG, "stop on channel " + channelName);
        ComboEngine engine = this.mEngineList[id];
        if (engine != null) {
            engine.currentEngine.stop();
        } else {
            LogUtils.e(TAG, "channel %s is null", channelName);
        }
    }

    public void setViceBtEnable(boolean on) {
        ComboEngine engine = this.mEngineList[2];
        if (engine != null) {
            if (on) {
                engine.hybridEninge.init();
                engine.mediaEngine.init();
                engine.isOn = true;
                return;
            }
            engine.hybridEninge.shutdown();
            engine.mediaEngine.shutdown();
            engine.isOn = false;
        }
    }
}

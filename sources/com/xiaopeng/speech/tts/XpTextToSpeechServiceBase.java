package com.xiaopeng.speech.tts;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadsetClientCall;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.speech.tts.ITextToSpeechCallback;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TtsEngines;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.util.Log;
import com.lzy.okgo.model.Progress;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.util.Const;
/* loaded from: classes.dex */
public abstract class XpTextToSpeechServiceBase extends Service {
    private static final String ACTION_ECALL_CHANGED = "com.xiaopeng.action.ACTION_ECALL_CHANGED";
    private static final String ACTION_VICE_BT_CONNECTION = "xiaopeng.bluetooth.a2dp.action.CONNECTION_STATE_CHANGED";
    private static final int BT_CONNECTED = 2;
    private static final int BT_DISCONNECTED = 0;
    private static final boolean DBG = true;
    private static final int DEFAULT_STREAM = 10;
    private static final int DRVIER_MODE = 1;
    private static final String EXTRA_ECALL = "com.xiaopeng.ecall.extra.CALL";
    private static final int PRIVATE_MODE = 2;
    public static final int QUEUE_DESTROY = 2;
    private static final int SHARE_MODE = 0;
    private static final String TAG = "XpTextToSpeechServiceBase";
    public static final int TTS_CHANNEL_BLUETOOTH = 2;
    public static final int TTS_CHANNEL_HEADREST = 1;
    public static final int TTS_CHANNEL_MAIN = 0;
    private static final String VICE_BT_SPEECH = "psn_tts_headset_out";
    private static final String VOL_DOWN_ON_NAVI = "decrease_volume_navigating";
    private static final String XP_DRIVER_MODE = "XpMainDriverMode";
    private static final String[] kViceBtModels = {"E38"};
    private AudioManager mAudioManager;
    private ITextToSpeechServiceWrapper mBinder;
    private CallbackHandler mCallbackHandler;
    private HandlerThread mCallbackThread;
    private CallbackMap mCallbacks;
    private Context mContext;
    private TtsEngines mEngineHelper;
    private Handler mHandler;
    private MainDriverMode mMainDriverMode;
    private String mPackageName;
    protected List<SynthChannel> mSynthChannelList;
    private HandlerThread mThread;
    private ViceBtSpeech mViceBtSpeech;
    private VolDownOnNavi mVolDownOnNavi;
    private final BroadcastReceiver mReceiver = new CallStateReceiver();
    private boolean mHasViceBt = false;
    private boolean mIsViceBtOn = false;
    private boolean mIsViceBtConnected = false;
    private boolean mIsViceBtSpeechOn = false;
    private volatile boolean mIsVolDownOnNavi = true;
    private volatile boolean mIsCallStateActive = false;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface UtteranceProgressDispatcher {
        void dispatchOnAudioAvailable(byte[] bArr);

        void dispatchOnBeginSynthesis(int i, int i2, int i3);

        void dispatchOnError(int i);

        void dispatchOnRangeStart(int i, int i2, int i3);

        void dispatchOnStart();

        void dispatchOnStop();

        void dispatchOnSuccess();

        void dispatchTtsInfo(Bundle bundle);
    }

    protected abstract String[] onGetLanguage();

    protected abstract int onIsLanguageAvailable(String str, String str2, String str3);

    protected abstract int onLoadLanguage(String str, String str2, String str3);

    protected abstract void onStop(Bundle bundle);

    protected abstract void onSynthesizeText(XpSynthesisRequest xpSynthesisRequest, XpSynthesisCallback xpSynthesisCallback);

    @Override // android.app.Service
    public void onCreate() {
        Log.d(TAG, "onCreate() v20221021");
        super.onCreate();
        this.mContext = this;
        this.mAudioManager = (AudioManager) getSystemService("audio");
        this.mCallbackThread = new HandlerThread("CallbackHandler");
        this.mCallbackThread.start();
        this.mCallbackHandler = new CallbackHandler(this.mCallbackThread.getLooper());
        createSynthChannels();
        this.mEngineHelper = new TtsEngines(this);
        this.mCallbacks = new CallbackMap();
        this.mBinder = new ITextToSpeechServiceWrapper(this);
        this.mPackageName = getApplicationInfo().packageName;
        String[] defaultLocale = getSettingsLocale();
        onLoadLanguage(defaultLocale[0], defaultLocale[1], defaultLocale[2]);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void afterOnCreate() {
        this.mThread = new HandlerThread("ProcessHandler");
        this.mThread.start();
        this.mHandler = new Handler(this.mThread.getLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED");
        intentFilter.addAction("android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction(ACTION_ECALL_CHANGED);
        if (this.mHasViceBt) {
            intentFilter.addAction(ACTION_VICE_BT_CONNECTION);
        }
        registerReceiver(this.mReceiver, intentFilter);
        ContentResolver contentResolver = getContentResolver();
        this.mMainDriverMode = new MainDriverMode(this.mHandler);
        contentResolver.registerContentObserver(Settings.System.getUriFor(XP_DRIVER_MODE), true, this.mMainDriverMode);
        this.mVolDownOnNavi = new VolDownOnNavi(this.mHandler);
        this.mIsVolDownOnNavi = Settings.System.getInt(contentResolver, VOL_DOWN_ON_NAVI, 1) == 1;
        contentResolver.registerContentObserver(Settings.System.getUriFor(VOL_DOWN_ON_NAVI), true, this.mVolDownOnNavi);
        if (this.mHasViceBt) {
            this.mViceBtSpeech = new ViceBtSpeech(this.mHandler);
            contentResolver.registerContentObserver(Settings.System.getUriFor(VICE_BT_SPEECH), true, this.mViceBtSpeech);
            viceBtInitCheck();
        }
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        for (SynthChannel synthChannel : this.mSynthChannelList) {
            synthChannel.quit();
        }
        this.mCallbacks.kill();
        this.mBinder.close();
        unregisterReceiver(this.mReceiver);
        ContentResolver contentResolver = getContentResolver();
        contentResolver.unregisterContentObserver(this.mMainDriverMode);
        contentResolver.unregisterContentObserver(this.mVolDownOnNavi);
        if (this.mHasViceBt) {
            contentResolver.unregisterContentObserver(this.mViceBtSpeech);
        }
        this.mHandler.removeCallbacksAndMessages(null);
        this.mThread.quit();
        super.onDestroy();
    }

    private void createSynthChannels() {
        this.mSynthChannelList = new ArrayList();
        this.mSynthChannelList.add(new SynthChannel(0));
        boolean hasHeadrest = false;
        String model = SystemProperties.get("ro.product.model", "");
        String[] strArr = kViceBtModels;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String viceBtModel = strArr[i];
            if (!model.equals(viceBtModel)) {
                i++;
            } else {
                this.mHasViceBt = true;
                break;
            }
        }
        int hasAMP = SystemProperties.getInt("persist.sys.xiaopeng.AMP", 0);
        if (hasAMP == 1) {
            hasHeadrest = true;
        }
        boolean isForceAllChannels = SystemProperties.getBoolean("persist.sys.xiaopeng.tts.forceAllChannels", false);
        if (isForceAllChannels) {
            this.mHasViceBt = true;
            hasHeadrest = true;
        }
        if (hasHeadrest) {
            this.mSynthChannelList.add(new SynthChannel(1));
        }
        if (this.mHasViceBt) {
            this.mSynthChannelList.add(new SynthChannel(2));
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public List<Integer> getChannelIdList() {
        List<Integer> list = new ArrayList<>();
        for (SynthChannel channel : this.mSynthChannelList) {
            list.add(new Integer(channel.getId()));
        }
        return list;
    }

    public static String getChannelNameForId(int id) {
        if (id == 0) {
            return "main";
        }
        if (id == 1) {
            return "headrest";
        }
        if (id == 2) {
            return "bluetooth";
        }
        return BuildInfoUtils.UNKNOWN;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SynthChannel getSynthChannelForId(int id) {
        for (SynthChannel synthChannel : this.mSynthChannelList) {
            if (synthChannel.getId() == id) {
                return synthChannel;
            }
        }
        return null;
    }

    private int getChannelIdForStreamType(String sourceName, int streamType) {
        int mainDriverMode = this.mAudioManager.getMainDriverMode();
        int channel = 0;
        if (streamType == 10) {
            if (mainDriverMode == 2) {
                channel = 1;
            } else {
                channel = 0;
            }
        } else if (streamType == 14) {
            channel = 1;
        } else if (streamType == 13) {
            channel = 2;
        } else if (streamType == 9) {
            if (mainDriverMode == 0) {
                channel = 0;
            } else {
                channel = 1;
            }
        }
        if (channel == 1) {
            boolean isConcurrent = SystemProperties.getBoolean("sys.xiaopeng.tts.concurrency", false);
            if (!isConcurrent) {
                return 0;
            }
            return channel;
        }
        return channel;
    }

    protected Set<String> onGetFeaturesForLanguage(String lang, String country, String variant) {
        return new HashSet();
    }

    private int getExpectedLanguageAvailableStatus(Locale locale) {
        if (!locale.getVariant().isEmpty()) {
            return 2;
        }
        if (locale.getCountry().isEmpty()) {
            return 0;
        }
        return 1;
    }

    public List<Voice> onGetVoices() {
        XpTextToSpeechServiceBase xpTextToSpeechServiceBase = this;
        ArrayList<Voice> voices = new ArrayList<>();
        Locale[] availableLocales = Locale.getAvailableLocales();
        int length = availableLocales.length;
        int i = 0;
        while (i < length) {
            Locale locale = availableLocales[i];
            int expectedStatus = xpTextToSpeechServiceBase.getExpectedLanguageAvailableStatus(locale);
            try {
                int localeStatus = xpTextToSpeechServiceBase.onIsLanguageAvailable(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant());
                if (localeStatus == expectedStatus) {
                    Set<String> features = xpTextToSpeechServiceBase.onGetFeaturesForLanguage(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant());
                    String voiceName = xpTextToSpeechServiceBase.onGetDefaultVoiceNameFor(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant());
                    voices.add(new Voice(voiceName, locale, 300, 300, false, features));
                }
            } catch (MissingResourceException e) {
            }
            i++;
            xpTextToSpeechServiceBase = this;
        }
        return voices;
    }

    public String onGetDefaultVoiceNameFor(String lang, String country, String variant) {
        Locale iso3Locale;
        int localeStatus = onIsLanguageAvailable(lang, country, variant);
        if (localeStatus == 0) {
            iso3Locale = new Locale(lang);
        } else if (localeStatus == 1) {
            iso3Locale = new Locale(lang, country);
        } else if (localeStatus != 2) {
            return null;
        } else {
            iso3Locale = new Locale(lang, country, variant);
        }
        Locale properLocale = TtsEngines.normalizeTTSLocale(iso3Locale);
        String voiceName = properLocale.toLanguageTag();
        if (onIsValidVoiceName(voiceName) != 0) {
            return null;
        }
        return voiceName;
    }

    public int onLoadVoice(String voiceName) {
        Locale locale = Locale.forLanguageTag(voiceName);
        if (locale == null) {
            return -1;
        }
        int expectedStatus = getExpectedLanguageAvailableStatus(locale);
        try {
            int localeStatus = onIsLanguageAvailable(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant());
            if (localeStatus != expectedStatus) {
                return -1;
            }
            onLoadLanguage(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant());
            return 0;
        } catch (MissingResourceException e) {
            return -1;
        }
    }

    public int onIsValidVoiceName(String voiceName) {
        Locale locale = Locale.forLanguageTag(voiceName);
        if (locale == null) {
            return -1;
        }
        int expectedStatus = getExpectedLanguageAvailableStatus(locale);
        try {
            int localeStatus = onIsLanguageAvailable(locale.getISO3Language(), locale.getISO3Country(), locale.getVariant());
            if (localeStatus != expectedStatus) {
                return -1;
            }
            return 0;
        } catch (MissingResourceException e) {
            return -1;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getDefaultSpeechRate() {
        return getSecureSettingInt("tts_default_rate", 100);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getDefaultPitch() {
        return getSecureSettingInt("tts_default_pitch", 100);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String[] getSettingsLocale() {
        Locale locale = this.mEngineHelper.getLocalePrefForEngine(this.mPackageName);
        return TtsEngines.toOldLocaleStringFormat(locale);
    }

    private int getSecureSettingInt(String name, int defaultValue) {
        return Settings.Secure.getInt(getContentResolver(), name, defaultValue);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SynthChannel {
        private AudioFocusHelper mAudioFocusHelper;
        private XpAudioPlaybackHandler mAudioPlaybackHandler;
        private final int mId;
        private Handler mRequestHandler;
        private HandlerThread mRequestThread;
        private LinkedList<SpeechItem> mSpeechItemLink;
        private SynthThread mSynthThread;
        private final String mTagChannel;
        private final Lock mLinkLock = new ReentrantLock();
        private final Condition mLinkCondition = this.mLinkLock.newCondition();
        private volatile SpeechItem mCurrentSpeechItem = null;
        private volatile Object mSoloCaller = null;
        private volatile boolean mIsViceBtChannelOn = false;

        public SynthChannel(int id) {
            this.mId = id;
            String name = XpTextToSpeechServiceBase.getChannelNameForId(id);
            this.mRequestThread = new HandlerThread("SynthHandler_" + name);
            this.mRequestThread.start();
            this.mRequestHandler = new Handler(this.mRequestThread.getLooper());
            this.mTagChannel = "XpTextToSpeechServiceBase_" + name;
            XpTextToSpeechServiceBase.this.mCallbackHandler.registerChannelId(id);
            this.mSpeechItemLink = new LinkedList<>();
            this.mSynthThread = new SynthThread(id);
            this.mSynthThread.start();
            this.mAudioPlaybackHandler = new XpAudioPlaybackHandler(name);
            this.mAudioPlaybackHandler.start();
            this.mAudioFocusHelper = new AudioFocusHelper(XpTextToSpeechServiceBase.this.mContext);
        }

        protected int getId() {
            return this.mId;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public XpAudioPlaybackHandler getAudioPlaybackHandler() {
            return this.mAudioPlaybackHandler;
        }

        protected AudioFocusHelper getAudioFocusHelper() {
            return this.mAudioFocusHelper;
        }

        public boolean isSpeaking() {
            return (this.mCurrentSpeechItem == null && this.mSpeechItemLink.size() == 0) ? false : true;
        }

        public void quit() {
            this.mRequestHandler.removeCallbacksAndMessages(null);
            this.mRequestThread.quit();
            this.mSynthThread.quit();
            this.mAudioPlaybackHandler.quit();
            SpeechItem current = null;
            this.mLinkLock.lock();
            if (this.mCurrentSpeechItem != null) {
                current = this.mCurrentSpeechItem;
                this.mCurrentSpeechItem = null;
            }
            this.mLinkLock.unlock();
            if (current != null) {
                Log.i(this.mTagChannel, "stop current item due to quit");
                current.stop();
            }
        }

        public void enqueueSpeechItem(final int queueMode, final SpeechItem speechItem) {
            UtteranceProgressDispatcher utterenceProgress = null;
            if (speechItem instanceof UtteranceProgressDispatcher) {
                utterenceProgress = (UtteranceProgressDispatcher) speechItem;
            }
            if (!speechItem.isValid()) {
                if (utterenceProgress != null) {
                    utterenceProgress.dispatchOnError(-8);
                }
                Log.e(this.mTagChannel, "item is not valid");
                return;
            }
            this.mRequestHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.SynthChannel.1
                @Override // java.lang.Runnable
                public void run() {
                    SynthChannel.this.onEnqueueSpeechItem(queueMode, speechItem);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onEnqueueSpeechItem(int queueMode, SpeechItem speechItem) {
            if (queueMode == 0) {
                onStop(speechItem.getCallerIdentity(), null);
            } else if (queueMode == 2) {
                onStopAll();
            }
            int priority = speechItem.getPriority();
            SpeechItem current = null;
            this.mLinkLock.lock();
            try {
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (this.mSoloCaller != null && speechItem.getCallerIdentity() != this.mSoloCaller && priority != 4) {
                    Log.w(this.mTagChannel, "item rejected by solo mode");
                    if (speechItem instanceof UtteranceProgressDispatcher) {
                        UtteranceProgressDispatcher utterenceProgress = (UtteranceProgressDispatcher) speechItem;
                        utterenceProgress.dispatchOnError(-10);
                    }
                    return;
                }
                if (this.mSpeechItemLink.size() == 0) {
                    Log.d(this.mTagChannel, "add the item to empty queue");
                    this.mSpeechItemLink.add(speechItem);
                    this.mLinkCondition.signal();
                } else if (priority == 1) {
                    Log.d(this.mTagChannel, "add the item to the end of the queue");
                    this.mSpeechItemLink.add(speechItem);
                } else if (priority == 2 || priority == 3 || priority == 4) {
                    ListIterator<SpeechItem> iter = this.mSpeechItemLink.listIterator();
                    while (true) {
                        if (!iter.hasNext()) {
                            break;
                        }
                        SpeechItem item = iter.next();
                        if (item.getPriority() < priority) {
                            Log.d(this.mTagChannel, "add the item to the middle of the queue");
                            iter.previous();
                            break;
                        }
                    }
                    if (!iter.hasNext()) {
                        Log.d(this.mTagChannel, "add the item to the end of the queue");
                    }
                    iter.add(speechItem);
                }
                if (priority >= 3 && this.mCurrentSpeechItem != null && priority > this.mCurrentSpeechItem.getPriority()) {
                    current = this.mCurrentSpeechItem;
                    this.mCurrentSpeechItem = null;
                }
                if (current != null) {
                    try {
                        Log.d(this.mTagChannel, "stop current item due to urgent add");
                        current.stop();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            } finally {
                this.mLinkLock.unlock();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void stop(final Object callerIdentity, final String utteranceId) {
            this.mRequestHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.SynthChannel.2
                @Override // java.lang.Runnable
                public void run() {
                    SynthChannel.this.onStop(callerIdentity, utteranceId);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onStop(Object callerIdentity, String utteranceId) {
            if (callerIdentity == null) {
                return;
            }
            String str = this.mTagChannel;
            Log.d(str, "handleStop " + utteranceId);
            SpeechItem current = null;
            this.mLinkLock.lock();
            try {
                try {
                    ListIterator<SpeechItem> iter = this.mSpeechItemLink.listIterator();
                    while (iter.hasNext()) {
                        SpeechItem item = iter.next();
                        if (item.getCallerIdentity() == callerIdentity) {
                            if (utteranceId == null) {
                                if (item instanceof UtteranceProgressDispatcher) {
                                    UtteranceProgressDispatcher dispatcher = (UtteranceProgressDispatcher) item;
                                    dispatcher.dispatchOnStop();
                                }
                                iter.remove();
                            } else if (item instanceof UtteranceSpeechItem) {
                                UtteranceSpeechItem utterenceItem = (UtteranceSpeechItem) item;
                                if (utterenceItem.getUtteranceId().equals(utteranceId)) {
                                    utterenceItem.dispatchOnStop();
                                    iter.remove();
                                }
                            }
                        }
                    }
                    if (this.mCurrentSpeechItem != null && this.mCurrentSpeechItem.getCallerIdentity() == callerIdentity) {
                        if (utteranceId == null) {
                            current = this.mCurrentSpeechItem;
                            this.mCurrentSpeechItem = null;
                        } else if ((this.mCurrentSpeechItem instanceof UtteranceSpeechItem) && ((UtteranceSpeechItem) this.mCurrentSpeechItem).getUtteranceId().equals(utteranceId)) {
                            current = this.mCurrentSpeechItem;
                            this.mCurrentSpeechItem = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (current != null) {
                    Log.d(this.mTagChannel, "stop current item due to app stop");
                    current.stop();
                }
            } finally {
                this.mLinkLock.unlock();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void stopAll() {
            this.mRequestHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.SynthChannel.3
                @Override // java.lang.Runnable
                public void run() {
                    SynthChannel.this.onStopAll();
                }
            });
        }

        public void onStopAll() {
            SpeechItem current = null;
            this.mLinkLock.lock();
            try {
                try {
                    ListIterator<SpeechItem> iter = this.mSpeechItemLink.listIterator();
                    while (iter.hasNext()) {
                        SpeechItem item = iter.next();
                        if (item.getPriority() < 4) {
                            if (item instanceof UtteranceProgressDispatcher) {
                                UtteranceProgressDispatcher dispatcher = (UtteranceProgressDispatcher) item;
                                dispatcher.dispatchOnStop();
                            }
                            iter.remove();
                        }
                    }
                    if (this.mCurrentSpeechItem != null && 4 > this.mCurrentSpeechItem.getPriority()) {
                        current = this.mCurrentSpeechItem;
                        this.mCurrentSpeechItem = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (current != null) {
                    Log.d(this.mTagChannel, "stop current item due to destory");
                    current.stop();
                }
            } finally {
                this.mLinkLock.unlock();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void stopStreamType(final int streamType) {
            this.mRequestHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.SynthChannel.4
                @Override // java.lang.Runnable
                public void run() {
                    SynthChannel.this.onStopStreamType(streamType);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onStopStreamType(int streamType) {
            String str = this.mTagChannel;
            Log.i(str, "onStopStreamType " + streamType);
            SpeechItem current = null;
            this.mLinkLock.lock();
            try {
                try {
                    ListIterator<SpeechItem> iter = this.mSpeechItemLink.listIterator();
                    while (iter.hasNext()) {
                        SpeechItem item = iter.next();
                        if (item instanceof SynthesisSpeechItem) {
                            SynthesisSpeechItem synthItem = (SynthesisSpeechItem) item;
                            int type = synthItem.getParams().getInt("streamType", 10);
                            if (type == streamType) {
                                synthItem.dispatchOnStop();
                                iter.remove();
                            }
                        }
                    }
                    if (this.mCurrentSpeechItem != null && (this.mCurrentSpeechItem instanceof SynthesisSpeechItem)) {
                        int type2 = ((SynthesisSpeechItem) this.mCurrentSpeechItem).getParams().getInt("streamType", 10);
                        if (type2 == streamType) {
                            current = this.mCurrentSpeechItem;
                            this.mCurrentSpeechItem = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (current != null) {
                    Log.i(this.mTagChannel, "onStopStreamType stop current item");
                    current.stop();
                }
            } finally {
                this.mLinkLock.unlock();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public int setSoloMode(Object caller, boolean on) {
            int ret = -1;
            SpeechItem current = null;
            this.mLinkLock.lock();
            try {
                try {
                    if (this.mSoloCaller == null) {
                        if (on) {
                            String str = this.mTagChannel;
                            Log.v(str, caller + " set solomode true");
                            this.mSoloCaller = caller;
                            ListIterator<SpeechItem> iter = this.mSpeechItemLink.listIterator();
                            while (iter.hasNext()) {
                                SpeechItem item = iter.next();
                                if (item.getPriority() != 4 && item.getCallerIdentity() != caller) {
                                    if (item instanceof UtteranceProgressDispatcher) {
                                        UtteranceProgressDispatcher dispatcher = (UtteranceProgressDispatcher) item;
                                        dispatcher.dispatchOnStop();
                                    }
                                    iter.remove();
                                }
                            }
                            if (this.mCurrentSpeechItem != null && this.mCurrentSpeechItem.getPriority() != 4 && this.mCurrentSpeechItem.getCallerIdentity() != this.mSoloCaller) {
                                current = this.mCurrentSpeechItem;
                                this.mCurrentSpeechItem = null;
                            }
                            ret = 0;
                        }
                    } else if (caller == this.mSoloCaller) {
                        if (!on) {
                            String str2 = this.mTagChannel;
                            Log.v(str2, this.mSoloCaller + " set solomode false");
                            this.mSoloCaller = null;
                        }
                        ret = 0;
                    } else {
                        String str3 = this.mTagChannel;
                        Log.e(str3, "solo caller not same, new " + caller + " old " + this.mSoloCaller);
                    }
                } finally {
                    this.mLinkLock.unlock();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (current != null) {
                try {
                    Log.i(this.mTagChannel, "stop current item due to solo");
                    current.stop();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return ret;
        }

        private int predictTimeFromText(String text) {
            int timePerWordMs = SystemProperties.getInt("sys.xiaopeng.tts.timePerWord", 250);
            return text.length() * timePerWordMs;
        }

        /* JADX WARN: Removed duplicated region for block: B:43:0x0158  */
        /* JADX WARN: Removed duplicated region for block: B:44:0x015b  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public com.xiaopeng.speech.tts.XpTtsStateInfoBean getTtsStateInfo(java.lang.String r50, int r51, android.os.Bundle r52) {
            /*
                Method dump skipped, instructions count: 416
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.SynthChannel.getTtsStateInfo(java.lang.String, int, android.os.Bundle):com.xiaopeng.speech.tts.XpTtsStateInfoBean");
        }

        public void handleViceBtChange(final boolean isOn) {
            this.mRequestHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.SynthChannel.5
                @Override // java.lang.Runnable
                public void run() {
                    SynthChannel.this.mIsViceBtChannelOn = isOn;
                    SynthChannel.this.mLinkLock.lock();
                    try {
                        SynthChannel.this.mLinkCondition.signal();
                    } finally {
                        SynthChannel.this.mLinkLock.unlock();
                    }
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.dex */
        public class SynthThread extends Thread {
            private boolean mFirstIdle = true;
            private int mId;
            private volatile boolean mIsWorking;

            public SynthThread(int id) {
                this.mId = id;
                setName("SynthThread_" + XpTextToSpeechServiceBase.getChannelNameForId(id));
                this.mIsWorking = true;
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                boolean isViceBtChannelOn = false;
                while (this.mIsWorking) {
                    SpeechItem item = null;
                    SynthChannel.this.mLinkLock.lock();
                    try {
                        try {
                            if (this.mId == 2 && SynthChannel.this.mIsViceBtChannelOn != isViceBtChannelOn) {
                                isViceBtChannelOn = SynthChannel.this.mIsViceBtChannelOn;
                                XpTextToSpeechServiceBase.this.setViceBtEnable(isViceBtChannelOn);
                            }
                            SynthChannel.this.mCurrentSpeechItem = null;
                            if (SynthChannel.this.mSpeechItemLink.size() == 0) {
                                Log.d(SynthChannel.this.mTagChannel, "List empty, wait for new item");
                                if (!this.mFirstIdle) {
                                    SynthChannel.this.mAudioFocusHelper.abandonAudioFocus();
                                } else {
                                    this.mFirstIdle = false;
                                }
                                SynthChannel.this.mLinkCondition.await();
                            }
                            item = (SpeechItem) SynthChannel.this.mSpeechItemLink.pollFirst();
                        } catch (Exception e) {
                            Log.e(SynthChannel.this.mTagChannel, "get item fail", e);
                        }
                        if (item != null) {
                            String str = SynthChannel.this.mTagChannel;
                            Log.d(str, "got a new item priority " + item.getPriority());
                            SynthChannel.this.mCurrentSpeechItem = item;
                            SynthChannel.this.mLinkLock.unlock();
                            if (item != null) {
                                Log.d(SynthChannel.this.mTagChannel, "play item start");
                                try {
                                    item.play();
                                } catch (Exception e2) {
                                    Log.e(SynthChannel.this.mTagChannel, "something wrong when playing");
                                }
                                Log.d(SynthChannel.this.mTagChannel, "play item end");
                            }
                        }
                    } finally {
                        SynthChannel.this.mLinkLock.unlock();
                    }
                }
                SynthChannel.this.mAudioFocusHelper.abandonAudioFocus();
                XpTextToSpeechServiceBase.this.mCallbackHandler.unRegisterChannelId(this.mId);
            }

            public void quit() {
                this.mIsWorking = false;
                SynthChannel.this.mLinkLock.lock();
                try {
                    SynthChannel.this.mLinkCondition.signal();
                } finally {
                    SynthChannel.this.mLinkLock.unlock();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class AudioOutputParams {
        public final AudioAttributes mAudioAttributes;
        public final float mPan;
        public final int mPitch;
        public final int mRate;
        public final int mSessionId;
        public final float mVolume;

        AudioOutputParams() {
            this.mSessionId = 0;
            this.mVolume = 1.0f;
            this.mPan = 0.0f;
            this.mRate = 100;
            this.mPitch = 100;
            this.mAudioAttributes = null;
        }

        AudioOutputParams(int sessionId, float volume, float pan, int rate, int pitch, AudioAttributes audioAttributes) {
            this.mSessionId = sessionId;
            this.mVolume = volume;
            this.mPan = pan;
            this.mRate = rate;
            this.mPitch = pitch;
            this.mAudioAttributes = audioAttributes;
        }

        static AudioOutputParams createFromParamsBundle(Bundle paramsBundle, boolean isSpeech) {
            int i;
            if (paramsBundle == null) {
                return new AudioOutputParams();
            }
            AudioAttributes audioAttributes = (AudioAttributes) paramsBundle.getParcelable("audioAttributes");
            if (audioAttributes == null) {
                int streamType = paramsBundle.getInt("streamType", 10);
                AudioAttributes.Builder legacyStreamType = new AudioAttributes.Builder().setLegacyStreamType(streamType);
                if (isSpeech) {
                    i = 1;
                } else {
                    i = 4;
                }
                audioAttributes = legacyStreamType.setContentType(i).build();
            }
            return new AudioOutputParams(paramsBundle.getInt("sessionId", 0), paramsBundle.getFloat("volume", 1.0f), paramsBundle.getFloat("pan", 0.0f), paramsBundle.getInt("rate", 100), paramsBundle.getInt("pitch", 100), audioAttributes);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public abstract class SpeechItem {
        private final Object mCallerIdentity;
        private final int mCallerPid;
        private final int mCallerUid;
        private final int mPriority;
        private volatile boolean mStarted;
        private volatile boolean mStopped;

        public abstract boolean isValid();

        protected abstract void playImpl();

        protected abstract void stopImpl();

        public SpeechItem(Object caller, int callerUid, int callerPid) {
            this.mStarted = false;
            this.mStopped = false;
            this.mCallerIdentity = caller;
            this.mCallerUid = callerUid;
            this.mCallerPid = callerPid;
            this.mPriority = 1;
        }

        public SpeechItem(Object caller, int callerUid, int callerPid, int priority) {
            this.mStarted = false;
            this.mStopped = false;
            this.mCallerIdentity = caller;
            this.mCallerUid = callerUid;
            this.mCallerPid = callerPid;
            this.mPriority = priority;
        }

        public Object getCallerIdentity() {
            return this.mCallerIdentity;
        }

        public int getCallerUid() {
            return this.mCallerUid;
        }

        public int getCallerPid() {
            return this.mCallerPid;
        }

        public int getPriority() {
            return this.mPriority;
        }

        public void play() {
            synchronized (this) {
                if (this.mStarted) {
                    throw new IllegalStateException("play() called twice");
                }
                this.mStarted = true;
            }
            playImpl();
        }

        public void stop() {
            synchronized (this) {
                if (this.mStopped) {
                    throw new IllegalStateException("stop() called twice");
                }
                this.mStopped = true;
            }
            stopImpl();
        }

        protected synchronized boolean isStopped() {
            return this.mStopped;
        }

        protected synchronized boolean isStarted() {
            return this.mStarted;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public abstract class UtteranceSpeechItem extends SpeechItem implements UtteranceProgressDispatcher {
        protected final long mDeadline;
        private boolean mIsStartDispatched;
        public final Condition mItemCondition;
        public final Lock mItemLock;
        protected final Bundle mParams;
        protected final String mUtteranceId;

        UtteranceSpeechItem(Object callerIdentity, int callerUid, int callerPid, int priority, Bundle params, String utteranceId) {
            super(callerIdentity, callerUid, callerPid, priority);
            this.mItemLock = new ReentrantLock(true);
            this.mItemCondition = this.mItemLock.newCondition();
            this.mIsStartDispatched = false;
            this.mParams = params;
            this.mUtteranceId = utteranceId;
            long delayTolerance = getIntParam(this.mParams, "delayTolerance", 0);
            if (delayTolerance != 0) {
                this.mDeadline = SystemClock.elapsedRealtime() + delayTolerance;
            } else {
                this.mDeadline = 0L;
            }
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceProgressDispatcher
        public void dispatchOnSuccess() {
            XpTextToSpeechServiceBase.this.mCallbackHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceSpeechItem.1
                @Override // java.lang.Runnable
                public void run() {
                    String utteranceId = UtteranceSpeechItem.this.getUtteranceId();
                    if (utteranceId != null) {
                        XpTextToSpeechServiceBase.this.mCallbacks.dispatchOnSuccess(UtteranceSpeechItem.this.getCallerIdentity(), utteranceId, UtteranceSpeechItem.this.mParams);
                    }
                    UtteranceSpeechItem.this.mItemLock.lock();
                    try {
                        UtteranceSpeechItem.this.mItemCondition.signal();
                    } finally {
                        UtteranceSpeechItem.this.mItemLock.unlock();
                    }
                }
            });
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceProgressDispatcher
        public void dispatchOnStop() {
            XpTextToSpeechServiceBase.this.mCallbackHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceSpeechItem.2
                @Override // java.lang.Runnable
                public void run() {
                    String utteranceId = UtteranceSpeechItem.this.getUtteranceId();
                    if (utteranceId != null) {
                        XpTextToSpeechServiceBase.this.mCallbacks.dispatchOnStop(UtteranceSpeechItem.this.getCallerIdentity(), utteranceId, UtteranceSpeechItem.this.isStarted(), UtteranceSpeechItem.this.mParams);
                    }
                    UtteranceSpeechItem.this.mItemLock.lock();
                    try {
                        UtteranceSpeechItem.this.mItemCondition.signal();
                    } finally {
                        UtteranceSpeechItem.this.mItemLock.unlock();
                    }
                }
            });
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceProgressDispatcher
        public void dispatchOnStart() {
            XpTextToSpeechServiceBase.this.mCallbackHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceSpeechItem.3
                @Override // java.lang.Runnable
                public void run() {
                    String utteranceId = UtteranceSpeechItem.this.getUtteranceId();
                    if (utteranceId != null) {
                        long startTime = System.currentTimeMillis();
                        UtteranceSpeechItem.this.mParams.putLong("startTime", startTime);
                        XpTextToSpeechServiceBase.this.mCallbacks.dispatchOnStart(UtteranceSpeechItem.this.getCallerIdentity(), utteranceId, UtteranceSpeechItem.this.mParams);
                        UtteranceSpeechItem.this.mIsStartDispatched = true;
                    }
                }
            });
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceProgressDispatcher
        public void dispatchOnError(final int errorCode) {
            XpTextToSpeechServiceBase.this.mCallbackHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceSpeechItem.4
                @Override // java.lang.Runnable
                public void run() {
                    String utteranceId = UtteranceSpeechItem.this.getUtteranceId();
                    if (utteranceId != null) {
                        XpTextToSpeechServiceBase.this.mCallbacks.dispatchOnError(UtteranceSpeechItem.this.getCallerIdentity(), utteranceId, errorCode, UtteranceSpeechItem.this.mParams);
                    }
                    UtteranceSpeechItem.this.mItemLock.lock();
                    try {
                        UtteranceSpeechItem.this.mItemCondition.signal();
                    } finally {
                        UtteranceSpeechItem.this.mItemLock.unlock();
                    }
                }
            });
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceProgressDispatcher
        public void dispatchOnBeginSynthesis(final int sampleRateInHz, final int audioFormat, final int channelCount) {
            XpTextToSpeechServiceBase.this.mCallbackHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceSpeechItem.5
                @Override // java.lang.Runnable
                public void run() {
                    String utteranceId = UtteranceSpeechItem.this.getUtteranceId();
                    if (utteranceId != null) {
                        XpTextToSpeechServiceBase.this.mCallbacks.dispatchOnBeginSynthesis(UtteranceSpeechItem.this.getCallerIdentity(), utteranceId, sampleRateInHz, audioFormat, channelCount);
                    }
                }
            });
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceProgressDispatcher
        public void dispatchOnAudioAvailable(final byte[] audio) {
            XpTextToSpeechServiceBase.this.mCallbackHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceSpeechItem.6
                @Override // java.lang.Runnable
                public void run() {
                    String utteranceId = UtteranceSpeechItem.this.getUtteranceId();
                    if (utteranceId != null) {
                        XpTextToSpeechServiceBase.this.mCallbacks.dispatchOnAudioAvailable(UtteranceSpeechItem.this.getCallerIdentity(), utteranceId, audio);
                    }
                }
            });
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceProgressDispatcher
        public void dispatchOnRangeStart(final int start, final int end, final int frame) {
            XpTextToSpeechServiceBase.this.mCallbackHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceSpeechItem.7
                @Override // java.lang.Runnable
                public void run() {
                    String utteranceId = UtteranceSpeechItem.this.getUtteranceId();
                    if (utteranceId != null) {
                        XpTextToSpeechServiceBase.this.mCallbacks.dispatchOnRangeStart(UtteranceSpeechItem.this.getCallerIdentity(), utteranceId, start, end, frame);
                    }
                }
            });
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceProgressDispatcher
        public void dispatchTtsInfo(final Bundle info) {
            XpTextToSpeechServiceBase.this.mCallbackHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.UtteranceSpeechItem.8
                @Override // java.lang.Runnable
                public void run() {
                    Log.d(XpTextToSpeechServiceBase.TAG, "dispatchTtsInfo " + info);
                    UtteranceSpeechItem.this.mParams.putAll(info);
                    if (UtteranceSpeechItem.this.mIsStartDispatched) {
                        XpTextToSpeechServiceBase.this.mCallbacks.dispatchTtsInfo(UtteranceSpeechItem.this.getCallerIdentity(), UtteranceSpeechItem.this.mParams);
                    }
                }
            });
        }

        String getStringParam(Bundle params, String key, String defaultValue) {
            return params == null ? defaultValue : params.getString(key, defaultValue);
        }

        int getIntParam(Bundle params, String key, int defaultValue) {
            return params == null ? defaultValue : params.getInt(key, defaultValue);
        }

        float getFloatParam(Bundle params, String key, float defaultValue) {
            return params == null ? defaultValue : params.getFloat(key, defaultValue);
        }

        boolean hasLanguage() {
            return !TextUtils.isEmpty(getStringParam(this.mParams, "language", null));
        }

        int getSpeechRate() {
            return getIntParam(this.mParams, "rate", XpTextToSpeechServiceBase.this.getDefaultSpeechRate());
        }

        int getPitch() {
            return getIntParam(this.mParams, "pitch", XpTextToSpeechServiceBase.this.getDefaultPitch());
        }

        public String getUtteranceId() {
            return this.mUtteranceId;
        }

        AudioOutputParams getAudioParams() {
            return AudioOutputParams.createFromParamsBundle(this.mParams, true);
        }

        public Bundle getParams() {
            return this.mParams;
        }

        long getDeadline() {
            return this.mDeadline;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class SynthesisSpeechItem extends UtteranceSpeechItem {
        private final int mCallerUid;
        private final String[] mDefaultLocale;
        private XpSynthesisCallback mSynthesisCallback;
        private final XpSynthesisRequest mSynthesisRequest;
        private final String mTagChannel;
        private final CharSequence mText;

        public SynthesisSpeechItem(Object callerIdentity, int callerUid, int callerPid, int priority, Bundle params, String utteranceId, CharSequence text) {
            super(callerIdentity, callerUid, callerPid, priority, params, utteranceId);
            this.mText = text;
            this.mCallerUid = callerUid;
            this.mSynthesisRequest = new XpSynthesisRequest(this.mText, this.mParams);
            this.mDefaultLocale = XpTextToSpeechServiceBase.this.getSettingsLocale();
            setRequestParams(this.mSynthesisRequest);
            this.mTagChannel = "XpTextToSpeechServiceBase_" + XpTextToSpeechServiceBase.getChannelNameForId(params.getInt("channel", -1));
        }

        public CharSequence getText() {
            return this.mText;
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.SpeechItem
        public boolean isValid() {
            CharSequence charSequence = this.mText;
            if (charSequence == null) {
                Log.e(this.mTagChannel, "null synthesis text");
                return false;
            } else if (charSequence.length() > TextToSpeech.getMaxSpeechInputLength()) {
                String str = this.mTagChannel;
                Log.w(str, "Text too long: " + this.mText.length() + " chars");
                return false;
            } else if (getPriority() < 1 || getPriority() > 4) {
                String txt = this.mTagChannel;
                Log.e(txt, "priority out of range");
                return false;
            } else {
                String txt2 = this.mText.toString();
                if (txt2.replaceAll("[\\s\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]", "").trim().equals("")) {
                    Log.e(this.mTagChannel, "text have no meaning");
                    return false;
                }
                return true;
            }
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.SpeechItem
        protected void playImpl() {
            SynthChannel synthChannel;
            long deadline = getDeadline();
            if (deadline != 0) {
                long currentTime = SystemClock.elapsedRealtime();
                if (currentTime > deadline) {
                    Log.w(this.mTagChannel, "exceeded the deadline, discard it");
                    dispatchOnError(-11);
                    return;
                }
            }
            this.mItemLock.lock();
            try {
                try {
                } catch (Exception e) {
                    Log.e(this.mTagChannel, "playImpl exception", e);
                }
                if (isStopped()) {
                    Log.w(this.mTagChannel, "item already stopped");
                    return;
                }
                if (XpTextToSpeechServiceBase.this.isUseServicePlayback()) {
                    Log.d(this.mTagChannel, "use service playback");
                    this.mSynthesisCallback = createSynthesisCallback();
                } else {
                    Log.d(this.mTagChannel, "use engine playback");
                    this.mSynthesisCallback = new XpSimpleSynthesisCallback(this);
                }
                XpSynthesisCallback synthesisCallback = this.mSynthesisCallback;
                int focusMode = this.mParams.getInt("audioFocus", 1);
                int streamType = this.mParams.getInt("streamType", 10);
                int channel = this.mParams.getInt("channel", 0);
                boolean isFocusGain = true;
                String caller = this.mParams.getString("source", "");
                if (!caller.equals("com.xiaopeng.montecarlo") || XpTextToSpeechServiceBase.this.mIsVolDownOnNavi) {
                    if (focusMode == 1 && (synthChannel = XpTextToSpeechServiceBase.this.getSynthChannelForId(channel)) != null) {
                        AudioFocusHelper audioFocusHelper = synthChannel.getAudioFocusHelper();
                        isFocusGain = audioFocusHelper.requestAudioFocusPosition(streamType, channel);
                    }
                } else if (XpTextToSpeechServiceBase.this.mIsCallStateActive) {
                    isFocusGain = false;
                }
                if (!isFocusGain && getPriority() != 4) {
                    Log.w(this.mTagChannel, "speak fail: request audio focus fail");
                    dispatchOnError(-4);
                    return;
                }
                boolean isNeedSetPosition = false;
                int voicePosition = -1;
                if (streamType == 10) {
                    isNeedSetPosition = true;
                    voicePosition = this.mParams.getInt("voicePosition", 0);
                }
                if (isNeedSetPosition) {
                    XpTextToSpeechServiceBase.this.mAudioManager.setVoicePosition(voicePosition, 0);
                }
                XpTextToSpeechServiceBase.this.onSynthesizeText(this.mSynthesisRequest, synthesisCallback);
                this.mItemCondition.await();
                if (isNeedSetPosition) {
                    XpTextToSpeechServiceBase.this.mAudioManager.setVoicePosition(-1, 0);
                }
                if (XpTextToSpeechServiceBase.this.isUseServicePlayback() && synthesisCallback.hasStarted() && !synthesisCallback.hasFinished()) {
                    synthesisCallback.done();
                }
            } finally {
                this.mItemLock.unlock();
            }
        }

        protected XpSynthesisCallback createSynthesisCallback() {
            SynthChannel synthChannel = XpTextToSpeechServiceBase.this.getSynthChannelForId(getParams().getInt("channel", 0));
            return new XpPlaybackSynthesisCallback(getAudioParams(), synthChannel.getAudioPlaybackHandler(), this, getCallerIdentity());
        }

        private void setRequestParams(XpSynthesisRequest request) {
            String voiceName = getVoiceName();
            request.setLanguage(getLanguage(), getCountry(), getVariant());
            if (!TextUtils.isEmpty(voiceName)) {
                request.setVoiceName(getVoiceName());
            }
            request.setSpeechRate(getSpeechRate());
            request.setCallerUid(this.mCallerUid);
            request.setPitch(getPitch());
        }

        @Override // com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.SpeechItem
        protected void stopImpl() {
            this.mItemLock.lock();
            try {
                XpSynthesisCallback synthesisCallback = this.mSynthesisCallback;
                if (synthesisCallback != null) {
                    synthesisCallback.stop();
                    XpTextToSpeechServiceBase.this.onStop(getParams());
                } else {
                    Log.i(this.mTagChannel, "stop before play");
                    dispatchOnStop();
                }
            } catch (Exception e) {
                Log.e(this.mTagChannel, "stopImpl exception", e);
            } finally {
                this.mItemLock.unlock();
            }
        }

        private String getCountry() {
            return !hasLanguage() ? this.mDefaultLocale[1] : getStringParam(this.mParams, "country", "");
        }

        private String getVariant() {
            return !hasLanguage() ? this.mDefaultLocale[2] : getStringParam(this.mParams, "variant", "");
        }

        public String getLanguage() {
            return getStringParam(this.mParams, "language", this.mDefaultLocale[0]);
        }

        public String getVoiceName() {
            return getStringParam(this.mParams, "voiceName", "");
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if ("android.intent.action.TTS_SERVICE".equals(intent.getAction())) {
            return this.mBinder;
        }
        return null;
    }

    private String getQueueModeName(int queueMode) {
        if (queueMode == 1) {
            return "QueAdd";
        }
        if (queueMode == 0) {
            return "QueFlush";
        }
        if (queueMode == 2) {
            return "QueDestroy";
        }
        return "QueUnknown";
    }

    private String getPriorityName(int priority) {
        if (priority == 1) {
            return "PriNormal";
        }
        if (priority == 2) {
            return "PriImportant";
        }
        if (priority == 3) {
            return "PriUrgent";
        }
        if (priority == 4) {
            return "PriInstant";
        }
        return "PriUnknown";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPackageNameByPid(int pid) {
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        List<ActivityManager.RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (pid == appProcess.pid) {
                String packageName = appProcess.processName;
                return packageName;
            }
        }
        return BuildInfoUtils.UNKNOWN;
    }

    protected void setViceBtEnable(boolean on) {
    }

    protected boolean isUseServicePlayback() {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int speak(final IBinder caller, final CharSequence text, final int queueMode, final Bundle params, final String utteranceId) {
        if (!checkNonNull(caller, text, params)) {
            return -1;
        }
        final int uid = Binder.getCallingUid();
        final int pid = Binder.getCallingPid();
        this.mHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.1
            @Override // java.lang.Runnable
            public void run() {
                XpTextToSpeechServiceBase.this.onSpeak(caller, text, queueMode, params, utteranceId, uid, pid);
            }
        });
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSpeak(IBinder caller, CharSequence text, int queueMode, Bundle params, String utteranceId, int uid, int pid) {
        float volume;
        int channelId;
        SynthChannel synthChannel;
        String str;
        int priority = params.getInt(Progress.PRIORITY, 1);
        String sourceName = params.getString("caller");
        String sourceName2 = sourceName == null ? getPackageNameByPid(pid) : sourceName;
        params.putString("source", sourceName2);
        float volume2 = params.getFloat("volume", 1.0f);
        if (volume2 < 0.0f) {
            volume = 0.0f;
        } else if (volume2 <= 1.0f) {
            volume = volume2;
        } else {
            volume = 1.0f;
        }
        params.putFloat("volume", volume);
        params.putString("uid", UUID.randomUUID().toString());
        params.putString("txt", text.toString());
        params.putString("utteranceId", utteranceId);
        int streamType = params.getInt("streamType", 10);
        int channelId2 = getChannelIdForStreamType(sourceName2, streamType);
        SynthChannel synthChannel2 = getSynthChannelForId(channelId2);
        if (synthChannel2 != null) {
            channelId = channelId2;
            synthChannel = synthChannel2;
        } else {
            Log.w(TAG, "No synth channel for " + channelId2 + " use default channel");
            channelId = 0;
            synthChannel = getSynthChannelForId(0);
        }
        params.putInt("channel", channelId);
        StringBuilder sb = new StringBuilder();
        sb.append("speak ");
        sb.append(getPriorityName(priority));
        sb.append(" ");
        sb.append(getQueueModeName(queueMode));
        sb.append(" by ");
        sb.append(sourceName2);
        sb.append(" at ");
        sb.append(getChannelNameForId(channelId));
        sb.append(" text ");
        if (text.length() > 40) {
            str = ((Object) text.subSequence(0, 20)) + "...";
        } else {
            str = text;
        }
        sb.append((Object) str);
        sb.append(" id ");
        sb.append(utteranceId);
        Log.i(TAG, sb.toString());
        SpeechItem item = new SynthesisSpeechItem(caller, uid, pid, priority, params, utteranceId, text);
        synthChannel.enqueueSpeechItem(queueMode, item);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isSpeaking() {
        return isSpeaking(0) || isSpeaking(1) || isSpeaking(2);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isSpeaking(int channel) {
        SynthChannel synthChannel = getSynthChannelForId(channel);
        if (synthChannel != null) {
            return synthChannel.isSpeaking();
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int stop(final IBinder caller, final String utteranceId) {
        if (!checkNonNull(caller)) {
            return -1;
        }
        final int pid = Binder.getCallingPid();
        this.mHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.2
            @Override // java.lang.Runnable
            public void run() {
                String name = XpTextToSpeechServiceBase.this.getPackageNameByPid(pid);
                Log.i(XpTextToSpeechServiceBase.TAG, "stop " + utteranceId + " by " + name);
                for (SynthChannel synthChannel : XpTextToSpeechServiceBase.this.mSynthChannelList) {
                    synthChannel.stop(caller, utteranceId);
                }
            }
        });
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int stop(final IBinder caller, final int[] channelList) {
        if (!checkNonNull(caller)) {
            return -1;
        }
        final int pid = Binder.getCallingPid();
        this.mHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.3
            @Override // java.lang.Runnable
            public void run() {
                int[] iArr;
                String name = XpTextToSpeechServiceBase.this.getPackageNameByPid(pid);
                for (int channelId : channelList) {
                    SynthChannel synthChannel = XpTextToSpeechServiceBase.this.getSynthChannelForId(channelId);
                    if (synthChannel != null) {
                        Log.i(XpTextToSpeechServiceBase.TAG, "stop " + XpTextToSpeechServiceBase.getChannelNameForId(channelId) + " channel item of " + name);
                        synthChannel.stop(caller, null);
                    } else {
                        Log.w(XpTextToSpeechServiceBase.TAG, "stop " + XpTextToSpeechServiceBase.getChannelNameForId(channelId) + " channel NOT exist");
                    }
                }
            }
        });
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String[] getLanguage() {
        return onGetLanguage();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String[] getClientDefaultLanguage() {
        return getSettingsLocale();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int isLanguageAvailable(String lang, String country, String variant) {
        if (!checkNonNull(lang)) {
            return -1;
        }
        return onIsLanguageAvailable(lang, country, variant);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String[] getFeaturesForLanguage(String lang, String country, String variant) {
        Set<String> features = onGetFeaturesForLanguage(lang, country, variant);
        if (features != null) {
            String[] featuresArray = new String[features.size()];
            features.toArray(featuresArray);
            return featuresArray;
        }
        return new String[0];
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int loadLanguage(IBinder caller, String lang, String country, String variant) {
        if (!checkNonNull(lang)) {
            return -1;
        }
        return onIsLanguageAvailable(lang, country, variant);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public List<Voice> getVoices() {
        return onGetVoices();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int loadVoice(IBinder caller, String voiceName) {
        if (!checkNonNull(voiceName)) {
            return -1;
        }
        return onIsValidVoiceName(voiceName);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getDefaultVoiceNameFor(String lang, String country, String variant) {
        if (checkNonNull(lang)) {
            int retVal = onIsLanguageAvailable(lang, country, variant);
            if (retVal == 0 || retVal == 1 || retVal == 2) {
                return onGetDefaultVoiceNameFor(lang, country, variant);
            }
            return null;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setCallback(IBinder caller, ITextToSpeechCallback cb) {
        if (!checkNonNull(caller)) {
            return;
        }
        String name = getPackageNameByPid(Binder.getCallingPid());
        this.mCallbacks.setCallback(caller, cb, name);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int setSoloMode(IBinder caller, int channel, boolean on) {
        if (checkNonNull(caller)) {
            String name = getPackageNameByPid(Binder.getCallingPid());
            Log.i(TAG, "setSoloMode channel " + channel + " on " + on + " by " + name);
            SynthChannel synthChannel = getSynthChannelForId(channel);
            if (synthChannel != null) {
                return synthChannel.setSoloMode(caller, on);
            }
            Log.e(TAG, "setSoloMode channel " + channel + " NOT found");
            return -1;
        }
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setListenTtsStateChanged(IBinder caller) {
        if (!checkNonNull(caller)) {
            return;
        }
        String name = getPackageNameByPid(Binder.getCallingPid());
        Log.d(TAG, "setListenTtsState by " + name);
        this.mCallbacks.setListenOnStateChanged(caller);
    }

    private boolean checkNonNull(Object... args) {
        for (Object o : args) {
            if (o == null) {
                return false;
            }
        }
        return true;
    }

    /* loaded from: classes.dex */
    private class CallbackMap extends RemoteCallbackList<ITextToSpeechCallback> {
        private final HashMap<IBinder, ITextToSpeechCallback> mCallerToCallback;
        private final HashMap<IBinder, String> mCallerToName;
        private ArrayList<IBinder> mListenerOnStateChanged;

        private CallbackMap() {
            this.mCallerToCallback = new HashMap<>();
            this.mCallerToName = new HashMap<>();
            this.mListenerOnStateChanged = new ArrayList<>();
        }

        public void setCallback(IBinder caller, ITextToSpeechCallback cb, String name) {
            ITextToSpeechCallback old;
            synchronized (this.mCallerToCallback) {
                if (cb != null) {
                    register(cb, caller);
                    old = this.mCallerToCallback.put(caller, cb);
                    this.mCallerToName.put(caller, name);
                } else {
                    old = this.mCallerToCallback.remove(caller);
                    this.mCallerToName.remove(caller);
                }
                if (old != null && old != cb) {
                    unregister(old);
                }
            }
            synchronized (this.mListenerOnStateChanged) {
                if (cb == null) {
                    if (this.mListenerOnStateChanged.contains(caller)) {
                        this.mListenerOnStateChanged.remove(caller);
                    }
                }
            }
        }

        public void setListenOnStateChanged(IBinder caller) {
            synchronized (this.mListenerOnStateChanged) {
                if (!this.mListenerOnStateChanged.contains(caller)) {
                    this.mListenerOnStateChanged.add(caller);
                }
            }
        }

        public void notifyOnStateChanged(IBinder caller, int state, Bundle params) {
            ITextToSpeechCallback cb;
            CallbackMap callbackMap = this;
            Log.v(XpTextToSpeechServiceBase.TAG, "notifyOnStateChanged state " + state + " params " + params);
            synchronized (callbackMap.mCallerToCallback) {
                try {
                    try {
                        String name = callbackMap.mCallerToName.get(caller);
                        if (name == null) {
                            name = "";
                        }
                        String name2 = name;
                        int channelId = params.getInt("channel", 0);
                        SynthChannel synthChannel = XpTextToSpeechServiceBase.this.getSynthChannelForId(channelId);
                        if (synthChannel == null) {
                            return;
                        }
                        XpTtsStateInfoBean info = synthChannel.getTtsStateInfo(name2, state, params);
                        String sInfo = XpTextToSpeechServiceBase.this.infoToJson(info);
                        synchronized (callbackMap.mListenerOnStateChanged) {
                            Iterator<IBinder> it = callbackMap.mListenerOnStateChanged.iterator();
                            while (it.hasNext()) {
                                IBinder listener = it.next();
                                synchronized (callbackMap.mCallerToCallback) {
                                    cb = callbackMap.mCallerToCallback.get(listener);
                                }
                                if (cb == null) {
                                    break;
                                }
                                try {
                                    cb.onStateChanged(sInfo);
                                } catch (RemoteException e) {
                                    Log.e(XpTextToSpeechServiceBase.TAG, "Callback onStateChanged failed: " + e);
                                }
                                callbackMap = this;
                            }
                        }
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }

        public void dispatchOnStop(Object callerIdentity, String utteranceId, boolean started, Bundle params) {
            if (started) {
                notifyOnStateChanged((IBinder) callerIdentity, 0, params);
            }
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb == null) {
                return;
            }
            try {
                cb.onStop(params, utteranceId, started);
            } catch (RemoteException e) {
                Log.e(XpTextToSpeechServiceBase.TAG, "Callback onStop failed: " + e);
            }
        }

        public void dispatchOnSuccess(Object callerIdentity, String utteranceId, Bundle params) {
            notifyOnStateChanged((IBinder) callerIdentity, 0, params);
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb == null) {
                return;
            }
            try {
                cb.onSuccess(params, utteranceId);
            } catch (RemoteException e) {
                Log.e(XpTextToSpeechServiceBase.TAG, "Callback onDone failed: " + e);
            }
        }

        public void dispatchOnStart(Object callerIdentity, String utteranceId, Bundle params) {
            notifyOnStateChanged((IBinder) callerIdentity, 1, params);
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb == null) {
                return;
            }
            try {
                cb.onStart(params, utteranceId);
            } catch (RemoteException e) {
                Log.e(XpTextToSpeechServiceBase.TAG, "Callback onStart failed: " + e);
            }
        }

        public void dispatchOnError(Object callerIdentity, String utteranceId, int errorCode, Bundle params) {
            notifyOnStateChanged((IBinder) callerIdentity, -1, params);
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb == null) {
                return;
            }
            try {
                cb.onError(params, utteranceId, errorCode);
            } catch (RemoteException e) {
                Log.e(XpTextToSpeechServiceBase.TAG, "Callback onError failed: " + e);
            }
        }

        public void dispatchOnBeginSynthesis(Object callerIdentity, String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb == null) {
                return;
            }
            try {
                cb.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
            } catch (RemoteException e) {
                Log.e(XpTextToSpeechServiceBase.TAG, "Callback dispatchOnBeginSynthesis(String, int, int, int) failed: " + e);
            }
        }

        public void dispatchOnAudioAvailable(Object callerIdentity, String utteranceId, byte[] buffer) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb == null) {
                return;
            }
            try {
                cb.onAudioAvailable(utteranceId, buffer);
            } catch (RemoteException e) {
                Log.e(XpTextToSpeechServiceBase.TAG, "Callback dispatchOnAudioAvailable(String, byte[]) failed: " + e);
            }
        }

        public void dispatchOnRangeStart(Object callerIdentity, String utteranceId, int start, int end, int frame) {
            ITextToSpeechCallback cb = getCallbackFor(callerIdentity);
            if (cb == null) {
                return;
            }
            try {
                cb.onRangeStart(utteranceId, start, end, frame);
            } catch (RemoteException e) {
                Log.e(XpTextToSpeechServiceBase.TAG, "Callback dispatchOnRangeStart(String, int, int, int) failed: " + e);
            }
        }

        public void dispatchTtsInfo(Object callerIdentity, Bundle params) {
            notifyOnStateChanged((IBinder) callerIdentity, 2, params);
        }

        @Override // android.os.RemoteCallbackList
        public void onCallbackDied(ITextToSpeechCallback callback, Object cookie) {
            IBinder caller = (IBinder) cookie;
            synchronized (this.mCallerToCallback) {
                this.mCallerToCallback.remove(caller);
            }
            synchronized (this.mListenerOnStateChanged) {
                if (this.mListenerOnStateChanged.contains(caller)) {
                    this.mListenerOnStateChanged.remove(caller);
                }
            }
            for (SynthChannel synthChannel : XpTextToSpeechServiceBase.this.mSynthChannelList) {
                synthChannel.setSoloMode(caller, false);
            }
        }

        @Override // android.os.RemoteCallbackList
        public void kill() {
            synchronized (this.mCallerToCallback) {
                this.mCallerToCallback.clear();
                super.kill();
            }
        }

        private ITextToSpeechCallback getCallbackFor(Object caller) {
            ITextToSpeechCallback cb;
            IBinder asBinder = (IBinder) caller;
            synchronized (this.mCallerToCallback) {
                cb = this.mCallerToCallback.get(asBinder);
            }
            return cb;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String infoToJson(XpTtsStateInfoBean info) {
        String json;
        XpTtsInfoExtra extra;
        List<XpTtsStackItem> stack;
        JSONArray arrayStack;
        Iterator<XpTtsStackItem> it;
        String json2 = "";
        try {
            extra = info.extra;
            stack = extra.stack;
            arrayStack = new JSONArray();
            it = stack.iterator();
        } catch (Exception e) {
            e = e;
            json = json2;
        }
        while (true) {
            XpTtsInfoExtra extra2 = extra;
            if (it.hasNext()) {
                XpTtsStackItem item = it.next();
                JSONObject obj = new JSONObject();
                json = json2;
                try {
                    obj.put("source", item.source);
                    obj.put("predictTime", item.predictTime);
                    obj.put(Progress.PRIORITY, item.priority);
                    obj.put("time", item.time);
                    obj.put("text", item.text);
                    obj.put("ttsId", item.ttsId);
                    obj.put("nlgId", item.nlgId);
                    obj.put("streamType", item.streamType);
                    obj.put("rate", item.rate);
                    obj.put("volume", item.volume);
                    arrayStack.put(obj);
                    extra = extra2;
                    json2 = json;
                    stack = stack;
                } catch (Exception e2) {
                    e = e2;
                }
            } else {
                json = json2;
                JSONObject objExtra = new JSONObject();
                objExtra.put("stack", arrayStack);
                JSONObject objInfo = new JSONObject();
                objInfo.put("extra", objExtra);
                objInfo.put("source", info.source);
                objInfo.put(Progress.PRIORITY, info.priority);
                objInfo.put("predictTime", info.predictTime);
                objInfo.put("time", info.time);
                objInfo.put("text", info.text);
                objInfo.put("ttsId", info.ttsId);
                objInfo.put("nlgId", info.nlgId);
                objInfo.put("channel", info.channel);
                objInfo.put("streamType", info.streamType);
                objInfo.put("rate", info.rate);
                objInfo.put("volume", info.volume);
                objInfo.put(Const.TableSchema.COLUMN_TYPE, info.type);
                return objInfo.toString();
            }
            e = e2;
            Log.e(TAG, "make json error", e);
            return json;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {
        private AudioManager mAudioManager;
        private volatile boolean mIsRequested = false;

        public AudioFocusHelper(Context ct) {
            this.mAudioManager = (AudioManager) ct.getSystemService("audio");
        }

        @Override // android.media.AudioManager.OnAudioFocusChangeListener
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == -3 || focusChange == -2 || focusChange == -1) {
                Log.d(XpTextToSpeechServiceBase.TAG, "audio focus loss " + focusChange);
            } else if (focusChange == 1 || focusChange == 2) {
                Log.d(XpTextToSpeechServiceBase.TAG, "onAudioFocusChange audio focus gained " + focusChange);
            }
        }

        public boolean requestAudioFocus(int streamType) {
            if (this.mIsRequested) {
                return true;
            }
            int ret = this.mAudioManager.requestAudioFocus(this, streamType, 3);
            if (ret == 0) {
                return false;
            }
            this.mIsRequested = true;
            return true;
        }

        public boolean requestAudioFocusPosition(int streamType, int channel) {
            int position;
            if (this.mIsRequested) {
                return true;
            }
            if (channel == 0 || channel == 1) {
                position = 0;
            } else if (channel != 2) {
                return false;
            } else {
                position = 1;
            }
            int ret = this.mAudioManager.requestAudioFocusPosition(this, new AudioAttributes.Builder().setLegacyStreamType(streamType).build(), 3, position);
            if (ret == 0) {
                return false;
            }
            this.mIsRequested = true;
            return true;
        }

        public void abandonAudioFocus() {
            if (this.mIsRequested) {
                this.mIsRequested = false;
                this.mAudioManager.abandonAudioFocus(this);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CallbackHandler extends Handler {
        List<Integer> mChannelIdList;

        public CallbackHandler(Looper looper) {
            super(looper);
            this.mChannelIdList = new ArrayList();
        }

        public void registerChannelId(final int id) {
            post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.CallbackHandler.1
                @Override // java.lang.Runnable
                public void run() {
                    Log.d(XpTextToSpeechServiceBase.TAG, "CallbackHandler registerChannelId " + id);
                    CallbackHandler.this.mChannelIdList.add(new Integer(id));
                }
            });
        }

        public void unRegisterChannelId(final int id) {
            post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.CallbackHandler.2
                @Override // java.lang.Runnable
                public void run() {
                    Log.d(XpTextToSpeechServiceBase.TAG, "CallbackHandler unRegisterChannelId " + id);
                    Iterator<Integer> it = CallbackHandler.this.mChannelIdList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Integer i = it.next();
                        if (i.intValue() == id) {
                            CallbackHandler.this.mChannelIdList.remove(i);
                            break;
                        }
                    }
                    if (CallbackHandler.this.mChannelIdList.isEmpty()) {
                        Log.d(XpTextToSpeechServiceBase.TAG, "CallbackThread quitSafely");
                        XpTextToSpeechServiceBase.this.mCallbackThread.quitSafely();
                    }
                }
            });
        }
    }

    /* loaded from: classes.dex */
    private class MainDriverMode extends ContentObserver {
        private int mMode;

        public MainDriverMode(Handler handler) {
            super(handler);
            this.mMode = 0;
            this.mMode = XpTextToSpeechServiceBase.this.mAudioManager.getMainDriverMode();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            int mode = XpTextToSpeechServiceBase.this.mAudioManager.getMainDriverMode();
            if (mode != this.mMode) {
                Log.i(XpTextToSpeechServiceBase.TAG, "main driver mode change " + this.mMode + " -> " + mode);
                if (mode == 2) {
                    Log.i(XpTextToSpeechServiceBase.TAG, "stop all item on channel main");
                    XpTextToSpeechServiceBase.this.getSynthChannelForId(0).stopAll();
                } else if (mode == 0) {
                    Log.i(XpTextToSpeechServiceBase.TAG, "stop all item on channel headrest");
                    SynthChannel synthChannel = XpTextToSpeechServiceBase.this.getSynthChannelForId(1);
                    if (synthChannel != null) {
                        synthChannel.stopAll();
                    }
                } else if (this.mMode == 0 && mode == 1) {
                    Log.i(XpTextToSpeechServiceBase.TAG, "stop STREAM_TTS on channel main");
                    XpTextToSpeechServiceBase.this.getSynthChannelForId(0).stopStreamType(9);
                } else if (this.mMode == 2 && mode == 1) {
                    Log.i(XpTextToSpeechServiceBase.TAG, "stop STREAM_ACCESSIBILITY on channel headrest");
                    SynthChannel synthChannel2 = XpTextToSpeechServiceBase.this.getSynthChannelForId(1);
                    if (synthChannel2 != null) {
                        synthChannel2.stopStreamType(10);
                    }
                }
                this.mMode = mode;
            }
        }
    }

    private void viceBtInitCheck() {
        this.mHandler.post(new Runnable() { // from class: com.xiaopeng.speech.tts.XpTextToSpeechServiceBase.4
            @Override // java.lang.Runnable
            public void run() {
                XpTextToSpeechServiceBase xpTextToSpeechServiceBase = XpTextToSpeechServiceBase.this;
                xpTextToSpeechServiceBase.mIsViceBtSpeechOn = Settings.System.getInt(xpTextToSpeechServiceBase.mContext.getContentResolver(), XpTextToSpeechServiceBase.VICE_BT_SPEECH, 0) == 1;
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null && adapter.isDeviceConnected(1)) {
                    XpTextToSpeechServiceBase.this.mIsViceBtConnected = true;
                }
                XpTextToSpeechServiceBase.this.handleViceBtChange();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleViceBtChange() {
        boolean isOn = this.mIsViceBtSpeechOn && this.mIsViceBtConnected;
        if (isOn != this.mIsViceBtOn) {
            Log.i(TAG, "bt on state " + isOn);
            this.mIsViceBtOn = isOn;
            SynthChannel synthChannel = getSynthChannelForId(2);
            if (synthChannel != null) {
                if (!isOn) {
                    synthChannel.stopAll();
                }
                synthChannel.handleViceBtChange(this.mIsViceBtOn);
            }
        }
    }

    /* loaded from: classes.dex */
    private class ViceBtSpeech extends ContentObserver {
        public ViceBtSpeech(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            boolean isEnable = Settings.System.getInt(XpTextToSpeechServiceBase.this.mContext.getContentResolver(), XpTextToSpeechServiceBase.VICE_BT_SPEECH, 0) == 1;
            if (isEnable != XpTextToSpeechServiceBase.this.mIsViceBtSpeechOn) {
                Log.d(XpTextToSpeechServiceBase.TAG, "vice bt speech switch change " + XpTextToSpeechServiceBase.this.mIsViceBtSpeechOn + " -> " + isEnable);
                XpTextToSpeechServiceBase.this.mIsViceBtSpeechOn = isEnable;
                XpTextToSpeechServiceBase.this.handleViceBtChange();
            }
        }
    }

    /* loaded from: classes.dex */
    private class VolDownOnNavi extends ContentObserver {
        public VolDownOnNavi(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            boolean isEnable = Settings.System.getInt(XpTextToSpeechServiceBase.this.mContext.getContentResolver(), XpTextToSpeechServiceBase.VOL_DOWN_ON_NAVI, 1) == 1;
            if (isEnable != XpTextToSpeechServiceBase.this.mIsVolDownOnNavi) {
                Log.i(XpTextToSpeechServiceBase.TAG, "volume down on navi tts " + isEnable);
                XpTextToSpeechServiceBase.this.mIsVolDownOnNavi = isEnable;
            }
        }
    }

    /* loaded from: classes.dex */
    private class CallStateReceiver extends BroadcastReceiver {
        private int mBtState;
        private int mCallState;

        private CallStateReceiver() {
            this.mCallState = -1;
            this.mBtState = -1;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int btState;
            String action = intent.getAction();
            Log.v(XpTextToSpeechServiceBase.TAG, "onReceive action " + action);
            if (action.equals("android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED") || action.equals(XpTextToSpeechServiceBase.ACTION_ECALL_CHANGED)) {
                int callState = 0;
                if (action.equals("android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED")) {
                    BluetoothHeadsetClientCall btCallState = (BluetoothHeadsetClientCall) intent.getExtra("android.bluetooth.headsetclient.extra.CALL", null);
                    if (btCallState != null) {
                        callState = btCallState.getState();
                    }
                } else if (action.equals(XpTextToSpeechServiceBase.ACTION_ECALL_CHANGED)) {
                    callState = ((Integer) intent.getExtra(XpTextToSpeechServiceBase.EXTRA_ECALL, null)).intValue();
                }
                if (callState != this.mCallState) {
                    Log.i(XpTextToSpeechServiceBase.TAG, "call state " + this.mCallState + " -> " + callState);
                    this.mCallState = callState;
                }
                if (callState == 0) {
                    XpTextToSpeechServiceBase.this.mIsCallStateActive = true;
                } else {
                    XpTextToSpeechServiceBase.this.mIsCallStateActive = false;
                }
                if (callState == 0 || callState == 2 || callState == 3 || callState == 4) {
                    for (SynthChannel synthChannel : XpTextToSpeechServiceBase.this.mSynthChannelList) {
                        synthChannel.stopAll();
                    }
                }
            } else if (action.equals("android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED")) {
                int newState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1);
                int oldState = intent.getIntExtra("android.bluetooth.profile.extra.PREVIOUS_STATE", -1);
                Log.i(XpTextToSpeechServiceBase.TAG, "bt connection state " + oldState + " -> " + newState);
                if (newState == 0) {
                    XpTextToSpeechServiceBase.this.mIsCallStateActive = false;
                }
            } else if (action.equals(XpTextToSpeechServiceBase.ACTION_VICE_BT_CONNECTION) && (btState = ((Integer) intent.getExtra("state", 0)).intValue()) != this.mBtState) {
                Log.i(XpTextToSpeechServiceBase.TAG, "bt connect state " + this.mBtState + " -> " + btState);
                this.mBtState = btState;
                if (this.mBtState == 2) {
                    XpTextToSpeechServiceBase.this.mIsViceBtConnected = true;
                } else {
                    XpTextToSpeechServiceBase.this.mIsViceBtConnected = false;
                }
                XpTextToSpeechServiceBase.this.handleViceBtChange();
            }
        }
    }
}

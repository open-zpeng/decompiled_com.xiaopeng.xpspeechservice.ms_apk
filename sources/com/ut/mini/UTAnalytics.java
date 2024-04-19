package com.ut.mini;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.alibaba.mtl.appmonitor.AppMonitor;
import com.alibaba.mtl.log.b;
import com.alibaba.mtl.log.c;
import com.alibaba.mtl.log.d.i;
import com.ut.mini.base.UTMIVariables;
import com.ut.mini.core.appstatus.UTMCAppStatusRegHelper;
import com.ut.mini.core.sign.IUTRequestAuthentication;
import com.ut.mini.core.sign.UTBaseRequestAuthentication;
import com.ut.mini.core.sign.UTSecuritySDKRequestAuthentication;
import com.ut.mini.internal.UTOriginalCustomHitBuilder;
import com.ut.mini.internal.UTTeamWork;
import com.ut.mini.plugin.UTPluginMgr;
import com.ut.mini.sdkevents.UTMI1010_2001Event;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.util.HashMap;
import java.util.Map;
/* loaded from: classes.dex */
public class UTAnalytics {
    private static UTAnalytics a = null;
    private boolean M;
    private boolean N;

    /* renamed from: a  reason: collision with other field name */
    private UTTracker f154a;
    private Map<String, UTTracker> w = new HashMap();
    private Map<String, UTTracker> x = new HashMap();

    private UTAnalytics() {
        if (Build.VERSION.SDK_INT < 14) {
            UTMI1010_2001Event uTMI1010_2001Event = new UTMI1010_2001Event();
            UTPluginMgr.getInstance().registerPlugin(uTMI1010_2001Event, false);
            UTMIVariables.getInstance().setUTMI1010_2001EventInstance(uTMI1010_2001Event);
            return;
        }
        UTMI1010_2001Event uTMI1010_2001Event2 = new UTMI1010_2001Event();
        UTMCAppStatusRegHelper.registerAppStatusCallbacks(uTMI1010_2001Event2);
        UTMIVariables.getInstance().setUTMI1010_2001EventInstance(uTMI1010_2001Event2);
    }

    @Deprecated
    public void setContext(Context aContext) {
        b.a().setContext(aContext);
        if (aContext != null) {
            UTTeamWork.getInstance().initialized();
        }
    }

    @Deprecated
    public void setAppApplicationInstance(Application aApplicationInstance) {
        b.a().setAppApplicationInstance(aApplicationInstance);
        AppMonitor.init(aApplicationInstance);
    }

    public void setAppApplicationInstance(Application application, IUTApplication utCallback) {
        try {
            if (this.M) {
                return;
            }
            if (application != null && utCallback != null && application.getApplicationContext() != null) {
                getInstance().setContext(application.getApplicationContext());
                getInstance().setAppApplicationInstance(application);
                if (utCallback.isUTLogEnable()) {
                    getInstance().turnOnDebug();
                }
                getInstance().setChannel(utCallback.getUTChannel());
                getInstance().setAppVersion(utCallback.getUTAppVersion());
                getInstance().setRequestAuthentication(utCallback.getUTRequestAuthInstance());
                this.N = true;
                this.M = true;
                return;
            }
            throw new IllegalArgumentException("application and callback must not be null");
        } catch (Throwable th) {
            try {
                i.a((String) null, th);
            } catch (Throwable th2) {
            }
        }
    }

    public void setAppApplicationInstance4sdk(Application application, IUTApplication utCallback) {
        try {
            if (this.N) {
                return;
            }
            if (application != null && utCallback != null && application.getApplicationContext() != null) {
                getInstance().setContext(application.getApplicationContext());
                getInstance().setAppApplicationInstance(application);
                if (utCallback.isUTLogEnable()) {
                    getInstance().turnOnDebug();
                }
                getInstance().setChannel(utCallback.getUTChannel());
                getInstance().setAppVersion(utCallback.getUTAppVersion());
                getInstance().setRequestAuthentication(utCallback.getUTRequestAuthInstance());
                this.N = true;
                return;
            }
            throw new IllegalArgumentException("application and callback must not be null");
        } catch (Throwable th) {
            try {
                i.a((String) null, th);
            } catch (Throwable th2) {
            }
        }
    }

    public static synchronized UTAnalytics getInstance() {
        UTAnalytics uTAnalytics;
        synchronized (UTAnalytics.class) {
            if (a == null) {
                a = new UTAnalytics();
            }
            uTAnalytics = a;
        }
        return uTAnalytics;
    }

    public synchronized UTTracker getDefaultTracker() {
        if (this.f154a == null) {
            this.f154a = new UTTracker();
        }
        if (this.f154a == null) {
            i.a("getDefaultTracker error", "Fatal Error,must call setRequestAuthentication method first.");
        }
        return this.f154a;
    }

    @Deprecated
    public void setRequestAuthentication(IUTRequestAuthentication aRequestAuthenticationInstance) {
        if (aRequestAuthenticationInstance == null) {
            i.a("setRequestAuthentication", "Fatal Error,pRequestAuth must not be null.");
        }
        if (aRequestAuthenticationInstance instanceof UTBaseRequestAuthentication) {
            AppMonitor.setRequestAuthInfo(false, aRequestAuthenticationInstance.getAppkey(), ((UTBaseRequestAuthentication) aRequestAuthenticationInstance).getAppSecret(), ((UTBaseRequestAuthentication) aRequestAuthenticationInstance).isEncode() ? BuildInfoUtils.BID_WAN : "0");
        } else {
            AppMonitor.setRequestAuthInfo(true, aRequestAuthenticationInstance.getAppkey(), null, ((UTSecuritySDKRequestAuthentication) aRequestAuthenticationInstance).getAuthCode());
        }
    }

    @Deprecated
    public void setAppVersion(String aAppVersion) {
        b.a().setAppVersion(aAppVersion);
    }

    public synchronized UTTracker getTracker(String aTrackId) {
        if (!TextUtils.isEmpty(aTrackId)) {
            if (this.w.containsKey(aTrackId)) {
                return this.w.get(aTrackId);
            }
            UTTracker uTTracker = new UTTracker();
            uTTracker.q(aTrackId);
            this.w.put(aTrackId, uTTracker);
            return uTTracker;
        }
        i.a("getTracker", "TrackId is null.");
        return null;
    }

    public synchronized UTTracker getTrackerByAppkey(String appkey) {
        if (!TextUtils.isEmpty(appkey)) {
            if (this.x.containsKey(appkey)) {
                return this.x.get(appkey);
            }
            UTTracker uTTracker = new UTTracker();
            uTTracker.r(appkey);
            this.x.put(appkey, uTTracker);
            return uTTracker;
        }
        i.a("getTracker", "TrackId is null.");
        return null;
    }

    @Deprecated
    public void setChannel(String aChannel) {
        AppMonitor.setChannel(aChannel);
    }

    @Deprecated
    public void turnOnDebug() {
        b.a().turnOnDebug();
    }

    public void updateUserAccount(String aUsernick, String aUserid) {
        b.a().updateUserAccount(aUsernick, aUserid);
    }

    public void userRegister(String aUsernick) {
        if (!TextUtils.isEmpty(aUsernick)) {
            UTTracker defaultTracker = getDefaultTracker();
            if (defaultTracker != null) {
                defaultTracker.send(new UTOriginalCustomHitBuilder("UT", 1006, aUsernick, null, null, null).build());
                return;
            } else {
                i.a("Record userRegister event error", "Fatal Error,must call setRequestAuthentication method first.");
                return;
            }
        }
        i.a("userRegister", "Fatal Error,usernick can not be null or empty!");
    }

    public void updateSessionProperties(Map<String, String> aMap) {
        Map<String, String> m15a = c.a().m15a();
        HashMap hashMap = new HashMap();
        if (m15a != null) {
            hashMap.putAll(m15a);
        }
        hashMap.putAll(aMap);
        c.a().c(hashMap);
    }

    public void turnOffAutoPageTrack() {
        UTPageHitHelper.getInstance().turnOffAutoPageTrack();
    }
}

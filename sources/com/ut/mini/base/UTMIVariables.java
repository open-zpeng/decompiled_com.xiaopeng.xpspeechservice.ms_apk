package com.ut.mini.base;

import com.ut.mini.sdkevents.UTMI1010_2001Event;
/* loaded from: classes.dex */
public class UTMIVariables {
    private static UTMIVariables a = new UTMIVariables();
    private String ar = null;
    private String ao = null;
    private String as = null;

    /* renamed from: a  reason: collision with other field name */
    private UTMI1010_2001Event f157a = null;
    private boolean S = false;

    public synchronized void setToAliyunOSPlatform() {
        this.S = true;
    }

    public synchronized boolean isAliyunOSPlatform() {
        return this.S;
    }

    public synchronized void setUTMI1010_2001EventInstance(UTMI1010_2001Event aInstance) {
        this.f157a = aInstance;
    }

    public synchronized UTMI1010_2001Event getUTMI1010_2001EventInstance() {
        return this.f157a;
    }

    public static UTMIVariables getInstance() {
        return a;
    }

    public String getH5Url() {
        return this.as;
    }

    public void setH5Url(String aH5Url) {
        this.as = aH5Url;
    }

    public String getRefPage() {
        return this.ao;
    }

    public void setRefPage(String aRefPage) {
        this.ao = aRefPage;
    }

    public String getH5RefPage() {
        return this.ar;
    }

    public void setH5RefPage(String aH5PrePage) {
        this.ar = aH5PrePage;
    }
}

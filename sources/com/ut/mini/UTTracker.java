package com.ut.mini;

import android.net.Uri;
import android.text.TextUtils;
import com.alibaba.mtl.log.a;
import com.alibaba.mtl.log.d.i;
import com.alibaba.mtl.log.d.q;
import com.alibaba.mtl.log.model.LogField;
import com.ut.mini.base.UTMIVariables;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
/* loaded from: classes.dex */
public class UTTracker {
    private static Pattern a = Pattern.compile("(\\|\\||[\t\r\n])+");
    private String g;
    private String aq = null;
    private Map<String, String> D = new HashMap();

    /* JADX INFO: Access modifiers changed from: package-private */
    public void q(String str) {
        this.aq = str;
    }

    public synchronized void setGlobalProperty(String aKey, String aValue) {
        if (!TextUtils.isEmpty(aKey) && aValue != null) {
            this.D.put(aKey, aValue);
        } else {
            i.a("setGlobalProperty", "key is null or key is empty or value is null,please check it!");
        }
    }

    public synchronized String getGlobalProperty(String aKey) {
        if (aKey != null) {
            return this.D.get(aKey);
        }
        return null;
    }

    public synchronized void removeGlobalProperty(String aKey) {
        if (aKey != null) {
            if (this.D.containsKey(aKey)) {
                this.D.remove(aKey);
            }
        }
    }

    private static String d(String str) {
        if (str != null && !"".equals(str)) {
            return a.matcher(str).replaceAll("");
        }
        return str;
    }

    private static String b(String str) {
        return d(str);
    }

    private Map<String, String> g(Map<String, String> map) {
        if (map != null) {
            HashMap hashMap = new HashMap();
            Iterator<String> it = map.keySet().iterator();
            if (it != null) {
                while (it.hasNext()) {
                    String next = it.next();
                    if (next != null) {
                        hashMap.put(next, b(map.get(next)));
                    }
                }
            }
            return hashMap;
        }
        return null;
    }

    public void send(Map<String, String> aLogMap) {
        if (aLogMap != null) {
            HashMap hashMap = new HashMap();
            hashMap.putAll(this.D);
            hashMap.putAll(aLogMap);
            if (!TextUtils.isEmpty(this.g)) {
                hashMap.put(LogField.APPKEY.toString(), this.g);
            }
            Map<String, String> g = g((Map<String, String>) hashMap);
            if (!TextUtils.isEmpty(this.aq)) {
                g.put(UTFields.TRACK_ID, this.aq);
            }
            UTMIVariables.getInstance().isAliyunOSPlatform();
            f(g);
            d(g);
            m73g(g);
            h(g);
            a.a(g.remove(LogField.PAGE.toString()), g.remove(LogField.EVENTID.toString()), g.remove(LogField.ARG1.toString()), g.remove(LogField.ARG2.toString()), g.remove(LogField.ARG3.toString()), g);
        }
    }

    private static void f(Map<String, String> map) {
        if (map != null) {
            if (map.containsKey(LogField.IMEI.toString())) {
                map.remove(LogField.IMEI.toString());
            }
            if (map.containsKey(LogField.IMSI.toString())) {
                map.remove(LogField.IMSI.toString());
            }
            if (map.containsKey(LogField.CARRIER.toString())) {
                map.remove(LogField.CARRIER.toString());
            }
            if (map.containsKey(LogField.ACCESS.toString())) {
                map.remove(LogField.ACCESS.toString());
            }
            if (map.containsKey(LogField.ACCESS_SUBTYPE.toString())) {
                map.remove(LogField.ACCESS_SUBTYPE.toString());
            }
            if (map.containsKey(LogField.CHANNEL.toString())) {
                map.remove(LogField.CHANNEL.toString());
            }
            if (map.containsKey(LogField.LL_USERNICK.toString())) {
                map.remove(LogField.LL_USERNICK.toString());
            }
            if (map.containsKey(LogField.USERNICK.toString())) {
                map.remove(LogField.USERNICK.toString());
            }
            if (map.containsKey(LogField.LL_USERID.toString())) {
                map.remove(LogField.LL_USERID.toString());
            }
            if (map.containsKey(LogField.USERID.toString())) {
                map.remove(LogField.USERID.toString());
            }
            if (map.containsKey(LogField.SDKVERSION.toString())) {
                map.remove(LogField.SDKVERSION.toString());
            }
            if (map.containsKey(LogField.START_SESSION_TIMESTAMP.toString())) {
                map.remove(LogField.START_SESSION_TIMESTAMP.toString());
            }
            if (map.containsKey(LogField.UTDID.toString())) {
                map.remove(LogField.UTDID.toString());
            }
            if (map.containsKey(LogField.SDKTYPE.toString())) {
                map.remove(LogField.SDKTYPE.toString());
            }
            if (map.containsKey(LogField.RESERVE2.toString())) {
                map.remove(LogField.RESERVE2.toString());
            }
            if (map.containsKey(LogField.RESERVE3.toString())) {
                map.remove(LogField.RESERVE3.toString());
            }
            if (map.containsKey(LogField.RESERVE4.toString())) {
                map.remove(LogField.RESERVE4.toString());
            }
            if (map.containsKey(LogField.RESERVE5.toString())) {
                map.remove(LogField.RESERVE5.toString());
            }
            if (map.containsKey(LogField.RESERVES.toString())) {
                map.remove(LogField.RESERVES.toString());
            }
            if (map.containsKey(LogField.RECORD_TIMESTAMP.toString())) {
                map.remove(LogField.RECORD_TIMESTAMP.toString());
            }
        }
    }

    private static void d(Map<String, String> map) {
        if (map != null) {
            if (map.containsKey(UTFields.OS)) {
                map.remove(UTFields.OS);
                map.put(LogField.OS.toString(), map.get(UTFields.OS));
            }
            if (map.containsKey(UTFields.OS_VERSION)) {
                map.remove(UTFields.OS_VERSION);
                map.put(LogField.OSVERSION.toString(), map.get(UTFields.OS_VERSION));
            }
        }
    }

    /* renamed from: g  reason: collision with other method in class */
    private static void m73g(Map<String, String> map) {
        map.put(LogField.SDKTYPE.toString(), "mini");
    }

    private static void h(Map<String, String> map) {
        HashMap hashMap = new HashMap();
        if (map.containsKey(UTFields.TRACK_ID)) {
            String str = map.get(UTFields.TRACK_ID);
            map.remove(UTFields.TRACK_ID);
            if (!TextUtils.isEmpty(str)) {
                hashMap.put("_tkid", str);
            }
        }
        if (hashMap.size() > 0) {
            map.put(LogField.RESERVES.toString(), q.d(hashMap));
        }
        if (!map.containsKey(LogField.PAGE.toString())) {
            map.put(LogField.PAGE.toString(), "UT");
        }
    }

    public void pageAppear(Object aPageObject) {
        UTPageHitHelper.getInstance().pageAppear(aPageObject);
    }

    public void pageAppearDonotSkip(Object aPageObject) {
        UTPageHitHelper.getInstance().a(aPageObject, null, true);
    }

    public void pageAppearDonotSkip(Object aPageObject, String aCustomPageName) {
        UTPageHitHelper.getInstance().a(aPageObject, aCustomPageName, true);
    }

    public void pageAppear(Object aPageObject, String aCustomPageName) {
        UTPageHitHelper.getInstance().pageAppear(aPageObject, aCustomPageName);
    }

    public void pageDisAppear(Object aPageObject) {
        UTPageHitHelper.getInstance().pageDisAppear(aPageObject);
    }

    public void updateNextPageProperties(Map<String, String> aProperties) {
        UTPageHitHelper.getInstance().updateNextPageProperties(aProperties);
    }

    public void updatePageName(Object aPageObject, String aPageName) {
        UTPageHitHelper.getInstance().updatePageName(aPageObject, aPageName);
    }

    public void updatePageProperties(Object aPageObject, Map<String, String> aProperties) {
        UTPageHitHelper.getInstance().updatePageProperties(aPageObject, aProperties);
    }

    public void updatePageStatus(Object aPageObject, UTPageStatus aPageStatus) {
        UTPageHitHelper.getInstance().updatePageStatus(aPageObject, aPageStatus);
    }

    public void updatePageUrl(Object aPageObject, Uri aUri) {
        UTPageHitHelper.getInstance().updatePageUrl(aPageObject, aUri);
    }

    public void skipPage(Object aPageObject) {
        UTPageHitHelper.getInstance().skipPage(aPageObject);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void r(String str) {
        this.g = str;
    }
}

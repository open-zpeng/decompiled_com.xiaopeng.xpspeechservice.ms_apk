package com.ut.mini;

import android.app.Activity;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.mtl.log.c;
import com.alibaba.mtl.log.d.i;
import com.lzy.okgo.model.Progress;
import com.ut.mini.UTHitBuilders;
import com.ut.mini.base.UTMIVariables;
import com.xiaopeng.lib.security.xmartv1.XmartV1Constants;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
/* loaded from: classes.dex */
public class UTPageHitHelper {
    private static UTPageHitHelper a = new UTPageHitHelper();
    private boolean O = false;
    private Map<String, String> z = new HashMap();
    private Map<String, UTPageEventObject> A = new HashMap();
    private String al = null;
    private Map<String, String> B = new HashMap();
    private String am = null;

    /* renamed from: a  reason: collision with other field name */
    private Queue<UTPageEventObject> f155a = new LinkedList();
    private Map<Object, String> C = new HashMap();

    /* loaded from: classes.dex */
    public static class UTPageEventObject {
        private Map<String, String> z = new HashMap();
        private long A = 0;
        private Uri a = null;
        private String an = null;
        private String ao = null;

        /* renamed from: a  reason: collision with other field name */
        private UTPageStatus f156a = null;
        private boolean P = false;
        private boolean Q = false;
        private boolean R = false;
        private String ap = null;

        public void setCacheKey(String aCacheKey) {
            this.ap = aCacheKey;
        }

        public String getCacheKey() {
            return this.ap;
        }

        public void resetPropertiesWithoutSkipFlagAndH5Flag() {
            this.z = new HashMap();
            this.A = 0L;
            this.a = null;
            this.an = null;
            this.ao = null;
            UTPageStatus uTPageStatus = this.f156a;
            if (uTPageStatus == null || uTPageStatus != UTPageStatus.UT_H5_IN_WebView) {
                this.f156a = null;
            }
            this.P = false;
            this.R = false;
        }

        public boolean isH5Called() {
            return this.R;
        }

        public void setH5Called() {
            this.R = true;
        }

        public void setToSkipPage() {
            this.Q = true;
        }

        public boolean isSkipPage() {
            return this.Q;
        }

        public void setPageAppearCalled() {
            this.P = true;
        }

        public boolean isPageAppearCalled() {
            return this.P;
        }

        public void setPageStatus(UTPageStatus aPageStatus) {
            this.f156a = aPageStatus;
        }

        public UTPageStatus getPageStatus() {
            return this.f156a;
        }

        public Map<String, String> getPageProperties() {
            return this.z;
        }

        public void setPageProperties(Map<String, String> aPageProperties) {
            this.z = aPageProperties;
        }

        public long getPageStayTimstamp() {
            return this.A;
        }

        public void setPageStayTimstamp(long aPageStayTimstamp) {
            this.A = aPageStayTimstamp;
        }

        public Uri getPageUrl() {
            return this.a;
        }

        public void setPageUrl(Uri aPageUrl) {
            this.a = aPageUrl;
        }

        public void setPageName(String aPageName) {
            this.an = aPageName;
        }

        public String getPageName() {
            return this.an;
        }

        public void setRefPage(String aRefPage) {
            this.ao = aRefPage;
        }

        public String getRefPage() {
            return this.ao;
        }
    }

    public static UTPageHitHelper getInstance() {
        return a;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized Map<String, String> c() {
        if (this.B != null && this.B.size() > 0) {
            HashMap hashMap = new HashMap();
            hashMap.putAll(this.B);
            this.B.clear();
            return hashMap;
        }
        return null;
    }

    synchronized void a(UTPageEventObject uTPageEventObject) {
        uTPageEventObject.resetPropertiesWithoutSkipFlagAndH5Flag();
        if (!this.f155a.contains(uTPageEventObject)) {
            this.f155a.add(uTPageEventObject);
        }
        if (this.f155a.size() > 200) {
            for (int i = 0; i < 100; i++) {
                UTPageEventObject poll = this.f155a.poll();
                if (poll != null && this.A.containsKey(poll.getCacheKey())) {
                    this.A.remove(poll.getCacheKey());
                }
            }
        }
    }

    @Deprecated
    public synchronized void turnOffAutoPageTrack() {
        this.O = true;
    }

    public String getCurrentPageName() {
        return this.am;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void pageAppearByAuto(Activity aActivity) {
        if (this.O) {
            return;
        }
        pageAppear(aActivity);
    }

    /* renamed from: a  reason: collision with other method in class */
    private String m69a(Object obj) {
        String simpleName;
        if (obj instanceof String) {
            simpleName = (String) obj;
        } else {
            simpleName = obj.getClass().getSimpleName();
        }
        int hashCode = obj.hashCode();
        return simpleName + hashCode;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: a  reason: collision with other method in class */
    public synchronized boolean m72a(Object obj) {
        if (obj != null) {
            UTPageEventObject a2 = a(obj);
            if (a2.getPageStatus() != null) {
                if (a2.getPageStatus() == UTPageStatus.UT_H5_IN_WebView) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: a  reason: collision with other method in class */
    public synchronized void m71a(Object obj) {
        if (obj != null) {
            UTPageEventObject a2 = a(obj);
            if (a2.getPageStatus() != null) {
                a2.setH5Called();
            }
        }
    }

    private synchronized UTPageEventObject a(Object obj) {
        String m69a = m69a(obj);
        if (this.A.containsKey(m69a)) {
            return this.A.get(m69a);
        }
        UTPageEventObject uTPageEventObject = new UTPageEventObject();
        this.A.put(m69a, uTPageEventObject);
        uTPageEventObject.setCacheKey(m69a);
        return uTPageEventObject;
    }

    private synchronized void a(String str, UTPageEventObject uTPageEventObject) {
        this.A.put(str, uTPageEventObject);
    }

    private synchronized void b(UTPageEventObject uTPageEventObject) {
        if (this.A.containsKey(uTPageEventObject.getCacheKey())) {
            this.A.remove(uTPageEventObject.getCacheKey());
        }
    }

    /* renamed from: b  reason: collision with other method in class */
    private synchronized void m70b(Object obj) {
        String m69a = m69a(obj);
        if (this.A.containsKey(m69a)) {
            this.A.remove(m69a);
        }
    }

    @Deprecated
    public synchronized void pageAppear(Object aPageObject) {
        a(aPageObject, null, false);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void a(Object obj, String str, boolean z) {
        if (obj != null) {
            String m69a = m69a(obj);
            if (m69a != null && m69a.equals(this.al)) {
                return;
            }
            if (this.al != null) {
                i.a("lost 2001", "Last page requires leave(" + this.al + ").");
            }
            UTPageEventObject a2 = a(obj);
            if (!z && a2.isSkipPage()) {
                i.a("skip page[pageAppear]", "page name:" + obj.getClass().getSimpleName());
                return;
            }
            String h5Url = UTMIVariables.getInstance().getH5Url();
            if (h5Url != null) {
                this.z.put("spm", Uri.parse(h5Url).getQueryParameter("spm"));
                UTMIVariables.getInstance().setH5Url(null);
            }
            String b = b(obj);
            if (TextUtils.isEmpty(str)) {
                str = b;
            }
            if (!TextUtils.isEmpty(a2.getPageName())) {
                str = a2.getPageName();
            }
            this.am = str;
            a2.setPageName(str);
            a2.setPageStayTimstamp(SystemClock.elapsedRealtime());
            a2.setRefPage(UTMIVariables.getInstance().getRefPage());
            a2.setPageAppearCalled();
            if (this.B != null) {
                Map<String, String> pageProperties = a2.getPageProperties();
                if (pageProperties == null) {
                    a2.setPageProperties(this.B);
                } else {
                    HashMap hashMap = new HashMap();
                    hashMap.putAll(pageProperties);
                    hashMap.putAll(this.B);
                    a2.setPageProperties(hashMap);
                }
            }
            this.B = null;
            this.al = m69a(obj);
            b(a2);
            a(m69a(obj), a2);
        } else {
            i.a("pageAppear", "The page object should not be null");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void pageAppear(Object aPageObject, String aCustomPageName) {
        a(aPageObject, aCustomPageName, false);
    }

    @Deprecated
    public synchronized void updatePageProperties(Map<String, String> aProperties) {
        if (aProperties != null) {
            this.z.putAll(aProperties);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void updatePageProperties(Object aPageObject, Map<String, String> aProperties) {
        if (aPageObject != null && aProperties != null) {
            if (aProperties.size() != 0) {
                HashMap hashMap = new HashMap();
                hashMap.putAll(aProperties);
                UTPageEventObject a2 = a(aPageObject);
                Map<String, String> pageProperties = a2.getPageProperties();
                if (pageProperties == null) {
                    a2.setPageProperties(hashMap);
                } else {
                    HashMap hashMap2 = new HashMap();
                    hashMap2.putAll(pageProperties);
                    hashMap2.putAll(hashMap);
                    a2.setPageProperties(hashMap2);
                }
                return;
            }
        }
        i.a("updatePageProperties", "failed to update project, parameters should not be null and the map should not be empty");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void updatePageName(Object aPageObject, String aPageName) {
        if (aPageObject != null) {
            if (!TextUtils.isEmpty(aPageName)) {
                a(aPageObject).setPageName(aPageName);
                this.am = aPageName;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void updatePageUrl(Object aPageObject, Uri aUrl) {
        if (aPageObject == null || aUrl == null) {
            return;
        }
        Log.i(Progress.URL, Progress.URL + aUrl.toString());
        a(aPageObject).setPageUrl(aUrl);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void updatePageStatus(Object aPageObject, UTPageStatus aPageStatus) {
        if (aPageObject == null || aPageStatus == null) {
            return;
        }
        a(aPageObject).setPageStatus(aPageStatus);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void updateNextPageProperties(Map<String, String> aProperties) {
        if (aProperties != null) {
            HashMap hashMap = new HashMap();
            hashMap.putAll(aProperties);
            this.B = hashMap;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void pageDisAppearByAuto(Activity aActivity) {
        if (this.O) {
            return;
        }
        pageDisAppear(aActivity);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void skipPage(Object aPageObject) {
        if (aPageObject == null) {
            return;
        }
        a(aPageObject).setToSkipPage();
    }

    @Deprecated
    public synchronized void pageDisAppear(Object aPageObject) {
        if (aPageObject != null) {
            if (this.al == null) {
                return;
            }
            UTPageEventObject a2 = a(aPageObject);
            if (a2.isPageAppearCalled()) {
                if (a2.getPageStatus() != null && UTPageStatus.UT_H5_IN_WebView == a2.getPageStatus() && a2.isH5Called()) {
                    a(a2);
                    return;
                }
                long elapsedRealtime = SystemClock.elapsedRealtime() - a2.getPageStayTimstamp();
                if (a2.getPageUrl() == null && (aPageObject instanceof Activity) && ((Activity) aPageObject).getIntent() != null) {
                    a2.setPageUrl(((Activity) aPageObject).getIntent().getData());
                }
                String pageName = a2.getPageName();
                String refPage = a2.getRefPage();
                refPage = (refPage == null || refPage.length() == 0) ? "-" : "-";
                Map<String, String> map = this.z;
                if (map == null) {
                    map = new HashMap<>();
                }
                if (a2.getPageProperties() != null) {
                    map.putAll(a2.getPageProperties());
                }
                if (aPageObject instanceof IUTPageTrack) {
                    IUTPageTrack iUTPageTrack = (IUTPageTrack) aPageObject;
                    String referPage = iUTPageTrack.getReferPage();
                    if (!TextUtils.isEmpty(referPage)) {
                        refPage = referPage;
                    }
                    Map<String, String> pageProperties = iUTPageTrack.getPageProperties();
                    if (pageProperties != null && pageProperties.size() > 0) {
                        this.z.putAll(pageProperties);
                        map = this.z;
                    }
                    String pageName2 = iUTPageTrack.getPageName();
                    if (!TextUtils.isEmpty(pageName2)) {
                        pageName = pageName2;
                    }
                }
                Uri pageUrl = a2.getPageUrl();
                if (pageUrl != null) {
                    HashMap hashMap = new HashMap();
                    String queryParameter = pageUrl.getQueryParameter("spm");
                    if (TextUtils.isEmpty(queryParameter)) {
                        try {
                            pageUrl = Uri.parse(URLDecoder.decode(pageUrl.toString(), XmartV1Constants.UTF8_ENCODING));
                            queryParameter = pageUrl.getQueryParameter("spm");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!TextUtils.isEmpty(queryParameter)) {
                        boolean z = false;
                        if (this.C.containsKey(aPageObject) && queryParameter.equals(this.C.get(aPageObject))) {
                            z = true;
                        }
                        if (!z) {
                            hashMap.put("spm", queryParameter);
                            this.C.put(aPageObject, queryParameter);
                        }
                    }
                    String queryParameter2 = pageUrl.getQueryParameter("scm");
                    if (!TextUtils.isEmpty(queryParameter2)) {
                        hashMap.put("scm", queryParameter2);
                    }
                    String a3 = a(pageUrl);
                    if (!TextUtils.isEmpty(a3)) {
                        c.a().e(a3);
                    }
                    if (hashMap.size() > 0) {
                        map.putAll(hashMap);
                    }
                }
                UTHitBuilders.UTPageHitBuilder uTPageHitBuilder = new UTHitBuilders.UTPageHitBuilder(pageName);
                uTPageHitBuilder.setReferPage(refPage).setDurationOnPage(elapsedRealtime).setProperties(map);
                UTMIVariables.getInstance().setRefPage(pageName);
                UTTracker defaultTracker = UTAnalytics.getInstance().getDefaultTracker();
                if (defaultTracker != null) {
                    defaultTracker.send(uTPageHitBuilder.build());
                } else {
                    i.a("Record page event error", "Fatal Error,must call setRequestAuthentication method first.");
                }
            } else {
                i.a("UT", "Please call pageAppear first(" + b(aPageObject) + ").");
            }
            this.z = new HashMap();
            if (a2.isSkipPage()) {
                a(a2);
            } else if (a2.getPageStatus() != null && UTPageStatus.UT_H5_IN_WebView == a2.getPageStatus()) {
                a(a2);
            } else {
                m70b(aPageObject);
            }
            this.al = null;
            this.am = null;
        } else {
            i.a("pageDisAppear", "The page object should not be null");
        }
    }

    private static String a(Uri uri) {
        List<String> queryParameters;
        if (uri != null && (queryParameters = uri.getQueryParameters("ttid")) != null) {
            for (String str : queryParameters) {
                if (!str.contains("@") && !str.contains("%40")) {
                    return str;
                }
            }
            return null;
        }
        return null;
    }

    private static String b(Object obj) {
        String simpleName = obj.getClass().getSimpleName();
        if (simpleName.toLowerCase().endsWith("activity")) {
            return simpleName.substring(0, simpleName.length() - 8);
        }
        return simpleName;
    }
}

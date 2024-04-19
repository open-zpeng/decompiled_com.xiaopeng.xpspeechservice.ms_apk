package com.ut.mini.internal;
/* loaded from: classes.dex */
public class CustomDNS {
    private IDnsResolver a;

    /* loaded from: classes.dex */
    public interface IDnsResolver {
        String[] resolveUrl(String str);
    }

    public static CustomDNS instance() {
        return a.a;
    }

    private CustomDNS() {
        this.a = null;
    }

    public void setDnsResolver(IDnsResolver resolver) {
        this.a = resolver;
    }

    public String[] resolveUrl(String url) {
        IDnsResolver iDnsResolver = this.a;
        if (iDnsResolver != null) {
            return iDnsResolver.resolveUrl(url);
        }
        return null;
    }

    /* loaded from: classes.dex */
    private static class a {
        private static final CustomDNS a = new CustomDNS();
    }
}

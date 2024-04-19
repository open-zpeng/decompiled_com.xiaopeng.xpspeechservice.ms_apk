package com.ta.utdid2.a;

import android.content.Context;
import android.util.Log;
import com.ta.utdid2.b.a.f;
import com.ta.utdid2.b.a.i;
import com.ta.utdid2.b.a.j;
import com.xiaopeng.lib.framework.moduleinterface.appresourcemodule.IAppResourceException;
/* compiled from: AidManager.java */
/* loaded from: classes.dex */
public class a {
    private Context mContext;
    private static a a = null;
    private static final String TAG = a.class.getName();

    public static synchronized a a(Context context) {
        a aVar;
        synchronized (a.class) {
            if (a == null) {
                a = new a(context);
            }
            aVar = a;
        }
        return aVar;
    }

    private a(Context context) {
        this.mContext = context;
    }

    public void a(String str, String str2, String str3, com.ut.device.a aVar) {
        if (aVar == null) {
            Log.e(TAG, "callback is null!");
        } else if (this.mContext == null || i.m61a(str) || i.m61a(str2)) {
            String str4 = TAG;
            StringBuilder sb = new StringBuilder("mContext:");
            sb.append(this.mContext);
            sb.append("; callback:");
            sb.append(aVar);
            sb.append("; has appName:");
            sb.append(!i.m61a(str));
            sb.append("; has token:");
            sb.append(!i.m61a(str2));
            Log.e(str4, sb.toString());
            aVar.a(1002, "");
        } else {
            String m58a = c.m58a(this.mContext, str, str2);
            if (!i.m61a(m58a) && j.a(c.a(this.mContext, str, str2), 1)) {
                aVar.a(1001, m58a);
            } else if (f.m60a(this.mContext)) {
                b.a(this.mContext).a(str, str2, str3, m58a, aVar);
            } else {
                aVar.a(IAppResourceException.REASON_PARAM_ERROR, m58a);
            }
        }
    }

    public String a(String str, String str2, String str3) {
        if (this.mContext == null || i.m61a(str) || i.m61a(str2)) {
            String str4 = TAG;
            StringBuilder sb = new StringBuilder("mContext:");
            sb.append(this.mContext);
            sb.append("; has appName:");
            sb.append(!i.m61a(str));
            sb.append("; has token:");
            sb.append(!i.m61a(str2));
            Log.e(str4, sb.toString());
            return "";
        }
        String m58a = c.m58a(this.mContext, str, str2);
        if (!i.m61a(m58a) && j.a(c.a(this.mContext, str, str2), 1)) {
            return m58a;
        }
        if (f.m60a(this.mContext)) {
            return b(str, str2, str3);
        }
        return m58a;
    }

    private synchronized String b(String str, String str2, String str3) {
        if (this.mContext == null) {
            Log.e(TAG, "no context!");
            return "";
        }
        String str4 = "";
        if (f.m60a(this.mContext)) {
            str4 = b.a(this.mContext).a(str, str2, str3, c.m58a(this.mContext, str, str2));
        }
        c.a(this.mContext, str, str4, str2);
        return str4;
    }
}

package com.xiaopeng.lib.http;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Priority;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.http.logging.HttpLoggingInterceptor;
import com.xiaopeng.lib.http.server.ServerBean;
import com.xiaopeng.lib.http.tls.HostNameVerifier;
import com.xiaopeng.lib.http.tls.SSLHelper;
import com.xiaopeng.lib.security.SecurityCommon;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import com.xiaopeng.system.delegate.module.SystemDelegateModuleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
/* loaded from: classes.dex */
public class HttpsUtils {
    private static final String ACTION_BROADCAST_CLIENT_SSL_UPDATE = "com.xiaopeng.action.CLIENT_SSL_UPDATE";
    private static final String TAG = "HttpsUtils";
    private static boolean sIsInitialized = false;

    @Deprecated
    /* loaded from: classes.dex */
    public interface ISSLSocketFactoryWrapper {
        SSLSocketFactory createFactoryWrapper(SSLSocketFactory sSLSocketFactory);
    }

    public static SSLSocketFactory getTLS2SocketFactory(Context context) {
        return SSLHelper.getTLS2SocketFactory(context);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static OkHttpClient buildOkHttpClient(Context context) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(TAG);
        loggingInterceptor.setPrintLevel((BuildInfoUtils.isEngVersion() || Build.TYPE.equals("userdebug")) ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC);
        loggingInterceptor.setColorLevel(Level.INFO);
        return new OkHttpClient.Builder().sslSocketFactory(SSLHelper.getTLS2SocketFactory(context), SSLHelper.getX509TrustManager(context)).connectionSpecs(SSLHelper.getConnectionSpecs()).connectionPool(new ConnectionPool()).addInterceptor(loggingInterceptor).readTimeout(30L, TimeUnit.SECONDS).hostnameVerifier(HostNameVerifier.INSTANCE).build();
    }

    public static synchronized boolean init(Application application, boolean needSecurity) {
        synchronized (HttpsUtils.class) {
            if (sIsInitialized) {
                return true;
            }
            SecurityCommon.checkSystemUid(application);
            if (needSecurity && !Security.isInitSuccess()) {
                Security.init(application);
                if (!Security.isInitSuccessWithoutIndiv()) {
                    LogUtils.w(TAG, "!Security.isInitSuccessWithoutIndiv, return false!");
                    return sIsInitialized;
                }
            }
            Module.register(SystemDelegateModuleEntry.class, new SystemDelegateModuleEntry(application));
            initOkHttpClient(application);
            if (SSLHelper.getX509TrustManager(application) == null) {
                LogUtils.w(TAG, "KeyManager init fail, return false!");
                return sIsInitialized;
            }
            sIsInitialized = true;
            return sIsInitialized;
        }
    }

    public static boolean isInitSuccess() {
        return sIsInitialized;
    }

    private static void initOkHttpClient(Application application) {
        LogUtils.d(TAG, "start to initOkHttpClient!!!");
        registerBroadcast(application);
        OkHttpClient okHttpClient = buildOkHttpClient(application);
        OkGo.getInstance().init(application).setOkHttpClient(okHttpClient);
        LogUtils.d(TAG, "finish initOkHttpClient!!!");
    }

    private static void registerBroadcast(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.xiaopeng.action.CLIENT_SSL_UPDATE");
        filter.setPriority(Priority.UI_TOP);
        context.registerReceiver(new BroadcastReceiver() { // from class: com.xiaopeng.lib.http.HttpsUtils.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                Log.e(HttpsUtils.TAG, "ACTION_BROADCAST_CLIENT_SSL_UPDATE");
                try {
                    OkHttpClient okHttpClient = HttpsUtils.buildOkHttpClient(context2);
                    OkGo.getInstance().setOkHttpClient(okHttpClient);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, filter);
    }

    public static OkHttpClient getOkhttpClient() {
        return OkGo.getInstance().getOkHttpClient();
    }

    public static String buildBody(Map<String, String> param) {
        try {
            Map<String, String> post_param = new HashMap<>(1);
            post_param.put("sData", Security.encode(new Gson().toJson(param)));
            return new Gson().toJson(post_param);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String buildBodyWithToken(Map<String, String> param) {
        try {
            Map<String, String> post_param = new HashMap<>(1);
            post_param.put("sData", Security.buildTokenData(Security.TOKEN_AC, new Gson().toJson(param).getBytes()));
            return new Gson().toJson(post_param);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String buildBodyWithAllToken(Map<String, String> param) {
        try {
            Map<String, String> post_param = new HashMap<>(1);
            post_param.put("sData", Security.buildTokenData(Security.TOKEN_ALL, new Gson().toJson(param).getBytes()));
            return new Gson().toJson(post_param);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decodeBody(ServerBean bean) {
        if (bean == null || TextUtils.isEmpty(bean.getData())) {
            Log.d(TAG, "decodeBody bean is null");
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(bean.getData());
            String data = jsonObject.getString("sData");
            if (TextUtils.isEmpty(data)) {
                Log.d(TAG, "sData is null");
                return null;
            }
            return Security.decode(data);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized void destroy() {
        synchronized (HttpsUtils.class) {
            Security.destroy();
        }
    }

    @Deprecated
    public static void setSSLSocketFactoryWrapper(@NonNull ISSLSocketFactoryWrapper wrapper) {
    }

    public static void setSSLSocketWrapper(@NonNull SSLHelper.ISSLSocketWrapper wrapper) {
        SSLHelper.setSSLSocketWrapper(wrapper);
    }
}

package com.xiaopeng.lib.framework.netchannelmodule.http.xmart;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.alibaba.mtl.log.a;
import com.alibaba.mtl.log.d.b;
import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.SharedPreferencesUtils;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import okhttp3.Dns;
/* loaded from: classes.dex */
public class TimeoutDns implements Dns {
    private static final String ACCOUNT_ID = "133084";
    private static final int MEANINGFUL_MIN_TIMEOUT = 1000;
    private static final String SECRET_KEY = "2bfcc6860a1eaa5ccfc9e54c020ba66e";
    private static final String TAG = "TimeoutDns";
    private static final String XMART_HOST = "xmart.xiaopeng.com";
    private static HttpDnsService sHttpDns;
    private LookupService mLookupService;
    private static int sTimeout = 10000;
    private static int sHttpDnsTimeout = 5000;
    private static boolean sInitialized = false;

    private TimeoutDns() {
        this.mLookupService = LookupService.create();
    }

    private void checkAndInitHttpDns(Context context) {
        if (sInitialized) {
            return;
        }
        synchronized (TimeoutDns.class) {
            if (sInitialized) {
                return;
            }
            if (context == null) {
                throw new IllegalStateException("Not init GlobalConfig with applicationContext");
            }
            sHttpDns = HttpDns.getService(context, ACCOUNT_ID, SECRET_KEY);
            sHttpDns.setExpiredIPEnabled(true);
            sHttpDns.setCachedIPEnabled(true);
            sHttpDns.setLogEnabled(true);
            sHttpDns.setHTTPSRequestEnabled(true);
            sHttpDns.setPreResolveAfterNetworkChanged(true);
            ArrayList<String> preSolveHosts = new ArrayList<>();
            preSolveHosts.add(XMART_HOST);
            sHttpDns.setPreResolveHosts(preSolveHosts);
            sHttpDns.setTimeoutInterval(sHttpDnsTimeout);
            hookHttpDns();
            sInitialized = true;
        }
    }

    private void hookHttpDns() {
        String[] targetClasses = {b.class.getName(), a.class.getName()};
        for (String className : targetClasses) {
            try {
                Class clazz = Class.forName(className);
                Field[] fieldList = clazz.getDeclaredFields();
                int i = 0;
                while (true) {
                    if (i < fieldList.length) {
                        Field field = fieldList[i];
                        if (field.getType().equals(Boolean.TYPE)) {
                            field.setAccessible(true);
                            field.set(null, true);
                        } else if (field.getType().equals(Context.class)) {
                            field.setAccessible(true);
                            field.set(null, null);
                            break;
                        }
                        i++;
                    }
                }
            } catch (Exception ex) {
                LogUtils.d(TAG, "Try to disable httpdns log:" + ex);
            }
        }
    }

    public static TimeoutDns getInstance() {
        return Holder.INSTANCE;
    }

    public static void timeout(int timeout) {
        if (timeout < 1000) {
            timeout = 1000;
        }
        sTimeout = timeout;
        sHttpDnsTimeout = (timeout * 2) / 3;
    }

    public static int timeout() {
        return sTimeout;
    }

    public List<InetAddress> lookupAsync(@NonNull String hostName) throws UnknownHostException {
        try {
            checkAndInitHttpDns(GlobalConfig.getApplicationContext());
            String ip = sHttpDns.getIpByHostAsync(hostName);
            LogUtils.d(TAG, "async get the ip getByHostAsync is " + ip);
            if (!TextUtils.isEmpty(ip)) {
                return Arrays.asList(InetAddress.getAllByName(ip));
            }
            return new ArrayList();
        } catch (Exception mE) {
            LogUtils.w(TAG, "exception occurs when getIpsByHttpDns, and details: ", mE);
            throw new UnknownHostException(mE.toString());
        }
    }

    @Override // okhttp3.Dns
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        checkAndInitHttpDns(GlobalConfig.getApplicationContext());
        Future<InetAddress[]> future = this.mLookupService.getAllByName(hostname);
        try {
            return Arrays.asList(future.get(sTimeout, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            LogUtils.d(TAG, "get ip timeout");
            InetAddress[] cache = this.mLookupService.getCacheAddress(hostname);
            if (cache != null && cache.length > 0) {
                return Arrays.asList(cache);
            }
            throw new UnknownHostException();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class LookupService {
        private static final String IP_CACHE = "ipCache";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        SharedPreferencesUtils sSharePrefs;

        private LookupService() {
        }

        static LookupService create() {
            return new LookupService();
        }

        Future<InetAddress[]> getAllByName(final String host) {
            FutureTask<InetAddress[]> future = new FutureTask<>(new Callable<InetAddress[]>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.xmart.TimeoutDns.LookupService.1
                @Override // java.util.concurrent.Callable
                public InetAddress[] call() {
                    try {
                        return LookupService.this.getAddressByHttpDns(host);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
            this.executor.execute(future);
            return future;
        }

        /* JADX INFO: Access modifiers changed from: private */
        @Nullable
        public InetAddress[] getAddressByHttpDns(String mHost) {
            String ip = TimeoutDns.sHttpDns.getIpByHostAsync(mHost);
            LogUtils.d(TimeoutDns.TAG, "the ip getByHostAsync is " + ip);
            if (!TextUtils.isEmpty(ip)) {
                try {
                    LogUtils.d(TimeoutDns.TAG, "the inetAddress getByHttpDns is " + Arrays.toString(InetAddress.getAllByName(ip)));
                    InetAddress[] result = InetAddress.getAllByName(ip);
                    saveValidIp(result, mHost);
                    return result;
                } catch (UnknownHostException mE) {
                    LogUtils.w(TimeoutDns.TAG, "exception occurs when getIpsByHttpDns, and details: ", mE);
                }
            }
            try {
                InetAddress[] inetAddresses = InetAddress.getAllByName(mHost);
                LogUtils.d(TimeoutDns.TAG, "the inetAddress getByLocalDns is " + Arrays.toString(inetAddresses));
                saveValidIp(inetAddresses, mHost);
                return inetAddresses;
            } catch (UnknownHostException mE2) {
                LogUtils.w(TimeoutDns.TAG, "exception occurs when getIpsByLocalDns, and details: ", mE2);
                return getCacheAddress(mHost);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public InetAddress[] getCacheAddress(String host) {
            if (this.sSharePrefs == null) {
                this.sSharePrefs = SharedPreferencesUtils.getInstance(GlobalConfig.getApplicationContext());
            }
            SharedPreferencesUtils sharedPreferencesUtils = this.sSharePrefs;
            String cacheIp = sharedPreferencesUtils.getString(IP_CACHE + host, null);
            LogUtils.d(TimeoutDns.TAG, "get ip from cache " + cacheIp);
            if (TextUtils.isEmpty(cacheIp)) {
                return null;
            }
            try {
                return InetAddress.getAllByName(cacheIp);
            } catch (UnknownHostException e) {
                LogUtils.w(TimeoutDns.TAG, "exception occurs when get ip from cache, and details: ", e);
                return null;
            }
        }

        private void saveValidIp(InetAddress[] addresses, String host) {
            if (addresses == null || addresses.length == 0) {
                return;
            }
            if (this.sSharePrefs == null) {
                this.sSharePrefs = SharedPreferencesUtils.getInstance(GlobalConfig.getApplicationContext());
            }
            for (InetAddress adr : addresses) {
                if ((!adr.isSiteLocalAddress() && !adr.isLoopbackAddress()) || adr.getHostAddress().startsWith("10.")) {
                    String ip = adr.getHostAddress();
                    LogUtils.d(TimeoutDns.TAG, "save valid ip " + ip);
                    this.sSharePrefs.putString(IP_CACHE + host, ip);
                    return;
                }
            }
        }
    }

    /* loaded from: classes.dex */
    private static final class Holder {
        private static final TimeoutDns INSTANCE = new TimeoutDns();

        private Holder() {
        }
    }
}

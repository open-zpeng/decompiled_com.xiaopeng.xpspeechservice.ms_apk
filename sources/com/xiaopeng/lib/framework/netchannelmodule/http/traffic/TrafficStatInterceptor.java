package com.xiaopeng.lib.framework.netchannelmodule.http.traffic;

import com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpCounter;
import java.io.IOException;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
/* loaded from: classes.dex */
public final class TrafficStatInterceptor implements Interceptor {
    private TrafficStatInterceptor() {
    }

    public static TrafficStatInterceptor getInstance() {
        return Holder.INSTANCE;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        countRequestSize(request);
        String domain = request.url().host();
        try {
            Response response = chain.proceed(request);
            countResponseSize(domain, response);
            return response;
        } catch (Exception ex) {
            HttpCounter.getInstance().increaseFailureWithSize(domain, 0L);
            throw ex;
        }
    }

    private void countRequestSize(Request request) {
        long dataSize = countHeadersSize(request.headers());
        HttpCounter.getInstance().increaseRequest(request.url().host(), dataSize + request.url().toString().length() + countRequestBodySize(request));
    }

    private void countResponseSize(String domain, Response response) {
        long dataSize = countHeadersSize(response.headers()) + response.message().length();
        if (response.isSuccessful()) {
            HttpCounter.getInstance().increaseSucceedWithSize(domain, dataSize);
        } else {
            HttpCounter.getInstance().increaseFailureWithSize(domain, dataSize);
        }
    }

    private long countHeadersSize(Headers headers) {
        long size = 0;
        if (headers == null) {
            return 0L;
        }
        int count = headers.size();
        for (int i = 0; i < count; i++) {
            size = size + headers.name(i).length() + headers.value(i).length();
        }
        return size;
    }

    private long countRequestBodySize(Request request) {
        if (request.body() == null) {
            return 0L;
        }
        try {
            long size = request.body().contentLength();
            return size;
        } catch (IOException ex) {
            ex.printStackTrace();
            return 0L;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class Holder {
        private static final TrafficStatInterceptor INSTANCE = new TrafficStatInterceptor();

        private Holder() {
        }
    }
}

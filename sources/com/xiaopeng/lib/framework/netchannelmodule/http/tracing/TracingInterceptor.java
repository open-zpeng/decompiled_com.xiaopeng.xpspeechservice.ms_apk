package com.xiaopeng.lib.framework.netchannelmodule.http.tracing;

import android.text.TextUtils;
import brave.Span;
import brave.Tracer;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.NetUtils;
import com.xiaopeng.lib.utils.SystemPropertyUtil;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
/* loaded from: classes.dex */
public class TracingInterceptor implements Interceptor {
    private static final String B3_HEADER_PARENT_ID = "X-B3-ParentSpanId";
    private static final String B3_HEADER_SAMPLED = "X-B3-Sampled";
    private static final String B3_HEADER_SPAN_ID = "X-B3-SpanId";
    private static final String B3_HEADER_TRACE_ID = "X-B3-TraceId";
    private static final String HOST_XIAOPENG_LAN = "10.0.13.28";
    private static final String HOST_XIAOPENG_WAN = "xiaopeng.com";
    private static final String RESPONSE_HEADER_SAMPLE_RATE = "logan-sample-rates";
    private static final String TAG = "Tracer";
    private static final String TAG_KEY_CDUID = "car.cdu_id";
    private static final String TAG_KEY_HOST = "http.host";
    private static final String TAG_KEY_METHOD = "http.method";
    private static final String TAG_KEY_NET = "car.net";
    private static final String TAG_KEY_PATH = "http.path";
    private static final String TAG_KEY_SID = "car.sid";
    private static final String TAG_KEY_STATUS = "http.status_code";
    private static final String TAG_KEY_URL = "http.url";
    private static final String TAG_KEY_VID = "car.vid";
    private static final String TAG_KEY_VIN = "car.vin";

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Span rootSpan;
        Response response;
        int responseCode;
        Request request = chain.request();
        HttpUrl url = request.url();
        if (!isXiaopengHost(url.host())) {
            LogUtils.d(TAG, "isXiaopengHost(url.host()) == false, host:" + url.host());
            return chain.proceed(request);
        }
        String urlStr = url.toString();
        String operationName = urlStr.substring(urlStr.lastIndexOf("/") + 1);
        Tracer tracer = TracingHolder.getInstance().getTracer();
        Span rootSpan2 = tracer.newTrace().start();
        tracer.withSpanInScope(rootSpan2);
        Span requestSpan = tracer.nextSpan();
        String traceId = Long.toHexString(rootSpan2.context().traceId());
        String parentId = Long.toHexString(rootSpan2.context().spanId());
        String spanId = Long.toHexString(requestSpan.context().spanId());
        Boolean sampled = rootSpan2.context().sampled();
        LogUtils.d(TAG, "Tracing intercept operationName:" + operationName + " traceId:" + traceId + " spanId:" + spanId + " sampled:" + sampled);
        Request newRequest = request.newBuilder().addHeader(B3_HEADER_TRACE_ID, String.valueOf(traceId)).addHeader(B3_HEADER_SPAN_ID, String.valueOf(spanId)).addHeader(B3_HEADER_SAMPLED, String.valueOf(sampled)).addHeader(B3_HEADER_PARENT_ID, String.valueOf(parentId)).build();
        try {
            response = chain.proceed(newRequest);
            String sampleRate = response.header(RESPONSE_HEADER_SAMPLE_RATE);
            TracingHolder.getInstance().updateSampleRate(sampleRate);
            responseCode = response.code();
            rootSpan = rootSpan2;
        } catch (Exception e) {
            e = e;
            rootSpan = rootSpan2;
        }
        try {
            decorateAndFinishSpan(requestSpan, operationName, request.method(), url, responseCode, null);
            decorateAndFinishSpan(rootSpan, operationName, request.method(), url, responseCode, null);
            LogUtils.d(TAG, "Tracing intercept finish, code:" + responseCode);
            return response;
        } catch (Exception e2) {
            e = e2;
            Exception exc = e;
            decorateAndFinishSpan(requestSpan, operationName, request.method(), url, -1, exc);
            decorateAndFinishSpan(rootSpan, operationName, request.method(), url, -1, exc);
            LogUtils.d(TAG, "Tracing intercept finish, code:-1");
            throw e;
        }
    }

    private boolean isXiaopengHost(String host) {
        if (TextUtils.isEmpty(host)) {
            return false;
        }
        if (BuildInfoUtils.isLanVersion()) {
            return host.contains(HOST_XIAOPENG_LAN);
        }
        return host.contains(HOST_XIAOPENG_WAN);
    }

    private void decorateAndFinishSpan(Span span, String operationName, String method, HttpUrl url, int responseCode, Exception error) {
        span.name(operationName).kind(Span.Kind.CLIENT).tag(TAG_KEY_HOST, url.host()).tag(TAG_KEY_METHOD, method).tag(TAG_KEY_PATH, url.encodedPath()).tag(TAG_KEY_URL, url.toString()).tag(TAG_KEY_STATUS, String.valueOf(responseCode)).tag(TAG_KEY_VID, String.valueOf(SystemPropertyUtil.getVehicleId())).tag(TAG_KEY_VIN, SystemPropertyUtil.getVIN()).tag(TAG_KEY_CDUID, SystemPropertyUtil.getHardwareId()).tag(TAG_KEY_SID, SystemPropertyUtil.getSoftwareId()).tag(TAG_KEY_NET, String.valueOf(NetUtils.getNetworkType(GlobalConfig.getApplicationContext()))).error(error).finish();
    }
}

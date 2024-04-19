package com.xiaopeng.lib.http.logging;

import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.utils.IOUtils;
import com.xiaopeng.lib.http.logging.LogCacher;
import com.xiaopeng.lib.security.xmartv1.XmartV1Constants;
import com.xiaopeng.lib.utils.LogUtils;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
/* loaded from: classes.dex */
public class HttpLoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName(XmartV1Constants.UTF8_ENCODING);
    private java.util.logging.Level colorLevel;
    private final String tag;
    private volatile Level printLevel = Level.NONE;
    private LogCacher cacher = new LogCacher();

    /* loaded from: classes.dex */
    public enum Level {
        NONE,
        BASIC,
        HEADERS,
        BODY
    }

    public HttpLoggingInterceptor(String tag) {
        this.tag = tag;
    }

    public void setPrintLevel(Level level) {
        if (this.printLevel == null) {
            throw new NullPointerException("printLevel == null. Use Level.NONE instead.");
        }
        this.printLevel = level;
    }

    public void setColorLevel(java.util.logging.Level level) {
        this.colorLevel = level;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        if (this.printLevel == Level.NONE) {
            return chain.proceed(request);
        }
        LogCacher.ILog log = this.cacher.getLogger();
        logForRequest(request, chain.connection(), log);
        long startNs = System.nanoTime();
        try {
            Response response = chain.proceed(request);
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            Response r = logForResponse(response, tookMs, log);
            log.end();
            return r;
        } catch (Exception e) {
            log.log("<-- HTTP FAILED: " + e);
            throw e;
        }
    }

    private void logForRequest(Request request, Connection connection, LogCacher.ILog log) throws IOException {
        StringBuilder sb;
        boolean logBody = this.printLevel == Level.BODY;
        boolean logHeaders = this.printLevel == Level.BODY || this.printLevel == Level.HEADERS;
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        try {
            try {
                String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
                log.log(requestStartMessage);
                if (logHeaders) {
                    if (hasRequestBody) {
                        if (requestBody.contentType() != null) {
                            log.log("\tContent-Type: " + requestBody.contentType());
                        }
                        if (requestBody.contentLength() != -1) {
                            log.log("\tContent-Length: " + requestBody.contentLength());
                        }
                    }
                    Headers headers = request.headers();
                    int count = headers.size();
                    for (int i = 0; i < count; i++) {
                        String name = headers.name(i);
                        if (!"Content-Type".equalsIgnoreCase(name) && !HttpHeaders.HEAD_KEY_CONTENT_LENGTH.equalsIgnoreCase(name)) {
                            log.log("\t" + name + ": " + headers.value(i));
                        }
                    }
                    log.log(" ");
                    if (logBody && hasRequestBody) {
                        try {
                            if (isPlaintext(requestBody.contentType())) {
                                bodyToString(request, log);
                            } else {
                                log.log("\tbody: maybe [binary body], omitted!");
                            }
                        } catch (Exception e) {
                            e = e;
                            LogUtils.w(this.tag, "logForRequest error!", e);
                            sb = new StringBuilder();
                            sb.append("--> END ");
                            sb.append(request.method());
                            log.log(sb.toString());
                        }
                    }
                }
                sb = new StringBuilder();
            } catch (Throwable th) {
                th = th;
                log.log("--> END " + request.method());
                throw th;
            }
        } catch (Exception e2) {
            e = e2;
        } catch (Throwable th2) {
            th = th2;
            log.log("--> END " + request.method());
            throw th;
        }
        sb.append("--> END ");
        sb.append(request.method());
        log.log(sb.toString());
    }

    private Response logForResponse(Response response, long tookMs, LogCacher.ILog log) {
        int code;
        StringBuilder sb;
        int i;
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        ResponseBody responseBody = clone.body();
        boolean logHeaders = false;
        boolean logBody = this.printLevel == Level.BODY;
        logHeaders = (this.printLevel == Level.BODY || this.printLevel == Level.HEADERS) ? true : true;
        try {
            try {
                code = clone.code();
                sb = new StringBuilder();
                sb.append("<-- ");
                sb.append(code);
                sb.append(' ');
                sb.append(clone.message());
                sb.append(' ');
                sb.append(clone.request().url());
                sb.append(" (");
            } catch (Throwable th) {
                th = th;
            }
        } catch (Exception e) {
            e = e;
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            sb.append(tookMs);
            sb.append("ms）");
            log.log(sb.toString());
            log.code(code);
            if (logHeaders) {
                Headers headers = clone.headers();
                int count = headers.size();
                for (int i2 = 0; i2 < count; i2++) {
                    try {
                        log.log("\t" + headers.name(i2) + ": " + headers.value(i2));
                    } catch (Exception e2) {
                        e = e2;
                        LogUtils.w(this.tag, "logForResponse error!", e);
                        log.log("<-- END HTTP");
                        return response;
                    } catch (Throwable th3) {
                        th = th3;
                        log.log("<-- END HTTP");
                        throw th;
                    }
                }
                log.log(" ");
                if (logBody && okhttp3.internal.http.HttpHeaders.hasBody(clone)) {
                    if (responseBody == null) {
                        log.log("<-- END HTTP");
                        return response;
                    }
                    try {
                        if (isPlaintext(responseBody.contentType())) {
                            byte[] bytes = IOUtils.toByteArray(responseBody.byteStream());
                            MediaType contentType = responseBody.contentType();
                            String body = new String(bytes, getCharset(contentType));
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("\tbody:");
                            sb2.append(body);
                            log.log(sb2.toString());
                            if (body.contains("\"code\":")) {
                                if (!body.contains("\"code\":0,") && !body.contains("\"code\":0}") && !body.contains("\"code\":200,") && !body.contains("\"code\":200}")) {
                                    i = code > 299 ? code : -1;
                                    log.code(i);
                                }
                                i = 200;
                                log.code(i);
                            }
                            Response build = response.newBuilder().body(ResponseBody.create(responseBody.contentType(), bytes)).build();
                            log.log("<-- END HTTP");
                            return build;
                        }
                        log.log("\tbody: maybe [binary body], omitted!");
                    } catch (Exception e3) {
                        e = e3;
                        LogUtils.w(this.tag, "logForResponse error!", e);
                        log.log("<-- END HTTP");
                        return response;
                    }
                }
            }
        } catch (Exception e4) {
            e = e4;
            LogUtils.w(this.tag, "logForResponse error!", e);
            log.log("<-- END HTTP");
            return response;
        } catch (Throwable th4) {
            th = th4;
            log.log("<-- END HTTP");
            throw th;
        }
        log.log("<-- END HTTP");
        return response;
    }

    private static Charset getCharset(MediaType contentType) {
        Charset charset = UTF8;
        if (contentType != null) {
            charset = contentType.charset(charset);
        }
        return charset == null ? UTF8 : charset;
    }

    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        if (mediaType.type() == null || !mediaType.type().equals("text")) {
            String subtype = mediaType.subtype();
            if (subtype != null) {
                String subtype2 = subtype.toLowerCase();
                if (subtype2.contains("x-www-form-urlencoded") || subtype2.contains("json") || subtype2.contains("xml") || subtype2.contains("html")) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private void bodyToString(Request request, LogCacher.ILog log) {
        try {
            Request copy = request.newBuilder().build();
            RequestBody body = copy.body();
            if (body == null) {
                return;
            }
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            Charset charset = getCharset(body.contentType());
            log.log("\tbody:" + buffer.readString(charset));
        } catch (Exception e) {
            LogUtils.w(this.tag, "bodyToString error!", e);
        }
    }
}

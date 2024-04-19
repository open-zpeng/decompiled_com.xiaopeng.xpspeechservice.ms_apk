package com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter;

import android.support.annotation.Nullable;
import com.lzy.okgo.model.Response;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse;
import com.xiaopeng.lib.framework.netchannelmodule.http.statistic.HttpCounter;
import com.xiaopeng.lib.framework.netchannelmodule.http.traffic.CountingInputStream;
import com.xiaopeng.lib.framework.netchannelmodule.http.traffic.ICollector;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
/* loaded from: classes.dex */
public class ResponseAdapter implements IResponse {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static volatile boolean mEnableStat;
    private ICollector mCollector;
    private String mDomain;
    private Response<String> mInternalResponse;
    private okhttp3.Response mRawResponse;

    public ResponseAdapter(Response response) {
        this.mCollector = new ICollector() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter.ResponseAdapter.1
            @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.ICollector
            public String getDomain() {
                return ResponseAdapter.this.mDomain;
            }
        };
        this.mInternalResponse = response;
        this.mRawResponse = null;
        this.mDomain = getDomain(this.mRawResponse);
    }

    public ResponseAdapter(okhttp3.Response response) {
        this.mCollector = new ICollector() { // from class: com.xiaopeng.lib.framework.netchannelmodule.http.okgoadapter.ResponseAdapter.1
            @Override // com.xiaopeng.lib.framework.netchannelmodule.http.traffic.ICollector
            public String getDomain() {
                return ResponseAdapter.this.mDomain;
            }
        };
        this.mRawResponse = response;
        this.mInternalResponse = new Response<>();
        this.mInternalResponse.setRawResponse(response);
        this.mDomain = getDomain(this.mRawResponse);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse
    public int code() {
        okhttp3.Response response = this.mRawResponse;
        if (response != null) {
            return response.code();
        }
        return this.mInternalResponse.code();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse
    @Nullable
    public String body() {
        String result = null;
        okhttp3.Response response = this.mRawResponse;
        if (response == null) {
            String result2 = this.mInternalResponse.body();
            String result3 = result2;
            if (result3 != null && mEnableStat) {
                HttpCounter.getInstance().addReceivedSize(this.mDomain, result3.length());
            }
            return result3;
        } else if (response.body() == null) {
            return null;
        } else {
            try {
                result = this.mRawResponse.body().string();
                if (mEnableStat) {
                    HttpCounter.getInstance().addReceivedSize(this.mDomain, result.length());
                }
            } catch (Exception e) {
            }
            return result;
        }
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse
    @Nullable
    public InputStream byteStream() {
        InputStream input = null;
        okhttp3.Response response = this.mRawResponse;
        if (response == null || response.body() == null) {
            return null;
        }
        try {
            input = new CountingInputStream(this.mCollector, this.mRawResponse.body().byteStream());
            ((CountingInputStream) input).setStatisticCounter(HttpCounter.getInstance());
            return input;
        } catch (Exception e) {
            return input;
        }
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse
    @Nullable
    public String header(String key) {
        okhttp3.Response response = this.mRawResponse;
        if (response == null) {
            return null;
        }
        return response.header(key);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse
    @Nullable
    public String message() {
        okhttp3.Response response = this.mRawResponse;
        if (response == null) {
            return this.mInternalResponse.message();
        }
        return response.message();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse
    @Nullable
    public Throwable getException() {
        return this.mInternalResponse.getException();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse
    @Nullable
    public okhttp3.Response getRawResponse() {
        okhttp3.Response response = this.mRawResponse;
        if (response != null) {
            return response;
        }
        return this.mInternalResponse.getRawResponse();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse
    @Nullable
    public Map<String, List<String>> headers() {
        okhttp3.Response response = getRawResponse();
        if (response == null || response.headers() == null) {
            return null;
        }
        return response.headers().toMultimap();
    }

    @Nullable
    private String getDomain(okhttp3.Response response) {
        if (response != null && response.isSuccessful() && response.request() != null) {
            return response.request().url().host();
        }
        return null;
    }

    public static void enableStat(boolean enable) {
        mEnableStat = enable;
    }
}

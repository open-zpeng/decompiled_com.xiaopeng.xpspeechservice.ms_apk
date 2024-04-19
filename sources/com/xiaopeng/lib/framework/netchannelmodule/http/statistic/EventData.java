package com.xiaopeng.lib.framework.netchannelmodule.http.statistic;

import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import com.xiaopeng.lib.framework.netchannelmodule.common.ContextNetStatusProvider;
import com.xiaopeng.lib.utils.NetUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Handshake;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
@Keep
/* loaded from: classes.dex */
public class EventData {
    private static final int CALL_END = 17;
    private static final int CALL_START = 0;
    private static final int CONNECTION_ACQUIRED = 7;
    private static final int CONNECTION_RELEASED = 16;
    private static final int CONNECT_END = 6;
    private static final int CONNECT_START = 3;
    private static final int DNS_END = 2;
    private static final int DNS_START = 1;
    private static final int REQUEST_BODY_END = 11;
    private static final int REQUEST_BODY_START = 10;
    private static final int REQUEST_HEADERS_END = 9;
    private static final int REQUEST_HEADERS_START = 8;
    private static final int RESPONSE_BODY_END = 15;
    private static final int RESPONSE_BODY_START = 14;
    private static final int RESPONSE_HEADERS_END = 13;
    private static final int RESPONSE_HEADERS_START = 12;
    private static final int SECURE_CONNECT_END = 5;
    private static final int SECURE_CONNECT_START = 4;
    private String mCallException;
    private String mCallUrl;
    private String mMethod;
    private long mRespCode = 0;
    private long mRespBodySize = 0;
    private long[] mTimeArray = new long[18];
    private int mNetType = 0;
    private int mNetStrength = 0;
    private boolean mReadyPublish = false;
    private long mCreateTime = System.currentTimeMillis();

    public EventData(Call call) {
        this.mCallUrl = call.request().url().toString();
        if (this.mCallUrl.length() > 70) {
            this.mCallUrl = this.mCallUrl.substring(0, 70);
        }
    }

    public void callStart() {
        this.mTimeArray[0] = timeDiv();
    }

    public void dnsStart(String domainName) {
        this.mTimeArray[1] = timeDiv();
    }

    public void dnsEnd(String domainName, List<InetAddress> inetAddressList) {
        this.mTimeArray[2] = timeDiv();
    }

    public void connectStart(InetSocketAddress inetSocketAddress, Proxy proxy) {
        this.mTimeArray[3] = timeDiv();
    }

    public void secureConnectStart() {
        this.mTimeArray[4] = timeDiv();
    }

    public void secureConnectEnd(@Nullable Handshake handshake) {
        this.mTimeArray[5] = timeDiv();
    }

    public void connectEnd(InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol) {
        this.mTimeArray[6] = timeDiv();
    }

    public void connectionAcquired(Connection connection) {
        this.mTimeArray[7] = timeDiv();
    }

    public void requestHeadersStart() {
        this.mTimeArray[8] = timeDiv();
    }

    public void requestHeadersEnd(Request request) {
        this.mMethod = request.method();
        this.mTimeArray[9] = timeDiv();
    }

    public void requestBodyStart() {
        this.mTimeArray[10] = timeDiv();
    }

    public void requestBodyEnd(long byteCount) {
        this.mTimeArray[11] = timeDiv();
    }

    public void responseHeadersStart() {
        this.mTimeArray[12] = timeDiv();
    }

    public void responseHeadersEnd(Response response) {
        this.mTimeArray[13] = timeDiv();
        this.mRespCode = response.code();
    }

    public void responseBodyStart() {
        this.mTimeArray[14] = timeDiv();
    }

    public void responseBodyEnd(long byteCount) {
        this.mRespBodySize = byteCount;
        this.mTimeArray[15] = timeDiv();
    }

    public void connectionReleased(Connection connection) {
        this.mTimeArray[16] = timeDiv();
    }

    public void callEnd() {
        this.mTimeArray[17] = timeDiv();
        this.mReadyPublish = true;
        this.mNetType = ContextNetStatusProvider.getNetType();
        this.mNetStrength = ContextNetStatusProvider.getNetStrength();
    }

    public void callFailed(IOException ioe) {
        this.mTimeArray[17] = timeDiv();
        this.mCallException = getExceptionString(ioe);
        this.mReadyPublish = true;
        this.mNetType = ContextNetStatusProvider.getNetType();
        this.mNetStrength = ContextNetStatusProvider.getNetStrength();
    }

    public void connectFailed(InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol, IOException ioe) {
    }

    private String getExceptionString(Exception e) {
        String s = e.toString();
        if (s.length() > 70) {
            return s.substring(0, 70);
        }
        return s;
    }

    public boolean readyPublish() {
        return this.mReadyPublish || System.currentTimeMillis() - this.mCreateTime > 600000;
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("ti", this.mCreateTime);
            object.put("u", this.mCallUrl);
            object.put("ne", this.mNetType);
            object.put("ns", this.mNetStrength);
            if (this.mRespBodySize > 0) {
                object.put("bs", this.mRespBodySize);
            }
            if (this.mRespCode > 0) {
                object.put("cd", this.mRespCode);
            }
            if (this.mCallException != null) {
                object.put("ce", this.mCallException);
                object.put("ta", NetUtils.getTrafficStatus());
            }
            if (this.mMethod != null) {
                object.put("md", this.mMethod);
            }
            object.put("tt", array(this.mTimeArray));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private long timeDiv() {
        return System.currentTimeMillis() - this.mCreateTime;
    }

    private JSONArray array(long[] data) {
        JSONArray array = new JSONArray();
        for (long d : data) {
            array.put(d);
        }
        return array;
    }
}

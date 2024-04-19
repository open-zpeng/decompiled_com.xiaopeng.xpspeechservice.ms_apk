package com.xiaopeng.lib.framework.netchannelmodule.http.statistic;

import android.support.annotation.Keep;
import com.google.gson.annotations.SerializedName;
@Keep
/* loaded from: classes.dex */
public class TrafficData {
    @SerializedName("fail")
    private long mFailed;
    @SerializedName("rx")
    private long mReceived;
    @SerializedName("tx")
    private long mSent;
    @SerializedName("succ")
    private long mSucceed;

    public TrafficData() {
        reset();
    }

    public void setCount(long succeed, long failed) {
        this.mFailed = failed;
        this.mSucceed = succeed;
    }

    public void setSize(long received, long sent) {
        this.mReceived = received;
        this.mSent = sent;
    }

    public void increaseSucceed(long receivedSize) {
        this.mSucceed++;
        this.mReceived += receivedSize;
    }

    public void increaseFailed(long receivedSize) {
        this.mFailed++;
        this.mReceived += receivedSize;
    }

    public void addReceivedSize(long receivedSize) {
        this.mReceived += receivedSize;
    }

    public void addSentSize(long sent) {
        this.mSent += sent;
    }

    public long succeed() {
        return this.mSucceed;
    }

    public long failed() {
        return this.mFailed;
    }

    public long receivedSize() {
        return this.mReceived;
    }

    public long sentSize() {
        return this.mSent;
    }

    public void reset() {
        this.mSent = 0L;
        this.mReceived = 0L;
        this.mFailed = 0L;
        this.mSucceed = 0L;
    }

    public String toString() {
        return "[ succeed:" + this.mSucceed + ", failed:" + this.mFailed + ", rx:" + this.mReceived + ", tx:" + this.mSent + " ]";
    }
}

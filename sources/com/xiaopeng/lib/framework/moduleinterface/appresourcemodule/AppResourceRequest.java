package com.xiaopeng.lib.framework.moduleinterface.appresourcemodule;

import android.support.annotation.Keep;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
@Keep
/* loaded from: classes.dex */
public class AppResourceRequest {
    private static long longInc = -1;
    private boolean mCheckUpdate;
    private long mEffectiveDate;
    private long mId;
    private String mLocalPath;
    private String mOperate;
    private String mRemoteUri;
    private boolean mUnzip;
    private int mUpdatePolicy;
    private String mUriPath;
    private String mShared = "SHARED";
    private boolean mCoverRecord = true;

    public AppResourceRequest() {
        long j = longInc + 1;
        longInc = j;
        this.mId = j;
    }

    public AppResourceRequest uriPath(String uriPath) {
        this.mUriPath = uriPath;
        return this;
    }

    public AppResourceRequest updatePolicy(int policy) {
        this.mUpdatePolicy = policy;
        return this;
    }

    public AppResourceRequest localPath(String path) {
        this.mLocalPath = path;
        return this;
    }

    public AppResourceRequest shared(String shared) {
        this.mShared = shared;
        return this;
    }

    public AppResourceRequest remoteUri(String uri) {
        this.mRemoteUri = uri;
        return this;
    }

    public AppResourceRequest effectiveDate(long date) {
        this.mEffectiveDate = date;
        return this;
    }

    public AppResourceRequest unzip(boolean unzip) {
        this.mUnzip = unzip;
        return this;
    }

    public AppResourceRequest checkUpdate(boolean check) {
        this.mCheckUpdate = check;
        return this;
    }

    public AppResourceRequest coverRecord(boolean replace) {
        this.mCoverRecord = replace;
        return this;
    }

    public int updatePolicy() {
        return this.mUpdatePolicy;
    }

    public String localPath() {
        return this.mLocalPath;
    }

    public String uriPath() {
        return this.mUriPath;
    }

    public String remoteUri() {
        return this.mRemoteUri;
    }

    public String shared() {
        return this.mShared;
    }

    public String operate() {
        return this.mOperate;
    }

    public boolean isUnzip() {
        return this.mUnzip;
    }

    public boolean checkUpdate() {
        return this.mCheckUpdate;
    }

    public boolean isCoverRecord() {
        return this.mCoverRecord;
    }

    public AppResourceRequest operate(String op) {
        this.mOperate = op;
        return this;
    }

    public long effectiveDate() {
        return this.mEffectiveDate;
    }

    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put("update_policy", this.mUpdatePolicy);
            object.put("local_path", this.mLocalPath);
            object.put("uri_path", this.mUriPath);
            object.put("remote_uri", this.mRemoteUri);
            object.put("effective", this.mEffectiveDate);
            object.put("unzip", this.mUnzip);
            object.put("shared", this.mShared);
            object.put("operate", this.mOperate);
            object.put("id", this.mId);
            object.put("check_update", this.mCheckUpdate);
            object.put("cover_record", this.mCoverRecord);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof AppResourceRequest) {
            AppResourceRequest other = (AppResourceRequest) o;
            return this.mUpdatePolicy == other.updatePolicy() && this.mEffectiveDate == other.mEffectiveDate && stringEqual(this.mLocalPath, other.mLocalPath) && stringEqual(this.mUriPath, other.mUriPath) && stringEqual(this.mRemoteUri, other.mRemoteUri) && stringEqual(this.mOperate, other.mOperate) && stringEqual(this.mShared, other.mShared) && this.mCheckUpdate == other.mCheckUpdate && this.mCoverRecord == other.mCoverRecord && this.mUnzip == other.mUnzip && this.mId == other.mId;
        }
        return false;
    }

    private boolean stringEqual(String le, String ri) {
        if (TextUtils.isEmpty(le) && TextUtils.isEmpty(ri)) {
            return true;
        }
        return le != null && le.equals(ri);
    }

    public static AppResourceRequest from(String src) {
        try {
            if (!TextUtils.isEmpty(src)) {
                JSONObject object = new JSONObject(src);
                AppResourceRequest request = new AppResourceRequest();
                request.mId = object.optLong("id");
                request.mUpdatePolicy = object.optInt("update_policy");
                request.mLocalPath = object.optString("local_path");
                request.mUriPath = object.optString("uri_path");
                request.mRemoteUri = object.optString("remote_uri");
                request.mShared = object.optString("shared", "SHARED");
                request.mEffectiveDate = object.optLong("effective");
                request.mUnzip = object.optBoolean("unzip");
                request.mOperate = object.optString("operate");
                request.mCoverRecord = object.optBoolean("cover_record", true);
                request.mCheckUpdate = object.optBoolean("check_update");
                return request;
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

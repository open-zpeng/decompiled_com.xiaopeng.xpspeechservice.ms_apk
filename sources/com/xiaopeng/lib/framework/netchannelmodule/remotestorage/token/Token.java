package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token;
/* loaded from: classes.dex */
public final class Token {
    private final long TOKEN_EXPIRE_TIME_IN_MS;
    private final String mAccessKeyId;
    private final String mAccessKeySecret;
    private final long mActiveTimeInMs;
    private final String mSecurityToken;

    private Token() {
        this.TOKEN_EXPIRE_TIME_IN_MS = 3000000L;
        this.mSecurityToken = "";
        this.mAccessKeySecret = "";
        this.mAccessKeyId = "";
        this.mActiveTimeInMs = 0L;
    }

    public Token(String securityToken, String accessKeySecret, String accessKeyId) {
        this.TOKEN_EXPIRE_TIME_IN_MS = 3000000L;
        this.mSecurityToken = securityToken;
        this.mAccessKeySecret = accessKeySecret;
        this.mAccessKeyId = accessKeyId;
        this.mActiveTimeInMs = System.currentTimeMillis();
    }

    public String securityToken() {
        return this.mSecurityToken;
    }

    public String acessKeySecret() {
        return this.mAccessKeySecret;
    }

    public String accessKeyId() {
        return this.mAccessKeyId;
    }

    public boolean stillFresh() {
        long currentTime = System.currentTimeMillis();
        long j = this.mActiveTimeInMs;
        return currentTime > j && currentTime - j < 3000000;
    }
}

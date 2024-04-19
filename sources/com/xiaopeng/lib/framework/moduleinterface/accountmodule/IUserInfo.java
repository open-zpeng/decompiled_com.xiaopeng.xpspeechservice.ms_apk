package com.xiaopeng.lib.framework.moduleinterface.accountmodule;
/* loaded from: classes.dex */
public interface IUserInfo {

    /* loaded from: classes.dex */
    public enum InfoType {
        CHANGED,
        UPDATE
    }

    /* loaded from: classes.dex */
    public enum UserType {
        TEMP,
        OWNER,
        USER,
        TENANT,
        DRIVER
    }

    String getAvatar();

    InfoType getInfoType();

    String getUserName();

    UserType getUserType();

    IUserInfo setAvatar(String url);

    IUserInfo setInfoType(InfoType type);

    IUserInfo setUserName(String name);

    @Deprecated
    IUserInfo setUserType(int type);

    IUserInfo setUserType(UserType type);
}

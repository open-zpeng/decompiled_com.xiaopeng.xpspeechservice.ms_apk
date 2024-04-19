package com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage;
/* loaded from: classes.dex */
public interface Callback {
    void onFailure(String remoteUrl, String localFilePath, StorageException storageException);

    void onStart(String remoteUrl, String localFilePath);

    void onSuccess(String remoteUrl, String localFilePath);
}

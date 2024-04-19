package com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage;

import android.app.Application;
import java.util.Map;
/* loaded from: classes.dex */
public interface IRemoteStorage {

    /* loaded from: classes.dex */
    public enum CATEGORY {
        CDU,
        CAN
    }

    void appendWithPathAndCallback(String bucketName, String remoteFolder, byte[] uploadContent, Callback callback) throws Exception;

    void downloadWithPathAndCallback(String bucketName, String remoteFolder, String localFile, Callback callback) throws Exception;

    @Deprecated
    void initWithCategoryAndContext(Application application) throws Exception;

    void initWithContext(Application application) throws Exception;

    void needCertified(boolean openCertification);

    void uploadWithCallback(CATEGORY category, String moduleName, String file, Callback callback) throws Exception;

    void uploadWithCallback(CATEGORY category, String moduleName, String file, Callback callback, Map<String, String> remoteCallbackParams) throws Exception;

    void uploadWithPathAndCallback(String bucketName, String remoteFolder, String file, Callback callback) throws Exception;

    void uploadWithPathAndCallback(String bucketName, String remoteFolder, String file, Callback callback, Map<String, String> remoteCallbackParams) throws Exception;
}

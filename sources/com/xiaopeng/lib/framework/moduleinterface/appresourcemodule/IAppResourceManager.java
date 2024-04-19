package com.xiaopeng.lib.framework.moduleinterface.appresourcemodule;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.reactivex.Observable;
/* loaded from: classes.dex */
public interface IAppResourceManager {

    /* loaded from: classes.dex */
    public interface IResourceObserver {
        void onChange(@NonNull String uriPath, @NonNull AppResourceResponse response);
    }

    boolean addResource(@NonNull AppResourceRequest request);

    Observable<AppResourceResponse> addResourceObSource(@NonNull AppResourceRequest request);

    void checkUpdate(@NonNull String uriPath);

    Observable<AppResourceResponse> checkUpdateObSource(@NonNull String uriPath);

    Observable<AppResourceResponse> clearResourceObSource();

    void clearResources();

    boolean deleteResource(@NonNull String uriPath);

    Observable<AppResourceResponse> deleteResourceObSource(@NonNull String uriPath);

    @Nullable
    AssetFileDescriptor getFileDescriptor(@NonNull String uriPath);

    Observable<AssetFileDescriptor> getFileDescriptorObSource(@NonNull String uriPath);

    @Nullable
    Uri getFullUri(String uriPath);

    Observable<Uri> getFullUrlObSource(@NonNull String uriPath);

    @NonNull
    Status getStatus(@NonNull String uriPath);

    Observable<Status> getStatusObSource(@NonNull String uriPath);

    void subscribeChanges(@NonNull IResourceObserver observer);

    void unSubscribeChanges(@NonNull IResourceObserver observer);

    /* loaded from: classes.dex */
    public enum UpdatePolicy {
        LOCAL(0),
        SYNC(1),
        ASYNC(2);
        
        final int mId;

        public int id() {
            return this.mId;
        }

        UpdatePolicy(int id) {
            this.mId = id;
        }
    }

    /* loaded from: classes.dex */
    public enum Status {
        NOT_REGISTER(0),
        NOT_AUTH(1),
        NOT_AVAILABLE(2),
        OK(3),
        ERROR(4);
        
        final int mId;

        public int id() {
            return this.mId;
        }

        Status(int id) {
            this.mId = id;
        }
    }
}

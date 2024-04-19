package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.cache.CacheEntity;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.Callback;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IHttp;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.IResponse;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.StorageException;
import com.xiaopeng.lib.framework.netchannelmodule.NetworkChannelsEntry;
import com.xiaopeng.lib.framework.netchannelmodule.common.GlobalConfig;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.exception.StorageExceptionImpl;
import com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.Bucket;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public final class TokenRetriever {
    private static final long BLOCKING_TIME_INTERVAL = 3600000;
    private static final String EMPTY_BODY = "{}";
    private static final long RESPONSE_NO_CERTIFICATE = 11001502;
    private static final String TAG = "NetChannel-TokenRetriever";
    private long mBlockingTime;
    private final CallbackList mCallbacks;
    private volatile boolean mRetrieving;
    private volatile Token mToken;

    /* loaded from: classes.dex */
    public interface IRetrievingCallback {
        void onFailure(StorageException storageException);

        void onSuccess(Token token);
    }

    private TokenRetriever() {
        this.mToken = null;
        this.mCallbacks = new CallbackList();
        this.mBlockingTime = 0L;
    }

    public static TokenRetriever getInstance() {
        return Holder.INSTANCE;
    }

    public void getTokenWithCallback(IRetrievingCallback callback) {
        if (this.mBlockingTime > 0 && System.currentTimeMillis() < this.mBlockingTime) {
            callback.onFailure(new StorageExceptionImpl(StorageException.REASON_GET_TOKEN_ERROR, String.valueOf((long) RESPONSE_NO_CERTIFICATE)));
        } else if (this.mToken != null && this.mToken.stillFresh()) {
            callback.onSuccess(this.mToken);
        } else {
            this.mCallbacks.add(callback);
            if (!this.mRetrieving) {
                this.mRetrieving = true;
                getNewTokenFromServer(GlobalConfig.getApplicationContext());
            }
        }
    }

    public void clearToken() {
        this.mToken = null;
        LogUtils.d(TAG, "Clear the existing token!");
    }

    private void getNewTokenFromServer(Context context) {
        IHttp http = (IHttp) Module.get(NetworkChannelsEntry.class).get(IHttp.class);
        http.bizHelper().post(Bucket.TOKEN_URL_V5, EMPTY_BODY).build().execute(new GetTokenCallback());
    }

    /* loaded from: classes.dex */
    private static final class Holder {
        public static final TokenRetriever INSTANCE = new TokenRetriever();

        private Holder() {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class GetTokenCallback implements Callback {
        private GetTokenCallback() {
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.Callback
        public void onFailure(IResponse response) {
            String responseMessage;
            TokenRetriever.this.mRetrieving = false;
            TokenRetriever.this.mBlockingTime = 0L;
            LogUtils.e(TokenRetriever.TAG, "Request token error! the error message---->" + response.message());
            if (response.getException() != null) {
                responseMessage = response.getException().getMessage();
            } else {
                responseMessage = response.message();
            }
            TokenRetriever.this.callFailureCallbacks(new StorageExceptionImpl(StorageException.REASON_GET_TOKEN_ERROR, responseMessage));
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.http.Callback
        public void onSuccess(IResponse response) {
            TokenRetriever.this.mRetrieving = false;
            TokenRetriever.this.mBlockingTime = 0L;
            if (TextUtils.isEmpty(response.body())) {
                TokenRetriever.this.callFailureCallbacks(new StorageExceptionImpl(StorageException.REASON_GET_TOKEN_ERROR, "Empty response!"));
                return;
            }
            JsonObject jsonObject = new JsonParser().parse(response.body()).getAsJsonObject();
            long code = jsonObject.get("code").getAsLong();
            if (code == 200) {
                JsonObject data = jsonObject.getAsJsonObject(CacheEntity.DATA);
                String securityToken = data.get("security_token").getAsString();
                String accessKeySecret = data.get("access_key_secret").getAsString();
                String accessKeyId = data.get("access_key_id").getAsString();
                TokenRetriever.this.mToken = new Token(securityToken, accessKeySecret, accessKeyId);
                TokenRetriever tokenRetriever = TokenRetriever.this;
                tokenRetriever.callSuccessCallbacks(tokenRetriever.mToken);
                return;
            }
            String responseBody = response.body();
            if (code == TokenRetriever.RESPONSE_NO_CERTIFICATE) {
                TokenRetriever.this.mBlockingTime = System.currentTimeMillis() + TokenRetriever.BLOCKING_TIME_INTERVAL;
            }
            TokenRetriever.this.callFailureCallbacks(new StorageExceptionImpl(StorageException.REASON_GET_TOKEN_ERROR, responseBody));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callSuccessCallbacks(final Token token) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever.1
            @Override // java.lang.Runnable
            public void run() {
                TokenRetriever.this.mCallbacks.onSuccess(token);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callFailureCallbacks(final StorageException exception) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.token.TokenRetriever.2
            @Override // java.lang.Runnable
            public void run() {
                TokenRetriever.this.mCallbacks.onFailure(exception);
            }
        });
    }

    /* loaded from: classes.dex */
    private class CallbackList {
        private List<IRetrievingCallback> mCallbacks = new ArrayList();

        public CallbackList() {
        }

        public synchronized void add(IRetrievingCallback callback) {
            this.mCallbacks.add(callback);
        }

        public synchronized void onFailure(StorageException exception) {
            for (IRetrievingCallback callback : this.mCallbacks) {
                callback.onFailure(exception);
            }
            this.mCallbacks.clear();
        }

        public synchronized void onSuccess(Token token) {
            for (IRetrievingCallback callback : this.mCallbacks) {
                callback.onSuccess(token);
            }
            this.mCallbacks.clear();
        }
    }
}

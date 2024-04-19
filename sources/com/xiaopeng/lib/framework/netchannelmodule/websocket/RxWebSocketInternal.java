package com.xiaopeng.lib.framework.netchannelmodule.websocket;

import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.websocket.IWebSocketInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class RxWebSocketInternal {
    private static final int CLOSE_BY_CANCELLATION = 3000;
    private static final int CLOSE_CLIENT_NORMAL = 1000;
    private static final int DEFAULT_RETRY_INTERVAL = 1000;
    private OkHttpClient mClient;
    private Map<String, String> mHeaders;
    private String mLogTag;
    private Map<String, Observable<IWebSocketInfo>> mObservableMap;
    private long mPingInterval;
    private long mReconnectInterval;
    private boolean mShowLog;
    private Map<String, WebSocket> mWebSocketMap;

    private RxWebSocketInternal() {
        this.mLogTag = "RxWebSocket";
        this.mReconnectInterval = 1000L;
        this.mPingInterval = 1000L;
        try {
            Class.forName("okhttp3.OkHttpClient");
            try {
                Class.forName("io.reactivex.Observable");
                try {
                    Class.forName("io.reactivex.android.schedulers.AndroidSchedulers");
                    this.mObservableMap = new ConcurrentHashMap();
                    this.mWebSocketMap = new ConcurrentHashMap();
                    this.mHeaders = new ConcurrentHashMap();
                    this.mClient = new OkHttpClient();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Must be dependency rxandroid 2.x");
                }
            } catch (ClassNotFoundException e2) {
                throw new RuntimeException("Must be dependency rxjava 2.x");
            }
        } catch (ClassNotFoundException e3) {
            throw new RuntimeException("Must be dependency okhttp3 !");
        }
    }

    public static RxWebSocketInternal getInstance() {
        return Holder.INSTANCE;
    }

    /* loaded from: classes.dex */
    private static final class Holder {
        private static final RxWebSocketInternal INSTANCE = new RxWebSocketInternal();

        private Holder() {
        }
    }

    public void client(OkHttpClient mClient) {
        if (mClient == null) {
            throw new NullPointerException("mClient == null");
        }
        this.mClient = mClient.newBuilder().pingInterval(this.mPingInterval, TimeUnit.MILLISECONDS).build();
    }

    public void header(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        this.mHeaders.put(key, value);
    }

    public void sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        this.mClient = this.mClient.newBuilder().sslSocketFactory(sslSocketFactory, trustManager).build();
    }

    public void showLog(boolean mShowLog) {
        this.mShowLog = mShowLog;
    }

    public void showLog(boolean showLog, String logTag) {
        showLog(showLog);
        this.mLogTag = logTag;
    }

    public void reconnectInterval(long interval) {
        this.mReconnectInterval = interval;
    }

    public void pingInterval(long interval) {
        this.mPingInterval = interval;
        this.mClient = this.mClient.newBuilder().pingInterval(interval, TimeUnit.MILLISECONDS).build();
    }

    public Observable<IWebSocketInfo> getWebSocketInfo(final String url, long timeout, TimeUnit timeUnit) {
        Observable<IWebSocketInfo> observable = this.mObservableMap.get(url);
        if (observable == null) {
            observable = Observable.create(new WebSocketOnSubscribe(url)).timeout(timeout, timeUnit).retry(new Predicate<Throwable>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.3
                public boolean test(Throwable throwable) throws Exception {
                    if (RxWebSocketInternal.this.mShowLog) {
                        String str = RxWebSocketInternal.this.mLogTag;
                        Log.d(str, "predicate retry for " + throwable);
                    }
                    return (throwable instanceof IOException) || (throwable instanceof TimeoutException);
                }
            }).doOnDispose(new Action() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.2
                public void run() throws Exception {
                    RxWebSocketInternal.this.mObservableMap.remove(url);
                    RxWebSocketInternal.this.mWebSocketMap.remove(url);
                    if (RxWebSocketInternal.this.mShowLog) {
                        Log.d(RxWebSocketInternal.this.mLogTag, "OnDispose");
                    }
                }
            }).doOnNext(new Consumer<IWebSocketInfo>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.1
                public void accept(IWebSocketInfo webSocketInfo) throws Exception {
                    if (webSocketInfo.isOnOpen()) {
                        RxWebSocketInternal.this.mWebSocketMap.put(url, ((WebSocketInfo) webSocketInfo).getWebSocket());
                    }
                }
            }).share().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            this.mObservableMap.put(url, observable);
        } else {
            WebSocket webSocket = this.mWebSocketMap.get(url);
            if (webSocket != null) {
                observable = observable.startWith(new WebSocketInfo(webSocket, IWebSocketInfo.STATE.OPEN));
            }
        }
        return observable.observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<IWebSocketInfo> getWebSocketInfo(String url) {
        return getWebSocketInfo(url, 1L, TimeUnit.HOURS);
    }

    public Observable<String> getWebSocketString(String url) {
        return getWebSocketInfo(url).filter(new Predicate<IWebSocketInfo>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.5
            public boolean test(@NonNull IWebSocketInfo webSocketInfo) throws Exception {
                return webSocketInfo.stringMessage() != null;
            }
        }).map(new Function<IWebSocketInfo, String>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.4
            public String apply(@NonNull IWebSocketInfo webSocketInfo) throws Exception {
                return webSocketInfo.stringMessage();
            }
        });
    }

    public Observable<ByteString> getWebSocketByteString(String url) {
        return getWebSocketInfo(url).filter(new Predicate<IWebSocketInfo>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.7
            public boolean test(@NonNull IWebSocketInfo webSocketInfo) throws Exception {
                return webSocketInfo.byteStringMessage() != null;
            }
        }).map(new Function<IWebSocketInfo, ByteString>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.6
            public ByteString apply(IWebSocketInfo webSocketInfo) throws Exception {
                return webSocketInfo.byteStringMessage();
            }
        });
    }

    public Observable<WebSocket> getWebSocket(String url) {
        return getWebSocketInfo(url).filter(new Predicate<IWebSocketInfo>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.9
            public boolean test(IWebSocketInfo webSocketInfo) throws Exception {
                return ((WebSocketInfo) webSocketInfo).getWebSocket() != null;
            }
        }).map(new Function<IWebSocketInfo, WebSocket>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.8
            public WebSocket apply(@NonNull IWebSocketInfo webSocketInfo) throws Exception {
                return ((WebSocketInfo) webSocketInfo).getWebSocket();
            }
        });
    }

    public void send(String url, String msg) {
        WebSocket webSocket = this.mWebSocketMap.get(url);
        if (webSocket != null) {
            webSocket.send(msg);
            return;
        }
        throw new IllegalStateException("WebSocket channel not open");
    }

    public void send(String url, ByteString byteString) {
        WebSocket webSocket = this.mWebSocketMap.get(url);
        if (webSocket != null) {
            webSocket.send(byteString);
            return;
        }
        throw new IllegalStateException("WebSocket channel not open");
    }

    public void asyncSend(String url, final String msg) {
        getWebSocket(url).take(1L).subscribe(new Consumer<WebSocket>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.10
            public void accept(WebSocket webSocket) throws Exception {
                webSocket.send(msg);
            }
        });
    }

    public void asyncSend(String url, final ByteString byteString) {
        getWebSocket(url).take(1L).subscribe(new Consumer<WebSocket>() { // from class: com.xiaopeng.lib.framework.netchannelmodule.websocket.RxWebSocketInternal.11
            public void accept(WebSocket webSocket) throws Exception {
                webSocket.send(byteString);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Request getRequest(String url) {
        Request.Builder builder = new Request.Builder().get().url(url);
        if (this.mHeaders.size() > 0) {
            for (String key : this.mHeaders.keySet()) {
                builder.addHeader(key, this.mHeaders.get(key));
            }
        }
        return builder.build();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public final class WebSocketOnSubscribe implements ObservableOnSubscribe<IWebSocketInfo> {
        private String url;
        private WebSocket webSocket;

        public WebSocketOnSubscribe(String url) {
            this.url = url;
        }

        public void subscribe(@NonNull ObservableEmitter<IWebSocketInfo> emitter) throws Exception {
            if (this.webSocket != null && Looper.getMainLooper().getThread() != Thread.currentThread()) {
                long ms = RxWebSocketInternal.this.mReconnectInterval;
                if (ms == 0) {
                    ms = 1000;
                }
                SystemClock.sleep(ms);
                emitter.onNext(WebSocketInfo.createReconnect());
            }
            initWebSocket(emitter);
        }

        private void initWebSocket(ObservableEmitter<IWebSocketInfo> emitter) {
            this.webSocket = RxWebSocketInternal.this.mClient.newWebSocket(RxWebSocketInternal.this.getRequest(this.url), new WebSocketListenerInternal(emitter, RxWebSocketInternal.this.mWebSocketMap, this.url, RxWebSocketInternal.this.mShowLog, RxWebSocketInternal.this.mLogTag));
            emitter.setCancellable(new CancelableInternal(this.webSocket, this.url, RxWebSocketInternal.this.mShowLog, RxWebSocketInternal.this.mLogTag));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class CancelableInternal implements Cancellable {
        private boolean showLog;
        private String tag;
        private String url;
        private WebSocket webSocket;

        public CancelableInternal(WebSocket webSocket, String url, boolean showLog, String tag) {
            this.webSocket = webSocket;
            this.url = url;
            this.showLog = showLog;
            this.tag = tag;
        }

        public void cancel() throws Exception {
            this.webSocket.close(RxWebSocketInternal.CLOSE_BY_CANCELLATION, "close WebSocket from rx cancel");
            if (this.showLog) {
                String str = this.tag;
                Log.d(str, this.url + " --> cancel for rx cancel");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class WebSocketListenerInternal extends WebSocketListener {
        private ObservableEmitter<IWebSocketInfo> emitter;
        private boolean showLog;
        private String tag;
        private String url;
        private Map<String, WebSocket> webSocketMap;

        public WebSocketListenerInternal(ObservableEmitter<IWebSocketInfo> emitter, Map<String, WebSocket> webSocketMap, String url, boolean showLog, String tag) {
            this.emitter = emitter;
            this.webSocketMap = webSocketMap;
            this.url = url;
            this.showLog = showLog;
            this.tag = tag;
        }

        @Override // okhttp3.WebSocketListener
        public void onOpen(WebSocket webSocket, Response response) {
            if (this.showLog) {
                String str = this.tag;
                Log.d(str, this.url + " --> onOpen");
            }
            this.webSocketMap.put(this.url, webSocket);
            if (!this.emitter.isDisposed()) {
                this.emitter.onNext(new WebSocketInfo(webSocket, IWebSocketInfo.STATE.OPEN));
            }
        }

        @Override // okhttp3.WebSocketListener
        public void onMessage(WebSocket webSocket, String text) {
            if (!this.emitter.isDisposed()) {
                this.emitter.onNext(new WebSocketInfo(webSocket, text));
            }
        }

        @Override // okhttp3.WebSocketListener
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if (!this.emitter.isDisposed()) {
                this.emitter.onNext(new WebSocketInfo(webSocket, bytes));
            }
        }

        @Override // okhttp3.WebSocketListener
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            if (this.showLog && !(t instanceof UnknownHostException)) {
                String str = this.tag;
                Log.e(str, t.toString() + webSocket.request().url().uri().getPath());
            }
            if (!this.emitter.isDisposed()) {
                this.emitter.onError(t);
            }
        }

        @Override // okhttp3.WebSocketListener
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(1000, null);
        }

        @Override // okhttp3.WebSocketListener
        public void onClosed(WebSocket webSocket, int code, String reason) {
            if (this.showLog) {
                String str = this.tag;
                Log.d(str, this.url + " --> onClosed:code= " + code + "reason:" + reason);
            }
            if (!this.emitter.isDisposed()) {
                WebSocketInfo info = new WebSocketInfo(webSocket, IWebSocketInfo.STATE.CLOSED);
                info.setClosedReason(code, reason);
                this.emitter.onNext(info);
            }
        }
    }

    public void close(String url) {
        WebSocket webSocket = this.mWebSocketMap.get(url);
        if (webSocket != null) {
            this.mObservableMap.remove(url);
            this.mWebSocketMap.remove(url);
            webSocket.close(1000, null);
        }
    }
}

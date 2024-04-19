package com.xiaopeng.lib.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
/* loaded from: classes.dex */
public class ThreadUtils {
    public static final int THREAD_BACKGROUND = 0;
    public static final int THREAD_NORMAL = 2;
    private static final int THREAD_POOL_SIZE = 4;
    public static final int THREAD_UI = 1;
    private static Handler sBackgroundHandler;
    private static volatile HandlerThread sBackgroundThread;
    private static Handler sMainThreadHandler;
    private static Handler sNormalHandler;
    private static volatile HandlerThread sNormalThread;
    private static final ExecutorService sThreadPool = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("order-%d").setDaemon(false).build());
    private static ConcurrentHashMap<Object, RunnableMap> sRunnableCache = new ConcurrentHashMap<>();

    private ThreadUtils() {
    }

    public static Looper getLooper(int threadType) {
        if (threadType == 0) {
            createBackgroundThread();
            return sBackgroundThread.getLooper();
        } else if (threadType == 1) {
            createMainThread();
            return sMainThreadHandler.getLooper();
        } else if (threadType == 2) {
            createNormalThread();
            return sNormalHandler.getLooper();
        } else {
            throw new IllegalArgumentException("invalid threadType:" + threadType);
        }
    }

    public static void runOnMainThread(Runnable runner) {
        post(1, runner);
    }

    public static void runOnMainThreadDelay(Runnable runner, long delayMs) {
        postDelayed(1, runner, delayMs);
    }

    public static void execute(Runnable runnable) {
        execute(runnable, null, 10);
    }

    public static void execute(Runnable runnable, Runnable callback) {
        execute(runnable, callback, 10);
    }

    public static void execute(final Runnable runnable, final Runnable callback, final int priority) {
        try {
            if (!sThreadPool.isShutdown()) {
                Handler handler = null;
                if (callback != null) {
                    handler = new Handler(Looper.myLooper());
                }
                final Handler finalHandler = handler;
                sThreadPool.execute(new Runnable() { // from class: com.xiaopeng.lib.utils.ThreadUtils.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Runnable runnable2;
                        Process.setThreadPriority(priority);
                        runnable.run();
                        Handler handler2 = finalHandler;
                        if (handler2 != null && (runnable2 = callback) != null) {
                            handler2.post(runnable2);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void post(int threadType, Runnable preCallback, Runnable task, Runnable postCallback, boolean callbackToMainThread) {
        doPost(threadType, preCallback, task, postCallback, callbackToMainThread, 0L);
    }

    public static void post(int threadType, Runnable task, Runnable postCallback, boolean callbackToMainThread) {
        doPost(threadType, null, task, postCallback, callbackToMainThread, 0L);
    }

    public static void post(int threadType, Runnable task) {
        doPost(threadType, null, task, null, false, 0L);
    }

    public static void postDelayed(int threadType, Runnable task, long delayMillis) {
        doPost(threadType, null, task, null, false, delayMillis);
    }

    public static void postMainThread(Runnable task) {
        doPost(1, null, task, null, false, 0L);
    }

    public static void postMainThread(Runnable task, long delayMillis) {
        doPost(1, null, task, null, false, delayMillis);
    }

    public static void postNormal(Runnable task) {
        doPost(2, null, task, null, false, 0L);
    }

    public static void postNormal(Runnable task, long delayMillis) {
        doPost(2, null, task, null, false, delayMillis);
    }

    public static void postBackground(Runnable task) {
        doPost(0, null, task, null, false, 0L);
    }

    public static void postBackground(Runnable task, long delayMillis) {
        doPost(0, null, task, null, false, delayMillis);
    }

    private static void doPost(int threadType, final Runnable preCallback, final Runnable task, final Runnable postCallback, final boolean callbackToMainThread, long delayMillis) {
        Handler handler;
        Handler handler2;
        Looper myLooper;
        if (task == null) {
            return;
        }
        if (sMainThreadHandler == null) {
            createMainThread();
        }
        if (threadType != 0) {
            if (threadType != 1) {
                if (threadType == 2) {
                    if (sNormalThread == null) {
                        createNormalThread();
                    }
                    handler = sNormalHandler;
                } else {
                    handler = sMainThreadHandler;
                }
            } else {
                handler = sMainThreadHandler;
            }
        } else {
            if (sBackgroundThread == null) {
                createBackgroundThread();
            }
            handler = sBackgroundHandler;
        }
        if (handler != null) {
            handler2 = handler;
        } else {
            Handler handler3 = sMainThreadHandler;
            handler2 = handler3;
        }
        final Handler finalHandler = handler2;
        if (callbackToMainThread) {
            myLooper = null;
        } else {
            Looper myLooper2 = Looper.myLooper();
            if (myLooper2 != null) {
                myLooper = myLooper2;
            } else {
                Looper myLooper3 = sMainThreadHandler.getLooper();
                myLooper = myLooper3;
            }
        }
        final Looper looper = myLooper;
        final Runnable postRunnable = new Runnable() { // from class: com.xiaopeng.lib.utils.ThreadUtils.2
            @Override // java.lang.Runnable
            public void run() {
                try {
                    task.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (postCallback != null) {
                    if (callbackToMainThread || looper == ThreadUtils.sMainThreadHandler.getLooper()) {
                        ThreadUtils.sMainThreadHandler.post(postCallback);
                    } else {
                        new Handler(looper).post(postCallback);
                    }
                }
                ThreadUtils.sRunnableCache.remove(task);
            }
        };
        Runnable realRunnable = new Runnable() { // from class: com.xiaopeng.lib.utils.ThreadUtils.3
            @Override // java.lang.Runnable
            public void run() {
                if (preCallback != null) {
                    if (callbackToMainThread || looper == ThreadUtils.sMainThreadHandler.getLooper()) {
                        ThreadUtils.sMainThreadHandler.post(new Runnable() { // from class: com.xiaopeng.lib.utils.ThreadUtils.3.1
                            @Override // java.lang.Runnable
                            public void run() {
                                preCallback.run();
                                finalHandler.post(postRunnable);
                            }
                        });
                        return;
                    } else {
                        new Handler(looper).post(new Runnable() { // from class: com.xiaopeng.lib.utils.ThreadUtils.3.2
                            @Override // java.lang.Runnable
                            public void run() {
                                preCallback.run();
                                finalHandler.post(postRunnable);
                            }
                        });
                        return;
                    }
                }
                postRunnable.run();
            }
        };
        finalHandler.postDelayed(realRunnable, delayMillis);
        if (preCallback == null) {
            sRunnableCache.put(task, new RunnableMap(realRunnable, Integer.valueOf(threadType)));
        } else {
            sRunnableCache.put(task, new RunnableMap(postRunnable, Integer.valueOf(threadType)));
        }
    }

    public static void removeRunnable(Runnable task) {
        RunnableMap map;
        Runnable realRunnable;
        Handler handler;
        if (task != null && (map = sRunnableCache.remove(task)) != null && (realRunnable = map.getRunnable()) != null) {
            int type = map.getType();
            if (type == 0) {
                Handler handler2 = sBackgroundHandler;
                if (handler2 != null) {
                    handler2.removeCallbacks(realRunnable);
                }
            } else if (type != 1) {
                if (type == 2 && (handler = sNormalHandler) != null) {
                    handler.removeCallbacks(realRunnable);
                }
            } else {
                Handler handler3 = sMainThreadHandler;
                if (handler3 != null) {
                    handler3.removeCallbacks(realRunnable);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class RunnableMap {
        private Runnable mRunnable;
        private Integer mType;

        public RunnableMap(Runnable runnable, Integer type) {
            this.mRunnable = runnable;
            this.mType = type;
        }

        public Runnable getRunnable() {
            return this.mRunnable;
        }

        public int getType() {
            return this.mType.intValue();
        }
    }

    private static synchronized void createMainThread() {
        synchronized (ThreadUtils.class) {
            if (sMainThreadHandler == null) {
                sMainThreadHandler = new Handler(Looper.getMainLooper());
            }
        }
    }

    private static synchronized void createBackgroundThread() {
        synchronized (ThreadUtils.class) {
            if (sBackgroundThread == null) {
                sBackgroundThread = new HandlerThread("BackgroundHandler", 10);
                sBackgroundThread.start();
                sBackgroundHandler = new Handler(sBackgroundThread.getLooper());
            }
        }
    }

    private static synchronized void createNormalThread() {
        synchronized (ThreadUtils.class) {
            if (sNormalThread == null) {
                sNormalThread = new HandlerThread("NormalHandler", 0);
                sNormalThread.start();
                sNormalHandler = new Handler(sNormalThread.getLooper());
            }
        }
    }

    public static synchronized void destroy() {
        synchronized (ThreadUtils.class) {
            if (sBackgroundThread != null) {
                sBackgroundThread.quit();
                sBackgroundThread.interrupt();
                sBackgroundThread = null;
            }
            if (sNormalThread != null) {
                sNormalThread.quit();
                sNormalThread.interrupt();
                sNormalThread = null;
            }
        }
    }

    public static boolean hasCallbacksOnBackground(Runnable runnable) {
        Handler handler = sBackgroundHandler;
        return handler != null && handler.hasCallbacks(runnable);
    }

    public static boolean hasMessagesOnBackground(int what, Object object) {
        Handler handler = sBackgroundHandler;
        return handler != null && handler.hasMessages(what, object);
    }

    public static boolean hasMessagesOnBackground(int what) {
        Handler handler = sBackgroundHandler;
        return handler != null && handler.hasMessages(what);
    }

    public static boolean hasCallbacksOnMain(Runnable runnable) {
        Handler handler = sMainThreadHandler;
        return handler != null && handler.hasCallbacks(runnable);
    }

    public static boolean hasMessagesOnMain(int what, Object object) {
        Handler handler = sMainThreadHandler;
        return handler != null && handler.hasMessages(what, object);
    }

    public static boolean hasMessagesOnMain(int what) {
        Handler handler = sMainThreadHandler;
        return handler != null && handler.hasMessages(what);
    }

    public static boolean hasCallbacksOnNormal(Runnable runnable) {
        Handler handler = sNormalHandler;
        return handler != null && handler.hasCallbacks(runnable);
    }

    public static boolean hasMessagesOnNormal(int what, Object object) {
        Handler handler = sNormalHandler;
        return handler != null && handler.hasMessages(what, object);
    }

    public static boolean hasMessagesOnNormal(int what) {
        Handler handler = sNormalHandler;
        return handler != null && handler.hasMessages(what);
    }

    /* loaded from: classes.dex */
    private static class ThreadFactoryBuilder {
        private static final boolean DAEMON_VALUE = false;
        private static final String NAME = "newFixedThreadPool";
        private boolean mDaemon;
        private String mName;
        private String mNameFormat;
        private ThreadFactory mThreadFactory;

        private ThreadFactoryBuilder() {
            this.mName = NAME;
            this.mDaemon = false;
        }

        public ThreadFactory getThreadFactory() {
            return this.mThreadFactory;
        }

        public ThreadFactoryBuilder setThreadFactory(ThreadFactory mThreadFactory) {
            this.mThreadFactory = mThreadFactory;
            return this;
        }

        public String getNameFormat() {
            return this.mNameFormat;
        }

        ThreadFactoryBuilder setNameFormat(String mNameFormat) {
            this.mNameFormat = mNameFormat;
            return this;
        }

        public String getName() {
            return this.mName;
        }

        public boolean ismDaemon() {
            return this.mDaemon;
        }

        ThreadFactoryBuilder setDaemon(boolean mDaemon) {
            this.mDaemon = mDaemon;
            return this;
        }

        ThreadFactory build() {
            return new ThreadFactory() { // from class: com.xiaopeng.lib.utils.ThreadUtils.ThreadFactoryBuilder.1
                @Override // java.util.concurrent.ThreadFactory
                public Thread newThread(@NonNull Runnable r) {
                    ThreadFactory threadFactory = ThreadFactoryBuilder.this.mThreadFactory != null ? ThreadFactoryBuilder.this.mThreadFactory : Executors.defaultThreadFactory();
                    Thread thread = threadFactory.newThread(r);
                    AtomicLong count = ThreadFactoryBuilder.this.mNameFormat != null ? new AtomicLong(0L) : null;
                    if (ThreadFactoryBuilder.this.mNameFormat != null) {
                        thread.setName(String.format(Locale.ROOT, ThreadFactoryBuilder.this.mNameFormat, Long.valueOf(count.getAndIncrement())));
                    }
                    thread.setDaemon(ThreadFactoryBuilder.this.mDaemon);
                    return thread;
                }
            };
        }
    }
}

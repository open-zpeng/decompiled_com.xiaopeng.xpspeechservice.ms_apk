package com.xiaopeng.speech.tts;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class XpAudioPlaybackHandler {
    private static final boolean DBG = false;
    private static final String TAG = "XpAudioPlaybackHandler";
    private final Thread mHandlerThread;
    private final LinkedBlockingQueue<XpPlaybackQueueItem> mQueue = new LinkedBlockingQueue<>();
    private volatile XpPlaybackQueueItem mCurrentWorkItem = null;

    /* JADX INFO: Access modifiers changed from: package-private */
    public XpAudioPlaybackHandler(String name) {
        MessageLoop messageLoop = new MessageLoop();
        this.mHandlerThread = new Thread(messageLoop, "XpAudioPlaybackHandler_" + name);
    }

    public void start() {
        this.mHandlerThread.start();
    }

    private void stop(XpPlaybackQueueItem item) {
        if (item == null) {
            return;
        }
        item.stop(-2);
    }

    public void enqueue(XpPlaybackQueueItem item) {
        try {
            this.mQueue.put(item);
        } catch (InterruptedException e) {
        }
    }

    public void stopForApp(Object callerIdentity) {
        removeWorkItemsFor(callerIdentity);
        XpPlaybackQueueItem current = this.mCurrentWorkItem;
        if (current != null && current.getCallerIdentity() == callerIdentity) {
            stop(current);
        }
    }

    public void stop() {
        removeAllMessages();
        stop(this.mCurrentWorkItem);
    }

    public boolean isSpeaking() {
        return (this.mQueue.peek() == null && this.mCurrentWorkItem == null) ? false : true;
    }

    public void quit() {
        removeAllMessages();
        stop(this.mCurrentWorkItem);
        this.mHandlerThread.interrupt();
    }

    private void removeAllMessages() {
        this.mQueue.clear();
    }

    private void removeWorkItemsFor(Object callerIdentity) {
        Iterator<XpPlaybackQueueItem> it = this.mQueue.iterator();
        while (it.hasNext()) {
            XpPlaybackQueueItem item = it.next();
            if (item.getCallerIdentity() == callerIdentity) {
                it.remove();
                stop(item);
            }
        }
    }

    /* loaded from: classes.dex */
    private final class MessageLoop implements Runnable {
        private MessageLoop() {
        }

        @Override // java.lang.Runnable
        public void run() {
            while (true) {
                try {
                    XpPlaybackQueueItem item = (XpPlaybackQueueItem) XpAudioPlaybackHandler.this.mQueue.take();
                    XpAudioPlaybackHandler.this.mCurrentWorkItem = item;
                    item.run();
                    XpAudioPlaybackHandler.this.mCurrentWorkItem = null;
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}

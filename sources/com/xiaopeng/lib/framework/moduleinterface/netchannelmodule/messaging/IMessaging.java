package com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging;

import android.app.Application;
/* loaded from: classes.dex */
public interface IMessaging {
    public static final long DEFAULT_MESSAGE_ID = 0;

    /* loaded from: classes.dex */
    public enum CHANNEL {
        COMMUNICATION,
        REPORTING,
        TESTING
    }

    boolean available();

    void initChannelWithContext(CHANNEL channel, Application application) throws Exception;

    long publish(String topic, String message) throws Exception;

    long publish(String topic, String message, QOS qosLevel) throws Exception;

    long publish(String topic, byte[] message) throws Exception;

    long publish(String topic, byte[] message, QOS qosLevel) throws Exception;

    void subscribe(String topic) throws Exception;

    /* loaded from: classes.dex */
    public enum QOS {
        LEVEL_0(0),
        LEVEL_1(1),
        LEVEL_2(2);
        
        private int value;

        QOS(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
}

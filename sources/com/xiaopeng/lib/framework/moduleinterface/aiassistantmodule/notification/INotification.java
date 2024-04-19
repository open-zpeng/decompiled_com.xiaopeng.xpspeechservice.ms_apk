package com.xiaopeng.lib.framework.moduleinterface.aiassistantmodule.notification;
/* loaded from: classes.dex */
public interface INotification {
    void close(String messageId);

    void send(String messageStr);

    void setOnNotificationCallback(INotificationCallback callback);
}

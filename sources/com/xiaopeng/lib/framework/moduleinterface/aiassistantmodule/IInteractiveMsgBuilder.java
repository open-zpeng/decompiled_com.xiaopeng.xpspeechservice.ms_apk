package com.xiaopeng.lib.framework.moduleinterface.aiassistantmodule;
/* loaded from: classes.dex */
public interface IInteractiveMsgBuilder {
    IInteractiveMsg build();

    IInteractiveMsgBuilder data(String data);

    IInteractiveMsgBuilder msgId(int msgId);
}

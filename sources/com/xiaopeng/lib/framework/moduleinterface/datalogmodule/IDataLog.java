package com.xiaopeng.lib.framework.moduleinterface.datalogmodule;

import java.util.List;
/* loaded from: classes.dex */
public interface IDataLog {
    IMoleEventBuilder buildMoleEvent();

    @Deprecated
    IStatEventBuilder buildStat();

    ICounterFactory counterFactory();

    void sendCanData(String data);

    void sendFiles(List<String> filePaths);

    String sendRecentSystemLog();

    void sendStatData(IMoleEvent event);

    @Deprecated
    void sendStatData(IStatEvent event);

    void sendStatData(IStatEvent event, List<String> filePaths);

    void sendStatData(String eventName, String data);

    void sendStatOriginData(String eventName, String data);
}

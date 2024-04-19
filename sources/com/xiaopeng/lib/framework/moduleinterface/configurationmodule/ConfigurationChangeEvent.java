package com.xiaopeng.lib.framework.moduleinterface.configurationmodule;

import android.support.annotation.Nullable;
import java.util.List;
/* loaded from: classes.dex */
public class ConfigurationChangeEvent {
    @Nullable
    private List<IConfigurationData> mChangeList;

    public List<IConfigurationData> getChangeList() {
        return this.mChangeList;
    }

    public void setChangeList(List<IConfigurationData> list) {
        this.mChangeList = list;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConfigurationChangeEvent{list size:");
        List<IConfigurationData> list = this.mChangeList;
        sb.append(list != null ? list.size() : 0);
        sb.append("}");
        return sb.toString();
    }
}

package com.xiaopeng.datalog;

import android.content.Context;
import android.text.TextUtils;
import com.xiaopeng.datalog.counter.CounterFactory;
import com.xiaopeng.datalog.stat.StatEventHelper;
import com.xiaopeng.lib.framework.module.Module;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.ICounterFactory;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEvent;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEvent;
import com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEventBuilder;
import java.util.List;
/* loaded from: classes.dex */
public class DataLogService implements IDataLog {
    private Context mContext;

    public DataLogService(Context context) {
        StatEventHelper.init(context);
        this.mContext = context;
        Module.register(DataLogModuleEntry.class, new DataLogModuleEntry(context));
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public void sendCanData(String data) {
        StatEventHelper.getInstance().uploadCan(data);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public void sendStatData(String eventName, String data) {
        StatEventHelper.getInstance().uploadLogImmediately(eventName, data);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public void sendStatOriginData(String eventName, String data) {
        StatEventHelper.getInstance().uploadLogOrigin(eventName, data);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public void sendStatData(IStatEvent statEvent) {
        StatEventHelper.getInstance().uploadCdu(statEvent);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public void sendStatData(IMoleEvent moleEvent) {
        StatEventHelper.getInstance().uploadCdu(moleEvent);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public void sendStatData(IStatEvent statEvent, List<String> filePaths) {
        StatEventHelper.getInstance().uploadCduWithFiles(statEvent, filePaths);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public void sendFiles(List<String> fileList) {
        StatEventHelper.getInstance().uploadFiles(fileList);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public String sendRecentSystemLog() {
        return StatEventHelper.getInstance().uploadRecentSystemLog();
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public IStatEventBuilder buildStat() {
        return new StatEventBuilder(this.mContext);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public IMoleEventBuilder buildMoleEvent() {
        return new MoleEventBuilder(this.mContext);
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IDataLog
    public ICounterFactory counterFactory() {
        return CounterFactory.getInstance();
    }

    /* loaded from: classes.dex */
    private class StatEventBuilder implements IStatEventBuilder {
        private IStatEvent event;

        StatEventBuilder(Context context) {
            this.event = new StatEvent(context);
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEventBuilder
        public IStatEventBuilder setEventName(String eventName) {
            this.event.setEventName(eventName);
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEventBuilder
        public IStatEventBuilder setProperty(String key, String value) {
            this.event.put(key, value);
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEventBuilder
        public IStatEventBuilder setProperty(String key, Number value) {
            this.event.put(key, value);
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEventBuilder
        public IStatEventBuilder setProperty(String key, boolean value) {
            this.event.put(key, Boolean.valueOf(value));
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEventBuilder
        public IStatEventBuilder setProperty(String key, char value) {
            this.event.put(key, Character.valueOf(value));
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IStatEventBuilder
        public IStatEvent build() {
            if (TextUtils.isEmpty(this.event.getEventName())) {
                throw new IllegalStateException("Please call setEventName first!");
            }
            return this.event;
        }
    }

    /* loaded from: classes.dex */
    private class MoleEventBuilder implements IMoleEventBuilder {
        MoleEvent event;

        private MoleEventBuilder(Context context) {
            this.event = new MoleEvent(context);
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder
        public IMoleEventBuilder setPageId(String pageId) {
            this.event.put(MoleEvent.KEY_PAGE_ID, pageId);
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder
        public IMoleEventBuilder setButtonId(String buttonId) {
            this.event.put(MoleEvent.KEY_BUTTON_ID, buttonId);
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder
        public IMoleEventBuilder setModule(String module) {
            this.event.put(IStatEvent.CUSTOM_MODULE, module);
            MoleEvent moleEvent = this.event;
            moleEvent.put(IStatEvent.CUSTOM_EVENT, module + "_page_button");
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder
        public IMoleEventBuilder setEvent(String eventName) {
            this.event.put(IStatEvent.CUSTOM_MODULE, StatEvent.getModuleNameFromEvent(eventName));
            this.event.put(IStatEvent.CUSTOM_EVENT, eventName);
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder
        public IMoleEventBuilder setProperty(String key, String s1) {
            this.event.put(key, s1);
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder
        public IMoleEventBuilder setProperty(String key, Number number) {
            this.event.put(key, number);
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder
        public IMoleEventBuilder setProperty(String key, boolean b) {
            this.event.put(key, Boolean.valueOf(b));
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder
        public IMoleEventBuilder setProperty(String key, char c) {
            this.event.put(key, Character.valueOf(c));
            return this;
        }

        @Override // com.xiaopeng.lib.framework.moduleinterface.datalogmodule.IMoleEventBuilder
        public IMoleEvent build() {
            this.event.checkValid();
            return this.event;
        }
    }
}

package com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol;

import com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile;
import com.xiaopeng.lib.utils.LogUtils;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.logging.Logger;
/* loaded from: classes.dex */
public class LoggerImpl implements Logger {
    private Set<String> mAcceptMsgId;
    private String mLogTag;

    public void initialise(ResourceBundle logMsgCatalog, String loggerID, String resourceContext) {
        AbstractChannelProfile profile = MqttChannel.getCurrentChannelProfile();
        this.mAcceptMsgId = profile.getAcceptedLogList();
        this.mLogTag = profile.logTag();
    }

    public void setResourceName(String logContext) {
    }

    public boolean isLoggable(int level) {
        return true;
    }

    public void severe(String sourceClass, String sourceMethod, String msg) {
        log(1, sourceClass, sourceMethod, msg, null, null);
    }

    public void severe(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        log(1, sourceClass, sourceMethod, msg, inserts, null);
    }

    public void severe(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        log(1, sourceClass, sourceMethod, msg, inserts, thrown);
    }

    public void warning(String sourceClass, String sourceMethod, String msg) {
        log(2, sourceClass, sourceMethod, msg, null, null);
    }

    public void warning(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        log(2, sourceClass, sourceMethod, msg, inserts, null);
    }

    public void warning(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        log(2, sourceClass, sourceMethod, msg, inserts, thrown);
    }

    public void info(String sourceClass, String sourceMethod, String msg) {
        log(3, sourceClass, sourceMethod, msg, null, null);
    }

    public void info(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        log(3, sourceClass, sourceMethod, msg, inserts, null);
    }

    public void info(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        log(3, sourceClass, sourceMethod, msg, inserts, thrown);
    }

    public void config(String sourceClass, String sourceMethod, String msg) {
        log(4, sourceClass, sourceMethod, msg, null, null);
    }

    public void config(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        log(4, sourceClass, sourceMethod, msg, inserts, null);
    }

    public void config(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        log(4, sourceClass, sourceMethod, msg, inserts, thrown);
    }

    public void log(int level, String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        Level julLevel = mapJULLevel(level);
        logInternal(julLevel, sourceClass, sourceMethod, msg, inserts, thrown);
    }

    public void fine(String sourceClass, String sourceMethod, String msg) {
        trace(5, sourceClass, sourceMethod, msg, null, null);
    }

    public void fine(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        trace(5, sourceClass, sourceMethod, msg, inserts, null);
    }

    public void fine(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        trace(5, sourceClass, sourceMethod, msg, inserts, ex);
    }

    public void finer(String sourceClass, String sourceMethod, String msg) {
        trace(6, sourceClass, sourceMethod, msg, null, null);
    }

    public void finer(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        trace(6, sourceClass, sourceMethod, msg, inserts, null);
    }

    public void finer(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        trace(6, sourceClass, sourceMethod, msg, inserts, ex);
    }

    public void finest(String sourceClass, String sourceMethod, String msg) {
        trace(7, sourceClass, sourceMethod, msg, null, null);
    }

    public void finest(String sourceClass, String sourceMethod, String msg, Object[] inserts) {
        trace(7, sourceClass, sourceMethod, msg, inserts, null);
    }

    public void finest(String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        trace(7, sourceClass, sourceMethod, msg, inserts, ex);
    }

    public void trace(int level, String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable ex) {
        Level julLevel = mapJULLevel(level);
        logInternal(julLevel, sourceClass, sourceMethod, msg, inserts, ex);
    }

    private void logInternal(Level julLevel, String sourceClass, String sourceMethod, String msg, Object[] inserts, Throwable thrown) {
        Set<String> set;
        if (msg == null) {
            return;
        }
        if (thrown == null && (set = this.mAcceptMsgId) != null && !set.contains(msg)) {
            return;
        }
        String message = julLevel + " " + sourceClass + "." + sourceMethod + ":" + msg + " " + Arrays.toString(inserts);
        checkException(inserts);
        if (thrown != null) {
            LogUtils.e(this.mLogTag, message, thrown);
        } else {
            LogUtils.d(this.mLogTag, message);
        }
    }

    private void checkException(Object[] inserts) {
        if (inserts == null) {
            return;
        }
        for (Object ex : inserts) {
            if (ex != null && (ex instanceof MqttException)) {
                int reasonCode = ((MqttException) ex).getReasonCode();
                Throwable throwable = ((MqttException) ex).getCause();
                if (reasonCode == 32101 || reasonCode == 32102 || reasonCode == 32109) {
                    MqttChannel.getInstance().disconnectedDueToException((MqttException) ex);
                    return;
                } else if (reasonCode == 0 && throwable != null) {
                    MqttChannel.getInstance().disconnectedDueToException(throwable);
                    return;
                } else {
                    return;
                }
            }
        }
    }

    private Level mapJULLevel(int level) {
        switch (level) {
            case 1:
                Level julLevel = Level.SEVERE;
                return julLevel;
            case 2:
                Level julLevel2 = Level.WARNING;
                return julLevel2;
            case 3:
                Level julLevel3 = Level.INFO;
                return julLevel3;
            case 4:
                Level julLevel4 = Level.CONFIG;
                return julLevel4;
            case 5:
                Level julLevel5 = Level.FINE;
                return julLevel5;
            case 6:
                Level julLevel6 = Level.FINER;
                return julLevel6;
            case 7:
                Level julLevel7 = Level.FINEST;
                return julLevel7;
            default:
                return null;
        }
    }

    public String formatMessage(String msg, Object[] inserts) {
        return msg;
    }

    public void dumpTrace() {
    }
}

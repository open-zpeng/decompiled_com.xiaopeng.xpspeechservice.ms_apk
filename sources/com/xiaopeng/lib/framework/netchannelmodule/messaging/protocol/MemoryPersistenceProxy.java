package com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol;

import android.support.annotation.NonNull;
import android.util.Pair;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
/* loaded from: classes.dex */
public class MemoryPersistenceProxy implements MqttClientPersistence {
    private static final int MAX_MESSAGE_NUMBER = 200;
    private static final String TAG = "MemoryPersistenceProxy";
    protected String mClientId;
    protected Hashtable<String, Pair<MqttPersistable, Long>> mHashTable = new Hashtable<>();
    protected String mServerURI;
    protected boolean mTraceEnabled;

    private void log(String message) {
        if (!BuildInfoUtils.isEngVersion() || !this.mTraceEnabled) {
            return;
        }
        LogUtils.d(TAG, message);
    }

    public MemoryPersistenceProxy() {
        log("new MemoryPersistenceProxy");
    }

    private void checkOpen() throws MqttPersistenceException {
        if (this.mHashTable == null) {
            throw new MqttPersistenceException();
        }
    }

    public void close() throws MqttPersistenceException {
        log("close()");
    }

    public Enumeration keys() throws MqttPersistenceException {
        checkOpen();
        Enumeration result = this.mHashTable.keys();
        log("keys():" + result + " hasMoreElements:" + result.hasMoreElements());
        return result;
    }

    public MqttPersistable get(String key) throws MqttPersistenceException {
        checkOpen();
        MqttPersistable result = (MqttPersistable) this.mHashTable.get(key).first;
        log("get key:" + key + " result:" + result);
        return result;
    }

    public void open(@NonNull String clientId, @NonNull String serverURI) throws MqttPersistenceException {
        log("open clientId:" + clientId + " serverURI:" + serverURI);
        if (clientId == null || serverURI == null) {
            throw new IllegalArgumentException("clientId or serverURI can't be null");
        }
        if (clientId.equals(this.mClientId) && serverURI.equals(this.mServerURI)) {
            log("same config, return!");
            return;
        }
        Hashtable<String, Pair<MqttPersistable, Long>> hashtable = this.mHashTable;
        if (hashtable != null) {
            hashtable.clear();
        }
        this.mClientId = clientId;
        this.mServerURI = serverURI;
        this.mHashTable = new Hashtable<>();
    }

    public void put(String key, MqttPersistable persistable) throws MqttPersistenceException {
        checkOpen();
        log("put key:" + key + " persistable:" + persistable + " size:" + this.mHashTable.size());
        if (this.mHashTable.size() >= 200) {
            this.mHashTable.clear();
            log("exceed max persist count");
        }
        this.mHashTable.put(key, new Pair<>(persistable, Long.valueOf(System.currentTimeMillis())));
    }

    public void remove(String key) throws MqttPersistenceException {
        checkOpen();
        log("remove key:" + key);
        this.mHashTable.remove(key);
    }

    public void clear() throws MqttPersistenceException {
        checkOpen();
        log("clear");
        this.mHashTable.clear();
    }

    public boolean containsKey(String key) throws MqttPersistenceException {
        checkOpen();
        boolean result = this.mHashTable.containsKey(key);
        log("containsKey key:" + key + " result:" + result);
        return result;
    }

    public void setTraceEnabled(boolean enabled) {
        this.mTraceEnabled = enabled;
    }
}

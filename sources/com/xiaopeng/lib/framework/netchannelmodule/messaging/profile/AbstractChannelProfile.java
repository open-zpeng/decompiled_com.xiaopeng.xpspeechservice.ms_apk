package com.xiaopeng.lib.framework.netchannelmodule.messaging.profile;

import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.messaging.IMessaging;
import com.xiaopeng.lib.framework.netchannelmodule.messaging.protocol.MemoryPersistenceProxy;
import com.xiaopeng.lib.utils.crypt.AESUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
/* loaded from: classes.dex */
public abstract class AbstractChannelProfile {
    private static final String AES_KEY = "@!chxpzi#0109$+/";
    private static final int CONNECTION_TIMEOUT = 10;
    private static final String DEBUG_FLAG_FILE = "/sdcard/MQTTTRACE.xp";
    private static final int MAX_INFLIGHT = 120;
    protected static final String SSL_PREFIX = "ssl://";
    public static final String SYSTEM_PROPERTY_MQTT_COMM_ID;
    public static final String SYSTEM_PROPERTY_MQTT_COMM_PASS;
    public static final String SYSTEM_PROPERTY_MQTT_COMM_URL;
    public static final String SYSTEM_PROPERTY_MQTT_COMM_USER;
    public static final String SYSTEM_PROPERTY_MQTT_ID;
    public static final String SYSTEM_PROPERTY_MQTT_PASS;
    public static final String SYSTEM_PROPERTY_MQTT_REPORTING_URL;
    public static final String SYSTEM_PROPERTY_MQTT_USER;
    private static final boolean sShorterAccountEncoding;
    private boolean mCanPublish;
    private boolean mCanSubscribe;
    private boolean mDebugging = checkDebuggingFlag();
    private MqttClientPersistenceFactory sPersistenceFactory;

    /* loaded from: classes.dex */
    public interface MqttClientPersistenceFactory {
        MqttClientPersistence makePersistence();
    }

    public abstract IMessaging.CHANNEL channel();

    public abstract String serverUrl();

    static {
        sShorterAccountEncoding = Build.VERSION.SDK_INT != 19;
        SYSTEM_PROPERTY_MQTT_USER = "persist.sys.mqtt.user" + BuildInfoUtils.getBid();
        SYSTEM_PROPERTY_MQTT_PASS = "persist.sys.mqtt.pass" + BuildInfoUtils.getBid();
        SYSTEM_PROPERTY_MQTT_ID = "persist.sys.mqtt.id" + BuildInfoUtils.getBid();
        SYSTEM_PROPERTY_MQTT_REPORTING_URL = "persist.sys.mqtt.url" + BuildInfoUtils.getBid();
        SYSTEM_PROPERTY_MQTT_COMM_URL = "persist.sys.mqtt.comm_url" + BuildInfoUtils.getBid();
        SYSTEM_PROPERTY_MQTT_COMM_ID = "persist.sys.mqtt.comm_id" + BuildInfoUtils.getBid();
        SYSTEM_PROPERTY_MQTT_COMM_USER = "persist.sys.mqtt.comm_user" + BuildInfoUtils.getBid();
        SYSTEM_PROPERTY_MQTT_COMM_PASS = "persist.sys.mqtt.comm_pass" + BuildInfoUtils.getBid();
    }

    public AbstractChannelProfile(boolean canPublish, boolean canSubscribe) {
        this.mCanPublish = canPublish;
        this.mCanSubscribe = canSubscribe;
    }

    public String logTag() {
        return "PahoLogger";
    }

    public boolean canPublish() {
        return this.mCanPublish;
    }

    public boolean canSubscribe() {
        return this.mCanSubscribe;
    }

    public int defaultQoSLevel() {
        return IMessaging.QOS.LEVEL_2.value();
    }

    public boolean sendOutAllLogs() {
        return false;
    }

    public boolean enableTrace() {
        return this.mDebugging;
    }

    public boolean needToCollectData() {
        return true;
    }

    public boolean enableExtremePing() {
        return false;
    }

    public String clientId() {
        String clientId = SystemProperties.get(SYSTEM_PROPERTY_MQTT_ID);
        if (TextUtils.isEmpty(clientId)) {
            throw new RuntimeException("Initialization failure!!");
        }
        return decode(clientId);
    }

    public MqttConnectOptions buildConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(false);
        options.setCleanSession(false);
        options.setMqttVersion(4);
        options.setMaxInflight((int) MAX_INFLIGHT);
        options.setConnectionTimeout(10);
        return options;
    }

    public DisconnectedBufferOptions buildBufferOptions() {
        return null;
    }

    public Set<String> getAcceptedLogList() {
        if (this.mDebugging) {
            return null;
        }
        return new HashSet(Arrays.asList("309", "627", "633", "802"));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String decode(String str) {
        String result;
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (sShorterAccountEncoding) {
            result = AESUtils.decryptWithBase64(str, AES_KEY);
        } else {
            result = AESUtils.decrypt(str, AES_KEY);
        }
        return result == null ? "" : result;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0021 A[Catch: UnknownHostException -> 0x007e, TryCatch #0 {UnknownHostException -> 0x007e, blocks: (B:3:0x0005, B:5:0x0017, B:6:0x001b, B:8:0x0021, B:10:0x002d, B:14:0x003f, B:16:0x0062, B:12:0x0033), top: B:23:0x0005 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.lang.String resolveWithDns(java.lang.String r7) {
        /*
            r0 = r7
            android.net.Uri r1 = android.net.Uri.parse(r7)
            com.xiaopeng.lib.framework.netchannelmodule.http.xmart.TimeoutDns r2 = com.xiaopeng.lib.framework.netchannelmodule.http.xmart.TimeoutDns.getInstance()     // Catch: java.net.UnknownHostException -> L7e
            java.lang.String r3 = r1.getHost()     // Catch: java.net.UnknownHostException -> L7e
            java.util.List r2 = r2.lookupAsync(r3)     // Catch: java.net.UnknownHostException -> L7e
            boolean r3 = r2.isEmpty()     // Catch: java.net.UnknownHostException -> L7e
            if (r3 != 0) goto L7d
            java.util.Iterator r3 = r2.iterator()     // Catch: java.net.UnknownHostException -> L7e
        L1b:
            boolean r4 = r3.hasNext()     // Catch: java.net.UnknownHostException -> L7e
            if (r4 == 0) goto L7d
            java.lang.Object r4 = r3.next()     // Catch: java.net.UnknownHostException -> L7e
            java.net.InetAddress r4 = (java.net.InetAddress) r4     // Catch: java.net.UnknownHostException -> L7e
            boolean r5 = r4.isSiteLocalAddress()     // Catch: java.net.UnknownHostException -> L7e
            if (r5 != 0) goto L33
            boolean r5 = r4.isLoopbackAddress()     // Catch: java.net.UnknownHostException -> L7e
            if (r5 == 0) goto L3f
        L33:
            java.lang.String r5 = r4.getHostAddress()     // Catch: java.net.UnknownHostException -> L7e
            java.lang.String r6 = "10."
            boolean r5 = r5.startsWith(r6)     // Catch: java.net.UnknownHostException -> L7e
            if (r5 == 0) goto L7c
        L3f:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.net.UnknownHostException -> L7e
            r3.<init>()     // Catch: java.net.UnknownHostException -> L7e
            java.lang.String r5 = r1.getScheme()     // Catch: java.net.UnknownHostException -> L7e
            r3.append(r5)     // Catch: java.net.UnknownHostException -> L7e
            java.lang.String r5 = "://"
            r3.append(r5)     // Catch: java.net.UnknownHostException -> L7e
            java.lang.String r5 = r4.getHostAddress()     // Catch: java.net.UnknownHostException -> L7e
            r3.append(r5)     // Catch: java.net.UnknownHostException -> L7e
            java.lang.String r3 = r3.toString()     // Catch: java.net.UnknownHostException -> L7e
            r0 = r3
            int r3 = r1.getPort()     // Catch: java.net.UnknownHostException -> L7e
            if (r3 <= 0) goto L7d
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.net.UnknownHostException -> L7e
            r3.<init>()     // Catch: java.net.UnknownHostException -> L7e
            r3.append(r0)     // Catch: java.net.UnknownHostException -> L7e
            java.lang.String r5 = ":"
            r3.append(r5)     // Catch: java.net.UnknownHostException -> L7e
            int r5 = r1.getPort()     // Catch: java.net.UnknownHostException -> L7e
            r3.append(r5)     // Catch: java.net.UnknownHostException -> L7e
            java.lang.String r3 = r3.toString()     // Catch: java.net.UnknownHostException -> L7e
            r0 = r3
            goto L7d
        L7c:
            goto L1b
        L7d:
            goto L7f
        L7e:
            r2 = move-exception
        L7f:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Update server uri:"
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            com.xiaopeng.lib.utils.LogUtils.d(r2)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xiaopeng.lib.framework.netchannelmodule.messaging.profile.AbstractChannelProfile.resolveWithDns(java.lang.String):java.lang.String");
    }

    private static boolean checkDebuggingFlag() {
        if (!BuildInfoUtils.isEngVersion()) {
            return false;
        }
        File file = new File(DEBUG_FLAG_FILE);
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    public MqttClientPersistence mqttClientPersistence() {
        MqttClientPersistenceFactory mqttClientPersistenceFactory = this.sPersistenceFactory;
        if (mqttClientPersistenceFactory != null) {
            return mqttClientPersistenceFactory.makePersistence();
        }
        return new MemoryPersistenceProxy();
    }

    public void setPersistenceFactory(MqttClientPersistenceFactory factory) {
        this.sPersistenceFactory = factory;
    }
}

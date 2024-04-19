package com.xiaopeng.lib.security.xmartv1;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Base64;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaopeng.lib.http.FileUtils;
import com.xiaopeng.lib.http.ICallback;
import com.xiaopeng.lib.http.Security;
import com.xiaopeng.lib.security.ISecurityModule;
import com.xiaopeng.lib.security.SecurityCommon;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
/* loaded from: classes.dex */
public class RandomKeySecurity implements ISecurityModule {
    private static final String INDIV_FLAG_FILE_NAME = "si_flag.dat";
    private static final String NLSS_FILE_PATH = "/mnt/vendor/private/sec/";
    private static final String TAG = "RandomKeySecurity";
    private static boolean sIsInitSuccess;
    private SharedPreferences mSecureStore;
    private Context sContext;

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public Security.EncryptionType getEncryptionType() {
        return Security.EncryptionType.TEE_RANDOM_KEY;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public RandomKeySecurity init(Context context) throws Exception {
        synchronized (RandomKeySecurity.class) {
            sIsInitSuccess = KeyStoreHelper.init();
            this.mSecureStore = context.getSharedPreferences(TAG, 0);
            this.sContext = context.getApplicationContext();
        }
        return this;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public RandomKeySecurity initForIndividual(Context context) throws Exception {
        if (!SecurityCommon.checkSystemUid(context)) {
            throw new RuntimeException("仅系统进程允许调用个性化操作!");
        }
        return init(context);
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public boolean isInitAndIndivSuccess() {
        return sIsInitSuccess && KeyStoreHelper.hasValidIndividualKeys();
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public boolean isInitSuccess() {
        return sIsInitSuccess;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String getIndividualFlag() {
        try {
            String flag = FileUtils.readTextFile(new File("/mnt/vendor/private/sec/si_flag.dat"), 1, null);
            return flag;
        } catch (Exception e) {
            return null;
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void clearIndividualData() {
        if (!SecurityCommon.checkSystemUid(this.sContext)) {
            throw new RuntimeException("仅系统进程允许调用个性化操作!");
        }
        try {
            KeyStoreHelper.cleanIndividualKeys();
            FileUtils.deleteFile("/mnt/vendor/private/sec/si_flag.dat");
            this.mSecureStore.edit().clear().commit();
        } catch (Exception e) {
            LogUtils.d(TAG, "清除个性化失败!");
            e.printStackTrace();
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String getIndividualRequestStr() throws Exception {
        return "";
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public boolean individualWithData(String data) throws Exception {
        LogUtils.d(TAG, data);
        Type type = new TypeToken<LinkedHashMap<Integer, SecurityKeyBean>>() { // from class: com.xiaopeng.lib.security.xmartv1.RandomKeySecurity.1
        }.getType();
        try {
            LinkedHashMap<Integer, SecurityKeyBean> list = (LinkedHashMap) new Gson().fromJson(data, type);
            if (list.size() != XmartV1Constants.LOCAL_KEYS_NUM) {
                LogUtils.d(TAG, "Wrong individual data with length " + list.size() + ", expected " + XmartV1Constants.LOCAL_KEYS_NUM);
                return false;
            }
            Map<String, byte[]> keys = new HashMap<>(XmartV1Constants.LOCAL_KEYS_NUM);
            for (Integer key : list.keySet()) {
                SecurityKeyBean bean = list.get(key);
                keys.put(XmartV1Constants.LOCAL_KEYS_PREFIX + key, Base64.decode(bean.key, 0));
                LogUtils.d(TAG, "Wrote security key #" + key + "!");
            }
            KeyStoreHelper.writeSecretData(keys);
            KeyStoreHelper.store();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String generateIndividualDataForServer() throws Exception {
        return "";
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void destroy() {
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void saveIndividualFlag() {
        try {
            FileUtils.stringToFile("/mnt/vendor/private/sec/si_flag.dat", SecurityCommon.getBuildInfoFlag());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void saveMCUSecurityKey(boolean force) {
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void asyncGetMCUSecurityKey(ICallback<byte[], String> callback) {
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String encode(String data) {
        if (!sIsInitSuccess) {
            throw new IllegalStateException("RandomKeySecurity:未初始化!");
        }
        try {
            EncodingItem item = EncodingItem.encode(data.getBytes(XmartV1Constants.UTF8_ENCODING));
            String result = item.encodedString();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String decode(String data) {
        if (!sIsInitSuccess) {
            throw new IllegalStateException("RandomKeySecurity:未初始化!");
        }
        try {
            EncodingItem item = EncodingItem.decode(data.getBytes(XmartV1Constants.UTF8_ENCODING));
            return item.rawContentString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String getString(String key) {
        if (!sIsInitSuccess) {
            throw new IllegalStateException("RandomKeySecurity:未初始化!");
        }
        String savedContent = this.mSecureStore.getString(key, "");
        if (TextUtils.isEmpty(savedContent)) {
            return "";
        }
        byte[] sourceBytes = SecurityCommon.parseHexStr2Byte(savedContent);
        byte[] decryptResult = KeyStoreHelper.decryptGeneral(sourceBytes, EncodingItem.getGeneralKeyAlias());
        if (decryptResult == null || decryptResult.length == 0) {
            return "";
        }
        try {
            String result = new String(decryptResult, XmartV1Constants.UTF8_ENCODING);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void setString(String key, String value) {
        if (!sIsInitSuccess) {
            throw new IllegalStateException("RandomKeySecurity:未初始化!");
        }
        if (TextUtils.isEmpty(value)) {
            this.mSecureStore.edit().putString(key, value).commit();
            return;
        }
        try {
            byte[] encoded = KeyStoreHelper.encryptGeneral(value.getBytes(XmartV1Constants.UTF8_ENCODING), EncodingItem.getGeneralKeyAlias());
            if (encoded != null && encoded.length > 0) {
                String result = SecurityCommon.parseByte2HexStr(encoded);
                this.mSecureStore.edit().putString(key, result).commit();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void deleteString(String key) {
        this.mSecureStore.edit().remove(key).commit();
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String buildTokenData(String[] ids, byte[] data) {
        if (!sIsInitSuccess) {
            throw new IllegalStateException("RandomKeySecurity:未初始化!");
        }
        byte[] data2 = Base64.encode(data, 2);
        LinkedHashMap<String, String> tokens = new LinkedHashMap<>();
        int extendedLength = 0;
        for (int i = 0; i < ids.length; i++) {
            String token = getString(ids[i]);
            if (!TextUtils.isEmpty(token)) {
                tokens.put(ids[i], token);
                extendedLength += ids[i].length() + 2 + token.length();
            }
        }
        EncodingItem item = null;
        if (extendedLength > 0) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(data2.length + extendedLength).put(data2);
                for (int i2 = 0; i2 < ids.length; i2++) {
                    buffer.put((byte) 38).put(ids[i2].getBytes(XmartV1Constants.UTF8_ENCODING)).put((byte) 61).put(tokens.get(ids[i2]).getBytes(XmartV1Constants.UTF8_ENCODING));
                }
                data2 = buffer.array();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        item = EncodingItem.encode(data2);
        if (item.isEmpty()) {
            return null;
        }
        String result = item.encodedString();
        return result;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void asyncSaveToken(final String[] ids, final String[] token, final Runnable runnable) {
        if (!sIsInitSuccess) {
            throw new IllegalStateException("RandomKeySecurity:未初始化!");
        }
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.security.xmartv1.RandomKeySecurity.2
            @Override // java.lang.Runnable
            public void run() {
                try {
                    RandomKeySecurity.this.saveToken(ids, token);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                runnable.run();
            }
        });
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void asyncSaveTokenWithListener(final String[] ids, final String[] token, final ISecurityModule.ResultListener listener) {
        if (!sIsInitSuccess) {
            throw new IllegalStateException("RandomKeySecurity:未初始化!");
        }
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.security.xmartv1.RandomKeySecurity.3
            @Override // java.lang.Runnable
            public void run() {
                ISecurityModule.ResultListener.RESULT result = ISecurityModule.ResultListener.RESULT.FAIL;
                String exceptionString = "";
                try {
                    RandomKeySecurity.this.saveToken(ids, token);
                    result = ISecurityModule.ResultListener.RESULT.SUCCEED;
                } catch (Throwable e) {
                    exceptionString = e.getMessage();
                    e.printStackTrace();
                }
                listener.onResult(result, exceptionString);
            }
        });
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String cryptoDecode(String key, String Iv, byte[] data) {
        byte[] result = cryptoDecodeInByteArray(key, Iv, data);
        if (result != null) {
            return new String(result);
        }
        return null;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public byte[] cryptoDecodeInByteArray(String key, String Iv, byte[] data) {
        if (!sIsInitSuccess) {
            throw new IllegalStateException("RandomKeySecurity:未初始化!");
        }
        return KeyStoreHelper.decryptStringWithKey(data, key, Iv.getBytes());
    }

    public String encodeWithIndex(String data, int keyIndex) {
        if (!sIsInitSuccess) {
            throw new IllegalStateException("RandomKeySecurity:未初始化!");
        }
        try {
            EncodingItem item = EncodingItem.encode(data.getBytes(XmartV1Constants.UTF8_ENCODING), keyIndex);
            String result = item.encodedString();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void saveToken(String[] ids, String[] tokens) {
        if (ids.length != tokens.length) {
            LogUtils.d(TAG, "Invalid tokens.");
            return;
        }
        for (int i = 0; i < ids.length; i++) {
            String token = decode(tokens[i]);
            if (token == null) {
                throw new IllegalStateException("RandomKeySecurity: token decode failed!");
            }
            setString(ids[i], token);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class Holder {
        private static final RandomKeySecurity INSTANCE = new RandomKeySecurity();

        private Holder() {
        }
    }

    public static RandomKeySecurity getInstance() {
        return Holder.INSTANCE;
    }

    @VisibleForTesting
    /* loaded from: classes.dex */
    public class SecurityKeyBean {
        public int index;
        public String key;

        public SecurityKeyBean() {
        }
    }
}

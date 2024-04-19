package com.xiaopeng.lib.security.none;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import com.xiaopeng.lib.http.ICallback;
import com.xiaopeng.lib.http.Security;
import com.xiaopeng.lib.security.ISecurityModule;
import com.xiaopeng.lib.security.SecurityCommon;
import com.xiaopeng.lib.security.xmartv1.KeyStoreHelper;
import com.xiaopeng.lib.security.xmartv1.XmartV1Constants;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.MD5Utils;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/* loaded from: classes.dex */
public class NoneSecurity implements ISecurityModule {
    private static final String NONE_ENCODING_ALGORITHM = "AES";
    private static final String TAG = "NoneSecurity";
    private static String mEncodingPsw = MD5Utils.getMD5(BuildInfoUtils.getHardwareId());
    private static boolean sIsInitSuccess;
    private SharedPreferences mSecureStore;

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public Security.EncryptionType getEncryptionType() {
        return Security.EncryptionType.NONE_ENCODING;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public ISecurityModule init(Context context) throws Exception {
        synchronized (this) {
            sIsInitSuccess = true;
            this.mSecureStore = context.getSharedPreferences(TAG, 0);
        }
        return this;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public ISecurityModule initForIndividual(Context context) throws Exception {
        return this;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public boolean isInitAndIndivSuccess() {
        return sIsInitSuccess;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public boolean isInitSuccess() {
        return sIsInitSuccess;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String getIndividualFlag() {
        return "";
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void saveIndividualFlag() {
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void clearIndividualData() {
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String getIndividualRequestStr() throws Exception {
        return "";
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public boolean individualWithData(String data) throws Exception {
        return true;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String generateIndividualDataForServer() throws Exception {
        return "";
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void destroy() {
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void saveMCUSecurityKey(boolean force) {
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void asyncGetMCUSecurityKey(ICallback<byte[], String> callback) {
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String encode(String data) {
        return data;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String decode(String data) {
        return data;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String getString(String key) {
        String savedContent = this.mSecureStore.getString(key, "");
        if (TextUtils.isEmpty(savedContent)) {
            return "";
        }
        byte[] decryptResult = null;
        try {
            byte[] sourceBytes = SecurityCommon.parseHexStr2Byte(savedContent);
            SecretKeySpec skeySpec = new SecretKeySpec(mEncodingPsw.getBytes(), NONE_ENCODING_ALGORITHM);
            Cipher cipher = Cipher.getInstance(NONE_ENCODING_ALGORITHM);
            cipher.init(2, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            decryptResult = cipher.doFinal(sourceBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (TextUtils.isEmpty(value)) {
            this.mSecureStore.edit().putString(key, value).commit();
            return;
        }
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(mEncodingPsw.getBytes(), NONE_ENCODING_ALGORITHM);
            Cipher cipher = Cipher.getInstance(NONE_ENCODING_ALGORITHM);
            cipher.init(1, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] encoded = cipher.doFinal(value.getBytes(XmartV1Constants.UTF8_ENCODING));
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
        try {
            LinkedHashMap<String, String> tokens = new LinkedHashMap<>();
            int extendedLength = 0;
            for (int i = 0; i < ids.length; i++) {
                String token = getString(ids[i]);
                if (!TextUtils.isEmpty(token)) {
                    tokens.put(ids[i], token);
                    extendedLength += ids[i].length() + 2 + token.length();
                }
            }
            if (extendedLength > 0) {
                ByteBuffer buffer = ByteBuffer.allocate(data.length + extendedLength).put(data);
                for (int i2 = 0; i2 < ids.length; i2++) {
                    buffer.put((byte) 38).put(ids[i2].getBytes(XmartV1Constants.UTF8_ENCODING)).put((byte) 61).put(tokens.get(ids[i2]).getBytes(XmartV1Constants.UTF8_ENCODING));
                }
                data = buffer.array();
            }
            return new String(Base64.encode(data, 2), XmartV1Constants.UTF8_ENCODING);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
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
        return KeyStoreHelper.decryptStringWithKey(data, key, Iv.getBytes());
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void asyncSaveToken(final String[] ids, final String[] token, final Runnable runnable) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.security.none.NoneSecurity.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    NoneSecurity.this.saveToken(ids, token);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                runnable.run();
            }
        });
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void asyncSaveTokenWithListener(final String[] ids, final String[] token, final ISecurityModule.ResultListener listener) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.security.none.NoneSecurity.2
            @Override // java.lang.Runnable
            public void run() {
                ISecurityModule.ResultListener.RESULT result = ISecurityModule.ResultListener.RESULT.FAIL;
                String exceptionString = "";
                try {
                    NoneSecurity.this.saveToken(ids, token);
                    result = ISecurityModule.ResultListener.RESULT.SUCCEED;
                } catch (Throwable e) {
                    exceptionString = e.getMessage();
                    e.printStackTrace();
                }
                listener.onResult(result, exceptionString);
            }
        });
    }

    public void saveToken(String[] ids, String[] tokens) {
        if (ids.length != tokens.length) {
            LogUtils.d(TAG, "Invalid tokens.");
            return;
        }
        for (int i = 0; i < ids.length; i++) {
            setString(ids[i], tokens[i]);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class Holder {
        private static final NoneSecurity INSTANCE = new NoneSecurity();

        private Holder() {
        }
    }

    public static NoneSecurity getInstance() {
        return Holder.INSTANCE;
    }

    private NoneSecurity() {
    }
}

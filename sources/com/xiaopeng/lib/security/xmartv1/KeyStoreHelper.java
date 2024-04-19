package com.xiaopeng.lib.security.xmartv1;

import android.os.Build;
import android.security.keystore.KeyProtection;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.xiaopeng.lib.utils.FileUtils;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.MD5Utils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
@VisibleForTesting
/* loaded from: classes.dex */
public final class KeyStoreHelper {
    private static KeyStore sKeyStore;
    private static boolean sUsedDefaultKeyStore;
    private static String TAG = "KeyStoreHelper";
    private static String PASSWORD = MD5Utils.getMD5(XmartV1Constants.KEY_MANAGER_PASSWORD);
    private static String KEY_STORE_PATH = "/private/sec/XpengAndroidKeyStore";
    private static HashMap<String, SecretKey> sSecretKeyMap = new HashMap<>();

    public static boolean init() {
        if (sKeyStore != null) {
            return true;
        }
        boolean result = loadWithXmartKeyStore();
        if (!result) {
            return loadWithDefaultKeyStore();
        }
        return result;
    }

    public static boolean hasValidIndividualKeys() {
        if (sKeyStore == null) {
            LogUtils.d(TAG, "KeyStore was not initialized!");
            return false;
        }
        for (int no = 0; no < XmartV1Constants.LOCAL_KEYS_NUM; no++) {
            try {
                if (!sKeyStore.containsAlias(XmartV1Constants.LOCAL_KEYS_PREFIX + no)) {
                    return false;
                }
            } catch (Exception ex) {
                LogUtils.d(TAG, ex);
                return false;
            }
        }
        return true;
    }

    public static boolean cleanIndividualKeys() throws KeyStoreException {
        if (sKeyStore == null) {
            LogUtils.d(TAG, "KeyStore was not initialized!");
            return false;
        }
        for (int no = 0; no < XmartV1Constants.LOCAL_KEYS_NUM; no++) {
            try {
                KeyStore keyStore = sKeyStore;
                if (keyStore.containsAlias(XmartV1Constants.LOCAL_KEYS_PREFIX + no)) {
                    KeyStore keyStore2 = sKeyStore;
                    keyStore2.deleteEntry(XmartV1Constants.LOCAL_KEYS_PREFIX + no);
                }
            } catch (Exception ex) {
                LogUtils.d(TAG, ex);
                throw ex;
            }
        }
        return true;
    }

    public static void writeSecretData(Map<String, byte[]> sharedSecrets) throws Exception {
        if (sKeyStore == null) {
            init();
        }
        if (sharedSecrets.size() != XmartV1Constants.LOCAL_KEYS_NUM) {
            throw new RuntimeException("Illegal security keys!!!!");
        }
        KeyProtection kp = null;
        if (Build.VERSION.SDK_INT >= 23) {
            kp = new KeyProtection.Builder(3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setRandomizedEncryptionRequired(false).build();
        }
        for (String key : sharedSecrets.keySet()) {
            SecretKeySpec signingKey = new SecretKeySpec(sharedSecrets.get(key), "AES");
            KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(signingKey);
            sKeyStore.setEntry(key, skEntry, kp);
        }
    }

    @Nullable
    public static byte[] decryptString(byte[] source, String keyAlias, byte[] iv) {
        if (source == null || source.length == 0 || TextUtils.isEmpty(keyAlias)) {
            return null;
        }
        return decryptWithStream(new ByteArrayInputStream(source), keyAlias, iv);
    }

    @Nullable
    public static byte[] decryptStringWithKey(byte[] source, String key, byte[] iv) {
        if (source == null || source.length == 0 || iv == null || iv.length == 0) {
            return null;
        }
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        return decryptWithStreamAndKey(new ByteArrayInputStream(source), keySpec, iv);
    }

    @Nullable
    public static byte[] decryptWithByteBuffer(ByteBuffer buffer, String keyAlias, byte[] iv) {
        if (buffer == null || TextUtils.isEmpty(keyAlias)) {
            return null;
        }
        return decryptWithStream(new ByteBufferInputStream(buffer), keyAlias, iv);
    }

    @Nullable
    private static byte[] decryptWithStream(InputStream stream, String keyAlias, byte[] iv) {
        if (stream == null || TextUtils.isEmpty(keyAlias)) {
            return null;
        }
        try {
            SecretKey secretKey = getSecretKey(keyAlias);
            if (secretKey == null) {
                return null;
            }
            byte[] values = decryptWithStreamAndKey(stream, secretKey, iv);
            return values;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static byte[] decryptGeneral(byte[] source, String keyAlias) {
        try {
            SecretKey secretKey = getSecretKey(keyAlias);
            if (secretKey == null) {
                return null;
            }
            byte[] result = decryptInternal(source, secretKey);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    private static byte[] decryptInternal(byte[] source, SecretKey secretKey) {
        if (source == null || source.length == 0) {
            return null;
        }
        try {
            Cipher inCipher = Cipher.getInstance(XmartV1Constants.AES_GCM_NO_PADDING);
            inCipher.init(2, secretKey, getParamsSpec(XmartV1Constants.FIXED_IV));
            byte[] values = inCipher.doFinal(source);
            return values;
        } catch (Exception e) {
            LogUtils.d(TAG, e);
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    private static byte[] decryptWithStreamAndKey(InputStream stream, SecretKey secretKey, byte[] iv) {
        byte[] values = null;
        if (stream == null || iv == null || iv.length == 0) {
            return null;
        }
        try {
            Cipher output = Cipher.getInstance(XmartV1Constants.AES_GCM_NO_PADDING);
            output.init(2, secretKey, getParamsSpec(iv));
            CipherInputStream cipherInputStream = new CipherInputStream(stream, output);
            ArrayList<Byte> decoded = new ArrayList<>();
            while (true) {
                int nextByte = cipherInputStream.read();
                if (nextByte == -1) {
                    break;
                }
                decoded.add(Byte.valueOf((byte) nextByte));
            }
            values = new byte[decoded.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = decoded.get(i).byteValue();
            }
        } catch (Exception e) {
            String str = TAG;
            LogUtils.d(str, "Exception:" + e);
        }
        return values;
    }

    @Nullable
    public static byte[] encryptString(byte[] source, String keyAlias, byte[] iv) {
        if (source == null || source.length == 0 || TextUtils.isEmpty(keyAlias) || iv == null || iv.length == 0) {
            return null;
        }
        try {
            SecretKey secretKey = getSecretKey(keyAlias);
            if (secretKey == null) {
                return null;
            }
            byte[] result = encryptInternal(source, secretKey, iv);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static byte[] encryptWithKey(byte[] source, String key, byte[] iv) {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        return encryptInternal(source, keySpec, iv);
    }

    @Nullable
    public static byte[] encryptGeneral(byte[] source, String keyAlias) {
        try {
            SecretKey secretKey = getSecretKey(keyAlias);
            if (secretKey == null) {
                return null;
            }
            byte[] result = encryptInternal(source, secretKey);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void store() {
        if (sUsedDefaultKeyStore) {
            saveKeyStoreToFile();
        }
    }

    @Nullable
    private static byte[] encryptInternal(byte[] source, SecretKey secretKey, byte[] iv) {
        byte[] values = null;
        if (source == null || source.length == 0 || iv == null || iv.length == 0) {
            return null;
        }
        try {
            Cipher inCipher = Cipher.getInstance(XmartV1Constants.AES_GCM_NO_PADDING);
            inCipher.init(1, secretKey, getParamsSpec(iv));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inCipher);
            cipherOutputStream.write(source);
            cipherOutputStream.close();
            values = outputStream.toByteArray();
            FileUtils.closeQuietly(outputStream);
            return values;
        } catch (Exception e) {
            LogUtils.d(TAG, e);
            e.printStackTrace();
            return values;
        }
    }

    @Nullable
    private static byte[] encryptInternal(byte[] source, SecretKey secretKey) {
        if (source == null || source.length == 0) {
            return null;
        }
        try {
            Cipher inCipher = Cipher.getInstance(XmartV1Constants.AES_GCM_NO_PADDING);
            inCipher.init(1, secretKey, getParamsSpec(XmartV1Constants.FIXED_IV));
            byte[] values = inCipher.doFinal(source);
            return values;
        } catch (Exception e) {
            LogUtils.d(TAG, e);
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    private static SecretKey getSecretKey(String keyAlias) throws Exception {
        if (!sKeyStore.containsAlias(keyAlias)) {
            String str = TAG;
            LogUtils.d(str, "Not found the key " + keyAlias);
            return null;
        }
        long start = System.currentTimeMillis();
        SecretKey secretKey = sSecretKeyMap.get(keyAlias);
        if (secretKey == null) {
            secretKey = (SecretKey) sKeyStore.getKey(keyAlias, null);
            sSecretKeyMap.put(keyAlias, secretKey);
        }
        if (secretKey == null) {
            String str2 = TAG;
            LogUtils.d(str2, "Failed to load secret key:" + keyAlias);
            return null;
        }
        String str3 = TAG;
        Log.i(str3, "cost time:\t" + (System.currentTimeMillis() - start));
        return secretKey;
    }

    /* loaded from: classes.dex */
    private static final class ByteBufferInputStream extends InputStream {
        ByteBuffer buf;

        public ByteBufferInputStream(ByteBuffer buf) {
            this.buf = buf;
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            if (!this.buf.hasRemaining()) {
                return -1;
            }
            return this.buf.get() & 255;
        }

        @Override // java.io.InputStream
        public int read(byte[] bytes, int off, int len) throws IOException {
            if (!this.buf.hasRemaining()) {
                return -1;
            }
            int len2 = Math.min(len, this.buf.remaining());
            this.buf.get(bytes, off, len2);
            return len2;
        }
    }

    private static boolean loadWithXmartKeyStore() {
        boolean result = false;
        try {
            sKeyStore = KeyStore.getInstance(XmartV1Constants.KEY_STORE_TYPE);
            sKeyStore.load(null);
            result = true;
            sUsedDefaultKeyStore = false;
            return true;
        } catch (Exception ex) {
            String str = TAG;
            LogUtils.d(str, "Failed to load key store, reason:" + ex);
            return result;
        }
    }

    private static boolean loadWithDefaultKeyStore() {
        boolean result = false;
        FileInputStream inputStream = null;
        try {
            try {
                sKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                if (new File(KEY_STORE_PATH).exists()) {
                    inputStream = new FileInputStream(KEY_STORE_PATH);
                    sKeyStore.load(inputStream, PASSWORD.toCharArray());
                } else {
                    sKeyStore.load(null);
                }
                result = true;
                sUsedDefaultKeyStore = true;
            } catch (Exception ex) {
                String str = TAG;
                LogUtils.d(str, "Failed to load default key store, reason:" + ex);
            }
            return result;
        } finally {
            FileUtils.closeQuietly(inputStream);
        }
    }

    private static boolean saveKeyStoreToFile() {
        boolean result = false;
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(KEY_STORE_PATH);
            sKeyStore.store(outputStream, PASSWORD.toCharArray());
            result = true;
        } catch (Exception e) {
        } catch (Throwable th) {
            FileUtils.closeQuietly(outputStream);
            throw th;
        }
        FileUtils.closeQuietly(outputStream);
        return result;
    }

    private static AlgorithmParameterSpec getParamsSpec(byte[] iv) {
        if (Build.VERSION.SDK_INT < 21) {
            return new IvParameterSpec(iv, 0, iv.length);
        }
        return new GCMParameterSpec(128, iv);
    }
}

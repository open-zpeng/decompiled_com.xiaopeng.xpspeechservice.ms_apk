package org.litepal.util.cipher;

import android.text.TextUtils;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/* loaded from: classes.dex */
public class CipherUtil {
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static String aesKey = "LitePalKey";

    public static String aesEncrypt(String plainText) {
        if (TextUtils.isEmpty(plainText)) {
            return plainText;
        }
        try {
            return AESCrypt.encrypt(aesKey, plainText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String aesDecrypt(String encryptedText) {
        if (TextUtils.isEmpty(encryptedText)) {
            return encryptedText;
        }
        try {
            return AESCrypt.decrypt(aesKey, encryptedText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String md5Encrypt(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(plainText.getBytes(Charset.defaultCharset()));
            return new String(toHex(digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static char[] toHex(byte[] data) {
        char[] toDigits = DIGITS_UPPER;
        int l = data.length;
        char[] out = new char[l << 1];
        int j = 0;
        for (int i = 0; i < l; i++) {
            int j2 = j + 1;
            out[j] = toDigits[(data[i] & 240) >>> 4];
            j = j2 + 1;
            out[j2] = toDigits[data[i] & 15];
        }
        return out;
    }
}

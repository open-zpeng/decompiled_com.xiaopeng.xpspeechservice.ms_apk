package com.xiaopeng.lib.framework.netchannelmodule.common.util;
/* loaded from: classes.dex */
public class EncryptionUtil {
    private static final String ALGORITHM = "EncryptionUtil-XOR";

    public static byte[] encrypt(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    public static byte[] decrypt(byte[] data, byte[] key) {
        return encrypt(data, key);
    }
}

package com.xiaopeng.lib.security.xmartv1;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Base64;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;
@VisibleForTesting
/* loaded from: classes.dex */
public final class EncodingItem {
    private static final int FIXED_DATA_LENGTH = 15;
    private static final String TAG = "EncodingItem";
    private static byte[] sDeviceId;
    private static final EncodingItem sEmptyItem;
    private byte[] mAesIv;
    private String mEncodedContent;
    private byte mKeyIndex;
    private byte[] mNonce;
    private byte[] mRawContent;
    private long mTimestamp;
    private static final byte[] sMagicNumber = "XP".getBytes();
    private static final SecureRandom sSecureRandom = new SecureRandom();
    private static final ArrayList<String> sKeyAlias = new ArrayList<>();
    private static byte sVersion = XmartV1Constants.ALGORITHM_DEFAULT_REVISION;

    static {
        for (int i = 0; i < XmartV1Constants.LOCAL_KEYS_NUM; i++) {
            sKeyAlias.add(XmartV1Constants.LOCAL_KEYS_PREFIX + i);
        }
        sDeviceId = BuildInfoUtils.getHardwareId().getBytes();
        sEmptyItem = empty();
    }

    public static void resetRandomSeed() {
        sSecureRandom.setSeed(System.currentTimeMillis());
    }

    public static EncodingItem empty() {
        EncodingItem item = new EncodingItem();
        item.mRawContent = new byte[0];
        item.mEncodedContent = "";
        item.mKeyIndex = (byte) -1;
        return item;
    }

    public static String getGeneralKeyAlias() {
        return sKeyAlias.get(0);
    }

    @NonNull
    public static EncodingItem decode(byte[] encodedContent) {
        byte[] values = Base64.decode(encodedContent, 2);
        ByteBuffer byteBuffer = ByteBuffer.wrap(values);
        if (byteBuffer.get() != sMagicNumber[0] || byteBuffer.get() != sMagicNumber[1]) {
            LogUtils.d(TAG, "Wrong magic number!");
            return sEmptyItem;
        } else if (byteBuffer.get() != sVersion) {
            LogUtils.d(TAG, "Wrong version number!");
            return sEmptyItem;
        } else {
            EncodingItem item = new EncodingItem();
            item.mKeyIndex = byteBuffer.get();
            int i = byteBuffer.get();
            if (i > 0) {
                byte[] temp = new byte[i];
                byteBuffer.get(temp);
            }
            item.mNonce = new byte[byteBuffer.get()];
            item.mTimestamp = byteBuffer.getLong();
            item.mAesIv = new byte[byteBuffer.get()];
            byteBuffer.get(item.mAesIv);
            int contentLength = values.length - item.getBasicLength();
            ByteBuffer contentBuffer = ByteBuffer.wrap(values, item.getBasicLength(), contentLength);
            byte[] output = KeyStoreHelper.decryptWithByteBuffer(contentBuffer, sKeyAlias.get(item.mKeyIndex), item.mAesIv);
            if (output != null && output.length > 0) {
                ByteBuffer decodedBuffer = ByteBuffer.wrap(output);
                decodedBuffer.get(item.mNonce);
                int rawContentLength = output.length - item.mNonce.length;
                item.mRawContent = new byte[rawContentLength];
                decodedBuffer.get(item.mRawContent);
                return item;
            }
            return sEmptyItem;
        }
    }

    @NonNull
    public static EncodingItem encode(byte[] content) {
        return encode(content, pickOneRandomKey());
    }

    @NonNull
    public static EncodingItem encode(byte[] content, int keyIndex) {
        if (content == null && content.length == 0) {
            return sEmptyItem;
        }
        if (keyIndex < 0 || keyIndex > XmartV1Constants.LOCAL_KEYS_NUM) {
            throw new RuntimeException("Wrong security key index!");
        }
        byte[] nonce = UUID.randomUUID().toString().getBytes();
        byte[] iv = new byte[XmartV1Constants.ALGORITHM_IV_LENGTH];
        sSecureRandom.nextBytes(iv);
        EncodingItem item = new EncodingItem();
        item.mNonce = nonce;
        item.mAesIv = iv;
        item.mKeyIndex = (byte) keyIndex;
        item.mRawContent = content;
        item.mTimestamp = System.currentTimeMillis();
        if (!item.encodeInternal()) {
            return sEmptyItem;
        }
        return item;
    }

    public byte[] nonce() {
        return this.mNonce;
    }

    public long timestamp() {
        return this.mTimestamp;
    }

    public boolean isEmpty() {
        byte[] bArr = this.mRawContent;
        return bArr == null || bArr.length == 0;
    }

    @Nullable
    public byte[] rawContent() {
        return this.mRawContent;
    }

    @NonNull
    public String rawContentString() {
        if (isEmpty()) {
            return "";
        }
        try {
            String result = new String(this.mRawContent, XmartV1Constants.UTF8_ENCODING);
            return result;
        } catch (UnsupportedEncodingException e) {
            LogUtils.d(TAG, "Failed to convert raw content to string!");
            return "";
        }
    }

    @Nullable
    public String encodedString() {
        return this.mEncodedContent;
    }

    private boolean encodeInternal() {
        byte[] value = ByteBuffer.allocate(this.mRawContent.length + this.mNonce.length).put(this.mNonce).put(this.mRawContent).array();
        byte[] output = KeyStoreHelper.encryptString(value, sKeyAlias.get(this.mKeyIndex), this.mAesIv);
        if (output == null || output.length == 0) {
            LogUtils.d(TAG, "Failed to encrypt the string.");
            return false;
        }
        byte[] values = ByteBuffer.allocate(getBasicLength() + output.length).put(sMagicNumber).put(sVersion).put(this.mKeyIndex).put((byte) sDeviceId.length).put(sDeviceId).put((byte) this.mNonce.length).putLong(this.mTimestamp).put((byte) this.mAesIv.length).put(this.mAesIv).put(output).array();
        this.mEncodedContent = Base64.encodeToString(values, 2);
        return true;
    }

    private EncodingItem() {
    }

    private int getBasicLength() {
        return sDeviceId.length + 15 + this.mAesIv.length;
    }

    private static byte pickOneRandomKey() {
        return (byte) ((sSecureRandom.nextInt() & 127) % XmartV1Constants.LOCAL_KEYS_NUM);
    }
}

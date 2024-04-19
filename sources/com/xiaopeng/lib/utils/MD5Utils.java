package com.xiaopeng.lib.utils;

import com.xiaopeng.lib.security.xmartv1.XmartV1Constants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/* loaded from: classes.dex */
public class MD5Utils {
    public static String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes(XmartV1Constants.UTF8_ENCODING));
            byte[] encryption = md5.digest();
            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(encryption[i] & 255).length() == 1) {
                    strBuf.append("0");
                    strBuf.append(Integer.toHexString(encryption[i] & 255));
                } else {
                    strBuf.append(Integer.toHexString(encryption[i] & 255));
                }
            }
            return strBuf.toString();
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (NoSuchAlgorithmException e2) {
            return "";
        }
    }

    public static String getFileMd5(File file) throws FileNotFoundException {
        RandomAccessFile randomAccessFile = null;
        try {
            try {
                try {
                    try {
                        try {
                            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                            if (file == null) {
                                return "";
                            }
                            if (!file.exists()) {
                                if (0 != 0) {
                                    try {
                                        randomAccessFile.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return "";
                            }
                            randomAccessFile = new RandomAccessFile(file, "r");
                            byte[] bytes = new byte[10485760];
                            while (true) {
                                int len = randomAccessFile.read(bytes);
                                if (len == -1) {
                                    break;
                                }
                                messageDigest.update(bytes, 0, len);
                            }
                            BigInteger bigInt = new BigInteger(1, messageDigest.digest());
                            String md5 = bigInt.toString(16);
                            while (md5.length() < 32) {
                                md5 = "0" + md5;
                            }
                            try {
                                randomAccessFile.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                            return md5;
                        } catch (IOException e3) {
                            e3.printStackTrace();
                            return "";
                        }
                    } catch (FileNotFoundException e4) {
                        e4.printStackTrace();
                        if (randomAccessFile != null) {
                            randomAccessFile.close();
                        }
                        return "";
                    }
                } catch (NoSuchAlgorithmException e5) {
                    e5.printStackTrace();
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                    return "";
                }
            } catch (IOException e6) {
                e6.printStackTrace();
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                return "";
            }
        } finally {
            if (0 != 0) {
                try {
                    randomAccessFile.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
        }
    }
}

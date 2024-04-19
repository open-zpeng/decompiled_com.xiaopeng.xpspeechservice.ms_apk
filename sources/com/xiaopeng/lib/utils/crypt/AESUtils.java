package com.xiaopeng.lib.utils.crypt;

import android.text.TextUtils;
import android.util.Base64;
import com.xiaopeng.lib.security.xmartv1.XmartV1Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
/* loaded from: classes.dex */
public class AESUtils {
    public static boolean decrypt(File sourceFile, File decryptFile, String sKey) throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            try {
                try {
                    try {
                        Cipher cipher = initAESCipher(sKey, 2);
                        inputStream = new FileInputStream(sourceFile);
                        outputStream = new FileOutputStream(decryptFile);
                        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
                        byte[] buffer = new byte[1048576];
                        while (true) {
                            int r = inputStream.read(buffer);
                            if (r < 0) {
                                break;
                            }
                            cipherOutputStream.write(buffer, 0, r);
                        }
                        cipherOutputStream.close();
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        return true;
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        return false;
                    }
                } catch (IOException e5) {
                    e5.printStackTrace();
                    return false;
                }
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return false;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            if (0 != 0) {
                try {
                    outputStream.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static boolean encrypt(File sourceFile, File encryptFile, String sKey) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            try {
                try {
                    try {
                        Cipher cipher = initAESCipher(sKey, 1);
                        inputStream = new FileInputStream(sourceFile);
                        outputStream = new FileOutputStream(encryptFile);
                        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
                        byte[] buffer = new byte[1048576];
                        while (true) {
                            int r = inputStream.read(buffer);
                            if (r < 0) {
                                break;
                            }
                            cipherOutputStream.write(buffer, 0, r);
                        }
                        cipherOutputStream.close();
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                        return true;
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        return false;
                    }
                } catch (IOException e5) {
                    e5.printStackTrace();
                    return false;
                }
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return false;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            if (0 != 0) {
                try {
                    outputStream.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static boolean encrypt(InputStream inputStream, File encryptFile, String sKey) {
        OutputStream outputStream = null;
        try {
            try {
                Cipher cipher = initAESCipher(sKey, 1);
                outputStream = new FileOutputStream(encryptFile);
                CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
                byte[] buffer = new byte[1048576];
                while (true) {
                    int r = inputStream.read(buffer);
                    if (r < 0) {
                        break;
                    }
                    cipherOutputStream.write(buffer, 0, r);
                }
                cipherOutputStream.flush();
                cipherOutputStream.close();
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                return true;
            } catch (Exception e3) {
                e3.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                return false;
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static String encrypt(String content, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(1, key);
            byte[] result = cipher.doFinal(byteContent);
            return parseByte2HexStr(result);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean encryptFile(String key, String sourceFilePath, String destFilePath) {
        if (TextUtils.isEmpty(sourceFilePath) || TextUtils.isEmpty(destFilePath)) {
            return false;
        }
        FileInputStream in = null;
        FileOutputStream out = null;
        CipherInputStream cipherInputStream = null;
        try {
            try {
                File outFile = new File(destFilePath);
                if (!outFile.exists()) {
                    if (!outFile.getParentFile().exists()) {
                        outFile.getParentFile().mkdir();
                    }
                    outFile.createNewFile();
                }
                Cipher cipher = initAESCipher(key, 1);
                in = new FileInputStream(sourceFilePath);
                out = new FileOutputStream(outFile);
                cipherInputStream = new CipherInputStream(in, cipher);
                byte[] cache = new byte[1024];
                while (true) {
                    int nRead = cipherInputStream.read(cache);
                    if (nRead == -1) {
                        break;
                    }
                    out.write(cache, 0, nRead);
                }
                out.flush();
                out.getFD().sync();
                try {
                    cipherInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                try {
                    in.close();
                    return true;
                } catch (IOException e3) {
                    e3.printStackTrace();
                    return true;
                }
            } catch (Throwable th) {
                if (cipherInputStream != null) {
                    try {
                        cipherInputStream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e7) {
            e7.printStackTrace();
            if (cipherInputStream != null) {
                try {
                    cipherInputStream.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e9) {
                    e9.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e10) {
                    e10.printStackTrace();
                }
            }
            return false;
        }
    }

    public static String parseByte2HexStr(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(buf.length * 2);
        for (int i = 0; i < buf.length; i++) {
            result.append("0123456789ABCDEF".charAt((buf[i] >> 4) & 15));
            result.append("0123456789ABCDEF".charAt(buf[i] & 15));
        }
        return result.toString();
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        int len = hexStr.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexStr.substring(i * 2, (i * 2) + 2), 16).byteValue();
        }
        return result;
    }

    public static String decrypt(String content, String password) {
        try {
            byte[] keyBytes = password.getBytes(Charset.forName(XmartV1Constants.UTF8_ENCODING));
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(2, key);
            byte[] result = cipher.doFinal(parseHexStr2Byte(content));
            return new String(result);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Cipher initAESCipher(String sKey, int cipherMode) {
        Cipher cipher = null;
        try {
            byte[] codeFormat = sKey.getBytes();
            SecretKeySpec key = new SecretKeySpec(codeFormat, "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, key);
            return cipher;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return cipher;
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
            return cipher;
        } catch (NoSuchPaddingException e3) {
            e3.printStackTrace();
            return cipher;
        }
    }

    public static String encryptWithBase64(String content, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(1, key);
            byte[] result = cipher.doFinal(byteContent);
            return Base64.encodeToString(result, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptWithBase64(String content, String password) {
        try {
            byte[] keyBytes = password.getBytes(Charset.forName(XmartV1Constants.UTF8_ENCODING));
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(2, key);
            byte[] result = cipher.doFinal(Base64.decode(content, 0));
            return new String(result);
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }
}

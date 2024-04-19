package com.xiaopeng.lib.utils;

import android.text.TextUtils;
import android.util.Base64;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/* loaded from: classes.dex */
public class ZipUtils {
    private static byte[] ZIP_HEADER_1 = {80, 75, 3, 4};
    private static byte[] ZIP_HEADER_2 = {80, 75, 5, 6};
    private static int ZIP_HELADER_LENGTH = 4;

    public static boolean isArchiveFile(File file) {
        boolean z = false;
        if (file == null || file.isDirectory()) {
            return false;
        }
        boolean isArchive = false;
        try {
            InputStream input = new FileInputStream(file);
            byte[] buffer = new byte[ZIP_HELADER_LENGTH];
            int length = input.read(buffer, 0, ZIP_HELADER_LENGTH);
            if (length == ZIP_HELADER_LENGTH) {
                isArchive = (Arrays.equals(ZIP_HEADER_1, buffer) || Arrays.equals(ZIP_HEADER_2, buffer)) ? true : true;
            }
            input.close();
        } catch (IOException e) {
        }
        return isArchive;
    }

    public static String compressForGzip(String unGzipStr) {
        if (TextUtils.isEmpty(unGzipStr)) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(unGzipStr.getBytes());
            gzip.close();
            byte[] encode = baos.toByteArray();
            baos.flush();
            baos.close();
            return compressForBase64(encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static String decompressForGzip(String gzipStr) {
        if (TextUtils.isEmpty(gzipStr)) {
            return null;
        }
        byte[] t = decompressForBase64(gzipStr);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(t);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            while (true) {
                int n = gzip.read(buffer, 0, buffer.length);
                if (n > 0) {
                    out.write(buffer, 0, n);
                } else {
                    gzip.close();
                    in.close();
                    out.close();
                    return out.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String compressForBase64(byte[] encode) {
        return compressForBase64(encode, 0);
    }

    public static String compressForBase64(byte[] encode, int flags) {
        if (encode == null || encode.length == 0) {
            return null;
        }
        return Base64.encodeToString(encode, flags);
    }

    public static byte[] decompressForBase64(String str) {
        return decompressForBase64(str, 0);
    }

    public static byte[] decompressForBase64(String str, int flags) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return Base64.decode(str, flags);
    }

    public static byte[] compressForUpload(String unGzipStr) {
        if (TextUtils.isEmpty(unGzipStr)) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(unGzipStr.getBytes());
            gzip.close();
            byte[] encode = baos.toByteArray();
            baos.flush();
            baos.close();
            return encode;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static String decompressForUpload(byte[] uploadBytes) {
        if (uploadBytes == null || uploadBytes.length == 0) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(uploadBytes);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            while (true) {
                int n = gzip.read(buffer, 0, buffer.length);
                if (n > 0) {
                    out.write(buffer, 0, n);
                } else {
                    gzip.close();
                    in.close();
                    out.close();
                    return out.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean zip(String src, String dest) throws IOException {
        ZipOutputStream out = null;
        boolean isFlag = false;
        try {
            try {
                File outFile = new File(dest);
                File fileOrDirectory = new File(src);
                out = new ZipOutputStream(new FileOutputStream(outFile));
                if (fileOrDirectory.isFile()) {
                    zipFileOrDirectory(out, fileOrDirectory, "");
                } else {
                    File[] entries = fileOrDirectory.listFiles();
                    if (entries != null) {
                        for (File file : entries) {
                            zipFileOrDirectory(out, file, "");
                        }
                    }
                }
                out.flush();
                isFlag = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                isFlag = false;
            }
        } catch (Throwable th) {
        }
        FileUtils.closeQuietly(out);
        return isFlag;
    }

    public static void zipFileOrDirectory(ZipOutputStream out, File fileOrDirectory, String curPath) throws IOException {
        zipFileOrDirectory(out, fileOrDirectory, curPath, true);
    }

    public static void zipFileOrDirectory(ZipOutputStream out, File fileOrDirectory, String curPath, boolean ignoreEmptyDir) throws IOException {
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        try {
            try {
                if (!fileOrDirectory.isDirectory()) {
                    byte[] buffer = new byte[4096];
                    fis = new FileInputStream(fileOrDirectory);
                    bis = new BufferedInputStream(fis);
                    ZipEntry entry = new ZipEntry(curPath + fileOrDirectory.getName());
                    out.putNextEntry(entry);
                    while (true) {
                        int bytes_read = bis.read(buffer);
                        if (bytes_read == -1) {
                            break;
                        }
                        out.write(buffer, 0, bytes_read);
                    }
                    out.flush();
                    out.closeEntry();
                } else {
                    File[] entries = fileOrDirectory.listFiles();
                    if (entries != null && entries.length > 0) {
                        for (File file : entries) {
                            zipFileOrDirectory(out, file, curPath + fileOrDirectory.getName() + "/", ignoreEmptyDir);
                        }
                    } else if (!ignoreEmptyDir) {
                        ZipEntry entry2 = new ZipEntry(fileOrDirectory.getAbsolutePath() + "/");
                        out.putNextEntry(entry2);
                        out.closeEntry();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            FileUtils.closeQuietly(null);
            FileUtils.closeQuietly(null);
        }
    }

    public static File zipFile(String srcFilePath, String dstFilePath) {
        ZipOutputStream out = null;
        FileInputStream ins = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        File dst = new File(dstFilePath);
        try {
            try {
                out = new ZipOutputStream(new FileOutputStream(dst));
                ins = new FileInputStream(srcFilePath);
                out.putNextEntry(new ZipEntry(new File(srcFilePath).getName()));
                bos = new BufferedOutputStream(out);
                bis = new BufferedInputStream(ins);
                byte[] buffer = new byte[1024];
                while (true) {
                    int tag = bis.read(buffer, 0, 1024);
                    if (tag == -1) {
                        bos.flush();
                        out.flush();
                        return dst;
                    }
                    bos.write(buffer, 0, tag);
                }
            } catch (Exception e) {
                e.printStackTrace();
                FileUtils.closeQuietly(out);
                FileUtils.closeQuietly(ins);
                FileUtils.closeQuietly(bos);
                FileUtils.closeQuietly(bis);
                return null;
            }
        } finally {
            FileUtils.closeQuietly(out);
            FileUtils.closeQuietly(ins);
            FileUtils.closeQuietly(bos);
            FileUtils.closeQuietly(bis);
        }
    }

    public static File zipMultiFiles(String dstFilePath, List<String> filePaths) throws IOException {
        return zipMultiFiles(dstFilePath, filePaths, true);
    }

    public static File zipMultiFiles(String dstFilePath, List<String> filePaths, boolean ignoreEmptyDir) throws IOException {
        try {
            return zipMultiFilesWithThrow(dstFilePath, filePaths, ignoreEmptyDir);
        } catch (Exception e) {
            e.printStackTrace();
            return new File(dstFilePath);
        }
    }

    public static File zipMultiFilesWithThrow(String dstFilePath, List<String> filePaths) throws IOException {
        return zipMultiFilesWithThrow(dstFilePath, filePaths, true);
    }

    public static File zipMultiFilesWithThrow(String dstFilePath, List<String> filePaths, boolean ignoreEmptyDir) throws IOException {
        File zipFile = new File(dstFilePath);
        if (zipFile.exists()) {
            zipFile.delete();
        }
        ZipOutputStream zipOutputStream = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(dstFilePath));
            zipOutputStream = new ZipOutputStream(fos);
            for (String filePath : filePaths) {
                File file = new File(filePath);
                String parent = file.getParent();
                if (parent == null) {
                    parent = "";
                }
                zipFileOrDirectory(zipOutputStream, file, parent + "/", ignoreEmptyDir);
            }
            zipOutputStream.flush();
            zipOutputStream.closeEntry();
            FileUtils.closeQuietly(zipOutputStream);
            FileUtils.closeQuietly(fos);
            return new File(dstFilePath);
        } catch (Throwable th) {
            FileUtils.closeQuietly(zipOutputStream);
            FileUtils.closeQuietly(fos);
            throw th;
        }
    }

    public static String compressForGzipAndBase64NoWrap(String unGzipStr) {
        if (TextUtils.isEmpty(unGzipStr)) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(unGzipStr.getBytes());
            gzip.close();
            byte[] encode = baos.toByteArray();
            baos.flush();
            baos.close();
            return compressForBase64(encode, 2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static String decompressForGzipAndBase64NoWrap(String gzipStr) {
        if (TextUtils.isEmpty(gzipStr)) {
            return null;
        }
        byte[] t = decompressForBase64(gzipStr, 2);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(t);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            while (true) {
                int n = gzip.read(buffer, 0, buffer.length);
                if (n > 0) {
                    out.write(buffer, 0, n);
                } else {
                    gzip.close();
                    in.close();
                    out.close();
                    return out.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

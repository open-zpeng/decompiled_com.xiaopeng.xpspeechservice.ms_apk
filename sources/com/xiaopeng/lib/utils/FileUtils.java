package com.xiaopeng.lib.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.text.TextUtils;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
/* loaded from: classes.dex */
public class FileUtils {
    private static final String FILE_DIR_NAME = "files";
    private static final String PACKAGE_NAME = "com.xiaopeng.xmart";
    public static final int SIZETYPE_B = 1;
    public static final int SIZETYPE_GB = 4;
    public static final int SIZETYPE_KB = 2;
    public static final int SIZETYPE_MB = 3;
    public static final long SIZE_1GB = 1073741824;
    public static final long SIZE_1KB = 1024;
    public static final long SIZE_1MB = 1048576;
    private static final String TAG = "FileUtils";
    private static final String XMART_USB_PATH = "/mnt/usbhost";

    /* loaded from: classes.dex */
    public interface OnUpdateFileCopyFromUsbListener {
        void onCopyFail();

        void onCopySuccess();
    }

    public static double getFileSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "getFileSize error!");
        }
        return FormatFileSize(blockSize, sizeType);
    }

    public static String getFileSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "getFileSize error!");
        }
        return FormatFileSize(blockSize);
    }

    private static long getFileSize(File file) throws FileNotFoundException {
        if (file != null && file.exists()) {
            return file.length();
        }
        throw new FileNotFoundException();
    }

    private static long getFileSizes(File file) throws Exception {
        long fileSize;
        long size = 0;
        File[] flist = file.listFiles();
        if (flist != null) {
            for (int i = 0; i < flist.length; i++) {
                if (flist[i].isDirectory()) {
                    fileSize = getFileSizes(flist[i]);
                } else {
                    fileSize = getFileSize(flist[i]);
                }
                size += fileSize;
            }
        } else {
            LogUtils.e(TAG, "File not exist");
        }
        return size;
    }

    private static String FormatFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (fileSize == 0) {
            return "0B";
        }
        if (fileSize < SIZE_1KB) {
            String fileSizeString = df.format(fileSize) + "B";
            return fileSizeString;
        } else if (fileSize < SIZE_1MB) {
            String fileSizeString2 = df.format(fileSize / 1024.0d) + "KB";
            return fileSizeString2;
        } else if (fileSize < SIZE_1GB) {
            String fileSizeString3 = df.format(fileSize / 1048576.0d) + "MB";
            return fileSizeString3;
        } else {
            String fileSizeString4 = df.format(fileSize / 1.073741824E9d) + "GB";
            return fileSizeString4;
        }
    }

    private static double FormatFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (sizeType == 1) {
            double fileSizeLong = Double.valueOf(df.format(fileS)).doubleValue();
            return fileSizeLong;
        } else if (sizeType == 2) {
            double fileSizeLong2 = Double.valueOf(df.format(fileS / 1024.0d)).doubleValue();
            return fileSizeLong2;
        } else if (sizeType == 3) {
            double fileSizeLong3 = Double.valueOf(df.format(fileS / 1048576.0d)).doubleValue();
            return fileSizeLong3;
        } else if (sizeType != 4) {
            return 0.0d;
        } else {
            double fileSizeLong4 = Double.valueOf(df.format(fileS / 1.073741824E9d)).doubleValue();
            return fileSizeLong4;
        }
    }

    public static String getFileBaseName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        int index = fileName.lastIndexOf(File.separator);
        if (index >= 0 && index < fileName.length()) {
            fileName = fileName.substring(index + 1);
        }
        int index2 = fileName.lastIndexOf(".");
        if (index2 > 0 && index2 < fileName.length()) {
            return fileName.substring(0, index2);
        }
        return fileName;
    }

    private static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        if (!status.equals("mounted")) {
            return false;
        }
        return true;
    }

    public static String getRootFilePath() {
        if (hasSDCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        }
        return Environment.getDataDirectory().getAbsolutePath() + "/data/";
    }

    public static String getSaveFilePath() {
        if (hasSDCard()) {
            return getRootFilePath() + PACKAGE_NAME + File.separator + FILE_DIR_NAME + File.separator;
        }
        return getRootFilePath() + PACKAGE_NAME + File.separator + FILE_DIR_NAME;
    }

    public static String storePicture(Bitmap bitmap, String storePath, int quality) {
        File file = new File(storePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File imageFile = new File(file, System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = null;
        FileOutputStream out = null;
        try {
            try {
                try {
                    try {
                        imageFile.createNewFile();
                        out = new FileOutputStream(imageFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                        out.flush();
                        if (0 != 0) {
                            fos.close();
                        }
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e2) {
                    e2.printStackTrace();
                    if (0 != 0) {
                        fos.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            } catch (IOException e3) {
                e3.printStackTrace();
                if (0 != 0) {
                    fos.close();
                }
                if (out != null) {
                    out.close();
                }
            }
            return imageFile.getAbsolutePath();
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fos.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                    throw th;
                }
            }
            if (out != null) {
                out.close();
            }
            throw th;
        }
    }

    public static void saveImageToGallery(Context context, Bitmap bmp, String subDir) {
        File appDir = new File(Environment.getExternalStorageDirectory(), subDir);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            try {
                try {
                    fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (Throwable th) {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e3) {
                e3.printStackTrace();
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (IOException e4) {
            e4.printStackTrace();
        }
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, (String) null);
        } catch (FileNotFoundException e5) {
            e5.printStackTrace();
        }
        context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));
    }

    public static String getFileStorePath(String subDir) {
        String storePath = getSaveFilePath();
        if (storePath.endsWith(File.separator)) {
            return storePath + subDir;
        }
        return storePath + File.separator + subDir;
    }

    public static String[] getUsbMountDir() {
        String dirs = SystemProperties.get("sys.usb.label", (String) null);
        LogUtils.d(TAG, "UsbMountDir-->" + dirs);
        if (TextUtils.isEmpty(dirs)) {
            return new String[]{XMART_USB_PATH};
        }
        return dirs.contains(",") ? dirs.split(",") : new String[]{dirs};
    }

    public static boolean isUsbAvailable() {
        File[] usbFiles;
        String[] usbMountDirs = getUsbMountDir();
        if (usbMountDirs != null) {
            for (String usbMountDir : usbMountDirs) {
                LogUtils.d(TAG, "subMountDir-->" + usbMountDir);
                File usbDir = new File(usbMountDir);
                if (usbDir.isDirectory() && (usbFiles = usbDir.listFiles()) != null && usbFiles.length > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void copyFile(String oldPath, String newPath, OnUpdateFileCopyFromUsbListener listener) {
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            try {
                try {
                    File oldFile = new File(oldPath);
                    if (oldFile.exists()) {
                        inStream = new FileInputStream(oldPath);
                        fs = new FileOutputStream(newPath);
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int byteRead = inStream.read(buffer);
                            if (byteRead == -1) {
                                break;
                            }
                            fs.write(buffer, 0, byteRead);
                        }
                        fs.flush();
                        if (listener != null) {
                            listener.onCopySuccess();
                        }
                    }
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    listener.onCopyFail();
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                }
            } catch (IOException e4) {
                e4.printStackTrace();
            }
        } catch (Throwable th) {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static boolean copyFile(String oldPath, String newPath) {
        InputStream inStream = null;
        FileOutputStream fs = null;
        boolean flag = false;
        try {
            try {
                try {
                    File oldFile = new File(oldPath);
                    if (oldFile.exists()) {
                        inStream = new FileInputStream(oldPath);
                        fs = new FileOutputStream(newPath);
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int byteRead = inStream.read(buffer);
                            if (byteRead == -1) {
                                break;
                            }
                            fs.write(buffer, 0, byteRead);
                        }
                        fs.flush();
                        flag = true;
                    }
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Throwable th) {
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (inStream != null) {
                        try {
                            inStream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e4.printStackTrace();
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                if (inStream != null) {
                    inStream.close();
                }
            }
            if (inStream != null) {
                inStream.close();
            }
        } catch (IOException e6) {
            e6.printStackTrace();
        }
        return flag;
    }

    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        try {
            File fileDir = new File(filePath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
        } catch (Exception e) {
        }
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return file;
    }

    public static void makeDirectory(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeTxtToFile(String content, String filePath, String fileName) {
        RandomAccessFile raf = null;
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;
        String strContent = content + "\r\n";
        try {
            try {
                try {
                    File file = new File(strFilePath);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    raf = new RandomAccessFile(file, "rwd");
                    raf.seek(file.length());
                    raf.write(strContent.getBytes());
                    raf.close();
                } catch (Exception e) {
                    LogUtils.w(TAG, "writeTxtToFile error!", e);
                    e.printStackTrace();
                    if (raf != null) {
                        raf.close();
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } catch (Throwable th) {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static void deleteFile(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                if (file.isFile()) {
                    file.delete();
                    return;
                }
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        deleteFile(files[i].getAbsolutePath());
                        if (files[i].isDirectory()) {
                            files[i].delete();
                        }
                    }
                }
                new File(path).delete();
            }
        }
    }

    public static long getDirSize(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                if (children == null || children.length == 0) {
                    return 0L;
                }
                long size = 0;
                for (File f : children) {
                    size += getDirSize(f);
                }
                return size;
            }
            long size2 = (file.length() / SIZE_1KB) / SIZE_1KB;
            return size2;
        }
        System.out.println("文件或者文件夹不存在，请检查路径是否正确！");
        return 0L;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

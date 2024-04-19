package com.xiaopeng.lib.http;

import android.text.TextUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: classes.dex */
public class FileUtils {
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
            }
        }
    }

    public static String readTextFile(File file, int max, String ellipsis) throws IOException {
        int len;
        int len2;
        InputStream input = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(input);
        try {
            long size = file.length();
            if (max > 0 || (size > 0 && max == 0)) {
                if (size > 0 && (max == 0 || size < max)) {
                    max = (int) size;
                }
                byte[] data = new byte[max + 1];
                int length = bis.read(data);
                if (length <= 0) {
                    return "";
                }
                if (length <= max) {
                    return new String(data, 0, length);
                }
                if (ellipsis == null) {
                    return new String(data, 0, max);
                }
                return new String(data, 0, max) + ellipsis;
            } else if (max >= 0) {
                ByteArrayOutputStream contents = new ByteArrayOutputStream();
                byte[] data2 = new byte[1024];
                do {
                    len = bis.read(data2);
                    if (len > 0) {
                        contents.write(data2, 0, len);
                    }
                } while (len == data2.length);
                return contents.toString();
            } else {
                boolean rolled = false;
                byte[] last = null;
                byte[] data3 = null;
                do {
                    if (last != null) {
                        rolled = true;
                    }
                    byte[] tmp = last;
                    last = data3;
                    data3 = tmp;
                    if (data3 == null) {
                        data3 = new byte[-max];
                    }
                    len2 = bis.read(data3);
                } while (len2 == data3.length);
                if (last != null || len2 > 0) {
                    if (last == null) {
                        return new String(data3, 0, len2);
                    }
                    if (len2 > 0) {
                        rolled = true;
                        System.arraycopy(last, len2, last, 0, last.length - len2);
                        System.arraycopy(data3, 0, last, last.length - len2, len2);
                    }
                    if (ellipsis != null && rolled) {
                        return ellipsis + new String(last);
                    }
                    return new String(last);
                }
                return "";
            }
        } finally {
            bis.close();
            input.close();
        }
    }

    public static void stringToFile(String filename, String string) throws IOException {
        FileWriter out = new FileWriter(filename);
        try {
            out.write(string);
        } finally {
            out.close();
        }
    }
}

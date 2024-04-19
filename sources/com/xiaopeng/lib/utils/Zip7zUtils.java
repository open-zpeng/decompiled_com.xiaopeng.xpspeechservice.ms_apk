package com.xiaopeng.lib.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
/* loaded from: classes.dex */
public class Zip7zUtils {
    public static void zip7z(String sourcePath, String destPath) {
        SevenZOutputFile sevenZOutput = null;
        try {
            try {
                File outputFile = new File(destPath);
                File src = new File(sourcePath);
                sevenZOutput = new SevenZOutputFile(outputFile);
                if (src.isDirectory()) {
                    zip7zDirectory(src, sevenZOutput, "");
                } else {
                    zip7zFile(src, sevenZOutput, "");
                }
            } catch (IOException ioe) {
                System.out.println(ioe.toString());
            }
        } finally {
            FileUtils.closeQuietly(sevenZOutput);
        }
    }

    private static void zip7zFile(File src, SevenZOutputFile sevenZOutput, String currentPath) throws IOException {
        BufferedInputStream instream = null;
        try {
            try {
                try {
                    instream = new BufferedInputStream(new FileInputStream(src));
                    SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(src, currentPath + src.getName());
                    sevenZOutput.putArchiveEntry(entry);
                    byte[] buffer = new byte[4096];
                    while (true) {
                        int len = instream.read(buffer);
                        if (len <= 0) {
                            break;
                        }
                        sevenZOutput.write(buffer, 0, len);
                    }
                    FileUtils.closeQuietly(instream);
                } catch (IOException e) {
                    e.printStackTrace();
                    FileUtils.closeQuietly(instream);
                    if (sevenZOutput == null) {
                        return;
                    }
                }
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                FileUtils.closeQuietly(instream);
                if (sevenZOutput == null) {
                    return;
                }
            }
            sevenZOutput.closeArchiveEntry();
        } catch (Throwable th) {
            FileUtils.closeQuietly(instream);
            if (sevenZOutput != null) {
                sevenZOutput.closeArchiveEntry();
            }
            throw th;
        }
    }

    private static void zip7zDirectory(File srcDirectory, SevenZOutputFile sevenZOutput, String currentPath) throws IOException {
        File[] listFiles = srcDirectory.listFiles();
        if (listFiles != null && listFiles.length > 0) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    zip7zDirectory(file, sevenZOutput, currentPath + file.getName() + "/");
                } else {
                    zip7zFile(file, sevenZOutput, currentPath);
                }
            }
        }
    }
}

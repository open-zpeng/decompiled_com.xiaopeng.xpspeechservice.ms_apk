package com.xiaopeng.lib.utils;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
/* loaded from: classes.dex */
public class StorageUtils {
    private static final int NORMAL_SIZE = 2;

    public static boolean isTFcardMounted() {
        List<StorageInfo> storageInfos = getStorageList();
        if (storageInfos.size() < 2) {
            return false;
        }
        String sd2 = storageInfos.remove(1).path;
        if (TextUtils.isEmpty(sd2)) {
            return false;
        }
        return checkTFWritable(sd2 + File.separator);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class StorageInfo {
        int number;
        String path;
        boolean readonly;
        boolean removable;

        StorageInfo(String path, boolean readonly, boolean removable, int number) {
            this.path = path;
            this.readonly = readonly;
            this.removable = removable;
            this.number = number;
        }
    }

    @NonNull
    private static List<StorageInfo> getStorageList() {
        int currRemovableNumber;
        BufferedReader bfr;
        Throwable th;
        List<StorageInfo> list = new ArrayList<>();
        HashSet<String> paths = new HashSet<>();
        int currRemovableNumber2 = 1;
        String innerSdPath = Environment.getExternalStorageDirectory().getPath();
        boolean innerSdRemovable = Environment.isExternalStorageRemovable();
        String innerSdState = Environment.getExternalStorageState();
        boolean innerSdAvailable = innerSdState.equals("mounted") || innerSdState.equals("mounted_ro");
        boolean innerSdReadOnly = Environment.getExternalStorageState().equals("mounted_ro");
        if (innerSdAvailable) {
            paths.add(innerSdPath);
            if (innerSdRemovable) {
                currRemovableNumber = 1 + 1;
            } else {
                currRemovableNumber = 1;
                currRemovableNumber2 = -1;
            }
            list.add(0, new StorageInfo(innerSdPath, innerSdReadOnly, innerSdRemovable, currRemovableNumber2));
        } else {
            currRemovableNumber = 1;
        }
        BufferedReader bfr2 = null;
        try {
            try {
                try {
                    bfr2 = new BufferedReader(new FileReader("/proc/mounts"));
                    while (true) {
                        String line = bfr2.readLine();
                        if (line == null) {
                            break;
                        } else if (line.contains("vfat") || line.contains("/mnt")) {
                            StringTokenizer tokens = new StringTokenizer(line, " ");
                            tokens.nextToken();
                            String mountPoint = tokens.nextToken();
                            if (!paths.contains(mountPoint)) {
                                tokens.nextToken();
                                List<String> flags = Arrays.asList(tokens.nextToken().split(","));
                                boolean readonly = flags.contains("ro");
                                if (line.contains("/dev/block/vold") && !line.contains("/mnt/secure") && !line.contains("/mnt/asec") && !line.contains("/mnt/obb") && !line.contains("/dev/mapper") && !line.contains("tmpfs")) {
                                    paths.add(mountPoint);
                                    int currRemovableNumber3 = currRemovableNumber + 1;
                                    try {
                                        list.add(new StorageInfo(mountPoint, readonly, true, currRemovableNumber));
                                        currRemovableNumber = currRemovableNumber3;
                                    } catch (FileNotFoundException e) {
                                        ex = e;
                                        ex.printStackTrace();
                                        if (bfr2 != null) {
                                            bfr2.close();
                                        }
                                        return list;
                                    } catch (IOException e2) {
                                        ex = e2;
                                        ex.printStackTrace();
                                        if (bfr2 != null) {
                                            bfr2.close();
                                        }
                                        return list;
                                    } catch (Throwable th2) {
                                        bfr = bfr2;
                                        th = th2;
                                        if (bfr != null) {
                                            try {
                                                bfr.close();
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                        throw th;
                                    }
                                }
                            }
                        }
                    }
                    bfr2.close();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            } catch (FileNotFoundException e3) {
                ex = e3;
            } catch (IOException e4) {
                ex = e4;
            }
            return list;
        } catch (Throwable th3) {
            bfr = null;
            th = th3;
        }
    }

    private static boolean checkTFWritable(String dir) {
        if (dir == null) {
            return false;
        }
        File directory = new File(dir);
        if (!directory.isDirectory() && !directory.exists() && !directory.mkdirs()) {
            return false;
        }
        File testFile = new File(directory, ".xpTF");
        try {
            if (testFile.exists()) {
                testFile.delete();
            }
            if (!testFile.createNewFile()) {
                return false;
            }
            testFile.delete();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

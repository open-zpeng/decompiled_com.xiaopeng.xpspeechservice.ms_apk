package com.xiaopeng.lib.utils.info;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Process;
import com.xiaopeng.lib.utils.FileUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
/* loaded from: classes.dex */
public class AppInfoUtils {
    private static String sProcessName = null;

    public static PackageInfo getPackageInfo(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ApplicationInfo getApplicationInfo(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            if (context.getPackageName().equals(packageName)) {
                return context.getApplicationInfo();
            }
            return pm.getApplicationInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getVersionName(Context context, String packageName) {
        PackageInfo info = getPackageInfo(context, packageName);
        return info == null ? "" : info.versionName;
    }

    public static int getVersionCode(Context context, String packageName) {
        PackageInfo info = getPackageInfo(context, packageName);
        if (info == null) {
            return -1;
        }
        return info.versionCode;
    }

    public static Drawable getApplicationIcon(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CharSequence getApplicationLabel(Context context, String packageName) {
        ApplicationInfo info = getApplicationInfo(context, packageName);
        return info == null ? "" : info.loadLabel(context.getPackageManager());
    }

    public static String getProcessName() {
        String str = sProcessName;
        if (str != null) {
            return str;
        }
        BufferedReader bufferedReader = null;
        try {
            try {
                File file = new File("/proc/" + Process.myPid() + "/cmdline");
                bufferedReader = new BufferedReader(new FileReader(file));
                String processName = bufferedReader.readLine().trim();
                sProcessName = processName;
            } catch (Exception e) {
                e.printStackTrace();
                sProcessName = "";
            }
            FileUtils.closeQuietly(bufferedReader);
            return sProcessName;
        } catch (Throwable th) {
            FileUtils.closeQuietly(bufferedReader);
            throw th;
        }
    }
}

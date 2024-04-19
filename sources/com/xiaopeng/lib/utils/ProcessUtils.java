package com.xiaopeng.lib.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Process;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
/* loaded from: classes.dex */
public class ProcessUtils {
    private static final String TAG = "ProcUtils";

    public static long getAppMemoryUsed(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessesList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessesList) {
            if (!TextUtils.isEmpty(runningAppProcessInfo.processName) && runningAppProcessInfo.processName.equals(context.getPackageName())) {
                int pid = runningAppProcessInfo.pid;
                int[] pids = {pid};
                Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(pids);
                int memorySize = memoryInfo[0].dalvikSharedDirty + memoryInfo[0].dalvikPrivateDirty;
                return memorySize * 1024;
            }
        }
        return 0L;
    }

    private void getRunningAppProcessInfo() {
    }

    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public static long getTotalMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem;
    }

    public static long getAppCpuTime() {
        String[] cpuInfos = null;
        BufferedReader reader = null;
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        try {
            try {
                try {
                    int pid = Process.myPid();
                    fileInputStream = new FileInputStream("/proc/" + pid + "/stat");
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    reader = new BufferedReader(inputStreamReader, 1000);
                    String load = reader.readLine();
                    if (load != null) {
                        cpuInfos = load.split(" ");
                    }
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        inputStreamReader.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    fileInputStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }
            } catch (IOException e5) {
                e5.printStackTrace();
            }
            if (cpuInfos == null || cpuInfos.length <= 16) {
                return 0L;
            }
            long appCpuTime = Long.parseLong(cpuInfos[13]) + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15]) + Long.parseLong(cpuInfos[16]);
            return appCpuTime;
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static long getTotalCpuTime() {
        String[] cpuInfos = null;
        BufferedReader reader = null;
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        try {
            try {
                try {
                    fileInputStream = new FileInputStream("/proc/stat");
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    reader = new BufferedReader(inputStreamReader, 1000);
                    String load = reader.readLine();
                    if (load != null) {
                        cpuInfos = load.split(" ");
                    }
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        inputStreamReader.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    fileInputStream.close();
                } catch (Throwable th) {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e5) {
                            e5.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e7) {
                        e7.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
        } catch (IOException e8) {
            e8.printStackTrace();
        }
        if (cpuInfos == null || cpuInfos.length <= 8) {
            return 0L;
        }
        long totalCpu = Long.parseLong(cpuInfos[2]) + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4]) + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5]) + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return totalCpu;
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x007c, code lost:
        r2.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x0080, code lost:
        r5 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x0081, code lost:
        r5.printStackTrace();
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x00c0, code lost:
        if (r1 == null) goto L19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:9:0x0036, code lost:
        com.xiaopeng.lib.utils.LogUtils.d("ProcessCpuRate", "Result-->" + r5);
        r5 = r5.split("%");
        r9 = r5[0].split("User");
        r10 = r5[1].split("System");
        r0[0] = java.lang.Integer.parseInt(r9[1].trim());
        r0[1] = java.lang.Integer.parseInt(r10[1].trim());
     */
    /* JADX WARN: Removed duplicated region for block: B:75:0x008f A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static int[] getProcessCpuRate() {
        /*
            Method dump skipped, instructions count: 232
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xiaopeng.lib.utils.ProcessUtils.getProcessCpuRate():int[]");
    }

    /* JADX WARN: Code restructure failed: missing block: B:44:0x0094, code lost:
        if (r4 == null) goto L17;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.lang.String execProcess(java.lang.String r11, int r12) {
        /*
            r0 = 0
            r1 = 0
            r2 = 0
            r3 = 0
            r4 = 0
            java.lang.Runtime r5 = java.lang.Runtime.getRuntime()     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            java.lang.Process r5 = r5.exec(r11)     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r4 = r5
            java.io.InputStream r5 = r4.getInputStream()     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r3 = r5
            java.io.InputStreamReader r5 = new java.io.InputStreamReader     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r5.<init>(r3)     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r2 = r5
            java.io.BufferedReader r5 = new java.io.BufferedReader     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r5.<init>(r2)     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r1 = r5
            java.lang.StringBuffer r5 = new java.lang.StringBuffer     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r5.<init>()     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            char[] r6 = new char[r12]     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r7 = 0
            r8 = r7
        L28:
            int r9 = r1.read(r6)     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r8 = r9
            r10 = -1
            if (r9 == r10) goto L34
            r5.append(r6, r7, r8)     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            goto L28
        L34:
            java.lang.String r7 = "ProcUtils"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r9.<init>()     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            java.lang.String r10 = "respBuff-->"
            r9.append(r10)     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r9.append(r5)     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            java.lang.String r9 = r9.toString()     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            com.xiaopeng.lib.utils.LogUtils.d(r7, r9)     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            java.lang.String r7 = r5.toString()     // Catch: java.lang.Throwable -> L70 java.lang.Exception -> L72
            r0 = r7
            r1.close()     // Catch: java.io.IOException -> L54
            goto L58
        L54:
            r5 = move-exception
            r5.printStackTrace()
        L58:
            r2.close()     // Catch: java.io.IOException -> L5d
            goto L61
        L5d:
            r5 = move-exception
            r5.printStackTrace()
        L61:
            if (r3 == 0) goto L6b
            r3.close()     // Catch: java.io.IOException -> L67
            goto L6b
        L67:
            r5 = move-exception
            r5.printStackTrace()
        L6b:
        L6c:
            r4.destroy()
            goto L97
        L70:
            r5 = move-exception
            goto L98
        L72:
            r5 = move-exception
            r5.printStackTrace()     // Catch: java.lang.Throwable -> L70
            if (r1 == 0) goto L80
            r1.close()     // Catch: java.io.IOException -> L7c
            goto L80
        L7c:
            r5 = move-exception
            r5.printStackTrace()
        L80:
            if (r2 == 0) goto L8a
            r2.close()     // Catch: java.io.IOException -> L86
            goto L8a
        L86:
            r5 = move-exception
            r5.printStackTrace()
        L8a:
            if (r3 == 0) goto L94
            r3.close()     // Catch: java.io.IOException -> L90
            goto L94
        L90:
            r5 = move-exception
            r5.printStackTrace()
        L94:
            if (r4 == 0) goto L97
            goto L6c
        L97:
            return r0
        L98:
            if (r1 == 0) goto La2
            r1.close()     // Catch: java.io.IOException -> L9e
            goto La2
        L9e:
            r6 = move-exception
            r6.printStackTrace()
        La2:
            if (r2 == 0) goto Lac
            r2.close()     // Catch: java.io.IOException -> La8
            goto Lac
        La8:
            r6 = move-exception
            r6.printStackTrace()
        Lac:
            if (r3 == 0) goto Lb6
            r3.close()     // Catch: java.io.IOException -> Lb2
            goto Lb6
        Lb2:
            r6 = move-exception
            r6.printStackTrace()
        Lb6:
            if (r4 == 0) goto Lbb
            r4.destroy()
        Lbb:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xiaopeng.lib.utils.ProcessUtils.execProcess(java.lang.String, int):java.lang.String");
    }

    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService("activity");
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    public static String getCurProcessName() {
        FileReader fileReader = null;
        try {
            try {
                File file = new File("/proc/" + Process.myPid() + "/cmdline");
                fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String processName = bufferedReader.readLine().trim();
                bufferedReader.close();
                fileReader.close();
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return processName;
            } catch (Exception e2) {
                e2.printStackTrace();
                try {
                    fileReader.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
                return null;
            }
        } catch (Throwable th) {
            try {
                fileReader.close();
            } catch (IOException e4) {
                e4.printStackTrace();
            }
            throw th;
        }
    }
}

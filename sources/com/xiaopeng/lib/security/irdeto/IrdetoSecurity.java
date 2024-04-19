package com.xiaopeng.lib.security.irdeto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.irdeto.securesdk.ISFException;
import com.irdeto.securesdk.SecureSDKManager;
import com.irdeto.securesdk.isf;
import com.xiaopeng.lib.http.FileUtils;
import com.xiaopeng.lib.http.ICallback;
import com.xiaopeng.lib.http.Security;
import com.xiaopeng.lib.http.systemdelegate.DelegateHelper;
import com.xiaopeng.lib.security.ISecurityModule;
import com.xiaopeng.lib.security.SecurityCommon;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.ThreadUtils;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.io.File;
/* loaded from: classes.dex */
public class IrdetoSecurity implements ISecurityModule {
    private static final String ACTION_BROADCAST_ISF_SECURE_STORE_RELOAD = "com.xiaopeng.action.ISF_SECURE_STORE_RELOAD";
    private static final String IRSS_FLAG_FILE_NAME = "si_flag.dat";
    private static final int ISF_MGR_ROOTDETECTED = 10;
    private static final int ISF_MGR_SHARED_SECURE_STORE_INVALID = 14;
    private static final String MCU_FLAG_FILE_NAME = "si_mcu.dat";
    private static final String NLSS0_FILE_NAME = "irss0.dat";
    private static final String NLSS1_FILE_NAME = "irss1.dat";
    private static final String NLSS2_FILE_NAME = "irss2.dat";
    public static final String NLSS_FILE_PATH = "/private/sec/";
    private static final String TAG = "IrdetoSecurity";
    private static Context sContext;
    private static boolean sIsInitSuccess;
    private static SecureSDKManager sSecureSDKManager;
    private volatile String sInitErrorMsg;

    private IrdetoSecurity() {
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public Security.EncryptionType getEncryptionType() {
        return Security.EncryptionType.IRDETO;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public IrdetoSecurity init(Context context) throws Exception {
        initInternal(context, false);
        return this;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public IrdetoSecurity initForIndividual(Context context) throws Exception {
        initInternal(context, true);
        return this;
    }

    private void initInternal(Context context, boolean forIndividual) {
        sContext = context;
        if (forIndividual) {
            File file = new File(NLSS_FILE_PATH);
            if (!file.exists()) {
                file.mkdir();
            }
            checkSaveIndividualFlag();
        } else {
            registerISFSecureStoreReloadReceiver();
        }
        sIsInitSuccess = false;
        try {
            sSecureSDKManager = new SecureSDKManager();
            sSecureSDKManager.isfInitialize(context, NLSS_FILE_PATH);
            sIsInitSuccess = true;
            LogUtils.d(TAG, "irdeto sdk init success");
        } catch (ISFException e) {
            LogUtils.e((Object) TAG, (Throwable) e);
            int errorCode = e.getErrorCode();
            if (errorCode != 10) {
                if (errorCode == 14) {
                    LogUtils.e(TAG, "irdeto sdk init failed : secure store error!!!!");
                    clearFlag();
                }
            } else if (BuildInfoUtils.isEngVersion()) {
                sIsInitSuccess = true;
                LogUtils.d(TAG, "irdeto sdk init success for eng root");
            }
            this.sInitErrorMsg = "irdeto init error for " + e.getMessage() + " error code is " + e.getErrorCode();
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void destroy() {
        SecureSDKManager secureSDKManager = sSecureSDKManager;
        if (secureSDKManager != null) {
            secureSDKManager.isfCleanup();
            sSecureSDKManager = null;
        }
        sIsInitSuccess = false;
    }

    private void checkSaveIndividualFlag() {
        if (!SecurityCommon.getBuildInfoFlag().equals(getIndividualFlag())) {
            LogUtils.e(TAG, "irdeto delete indiv files");
            clearFlag();
        }
    }

    private void clearFlag() {
        try {
            FileUtils.deleteFile("/private/sec/irss0.dat");
            FileUtils.deleteFile("/private/sec/irss1.dat");
            FileUtils.deleteFile("/private/sec/irss2.dat");
            FileUtils.deleteFile("/private/sec/si_flag.dat");
            FileUtils.deleteFile("/private/sec/si_mcu.dat");
            File file = new File(sContext.getFilesDir().getPath() + "/" + NLSS0_FILE_NAME);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void clearIndividualData() {
        try {
            FileUtils.deleteFile("/private/sec/irss2.dat");
            FileUtils.deleteFile("/private/sec/si_flag.dat");
            FileUtils.deleteFile("/private/sec/si_mcu.dat");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String getIndividualRequestStr() throws Exception {
        return isf.isfGetProvisionRequest();
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String generateIndividualDataForServer() throws Exception {
        return isf.isfGenerateOpaqueData();
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public boolean individualWithData(String data) throws Exception {
        isf.isfProvisionResponse(data);
        return isf.isfProvisioned();
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void saveIndividualFlag() {
        try {
            FileUtils.stringToFile("/private/sec/si_flag.dat", SecurityCommon.getBuildInfoFlag());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void saveMCUSecurityKey(boolean force) {
        if (!force) {
            File file = new File("/private/sec/si_mcu.dat");
            if (file.exists()) {
                return;
            }
        }
        try {
            byte[] data = isf.isfGetMCUSecurityAsset();
            FileUtils.stringToFile("/private/sec/si_mcu.dat", SecurityCommon.parseByte2HexStr(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public void asyncGetMCUSecurityKey(final ICallback<byte[], String> callback) {
        LogUtils.v(TAG, "asyncGetMCUSecurityKey");
        new Thread(new Runnable() { // from class: com.xiaopeng.lib.security.irdeto.IrdetoSecurity.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String content = FileUtils.readTextFile(new File("/private/sec/si_mcu.dat"), 0, null);
                    byte[] data = SecurityCommon.parseHexStr2Byte(content);
                    if (data == null && data.length != 0) {
                        callback.onError("null mcu security key");
                    }
                    callback.onSuccess(data);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.v(IrdetoSecurity.TAG, "null mcu security key");
                    callback.onError("null mcu security key");
                }
            }
        }).start();
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String encode(String str) {
        synchronized (IrdetoSecurity.class) {
            if (!sIsInitSuccess) {
                printErrorReason("encode error, ");
                return "";
            }
            try {
                String result = isf.isfEncrypt(SecurityCommon.base64UrlEncode(str.getBytes()));
                LogUtils.d(TAG, "irdeto encode result " + result);
                return result;
            } catch (Exception e) {
                LogUtils.e(TAG, e);
                return "";
            }
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String decode(String str) {
        synchronized (IrdetoSecurity.class) {
            if (!sIsInitSuccess) {
                printErrorReason("decode error, ");
                return "";
            }
            try {
                String result = isf.isfDecrypt(str);
                LogUtils.d(TAG, "irdeto decode result " + result);
                return result;
            } catch (Exception e) {
                LogUtils.e(TAG, e);
                return "";
            }
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public synchronized String getString(String key) {
        synchronized (IrdetoSecurity.class) {
            try {
                if (!sIsInitSuccess) {
                    try {
                        return "";
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
                try {
                    return new String(isf.isfStoreGet(key));
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public synchronized void setString(String key, String value) {
        synchronized (IrdetoSecurity.class) {
            try {
                if (!sIsInitSuccess) {
                    try {
                        return;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
                try {
                    isf.isfStorePut(key, value.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public synchronized void deleteString(String key) {
        synchronized (IrdetoSecurity.class) {
            try {
                if (!sIsInitSuccess) {
                    try {
                        return;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
                try {
                    isf.isfStoreDelete(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String buildTokenData(String[] ids, byte[] data) {
        if (!SecurityCommon.checkSystemUid(sContext)) {
            return DelegateHelper.callBuildTokenDataThroughSystemDelegate(sContext, ids, data);
        }
        synchronized (IrdetoSecurity.class) {
            if (!sIsInitSuccess) {
                printErrorReason("build token data error, ");
                return null;
            }
            try {
                return isf.isfTokenOperate(ids, SecurityCommon.base64UrlEncode(data).getBytes());
            } catch (Exception e) {
                LogUtils.e(TAG, e);
                return null;
            }
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String cryptoDecode(String key, String Iv, byte[] data) {
        byte[] result = cryptoDecodeInByteArray(key, Iv, data);
        if (result != null) {
            return new String(result);
        }
        return null;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public String getIndividualFlag() {
        try {
            String flag = FileUtils.readTextFile(new File("/private/sec/si_flag.dat"), 1, null);
            return flag;
        } catch (Exception e) {
            return null;
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public boolean isInitAndIndivSuccess() {
        synchronized (IrdetoSecurity.class) {
            if (!sIsInitSuccess) {
                LogUtils.d(TAG, "Irdeto sdk not init " + this.sInitErrorMsg);
                return false;
            }
            try {
                if (isf.isfProvisioned()) {
                    if (SecurityCommon.getBuildInfoFlag().equals(getIndividualFlag())) {
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public boolean isInitSuccess() {
        return sIsInitSuccess;
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public synchronized void asyncSaveToken(final String[] ids, final String[] token, final Runnable runnable) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.security.irdeto.IrdetoSecurity.2
            @Override // java.lang.Runnable
            public void run() {
                try {
                    IrdetoSecurity.saveToken(ids, token);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                runnable.run();
            }
        });
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public synchronized void asyncSaveTokenWithListener(final String[] ids, final String[] token, final ISecurityModule.ResultListener listener) {
        ThreadUtils.postBackground(new Runnable() { // from class: com.xiaopeng.lib.security.irdeto.IrdetoSecurity.3
            @Override // java.lang.Runnable
            public void run() {
                ISecurityModule.ResultListener.RESULT result = ISecurityModule.ResultListener.RESULT.FAIL;
                String exceptionString = "";
                try {
                    IrdetoSecurity.saveToken(ids, token);
                    result = ISecurityModule.ResultListener.RESULT.SUCCEED;
                } catch (Throwable e) {
                    exceptionString = e.getMessage();
                    e.printStackTrace();
                }
                listener.onResult(result, exceptionString);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void saveToken(String[] ids, String[] token) throws Exception {
        synchronized (IrdetoSecurity.class) {
            if (sIsInitSuccess) {
                try {
                    isf.isfTokenSave(ids, token);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
    }

    @Override // com.xiaopeng.lib.security.ISecurityModule
    public byte[] cryptoDecodeInByteArray(String key, String Iv, byte[] data) {
        synchronized (IrdetoSecurity.class) {
            if (sIsInitSuccess) {
                try {
                    return isf.isfCryptoOperate("IR_CRYPTO_AES_CBC_DECRYPT", key, Iv, data);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }
    }

    private static void registerISFSecureStoreReloadReceiver() {
        LogUtils.d(TAG, "registerISFSecureStoreReloadReceiver");
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_BROADCAST_ISF_SECURE_STORE_RELOAD);
            sContext.registerReceiver(new BroadcastReceiver() { // from class: com.xiaopeng.lib.security.irdeto.IrdetoSecurity.4
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (IrdetoSecurity.ACTION_BROADCAST_ISF_SECURE_STORE_RELOAD.equals(action)) {
                        try {
                            LogUtils.d(IrdetoSecurity.TAG, "registerISFSecureStoreReloadReceiver ACTION_BROADCAST_ISF_SECURE_STORE_RELOAD");
                            isf.isfSecureStoreReload();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class Holder {
        private static final IrdetoSecurity INSTANCE = new IrdetoSecurity();

        private Holder() {
        }
    }

    public static IrdetoSecurity getInstance() {
        return Holder.INSTANCE;
    }

    private void printErrorReason(String head) {
        LogUtils.d(TAG, head + this.sInitErrorMsg);
    }
}

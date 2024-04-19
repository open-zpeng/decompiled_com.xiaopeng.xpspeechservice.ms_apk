package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss;

import android.os.SystemProperties;
import com.xiaopeng.lib.framework.moduleinterface.netchannelmodule.remotestorage.IRemoteStorage;
import com.xiaopeng.lib.utils.DateUtils;
import com.xiaopeng.lib.utils.LogUtils;
import com.xiaopeng.lib.utils.SystemPropertyUtil;
import com.xiaopeng.lib.utils.config.CommonConfig;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
/* loaded from: classes.dex */
public class Bucket {
    public static String BUCKET_AND_ENDPOINT = null;
    public static String END_POINT = null;
    public static final String END_POINT_DOMAIN = "oss-cn-hangzhou.aliyuncs.com/";
    public static final String END_POINT_DOMAIN_INT = "oss-eu-central-1.aliyuncs.com/";
    public static final String END_POINT_DOMAIN_PRIVATE = "oss.xiaopeng.com/";
    private static final long MAX_LOG_LENGTH = 20971520;
    private static final String SCHEMA = "https://";
    public static String TOKEN_URL_V5;
    private String mPrivateUrl;
    private String mRootName;
    private String mUrl;
    public static String END_POINT_PRIVATE = "https://oss.xiaopeng.com/";
    public static String BUCKET_AND_ENDPOINT_PRIVATE = "https://%s.oss.xiaopeng.com/";

    static {
        TOKEN_URL_V5 = CommonConfig.HTTP_BUSINESS_HOST + "/v5/aliyun/token";
        END_POINT = "https://oss-cn-hangzhou.aliyuncs.com/";
        BUCKET_AND_ENDPOINT = "https://%s.oss-cn-hangzhou.aliyuncs.com/";
        try {
            boolean international = !getVersionInCountryCode().contains("ZH");
            LogUtils.i("Bucket", "Version = " + getVersionInCountryCode() + ", international = " + international);
            if (international) {
                TOKEN_URL_V5 = "https://xmart-eu.xiaopeng.com/biz/v5/aliyun/token";
                END_POINT = "https://oss-eu-central-1.aliyuncs.com/";
                BUCKET_AND_ENDPOINT = "https://%s.oss-eu-central-1.aliyuncs.com/";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* renamed from: com.xiaopeng.lib.framework.netchannelmodule.remotestorage.oss.Bucket$1  reason: invalid class name */
    /* loaded from: classes.dex */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$xiaopeng$lib$framework$moduleinterface$netchannelmodule$remotestorage$IRemoteStorage$CATEGORY = new int[IRemoteStorage.CATEGORY.values().length];

        static {
            try {
                $SwitchMap$com$xiaopeng$lib$framework$moduleinterface$netchannelmodule$remotestorage$IRemoteStorage$CATEGORY[IRemoteStorage.CATEGORY.CAN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$xiaopeng$lib$framework$moduleinterface$netchannelmodule$remotestorage$IRemoteStorage$CATEGORY[IRemoteStorage.CATEGORY.CDU.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public static Bucket get(IRemoteStorage.CATEGORY item) {
        int i = AnonymousClass1.$SwitchMap$com$xiaopeng$lib$framework$moduleinterface$netchannelmodule$remotestorage$IRemoteStorage$CATEGORY[item.ordinal()];
        if (i != 1) {
            if (i == 2) {
                return new CduBucket(null);
            }
            throw new RuntimeException("Wrong bucket was specified!");
        }
        return new CanBucket(null);
    }

    public Bucket(String rootName) {
        this.mRootName = rootName;
        this.mUrl = String.format(BUCKET_AND_ENDPOINT, rootName);
        this.mPrivateUrl = String.format(BUCKET_AND_ENDPOINT_PRIVATE, rootName);
    }

    private Bucket() {
    }

    public String getUrl() {
        return this.mUrl;
    }

    public String getPrivateUrl() {
        return this.mPrivateUrl;
    }

    public String getRootName() {
        return this.mRootName;
    }

    public long getMaxObjectSize() {
        return MAX_LOG_LENGTH;
    }

    public String generateObjectKey(String moduleName) {
        long timeStamp = System.currentTimeMillis();
        return this.mRootName + "/" + moduleName + "/" + BuildInfoUtils.getSystemVersion() + "/" + DateUtils.formatDate7(timeStamp) + "/" + SystemPropertyUtil.getVehicleId() + "/" + timeStamp + ".zip";
    }

    /* loaded from: classes.dex */
    private static final class CduBucket extends Bucket {
        private static final String CDU_BUCKET_NAME = "xmart-cdu-service-log";

        /* synthetic */ CduBucket(AnonymousClass1 x0) {
            this();
        }

        private CduBucket() {
            super("xmart-cdu-service-log");
        }
    }

    /* loaded from: classes.dex */
    private static final class CanBucket extends Bucket {
        private static final String CAN_BUCKET_NAME = "xmart-can-service-log";

        /* synthetic */ CanBucket(AnonymousClass1 x0) {
            this();
        }

        private CanBucket() {
            super(CAN_BUCKET_NAME);
        }
    }

    public static String getVersionInCountryCode() throws Exception {
        String versionFinger = SystemProperties.get("ro.xiaopeng.software", "");
        if (!"".equals(versionFinger) && versionFinger != null && versionFinger.length() >= 9) {
            String countryCode = versionFinger.substring(7, 9);
            return countryCode;
        }
        return versionFinger;
    }
}

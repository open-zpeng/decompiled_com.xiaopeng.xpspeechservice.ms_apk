package com.xiaopeng.lib.framework.netchannelmodule.remotestorage.aws;
/* loaded from: classes.dex */
public class UploadInfo {
    public static final String APP_ID = "XP-Appid";
    public static final String MODEL = "";
    public static final String UPLOAD_TYPE = "81";
    public static final String VERSION = "2";
    public String app_id;
    public String file;
    public String model;
    public String sign;
    public String timestamp;
    public String type;
    public String v;

    /* loaded from: classes.dex */
    public static final class Builder {
        String file = "";
        String app_id = "XP-Appid";
        String model = "";
        String type = UploadInfo.UPLOAD_TYPE;
        String v = UploadInfo.VERSION;
        String timestamp = "";
        String sign = "";

        public Builder file(String file) {
            this.file = file;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder v(String v) {
            this.v = v;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder sign(String sign) {
            this.sign = sign;
            return this;
        }

        public UploadInfo build() {
            return new UploadInfo(this);
        }
    }

    public UploadInfo() {
        this(new Builder());
    }

    UploadInfo(Builder builder) {
        this.file = builder.file;
        this.app_id = builder.app_id;
        this.model = builder.model;
        this.type = builder.type;
        this.v = builder.v;
        this.timestamp = builder.timestamp;
        this.sign = builder.sign;
    }
}

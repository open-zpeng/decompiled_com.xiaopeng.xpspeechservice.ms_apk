package com.xiaopeng.lib.framework.configuration;

import android.os.Parcel;
import android.os.Parcelable;
import com.xiaopeng.lib.framework.moduleinterface.configurationmodule.IConfigurationData;
/* loaded from: classes.dex */
public class ConfigurationDataImpl implements IConfigurationData, Parcelable {
    public static final Parcelable.Creator<ConfigurationDataImpl> CREATOR = new Parcelable.Creator<ConfigurationDataImpl>() { // from class: com.xiaopeng.lib.framework.configuration.ConfigurationDataImpl.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public ConfigurationDataImpl createFromParcel(Parcel in) {
            return new ConfigurationDataImpl(in);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public ConfigurationDataImpl[] newArray(int size) {
            return new ConfigurationDataImpl[size];
        }
    };
    private String mKey;
    private String mValue;

    @Override // com.xiaopeng.lib.framework.moduleinterface.configurationmodule.IConfigurationData
    public String getKey() {
        return this.mKey;
    }

    @Override // com.xiaopeng.lib.framework.moduleinterface.configurationmodule.IConfigurationData
    public String getValue() {
        return this.mValue;
    }

    public ConfigurationDataImpl(String key, String value) {
        this.mKey = key;
        this.mValue = value;
    }

    protected ConfigurationDataImpl(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mKey);
        parcel.writeString(this.mValue);
    }

    private void readFromParcel(Parcel in) {
        this.mKey = in.readString();
        this.mValue = in.readString();
    }
}

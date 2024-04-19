package com.xiaopeng.lib.framework.moduleinterface.locationmodule;

import android.os.Parcelable;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
@Keep
/* loaded from: classes.dex */
public interface ILocation extends Parcelable {

    /* loaded from: classes.dex */
    public enum Category {
        GPS_LOCATION,
        DR_LOCATION
    }

    int accuracy();

    ILocation accuracy(int value);

    ILocation adCode(String value);

    @Nullable
    String adCode();

    int altitude();

    ILocation altitude(int value);

    float angle();

    ILocation angle(float value);

    Category category();

    ILocation category(Category value);

    ILocation city(String value);

    @Nullable
    String city();

    float latitude();

    ILocation latitude(float value);

    float longitude();

    ILocation longitude(float value);

    float rawLatitude();

    ILocation rawLatitude(float value);

    float rawLongitude();

    ILocation rawLongitude(float value);

    int satellites();

    ILocation satellites(int value);

    int sourceType();

    ILocation sourceType(int value);

    float speed();

    ILocation speed(float value);

    long time();

    ILocation time(long value);
}

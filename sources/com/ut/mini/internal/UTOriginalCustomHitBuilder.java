package com.ut.mini.internal;

import android.text.TextUtils;
import com.ut.mini.UTHitBuilders;
import java.util.Map;
/* loaded from: classes.dex */
public class UTOriginalCustomHitBuilder extends UTHitBuilders.UTHitBuilder {
    public UTOriginalCustomHitBuilder(String aPage, int aEventId, String aArg1, String aArg2, String aArg3, Map<String, String> aHitMap) {
        if (!TextUtils.isEmpty(aPage)) {
            super.setProperty(UTHitBuilders.UTHitBuilder.FIELD_PAGE, aPage);
        }
        super.setProperty(UTHitBuilders.UTHitBuilder.FIELD_EVENT_ID, "" + aEventId);
        if (!TextUtils.isEmpty(aArg1)) {
            super.setProperty(UTHitBuilders.UTHitBuilder.FIELD_ARG1, aArg1);
        }
        if (!TextUtils.isEmpty(aArg2)) {
            super.setProperty(UTHitBuilders.UTHitBuilder.FIELD_ARG2, aArg2);
        }
        if (!TextUtils.isEmpty(aArg3)) {
            super.setProperty(UTHitBuilders.UTHitBuilder.FIELD_ARG3, aArg3);
        }
        super.setProperties(aHitMap);
    }
}

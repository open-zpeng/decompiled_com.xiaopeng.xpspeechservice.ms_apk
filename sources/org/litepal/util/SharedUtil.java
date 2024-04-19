package org.litepal.util;

import android.content.SharedPreferences;
import android.text.TextUtils;
import org.litepal.LitePalApplication;
import org.litepal.util.Const;
/* loaded from: classes.dex */
public class SharedUtil {
    private static final String LITEPAL_PREPS = "litepal_prefs";
    private static final String VERSION = "litepal_version";

    public static void updateVersion(String extraKeyName, int newVersion) {
        SharedPreferences.Editor sEditor = LitePalApplication.getContext().getSharedPreferences(LITEPAL_PREPS, 0).edit();
        if (TextUtils.isEmpty(extraKeyName)) {
            sEditor.putInt(VERSION, newVersion);
        } else {
            if (extraKeyName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                extraKeyName = extraKeyName.replace(Const.Config.DB_NAME_SUFFIX, "");
            }
            sEditor.putInt("litepal_version_" + extraKeyName, newVersion);
        }
        sEditor.apply();
    }

    public static int getLastVersion(String extraKeyName) {
        SharedPreferences sPref = LitePalApplication.getContext().getSharedPreferences(LITEPAL_PREPS, 0);
        if (TextUtils.isEmpty(extraKeyName)) {
            return sPref.getInt(VERSION, 0);
        }
        if (extraKeyName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
            extraKeyName = extraKeyName.replace(Const.Config.DB_NAME_SUFFIX, "");
        }
        return sPref.getInt("litepal_version_" + extraKeyName, 0);
    }

    public static void removeVersion(String extraKeyName) {
        SharedPreferences.Editor sEditor = LitePalApplication.getContext().getSharedPreferences(LITEPAL_PREPS, 0).edit();
        if (TextUtils.isEmpty(extraKeyName)) {
            sEditor.remove(VERSION);
        } else {
            if (extraKeyName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                extraKeyName = extraKeyName.replace(Const.Config.DB_NAME_SUFFIX, "");
            }
            sEditor.remove("litepal_version_" + extraKeyName);
        }
        sEditor.apply();
    }
}

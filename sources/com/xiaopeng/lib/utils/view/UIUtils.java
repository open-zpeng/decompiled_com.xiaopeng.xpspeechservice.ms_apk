package com.xiaopeng.lib.utils.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.xiaopeng.lib.utils.LogUtils;
/* loaded from: classes.dex */
public class UIUtils {
    private static int height;
    private static int width;

    public static void init(Context context) {
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
    }

    public static int getScreenWidth() {
        return width;
    }

    public static int getScreenHeight() {
        return height;
    }

    public static int dip2px(Context context, float dipValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) ((dipValue * density) + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) ((pxValue / density) + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) ((spValue * fontScale) + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) ((pxValue / fontScale) + 0.5f);
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static float getTextWidth(String text) {
        Paint paint = new Paint(1);
        return paint.measureText(text);
    }

    public static float getTextHeight(String text) {
        Paint pFont = new Paint();
        Rect rect = new Rect();
        pFont.getTextBounds(text, 0, 1, rect);
        return rect.height();
    }

    public static int setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return 0;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + totalHeight;
        listView.setLayoutParams(params);
        return totalHeight;
    }

    public static void hideSystemUi(Context context, Window window) {
    }

    public static void showSystemUi(Context context, Window window) {
    }

    public static int getStatusBarHeight(Context context) {
        int resourceId1 = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int resourceId2 = context.getResources().getIdentifier("status_bar_bottom_height", "dimen", "android");
        try {
            int statusHeight = context.getResources().getDimensionPixelSize(resourceId1) + context.getResources().getDimensionPixelSize(resourceId2);
            return statusHeight;
        } catch (Exception e) {
            LogUtils.d("UIUtils", "e: " + e.getMessage());
            return 0;
        }
    }

    public static int getStatusBarBottomHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_bottom_height", "dimen", "android");
        try {
            int statusHeight = context.getResources().getDimensionPixelSize(resourceId);
            return statusHeight;
        } catch (Exception e) {
            LogUtils.d("UIUtils", "e: " + e.getMessage());
            return 0;
        }
    }
}

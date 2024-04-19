package com.xiaopeng.lib.utils.view.anim;

import android.view.View;
import android.view.ViewGroup;
import com.xiaopeng.lib.utils.view.anim.BaseAnimation;
/* loaded from: classes.dex */
public class LayoutAnimation extends BaseAnimation {
    private int mOrderIndex = 0;

    public void startAnimation(View root, BaseAnimation.AnimationType type) {
        bindAnimation(root, 0, type);
    }

    private void bindAnimation(View view, int depth, BaseAnimation.AnimationType type) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            if (type == BaseAnimation.AnimationType.HORIZON_CROSS) {
                for (int i = 0; i < group.getChildCount(); i++) {
                    bindAnimation(group.getChildAt(i), depth + 1, i % 2 == 0 ? BaseAnimation.AnimationType.HORIZON_LEFT : BaseAnimation.AnimationType.HORIZON_RIGHT);
                }
                return;
            }
            for (int i2 = 0; i2 < group.getChildCount(); i2++) {
                bindAnimation(group.getChildAt(i2), depth + 1, type);
            }
            return;
        }
        runAnimation(view, this.mDelay * this.mOrderIndex, type);
        this.mOrderIndex++;
    }
}

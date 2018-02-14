package com.app.xz.mynote.publics.core.utils;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dixon.xu on 2018/1/9.
 */

public class AnimUtils {

    public static void alpha(View view, long time, TimeInterpolator interpolator, AnimatorListenerAdapter adapter, float... value) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", value);
        alpha.setDuration(time);
        if (interpolator != null) {
            alpha.setInterpolator(interpolator);
        }
        if (adapter != null) {
            alpha.addListener(adapter);
        }
        alpha.start();
    }

    public static void height(final View view, long time, TimeInterpolator interpolator, AnimatorListenerAdapter adapter, int... value) {

        ValueAnimator height = ValueAnimator.ofInt(value);
        if (interpolator != null) {
            height.setInterpolator(interpolator);
        }
        if (adapter != null) {
            height.addListener(adapter);
        }
        height.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.requestLayout();
            }
        });
        height.setDuration(time);
        height.start();
    }

    public static void tranX(final View view, long time, TimeInterpolator interpolator, AnimatorListenerAdapter adapter, float... value) {
        ObjectAnimator tranX = ObjectAnimator.ofFloat(view, "translationX", value);
        tranX.setDuration(time);
        if (interpolator != null) {
            tranX.setInterpolator(interpolator);
        }
        if (adapter != null) {
            tranX.addListener(adapter);
        }
        tranX.start();
    }

    public static void tranY(final View view, long time, TimeInterpolator interpolator, AnimatorListenerAdapter adapter, float... value) {
        ObjectAnimator tranY = ObjectAnimator.ofFloat(view, "translationY", value);
        tranY.setDuration(time);
        if (interpolator != null) {
            tranY.setInterpolator(interpolator);
        }
        if (adapter != null) {
            tranY.addListener(adapter);
        }
        tranY.start();
    }
}

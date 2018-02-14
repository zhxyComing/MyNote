package com.app.xz.mynote.publics.core.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.TypedValue;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class DeviceUtils {

    private DeviceUtils() {

    }

    /***
     * 获取设备相关信息
     * @param context
     * @return
     */
    public static List<String> getDeviceInfo(Context context) {
        List<String> infos = new ArrayList<>();
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;

        try {
            info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (info != null) {
            infos.add("=====\tDevice Info\t=====");
            infos.add("manufacture:" + Build.MANUFACTURER);
            infos.add("product:" + Build.PRODUCT);
            infos.add("model:" + Build.MODEL);
            infos.add("version.release:" + Build.VERSION.RELEASE);
            infos.add("version:" + Build.DISPLAY);
            infos.add("=====\tApp Info\t=====");
            infos.add("versionCode:" + info.versionCode);
            infos.add("versionName:" + info.versionName);
            infos.add("lastUpdateTime:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format
                    (info.lastUpdateTime));
        }
        return infos;
    }


    /**
     * 是否是vivo手机
     */
    public static boolean isVivo() {
        return Build.BRAND.equalsIgnoreCase("vivo")
                || Build.MANUFACTURER.equalsIgnoreCase("vivo")
                || Build.MODEL.contains("vivo");
    }

    public static boolean isOppo() {
        return Build.BRAND.equalsIgnoreCase("oppo")
                || Build.MANUFACTURER.equalsIgnoreCase("oppo")
                || Build.MODEL.toLowerCase().contains("oppo");
    }

    public static int dp2px(Context context, float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    public static int px2dp(Context context, int px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight();
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //限定最大最小值
    public static int clamp(int target, int min, int max) {
        if (min >= max) return target;
        if (target < min) {
            return min;
        } else if (target > max) {
            return max;
        }
        return target;
    }

}

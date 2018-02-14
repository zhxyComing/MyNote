package com.app.xz.mynote.publics.core.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 权限验证工具类  一般可用 特殊权限如悬浮窗不可用
 * <p>
 * 包括权限 验证 获取 拒绝后获取
 * <p>
 * Created by dixon.xu on 2018/1/31.
 * <p>
 * 7.0动态权限 即使配置了权限 应用也不一定有该权限 这个类就是为适配这种情况：
 * 如没有存储权限 先检查 没有就申请获取 成功或失败有回调 失败后再打开弹窗提醒申请  所有这是一个动态申请权限的类
 * 但是对于悬浮窗的动态申请，无论是否开启都返回false，去开启也没有用 需要跳到专门的页面去获取
 * <p>
 * 至于为什么悬浮窗权限获取不到 应该和该权限是国产自己加的 android api找不到有关 所以申请也没用
 */

public class PermissionUtils {

    public static final int RC_PERM = 123;
    public static final int SETTINGS_REQ_CODE = 16061;
    private static AlertDialog mAlertDialog;

    /**
     * 检查并获取权限
     *
     * @param context
     * @param resString 权限被拒绝后 再次请求时 dialog的提示文字
     * @param mPerms    Manifest.permission.READ_EXTERNAL_STORAGE...等等
     * @return
     */
    public static boolean checkPermission(Context context, int resString, String... mPerms) {
        if (PermissionUtils.hasPermissions(context, mPerms)) {
            return true;
        } else {
            PermissionUtils.requestPermissions(context, context.getString(resString), RC_PERM, mPerms);
            return false;
        }
    }

    public static boolean checkPermission(Context context, String resString, String... mPerms) {
        if (PermissionUtils.hasPermissions(context, mPerms)) {
            return true;
        } else {
            PermissionUtils.requestPermissions(context, resString, RC_PERM, mPerms);
            return false;
        }
    }

    /**

     */
    public static boolean hasPermissions(Context context, String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }

        return true;
    }

    public static void requestPermissions(final Object object, String rationale, final int requestCode, final String... perms) {

        checkCallingObjectSuitability(object);

        boolean shouldShowRationale = false;
        for (String perm : perms) {
            shouldShowRationale = shouldShowRationale || shouldShowRequestPermissionRationale(object, perm);
        }

        if (shouldShowRationale) {//第一次拒绝 再次申请时 就会弹出弹窗
            Activity activity = getActivity(object);
            if (null == activity) {
                return;
            }

            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setMessage(rationale)
                    .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executePermissionsRequest(object, perms, requestCode);
                        }
                    })
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // act as if the permissions were denied
                            if (object instanceof PermissionResultListener) {
                                ((PermissionResultListener) object).onPermissionsDenied(requestCode, Arrays.asList(perms));
                            }
                        }
                    }).create();
            dialog.show();
        } else {
            executePermissionsRequest(object, perms, requestCode);
        }
    }

    /**
     *
     */
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, Object object) {
        checkCallingObjectSuitability(object);

        // Make a collection of granted and denied permissions from the request.
        ArrayList<String> granted = new ArrayList<>();
        ArrayList<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        // Report granted permissions, if any.
        if (!granted.isEmpty()) {
            // Notify callbacks
            if (object instanceof PermissionResultListener) {
                ((PermissionResultListener) object).onPermissionsGranted(requestCode, granted);
            }
        }

        // Report denied permissions, if any.
        if (!denied.isEmpty()) {
            if (object instanceof PermissionResultListener) {
                ((PermissionResultListener) object).onPermissionsDenied(requestCode, denied);
            }
        }

        // If 100% successful, call annotated methods
        if (!granted.isEmpty() && denied.isEmpty()) {
            if (object instanceof PermissionResultListener)
                ((PermissionResultListener) object).onPermissionsAllGranted();
        }
    }

    public static boolean checkDeniedPermissionsNeverAskAgain(final Object object, String rationale, List<String> deniedPerms) {
        boolean shouldShowRationale;
        for (String perm : deniedPerms) {
            shouldShowRationale = shouldShowRequestPermissionRationale(object, perm);
            if (!shouldShowRationale) {
                final Activity activity = getActivity(object);
                if (null == activity) {
                    return true;
                }

                mAlertDialog = new AlertDialog.Builder(activity)
                        .setMessage(rationale)
                        .setPositiveButton("SET", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                intent.setData(uri);
                                startAppSettingsScreen(object, intent);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (object instanceof PermissionResultListener) {
                                    ((PermissionResultListener) object).onCloseActivity();
                                }
                            }
                        })
                        .create();
                mAlertDialog.show();

                return true;
            }
        }

        return false;
    }

    public static void hideDialog() {
        if (mAlertDialog != null)
            mAlertDialog.dismiss();

        mAlertDialog = null;
    }

    @TargetApi(23)
    private static boolean shouldShowRequestPermissionRationale(Object object, String perm) {
        if (object instanceof Activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else {
            return false;
        }
    }

    @TargetApi(23)
    private static void executePermissionsRequest(Object object, String[] perms, int requestCode) {
        checkCallingObjectSuitability(object);

        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, perms, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(perms, requestCode);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).requestPermissions(perms, requestCode);
        }
    }

    @TargetApi(11)
    private static Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).getActivity();
        } else {
            return null;
        }
    }

    @TargetApi(11)
    private static void startAppSettingsScreen(Object object,
                                               Intent intent) {
        if (object instanceof Activity) {
            ((Activity) object).startActivityForResult(intent, SETTINGS_REQ_CODE);
        } else if (object instanceof Fragment) {
            ((Fragment) object).startActivityForResult(intent, SETTINGS_REQ_CODE);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).startActivityForResult(intent, SETTINGS_REQ_CODE);
        }
    }


    private static void checkCallingObjectSuitability(Object object) {
        // Make sure Object is an Activity or Fragment
        boolean isActivity = object instanceof Activity;
        boolean isSupportFragment = object instanceof Fragment;
        boolean isAppFragment = object instanceof android.app.Fragment;
        boolean isMinSdkM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

        if (!(isSupportFragment || isActivity || (isAppFragment && isMinSdkM))) {
            if (isAppFragment) {
                throw new IllegalArgumentException(
                        "Target SDK needs to be greater than 23 if caller is android.app.Fragment");
            } else {
                throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
            }
        }
    }


    /**
     * context可以继承这个接口 来实现权限验证回调的操作
     */
    public interface PermissionResultListener extends ActivityCompat.OnRequestPermissionsResultCallback {

        void onPermissionsGranted(int requestCode, List<String> perms);

        void onPermissionsDenied(int requestCode, List<String> perms);

        void onPermissionsAllGranted();

        void onCloseActivity();
    }

}

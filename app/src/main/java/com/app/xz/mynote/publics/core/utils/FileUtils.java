package com.app.xz.mynote.publics.core.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件工具类
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    public static void fileClone(File fromFile, File toFile, Boolean rewrite) {
        if (!fromFile.exists()) {
            return;
        }
        if (!fromFile.isFile()) {
            return;
        }
        if (!fromFile.canRead()) {
            return;
        }

        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        if (toFile.exists() && rewrite) {
            toFile.delete();
        }
        FileInputStream fosfrom = null;
        FileOutputStream fosto = null;
        try {
            fosfrom = new FileInputStream(fromFile);
            fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c); // 将内容写到新文件当中
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeQuietly(fosfrom);
            closeQuietly(fosto);
        }

    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    //7.0文件访问权限

    /**
     * 为什么这样做？
     * 7.0严格了权限限制 所以外部app访问本应用的文件路径时 不能再使用file类型的uri
     * 所以可以使用contentProvider的方式暴露出来
     * <p>
     * 1.
     * AndroidManifest添加  注意authorities要与方法中第二个参数相同 ：
     * <provider
     * android:name="android.support.v4.content.FileProvider"
     * android:authorities="com.app.xz.studyintent.fileProvider"
     * android:exported="false"
     * android:grantUriPermissions="true">
     * <!--设置要分享的路径-->
     * <meta-data
     * android:name="android.support.FILE_PROVIDER_PATHS"
     * android:resource="@xml/file_paths" />
     * </provider>
     * <p>
     * 2.
     * xml中添加文件 ：
     * <?xml version="1.0" encoding="utf-8"?>
     * <paths>
     * <!--注意 name是唯一的-->
     * <!--<files-path/>代表的根目录： Context.getFilesDir()-->
     * <!--<external-path/>代表的根目录: Environment.getExternalStorageDirectory()-->
     * <!--<cache-path/>代表的根目录: getCacheDir()-->
     * <external-path
     * name="files"
     * path="." />
     * </paths>
     * <p>
     * 3.
     * 添加临时权限
     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
     * intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
     * <p>
     * 这样做 就能获取供外部调用的uri地址了
     * <p>
     * 详情参考：https://www.jianshu.com/p/55eae30d133c
     *
     * @param context
     * @param file
     * @return
     */
    public static Uri getUriFromFile(Context context, File file) {
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
        } else {
            contentUri = Uri.fromFile(file);
        }

        return contentUri;
    }

    /**
     * 关闭流
     *
     * @param c
     */
    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException var2) {

            }
        }
    }

}

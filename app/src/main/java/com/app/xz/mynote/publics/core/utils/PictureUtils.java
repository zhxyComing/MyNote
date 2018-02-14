package com.app.xz.mynote.publics.core.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.app.xz.mynote.publics.core.utils.handler.ExecutorFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 照相或从相册获取图片的工具类
 * <p>
 * 使用说明：
 * 1.new 初始化
 * 2.activity的onActivityResult中调用pictureUtils.onActivityResult 以便回调可以被执行
 * 3.setOnSelectionListener 实现图片回调时的方法调用
 * <p>
 * 做了什么：
 * 1.检查权限 申请权限 PermissionUtils
 * 2.获取存储拍照临时图片的uri 7.0安全要求
 * 3.startAtyForResult 跳转
 * 4.回调获取大图File
 * 5.判断File是否满足大小要求 满足返回原图 不满足
 * 6.压缩 File大图文件->Bitmap缩略图->Bitmap压缩流输出到cacheFile文件
 * 7.返回cacheFile文件
 * <p>
 * 可见从头到位转化为bitmap的只有缩略图 压缩就直接输出到file了 最终获取到的能用的是file 不是bitmap
 * 这样做的好处是 一直是file的操作 很少涉及bitmap 节省内存
 * <p>
 * Created by dixon.xu on 2018/1/31.
 */

public class PictureUtils {

    private Activity context;

    private File mPhotoFile;

    private static final int UPLOAD_IMG_MAX_SIZE = 350 * 1024;//限制单张图片最大500kb，设置成350是为了【防止压缩】出误差

    //复用时只需要改这个的提示文字即可
    private static final String TIP_SELECT_FROM_GALLERY = "请给予应用必要的权限 才能从相册获取您的图片";
    private static final String TIP_SELECT_FROM_CAMERA = "请给予应用必要的权限 才能拍照获取图片";

    private OnSelectionListener selectionListener;

    public static final int REQUEST_CODE_CROP_IMAGE = 0x1000;
    public static final int REQUEST_CODE_PICK_IMAGE = 0x1001;
    public static final int REQUEST_CODE_TAKE_PHOTO = 0x1002;
    public static final int REQUEST_CODE_DEFAULT_IMAGE = 0x1003;

    public PictureUtils(Activity context) {
        this.context = context;
    }

    public void setOnSelectionListener(OnSelectionListener listener) {
        this.selectionListener = listener;
    }

    /**
     * 从相册获取图片
     */
    public void selectFromGrllery() {
        if (PermissionUtils.checkPermission(context, TIP_SELECT_FROM_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "你的手机不支持选图", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 拍照获取图片
     * <p>
     * 从相册获取图片有两种方法 一种是即可拿取原图 第二是现将图片存在本地 然后从本地获取该图
     * <p>
     * 这个方法是第二种
     */
    public void selectFromCamera() {

        // 注意 AndroidManifest里必须有这个权限 才能申请
        if (PermissionUtils.checkPermission(context, TIP_SELECT_FROM_CAMERA, Manifest.permission.CAMERA)) {

            mPhotoFile = null;

            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                //7.0权限  授予目录临时共享权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    try {
                        mPhotoFile = createImageFile(context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (mPhotoFile != null) {
                        /**
                         * 这个方法会将拍照完的图片保存在 指定的 本地uri路径上 (即保存在mPhotoFile下 这样在onAtyResult里就能直接拿 而不用uri转path了)
                         * 但是7.0系统无法简单的将file转化为uri 于是有如下方法
                         */
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUtils.getUriFromFile(context, mPhotoFile));
                        //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                    }
                    context.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
                }
            } else {
                Toast.makeText(context, "无法启动照相机", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != context.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_PICK_IMAGE:
            case REQUEST_CODE_TAKE_PHOTO:
                resultEvent(requestCode, data);
                break;
        }

    }

    public void onActivityResultWithTag(int requestCode, int resultCode, Intent data) {

        if (resultCode != context.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_PICK_IMAGE:
            case REQUEST_CODE_TAKE_PHOTO:
                resultEvent(requestCode, data);
                break;
        }

    }

    /**
     * 处理相册和拍照图片返回的事件
     * 包括：
     * 1.根据uri获取图片
     * 2.检查是否符合要求 （包括是否过大 但是这个过大并非MAX_SIZE 而是将max_size扩大100倍 目的是提前就筛掉一些过大的图）
     * 3.压缩图片
     * 4.返回图片
     *
     * @param requestCode
     * @param data
     */
    private void resultEvent(int requestCode, Intent data) {
        File uploadFile = null;//这个文件最终获取到的是原始大图file 不是压缩后的图 也不一定是返回的图
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            uploadFile = mPhotoFile;
        }
        String path;
        if (uploadFile == null && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    path = getRealFilePath(context, uri);
                    uploadFile = new File(path);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        if (uploadFile != null && uploadFile.exists()) {
            //检查文件是否符合大小要求
            if (!checkFile(uploadFile)) {
                return;
            }
            handleImageFile(uploadFile);
        } else {
            Toast.makeText(context, "选择图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * uri转path
     *
     * @param context
     * @param uri
     * @return
     */
    private String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 检查文件是否符合要求
     */
    private boolean checkFile(File file) {
        String filePath = file.getPath();
        if (!file.exists()) {
            Toast.makeText(context, "文件不存在", Toast.LENGTH_LONG).show();
            return false;
        }

        if (file.length() > 100 * UPLOAD_IMG_MAX_SIZE) {
            Toast.makeText(context, "图片过大", Toast.LENGTH_LONG).show();
            return false;
        }
        filePath = filePath.toLowerCase();
        boolean isPngOrJpg = filePath.endsWith("png") || filePath.endsWith("jpeg") || filePath.endsWith("jpg");
        if (!isPngOrJpg) {
            Toast.makeText(context, "图片格式不支持", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    /**
     * 子线程压缩图片 之后分发到主线程
     *
     * @param imageFile
     */
    protected void handleImageFile(final File imageFile) {
        ExecutorFactory.ASYNC.execute(new Runnable() {
            @Override
            public void run() {
                //压缩图片时 会自行判断 是否有必要压缩 没必要：返回原路径 有必要：返回新的缓存路径
                final File finalFile = compressImageFile(imageFile.getPath());
                ExecutorFactory.MAIN.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (selectionListener != null) {
                            selectionListener.onPictureSelected(finalFile);
                        }
                    }
                });
            }
        });
    }


    /**
     * 压缩图片
     *
     * @param filePath
     * @return
     */
    public File compressImageFile(String filePath) {
        File srcFile = new File(filePath);
//        File cacheDirectory = context.getCacheDir();//压缩后的图片保存在缓存文件夹下 文件浏览器看不到 这样用户即使删除了无用的拍完照后的文件 也不会影响到压缩完后有用的小文件
        File cacheDirectory = context.getFilesDir();//压缩后的图片保存在缓存文件夹下 文件浏览器看不到 这样用户即使删除了无用的拍完照后的文件 也不会影响到压缩完后有用的小文件 这里不使用cache 以防丢失
        File cachedFile = BitmapUtils.compressWithMaxSize(filePath, UPLOAD_IMG_MAX_SIZE, cacheDirectory);
        //压缩的过程出了问题，直接中断，并进行提示；返回原始图片
        if (cachedFile == null || !cachedFile.exists() || cachedFile.length() > UPLOAD_IMG_MAX_SIZE) {
            return srcFile;
        } else {
            //返回压缩图片 即app内缓存目录压缩好的文件
            /**
             * 这里 不管图片从拍照还是相册 最终压缩完的缓存图存储的路径是一致的 即app内的cache 文件浏览器看不到
             * 区别是 相册的图还在相册 而拍照的原图存在app sd卡目录 app被卸载时被删除 可被文件浏览器看到
             */
            return cachedFile;
        }
    }


    /**
     * 图片选取 压缩后的回调
     * <p>
     * ⭐️这个回调的file 存储在app内 属于app的文件 外部看不到 所以作为便签存储图来说 是安全的 且外部不可见！
     */
    public interface OnSelectionListener {
        void onPictureSelected(File pictureFile);
    }


    /**
     * 创建临时Image文件：因为拍的照比较大 所以只是临时存储 项目中使用肯定要压缩 tempfile
     * <p/>
     * NOTE: 所有存储在getExternalFilesDir())提供的目录中的文件会在用户卸载你的app后被删除。
     * <p>
     * 注：这个方法获取的是拍照后存储的图片路径 是压缩前的原始图片 是临时图片 占用sd但是不占用内存 因为内存获取的是压缩后的
     * 另外 createTempFile这个方法创建的文件同样不会被删除 除非app被卸载 所以它属于缓存文件的一部分
     * 这部分可以被优化 将文件存储在缓存文件中 方便统一删除缓存
     */
    public static File createImageFile(Context context) throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timeStamp = simpleDateFormat.format(new Date());
        String filename = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(filename, ".jpg", storageDir);
    }

}

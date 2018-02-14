package com.app.xz.mynote.publics.core.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片工具类
 *
 * 包括图片的 保存 压缩 获取
 */
public class BitmapUtils {

    public final static int BITMAP_COMPRESS_512 = 512;//KB
    public final static int BITMAP_COMPRESS_5120 = 5120;

    public static Bitmap compassBitmap(ContentResolver cr, Uri url) throws IOException {
        Bitmap bmp = null;
        InputStream input = cr.openInputStream(url);
        if (input != null) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 4;
            opt.inDither = false;
            bmp = BitmapFactory.decodeStream(input, null, opt);
        }
        return bmp;
    }

    public static Bitmap compassBitmap(ContentResolver cr, Uri url, int inSampleSize) throws IOException {
        Bitmap bmp = null;
        InputStream input = cr.openInputStream(url);
        if (input != null) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = inSampleSize;
            opt.inDither = false;
            bmp = BitmapFactory.decodeStream(input, null, opt);
        }
        return bmp;
    }

    /**
     * 给出一个原图 与目标图片的大小 计算压缩比例
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 存储图片到相册
     *
     * @param bmp
     * @param fileName
     * @return
     */
    public static File saveBitmapToFile(Bitmap bmp, String fileName) {
        //得到图片存储路径
        File picutreDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!picutreDir.exists())
            picutreDir.mkdirs();
        if (!fileName.endsWith(".png")) {
            fileName = fileName + ".png";
        }
        //生成目标文件
        File file = new File(picutreDir.getAbsolutePath(), fileName);
        //将目标bitmap转为流 存储在文件里
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }


    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    /**
     * bitmap转byte[]
     *
     * @param bmp
     * @param needRecycle bitmap是否回收
     * @return
     */
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 通过资源id获取bitmap
     *
     * @param context
     * @param redId
     * @return
     */
    public static Bitmap getBitmap(Context context, int redId) {
        Resources res = context.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, redId);
        return bmp;
    }

    /**
     * 保存bitmap到某文件
     *
     * @param bitmap
     * @param file   file是全路径
     * @return
     */
    public static boolean saveImageFile(Bitmap bitmap, File file) {
        if (bitmap == null || file == null)
            return false;

        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            //
            FileOutputStream output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 计算需要几倍采样
     * <p>
     * 不是很清楚其原理
     *
     * @param size
     * @param targetSize
     * @return
     */
    public static int needInSample(long size, long targetSize) {

        //double multiple = (int) ((double)size / (double)targetSize);
        long multiple = size / targetSize;

        if (size > multiple * targetSize) {
            multiple++;
        }

        if (multiple <= 1) {
            return 1;
        }

        int result = 1;
        while ((result * result) < multiple) {
            result = result * 2;
        }

        return result;
    }

    /**
     * 直接把图片文件进行压缩，并保存到cache中,优先改变图片大小，其次压缩
     * <p>
     * 压缩过程：
     * 1.判断是否过大 （超过max 100倍）
     * 2.获取缩略图    BitmapFactory.decodeFile(path, options);
     * 3.获取压缩图    bitmap.compress(format, quality, outputStream);
     *
     * @param originPath 源文件路径
     * @param maxSize
     * @param directory  保存文件路径 不包括文件名
     * @return 失败，或者图片没有超过最大的大小，则直接把原路径返回。否则返回压缩后的文件
     */
    public static File compressWithMaxSize(String originPath, int maxSize, File directory) throws IllegalArgumentException {

        if (originPath == null) {
            throw new IllegalArgumentException("originPath == null");
        }

        File srcFile = new File(originPath);
        File desFile = new File(directory.getAbsolutePath() + "/" + srcFile.getName());

        if (!srcFile.exists()) {
            return srcFile;
        }

        //目标文件已经足够小了，没必要再小了
        if (srcFile.length() < maxSize) {
            return srcFile;
        }

        FileOutputStream outputStream = null;
        try {
            //1.判断是否过大 压缩到1%还不满足就过大 inSample意思为压缩以为源图片的百分之xx （以防巨图撑爆内存）
            int inSample = (int) (maxSize * 100 / srcFile.length());

            if (inSample > 100) {
                inSample = 100;
            }

            if (inSample >= 1) {

                // 2.通过计算采样率 获取缩小了一定比例的缩略图 而不是原图
                // 这还不是最终图 因为可能比max要大 也可能比max小（缩略图节省内存）
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = needInSample(srcFile.length(), Long.valueOf(maxSize));

                Bitmap targetBitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);

                //开始压缩
                if (desFile.exists()) {
                    desFile.delete();
                }
                desFile.createNewFile();


                outputStream = new FileOutputStream(desFile);
                Bitmap.CompressFormat format;
                if (originPath.endsWith("jpg") || originPath.endsWith("jpeg")) {
                    format = Bitmap.CompressFormat.JPEG;
                } else {
                    format = Bitmap.CompressFormat.PNG;
                }

                //这里的计算方法是通过不断尝试压缩来实现的，效率很低，目前没有什么好办法，以后有新的再改
                int quality = getCompressQuality(targetBitmap, maxSize / 1024);

                //3.bitmap.compress最终压缩 ：通过循环试压缩得到最佳结果比例 然后执行压缩
                targetBitmap.compress(format, quality, outputStream);
                outputStream.flush();
                outputStream.close();
            } else {
                //需要提示用户这个图片实在太大了，压缩到1%都不行
                return null;
            }
        } catch (Exception e) {

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e1) {
                }
            }

            return null;
        }

        return desFile;
    }

    /**
     * getCompressOptions不明白为什么要有个[200,500]KB的判断，所以弄个不限制的出来用
     *
     * @param srcBitmap
     * @param size
     * @return
     */
    public static int getCompressQuality(Bitmap srcBitmap, int size) {
        if (srcBitmap == null)
            return 0;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;

        while (options >= 30 && byteArrayOutputStream.toByteArray().length / 1024 > size) {  //循环判断如果压缩后图片是否大于size,大于继续压缩
            byteArrayOutputStream.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, options, byteArrayOutputStream);//这里压缩options%，把压缩后的数据存放到baos中
        }

        return options;
    }

    //指定大小，size表示kb，一般200,500等
    public static int getCompressOptions(Bitmap srcBitmap, int size) {
        if (srcBitmap == null)
            return 0;

        if (size < 200)
            size = 200;
        else if (size >= 5120)
            size = 5120;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;

        while (options >= 30 && byteArrayOutputStream.toByteArray().length / 1024 > size) {  //循环判断如果压缩后图片是否大于size,大于继续压缩
            byteArrayOutputStream.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, options, byteArrayOutputStream);//这里压缩options%，把压缩后的数据存放到baos中
        }

        return options;
    }

    //指定尺寸
    public static Bitmap compressBitmapScale(Bitmap srcBitmap) {
        if (srcBitmap == null)
            return null;

        int bitmapWidth = srcBitmap.getWidth();
        int bitmapHeight = srcBitmap.getHeight();

        //指定尺寸
        float width = 1080;
        float height = 1080;

        Bitmap resizeBitmap = null;
        if (bitmapWidth > width || bitmapHeight > height) {
            if (bitmapWidth >= bitmapHeight)
                height = (width / bitmapWidth) * bitmapHeight;
            else
                width = (height / bitmapHeight) * bitmapWidth;

            float scaleWidth = width / bitmapWidth;
            float scaleHeight = height / bitmapHeight;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 产生缩放后的Bitmap对象
            resizeBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
        } else
            resizeBitmap = srcBitmap;

        return resizeBitmap;
    }
}

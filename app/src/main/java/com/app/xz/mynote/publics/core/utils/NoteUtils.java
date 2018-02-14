package com.app.xz.mynote.publics.core.utils;

import android.content.Context;

import java.io.File;

/**
 * Created by dixon.xu on 2018/2/1.
 */

public class NoteUtils {

    public static File getCacheFile(Context context) {
        return context.getCacheDir();
    }

    public static File getNoteFile(Context context) {
        File file = new File(getCacheFile(context) + "/note");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            return file;
        }
        return null;
    }
}

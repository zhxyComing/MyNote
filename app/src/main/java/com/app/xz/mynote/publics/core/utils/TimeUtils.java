package com.app.xz.mynote.publics.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dixon.xu on 2018/2/2.
 */

public class TimeUtils {

    private static final String FORMAT = "yyyy-MM-dd";

    public static String format(long time) {
        SimpleDateFormat format = new SimpleDateFormat(FORMAT);
        return format.format(new Date(time));
    }

    public static String format(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(FORMAT);
        return format.format(date);
    }

}

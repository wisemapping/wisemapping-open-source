package com.wisemapping.util;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

final public class TimeUtils
{
    private static final SimpleDateFormat sdf;
    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String toISO8601(@Nullable Date date) {
        String result = "";
        if (date != null) {
            result = sdf.format(date) + "Z";
        }
        return result;
    }

}

package com.tuanfadbg.progress.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static final String FOLDER = "Before vs After";

    public static int diffDay(long first, long end) {
        long diffInMillies = first - end;
        int diff = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        if (diff < 1) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.ENGLISH);
            String currentDate = sdf.format(new Date(first));
            String createAt = sdf.format(new Date(end));
            if (currentDate.equals(createAt))
                diff = 0;
            else diff = 1;
        }
        return diff;
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String getTimeFromLong(long time) {
        Date date = new Date(time);
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    }

    public static String getFolderPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/" + Utils.FOLDER;
    }

    public static boolean isGraintedPermission(Context activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED;
    }
}

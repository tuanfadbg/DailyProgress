package com.tuanfadbg.trackprogress.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.activation.MimetypesFileTypeMap;

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

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context) {
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

    public static boolean isEmailValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    public static int[] getScreenWidthAndHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        int[] result = new int[2];
        result[0] = width;
        result[1] = height;
        return result;
    }

    public static boolean isImage(String filepath) {
        try {
            File f = new File(filepath);
            String mimetype = new MimetypesFileTypeMap().getContentType(f);
            String type = mimetype.split("/")[0];
            if (type.equals("image"))
                return true;
            else
                return false;
        } catch (Exception e) {
            return true;
        }
    }

    public static int getRandomColor(int position) {
        position = position % 16;
        int[] color = new int[16];
        color[0] = Color.parseColor("#ffbddc");
        color[1] = Color.parseColor("#91c8ff");
        color[2] = Color.parseColor("#3c9c69");
        color[3] = Color.parseColor("#a0c29b");
        color[4] = Color.parseColor("#00b9ae");
        color[5] = Color.parseColor("#bd9bc2");
        color[6] = Color.parseColor("#db4365");
        color[7] = Color.parseColor("#fe6594");
        color[8] = Color.parseColor("#7b77c8");
        color[9] = Color.parseColor("#4cc3f8");
        color[10] = Color.parseColor("#feb794");
        color[11] = Color.parseColor("#ffdb5c");
        color[12] = Color.parseColor("#7f86ce");
        color[13] = Color.parseColor("#b8e0d2");
        color[14] = Color.parseColor("#cd6f8e");
        color[15] = Color.parseColor("#f6dfd7");
        return color[position];
    }

    public static int convertColorToHex(String color) {
        if (color.length() == 6)
            color = "ff" + color;
        return (int) Long.parseLong(color, 16);
    }

    public static Drawable getCircleDrawableByColor(int randomColor) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(randomColor);
        gradientDrawable.setCornerRadius(100f);
        return gradientDrawable;
    }
}

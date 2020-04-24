package com.tuanfadbg.progress.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

import com.tuanfadbg.progress.ui.passcode.CheckPasscodeActivity;

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
}

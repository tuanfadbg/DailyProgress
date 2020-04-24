package com.tuanfadbg.progress.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SharePreferentUtils {
    private static final SharedPreferencesLoader sPrefsLoader = new SharedPreferencesLoader();
    private static Future<SharedPreferences> sReferrerPrefs;
    private static Context mContext;
    public static final String IS_PREMIUM = "IS_PREMIUM";
    public static final String IS_PASSCODE_ENABLE = "IS_PASSCODE_ENABLE";
    public static final String PASSCODE = "PASSCODE";
    public static final String TEMP_PASSCODE = "TEMP_PASSCODE";
    public static final String EMAIL = "EMAIL";

    public static void initial(Context context) {
        mContext = context;
        if (null == sReferrerPrefs) {
            sReferrerPrefs = sPrefsLoader.loadPreferences(context, context.getPackageName(), null);
        }
    }

    public static boolean isPremium() {
        return (boolean) SharePreferentUtils.getSharedPreference(IS_PREMIUM, false);
    }

    public static void setPremium(boolean isPremium) {
        SharePreferentUtils.setSharedPreference(IS_PREMIUM, isPremium);
    }

    public static boolean isPasscodeEnable() {
        return (boolean) SharePreferentUtils.getSharedPreference(IS_PASSCODE_ENABLE, false);
    }

    public static void setPasscodeEnable(boolean isEnable) {
        SharePreferentUtils.setSharedPreference(IS_PASSCODE_ENABLE, isEnable);
    }

    public static boolean checkPasscode(String passcode) {
        String pass = (String) SharePreferentUtils.getSharedPreference(PASSCODE, "");
        if (TextUtils.isEmpty(pass))
            return false;
        String tempPasscode = getTempPasscode();
        if (tempPasscode.equals(passcode)) {
            setTempPasscode("");
            setNewPasscode(tempPasscode);
            return true;
        }
        return pass.equals(passcode);
    }

    public static void setNewPasscode(String newPasscode) {
        SharePreferentUtils.setSharedPreference(PASSCODE, newPasscode);
    }

    public static String createTempPasscode() {
        String tempPasscode = String.format(Locale.US, "%4d", new Random().nextInt(9999));
        setTempPasscode(tempPasscode);
        return tempPasscode;
    }

    public static String getTempPasscode() {
        return (String) SharePreferentUtils.getSharedPreference(TEMP_PASSCODE, "");
    }

    public static void setTempPasscode(String newPasscode) {
        SharePreferentUtils.setSharedPreference(TEMP_PASSCODE, newPasscode);
    }

    public static void setEmail(String email) {
        SharePreferentUtils.setSharedPreference(EMAIL, email);
    }

    public static String getEmail() {
        return (String) SharePreferentUtils.getSharedPreference(EMAIL, "");
    }

    public static Object getSharedPreference(String keyPref, Object defaultValue) {
        SharedPreferences pref;
        try {
            pref = sReferrerPrefs.get();

            if (defaultValue instanceof String) {
                return pref.getString(keyPref, (String) defaultValue);
            } else if (defaultValue instanceof Integer) {
                return pref.getInt(keyPref, (Integer) defaultValue);
            } else if (defaultValue instanceof Float) {
                return pref.getFloat(keyPref, (Float) defaultValue);
            } else if (defaultValue instanceof Boolean) {
                return pref.getBoolean(keyPref, (Boolean) defaultValue);
            } else if (defaultValue instanceof Long) {
                return pref.getLong(keyPref, (Long) defaultValue);
            }
        } catch (InterruptedException | ExecutionException ignored) {
        }
        return null;
    }

    /**
     * Set data pref into file save pref with multitype data
     *
     * @param keyPref   Key to map with column in file pref
     * @param valuePref value to input column value in file pref
     * @Link String, Integer, Float, Boolean
     */
    public static void setSharedPreference(String keyPref, Object valuePref) {
        try {
            SharedPreferences pref = sReferrerPrefs.get();
            SharedPreferences.Editor editor = pref.edit();
            if (valuePref == null) return;
            if (valuePref instanceof String) {
                editor.putString(keyPref, (String) valuePref);
            } else if (valuePref instanceof Integer) {
                editor.putInt(keyPref, (Integer) valuePref);
            } else if (valuePref instanceof Float) {
                editor.putFloat(keyPref, (Float) valuePref);
            } else if (valuePref instanceof Boolean) {
                editor.putBoolean(keyPref, (Boolean) valuePref);
            } else if (valuePref instanceof Long) {
                editor.putLong(keyPref, (Long) valuePref);
            }
            editor.apply();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }
}

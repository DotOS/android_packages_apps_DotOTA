package com.dot.systemupdates.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs implements Constants {
    public static String PREF_NAME = "SystemUpdates";

    private Prefs() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean getDownloadFinished(Context context) {
        return getPrefs(context).getBoolean(IS_DOWNLOAD_FINISHED, false);
    }

    public static boolean getDeleteAfterInstall(Context context) {
        return getPrefs(context).getBoolean(DELETE_AFTER_INSTALL, false);
    }

    public static boolean getWipeData(Context context) {
        return getPrefs(context).getBoolean(WIPE_DATA, false);
    }

    public static boolean getWipeCache(Context context) {
        return getPrefs(context).getBoolean(WIPE_CACHE, false);
    }

    public static boolean getWipeDalvik(Context context) {
        return getPrefs(context).getBoolean(WIPE_DALVIK, false);
    }

    public static void setUpdateLastChecked(Context context, String time) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(LAST_CHECKED, time);
        editor.apply();
    }

    public static void setDownloadFinished(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(IS_DOWNLOAD_FINISHED, value);
        editor.apply();
    }

    public static void setTheme(Context context, String value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(CURRENT_THEME, value);
        editor.apply();
    }

    public static void setDeleteAfterInstall(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(DELETE_AFTER_INSTALL, value);
        editor.apply();
    }

    public static void setWipeData(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(WIPE_DATA, value);
        editor.apply();
    }

    public static void setWipeCache(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(WIPE_CACHE, value);
        editor.apply();
    }

    public static void setWipeDalvik(Context context, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(WIPE_DALVIK, value);
        editor.apply();
    }
}

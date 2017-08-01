package com.rusdelphi.timer;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;


public class SP {
    private final static String PREF_FILE = "TimerActivity";

    static final String STOPWATCH_LIST = "STOPWATCH_LIST";
    static final String TIMER_LIST = "TIMER_LIST";
    static final String CHECKED = "CHECKED";
    static final String GRID = "GRID";
    static final String ID = "ID";
    static final String MELODY = "MELODY";
    static final String VIBRATE = "VIBRATE";
    static final String VIBRATE_SHORT = "VIBRATE_SHORT";
    static final String VIBRATE_LONG = "VIBRATE_LONG";
    static final String WIDGET = "WIDGET";
    private static SharedPreferences mSettings;

    private static SharedPreferences getSettings(Context context) {
        if (mSettings == null)
            mSettings = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        return mSettings;
    }

    static void setString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    static void setBoolean(Context context, String key, Boolean value) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    static void setInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    static String getString(Context context, String key, String defValue) {
        return getSettings(context).getString(key, defValue);
    }


    static int getInt(Context context, String key, int defValue) {
        return getSettings(context).getInt(key, defValue);
    }


    static boolean getBoolean(Context context, String key, boolean defValue) {
        return getSettings(context).getBoolean(key, defValue);
    }
}

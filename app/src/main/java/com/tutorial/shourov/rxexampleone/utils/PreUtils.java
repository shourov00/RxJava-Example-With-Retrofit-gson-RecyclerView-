package com.tutorial.shourov.rxexampleone.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Shourov on 01,December,2018
 * Store api key that trigger every http authentication
 */
public class PreUtils {
    /**
     * Storing API Key in shared preferences to
     * add it in header part of every retrofit request
     */
    private static final String APP_PREF ="APP_PREF";
    public PreUtils() {
    }

    public static String getApiKey(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(APP_PREF,null);
    }

    public static void setApiKey(Context context,String apiKey){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(APP_PREF,apiKey)
                .apply();
    }

    /*private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE);
    }

    public static void storeApiKey(Context context, String apiKey) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("API_KEY", apiKey);
        editor.commit();
    }

    public static String getApiKey(Context context) {
        return getSharedPreferences(context).getString("API_KEY", null);
    }*/
}

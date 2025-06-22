package com.smart24.alpha_robot.utils;


import static com.smart24.alpha_robot.utils.ConstantStrings.appLocaleKey;
import static com.smart24.alpha_robot.utils.ConstantStrings.myPreferenceKey;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MyApp extends Application {
    private static Context context;
    private static MyApp instance;
    private SharedPreferences sharedPreferences;

    public static MyApp getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return context;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();
        sharedPreferences = getSharedPreferences(myPreferenceKey, Context.MODE_PRIVATE);


    }

    public void saveLanguageCode(String newLocale) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(appLocaleKey, newLocale);
        editor.apply();
    }

    public String getAppLanguage() {
        if (sharedPreferences.contains(appLocaleKey)) {
            return sharedPreferences.getString(appLocaleKey, "error");
        } else {
            return "error";
        }
    }
}

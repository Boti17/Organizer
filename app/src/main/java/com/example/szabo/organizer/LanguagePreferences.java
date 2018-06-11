package com.example.szabo.organizer;

import android.content.Context;
import android.content.SharedPreferences;

public class LanguagePreferences {

    private static LanguagePreferences mInstance;
    private SharedPreferences preferences;

    public static LanguagePreferences getInstance() {
        if (mInstance == null) {
            mInstance = new LanguagePreferences();
        }
        return mInstance;
    }

    public void setContext(Context context) {
        preferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
    }

    public void setLanguage(String langCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language", langCode);
        editor.apply();
    }

    ;

    public String getLanguage() {
        return preferences.getString("language", null);
    }
}

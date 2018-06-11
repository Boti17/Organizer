package com.example.szabo.organizer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST = 112;
    private static final int SPLASH_TIME_OUT = 3000;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LanguagePreferences languagePreferences = LanguagePreferences.getInstance();
        languagePreferences.setContext(this);
        final String language= languagePreferences.getLanguage();
        if (!Locale.getDefault().getLanguage().equals(language) && language!=null) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            languagePreferences.setLanguage(language);

            this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        if (!isNetworkConnected())
        {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.checkInternet), Toast.LENGTH_LONG).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isNetworkConnected()) {
                    while (!isNetworkConnected());
                    checkPermissions();
                }
                else
                {
                    checkPermissions();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void startHomeActivity() {
        Intent mainIntent = new Intent(MainActivity.this,EventListActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0) {
                    boolean grantedPermissions = true;
                    for (int result: grantResults)
                    {
                        if (result != PackageManager.PERMISSION_GRANTED)
                        {
                            grantedPermissions = false;
                        }
                    }
                    if (!grantedPermissions)
                    {
                        Toast.makeText(this, getResources().getString(R.string.permissionsNotGranted), Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        startHomeActivity();
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.permissionsNotGranted), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private static boolean hasPermissions(Context context, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkPermissions()
    {
        if (Build.VERSION.SDK_INT >= 21) {
            String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE};
            if (!hasPermissions(context, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, REQUEST);
            } else {
                startHomeActivity();
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}

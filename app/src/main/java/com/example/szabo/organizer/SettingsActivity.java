package com.example.szabo.organizer;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity {

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RecyclerView languageList = (RecyclerView) findViewById(R.id.languageList);
        languageList.setLayoutManager(new LinearLayoutManager(this));
        LanguageAdapter adapter = new LanguageAdapter(this);
        languageList.setAdapter(adapter);
        TextView mAbout = (TextView) findViewById(R.id.aboutTV);
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);

            StringBuilder information = new StringBuilder();
            information.append("\nPackage name: ").append(info.packageName)
                    .append("\nVersion code: ").append(info.versionCode)
                    .append("\nVersion name: ").append(info.versionName)
                    .append("\nFirst install: ").append(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").format(new Date(info.firstInstallTime)))
                    .append("\nLast update: ").append(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").format(new Date(info.lastUpdateTime)))
                    .append("\n\n").append(getResources().getString(R.string.contact)+" szabobotond17@yahoo.com");
            mAbout.setText(information);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}

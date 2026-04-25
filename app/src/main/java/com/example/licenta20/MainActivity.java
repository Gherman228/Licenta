package com.example.licenta20;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.data.AppConfig;
import com.example.licenta20.data.AppDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rvApps);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 1. Inițializăm baza de date
        AppDatabase db = AppDatabase.getInstance(this);

        // 2. Luăm lista de aplicații (care acum verifică și baza de date)
        List<AppInfo> installedApps = getInstalledApps(db);

        // 3. Pasăm baza de date și la Adapter ca să poată salva click-urile
        AppAdapter adapter = new AppAdapter(installedApps, db);
        recyclerView.setAdapter(adapter);
    }

    private List<AppInfo> getInstalledApps(AppDatabase db) {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = getPackageManager();


        List<AppConfig> savedConfigs = db.appDao().getAllConfigs();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : packages) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {

               //Nume+iconita
                String name = app.loadLabel(pm).toString();
                android.graphics.drawable.Drawable icon = app.loadIcon(pm);

                AppInfo appInfo = new AppInfo(name, app.packageName, icon);

                for (AppConfig config : savedConfigs) {
                    if (config.getPackageName().equals(app.packageName)) {
                        appInfo.setSelected(config.isBlocked());
                        break;
                    }
                }
                appList.add(appInfo);
            }
        }

        Collections.sort(appList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return appList;
    }
}
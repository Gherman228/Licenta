package com.example.licenta20;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        //Metoda de citire

        List<AppInfo> installedApps = getInstalledApps();
        AppAdapter adapter = new AppAdapter(installedApps);
        recyclerView.setAdapter(adapter);

    }

    private List<AppInfo> getInstalledApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = getPackageManager();

        // Aplicati instalate
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : packages) {
            // Verificare de deschidere
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                String name = app.loadLabel(pm).toString();
                android.graphics.drawable.Drawable icon = app.loadIcon(pm);
                appList.add(new AppInfo(name, app.packageName, icon));
            }
        }

        // Sortare alfabetica
        Collections.sort(appList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        return appList;
    }
}
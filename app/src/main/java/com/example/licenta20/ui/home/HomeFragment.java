package com.example.licenta20.ui.home;


import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.data.AppConfig;
import com.example.licenta20.data.AppDatabase;
import com.example.licenta20.ui.home.AppAdapter;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private AppDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Umflăm layout-ul fragmentului
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Legăm RecyclerView-ul
        recyclerView = view.findViewById(R.id.rvAppsHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Inițializăm baza de date
        db = AppDatabase.getInstance(requireContext());

        // Pornim procesul de încărcare
        loadApps();

        return view;
    }

    private void loadApps() {
        // Rulăm pe thread separat pentru a nu bloca interfața (obligatoriu pentru Room)
        new Thread(() -> {
            PackageManager pm = requireContext().getPackageManager();
            List<AppConfig> savedConfigs = db.appDao().getAllConfigs();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            List<AppInfo> appList = new ArrayList<>();

            for (ApplicationInfo app : packages) {
                if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                    String name = app.loadLabel(pm).toString();
                    android.graphics.drawable.Drawable icon = app.loadIcon(pm);
                    AppInfo appInfo = new AppInfo(name, app.packageName, icon);

                    // Verificăm dacă e salvată în DB
                    for (AppConfig config : savedConfigs) {
                        if (config.getPackageName().equals(app.packageName)) {
                            appInfo.setSelected(config.isBlocked());
                            break;
                        }
                    }
                    appList.add(appInfo);
                }
            }

            // Sortăm alfabetic
            Collections.sort(appList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

            // Afișăm pe ecran (pe thread-ul principal de UI)
            if (isAdded()) { // Verificăm dacă fragmentul mai e activ
                requireActivity().runOnUiThread(() -> {
                    adapter = new AppAdapter(appList, db);
                    recyclerView.setAdapter(adapter);
                });
            }
        }).start();
    }
}
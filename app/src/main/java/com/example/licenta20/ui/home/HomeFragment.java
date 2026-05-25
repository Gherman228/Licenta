package com.example.licenta20.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer; // Import nou
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar; // Import nou
import android.widget.TextView; // Import nou
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.data.AppConfig;
import com.example.licenta20.data.AppDatabase;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale; // Import nou

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private AppDatabase db;
    private Button btnStartFocus;

    // Variabile noi pentru Timer
    private TextView tvTimerString;
    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private long timeInMillis = 25 * 60 * 1000; // 25 de minute în milisecunde

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Legăm componentele din XML
        recyclerView = view.findViewById(R.id.rvAppsHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        btnStartFocus = view.findViewById(R.id.btnStartFocus);

        // Legăm elementele noi din XML
        tvTimerString = view.findViewById(R.id.tvTimerString);
        progressBar = view.findViewById(R.id.progressBar);

        // Inițializăm baza de date
        db = AppDatabase.getInstance(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("KairosPrefs", Context.MODE_PRIVATE);

        // 1. Când deschidem aplicația, forțăm resetarea Focusului la fals (pentru a nu avea bug-uri cu timerul în fundal deocamdată)
        prefs.edit().putBoolean("isFocusActive", false).apply();
        updateButtonUI(false);
        updateTimerText(timeInMillis);
        progressBar.setProgress(100);

        // 2. Logica pentru click pe butonul de START/STOP
        btnStartFocus.setOnClickListener(v -> {
            boolean isFocusActive = prefs.getBoolean("isFocusActive", false);

            if (!isFocusActive) {
                // PORNIM FOCUSUL
                prefs.edit().putBoolean("isFocusActive", true).apply();
                updateButtonUI(true);
                startTimer(prefs);
                Toast.makeText(getContext(), "Focus activat! Rămâi concentrat.", Toast.LENGTH_SHORT).show();
            } else {
                // OPRIM FOCUSUL MANUAL
                prefs.edit().putBoolean("isFocusActive", false).apply();
                updateButtonUI(false);
                stopTimer();
                Toast.makeText(getContext(), "Focus oprit. Liber la navigare!", Toast.LENGTH_SHORT).show();
            }
        });

        // Pornim procesul de încărcare pentru listă
        loadApps();

        return view;
    }

    // --- METODE NOI PENTRU TIMER ---

    private void startTimer(SharedPreferences prefs) {
        // Creăm un cronometru care scade o dată pe secundă (1000 ms)
        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeInMillis = millisUntilFinished;
                updateTimerText(millisUntilFinished);

                // Actualizăm progresul cercului
                int progress = (int) (millisUntilFinished * 100 / (25 * 60 * 1000));
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                // Când timpul a expirat, oprim blocarea automat
                prefs.edit().putBoolean("isFocusActive", false).apply();
                updateButtonUI(false);

                // Resetăm timpul pentru următoarea sesiune
                timeInMillis = 25 * 60 * 1000;
                updateTimerText(timeInMillis);
                progressBar.setProgress(100);

                Toast.makeText(getContext(), "Bravo! Sesiunea de focus s-a terminat.", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // Resetăm timpul înapoi la 25 de minute
        timeInMillis = 25 * 60 * 1000;
        updateTimerText(timeInMillis);
        progressBar.setProgress(100);
    }

    private void updateTimerText(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        // Formatăm textul sub forma MM:SS (ex: 24:59)
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimerString.setText(timeLeftFormatted);
    }

    // --- METODE EXISTENTE ---

    private void updateButtonUI(boolean isActive) {
        if (isActive) {
            btnStartFocus.setText("STOP FOCUS");
            btnStartFocus.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            btnStartFocus.setTextColor(Color.WHITE);
        } else {
            btnStartFocus.setText("START FOCUS");
            btnStartFocus.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btnStartFocus.setTextColor(Color.BLACK);
        }
    }

    private void loadApps() {
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

                    for (AppConfig config : savedConfigs) {
                        if (config.getPackageName().equals(app.packageName)) {
                            appInfo.setSelected(config.isBlocked());
                            appInfo.setInterceptCount(config.getInterceptCount());
                            break;
                        }
                    }
                    appList.add(appInfo);
                }
            }

            Collections.sort(appList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    adapter = new AppAdapter(appList, db);
                    recyclerView.setAdapter(adapter);
                });
            }
        }).start();
    }
}
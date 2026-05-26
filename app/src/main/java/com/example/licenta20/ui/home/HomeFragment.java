package com.example.licenta20.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker; // Import nou pentru rotițe
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.data.AppConfig;
import com.example.licenta20.data.AppDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private AppDatabase db;
    private Button btnStartFocus;

    // Variabile pentru Timer și UI
    private TextView tvTimerString;
    private ProgressBar progressBar;
    private TimePicker timePicker; // Rotițele
    private View presetScrollView; // Containerul cu preseturi

    private CountDownTimer countDownTimer;
    private long timeInMillis = 25 * 60 * 1000; // Timpul curent
    private long initialSelectedTimeMax = 25 * 60 * 1000; // Salvează maximul ales pentru calculul corect al cercului

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Legăm componentele
        recyclerView = view.findViewById(R.id.rvAppsHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        btnStartFocus = view.findViewById(R.id.btnStartFocus);
        tvTimerString = view.findViewById(R.id.tvTimerString);
        progressBar = view.findViewById(R.id.progressBar);

        // Legăm elementele noi
        timePicker = view.findViewById(R.id.timePicker);
        presetScrollView = view.findViewById(R.id.presetScrollView);

        db = AppDatabase.getInstance(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("KairosPrefs", Context.MODE_PRIVATE);

        // Forțăm resetarea Focusului la pornire
        prefs.edit().putBoolean("isFocusActive", false).apply();
        updateButtonUI(false);
        progressBar.setProgress(100);

        // Configurare TimePicker
        timePicker.setIs24HourView(true);
        timePicker.setHour(0);
        timePicker.setMinute(25);
        timePicker.setOnTimeChangedListener((viewPicker, hourOfDay, minute) -> {
            timeInMillis = ((hourOfDay * 60L) + minute) * 60 * 1000;
            if (timeInMillis == 0) {
                timeInMillis = 60 * 1000; // Evităm crash, punem minim 1 minut
            }
        });

        // Configurare butoane PRESETS
        view.findViewById(R.id.btnPreset15).setOnClickListener(v -> setPresetTime(0, 15));
        view.findViewById(R.id.btnPreset25).setOnClickListener(v -> setPresetTime(0, 25));
        view.findViewById(R.id.btnPreset30).setOnClickListener(v -> setPresetTime(0, 30));
        view.findViewById(R.id.btnPreset45).setOnClickListener(v -> setPresetTime(0, 45));
        view.findViewById(R.id.btnPreset60).setOnClickListener(v -> setPresetTime(1, 0));

        // Logica START/STOP
        btnStartFocus.setOnClickListener(v -> {
            boolean isFocusActive = prefs.getBoolean("isFocusActive", false);

            if (!isFocusActive) {
                // Dacă se apasă START, preluăm timpul exact de pe rotițe
                timeInMillis = ((timePicker.getHour() * 60L) + timePicker.getMinute()) * 60 * 1000;
                if (timeInMillis == 0) timeInMillis = 60 * 1000; // Minim 1 minut

                initialSelectedTimeMax = timeInMillis; // Salvăm totalul pentru cerc

                prefs.edit().putBoolean("isFocusActive", true).apply();
                updateButtonUI(true);

                // Schimbăm UI-ul: Ascundem rotițele și preset-urile, arătăm textul care scade
                timePicker.setVisibility(View.GONE);
                presetScrollView.setVisibility(View.GONE);
                tvTimerString.setVisibility(View.VISIBLE);

                startTimer(prefs);
                Toast.makeText(getContext(), "Focus activat! Rămâi concentrat.", Toast.LENGTH_SHORT).show();
            } else {
                // OPRIM FOCUSUL
                prefs.edit().putBoolean("isFocusActive", false).apply();
                updateButtonUI(false);
                stopTimer();

                // Schimbăm UI-ul la loc
                timePicker.setVisibility(View.VISIBLE);
                presetScrollView.setVisibility(View.VISIBLE);
                tvTimerString.setVisibility(View.GONE);

                Toast.makeText(getContext(), "Focus oprit. Liber la navigare!", Toast.LENGTH_SHORT).show();
            }
        });

        loadApps();

        return view;
    }

    // --- METODE NOI ---

    private void setPresetTime(int hours, int minutes) {
        // Mișcăm rotițele automat la valoarea selectată din buton
        timePicker.setHour(hours);
        timePicker.setMinute(minutes);
        timeInMillis = ((hours * 60L) + minutes) * 60 * 1000;
    }

    private void startTimer(SharedPreferences prefs) {
        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeInMillis = millisUntilFinished;
                updateTimerText(millisUntilFinished);

                // Progresul se calculează raportat la timpul selectat la început
                int progress = (int) (millisUntilFinished * 100 / initialSelectedTimeMax);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                prefs.edit().putBoolean("isFocusActive", false).apply();
                updateButtonUI(false);

                // Readucem interfața la modul de selecție
                timePicker.setVisibility(View.VISIBLE);
                presetScrollView.setVisibility(View.VISIBLE);
                tvTimerString.setVisibility(View.GONE);
                progressBar.setProgress(100);

                Toast.makeText(getContext(), "Bravo! Sesiunea de focus s-a terminat.", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        progressBar.setProgress(100);
    }

    private void updateTimerText(long millis) {
        int hours = (int) (millis / 1000) / 3600;
        int minutes = (int) ((millis / 1000) % 3600) / 60;
        int seconds = (int) (millis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
        tvTimerString.setText(timeLeftFormatted);
    }

    // --- METODE EXISTENTE PĂSTRATE ---

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
package com.example.licenta20.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licenta20.R;
import com.example.licenta20.data.AppConfig;
import com.example.licenta20.data.AppDatabase;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private AppDatabase db;
    private Button btnStartFocus;
    private Button btnEditBlocklist;

    // Variabile Dashboard
    private TextView tvFocusScore, tvSavedTime, tvDistractionsScore;
    private TextView tvTimerString;
    private ProgressBar progressBar;
    private View viewFocusPulse; // Nucleul central
    private View controlPanel; // Rândul cu pastile de timp

    // Variabile Timp (Butoane Pastile)
    private Button btn15, btn25, btn30, btn60;

    // Logica Timer
    private CountDownTimer countDownTimer;
    private long selectedTimeInMillis = 25 * 60 * 1000; // Valoare default (25m)
    private long currentMillisLeft;
    private long initialSelectedTimeMax;

    // Animație pentru nucleu
    private AlphaAnimation pulseAnimation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Legăm componentele Dashboard
        tvFocusScore = view.findViewById(R.id.tvFocusScore);
        tvSavedTime = view.findViewById(R.id.tvSavedTime);
        tvDistractionsScore = view.findViewById(R.id.tvDistractionsScore);
        tvTimerString = view.findViewById(R.id.tvTimerString);
        progressBar = view.findViewById(R.id.progressBar);
        viewFocusPulse = view.findViewById(R.id.viewFocusPulse);
        controlPanel = view.findViewById(R.id.controlPanel);
        btnStartFocus = view.findViewById(R.id.btnStartFocus);
        btnEditBlocklist = view.findViewById(R.id.btnEditBlocklist);

        // Legăm Butoanele Pastile de Timp
        btn15 = view.findViewById(R.id.btnPreset15);
        btn25 = view.findViewById(R.id.btnPreset25);
        btn30 = view.findViewById(R.id.btnPreset30);
        btn60 = view.findViewById(R.id.btnPreset60);

        db = AppDatabase.getInstance(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("KairosPrefs", Context.MODE_PRIVATE);

        // Resetăm starea Focusului la pornire
        prefs.edit().putBoolean("isFocusActive", false).apply();
        updateButtonUI(false);
        progressBar.setProgress(100);

        // Inițializăm Datele pe Dashboard
        populateStats();

        // Configuram animația de puls
        setupPulseAnimation();

        // Configurare logică Pastile de Timp
        btn15.setOnClickListener(v -> setTime(0, 15, btn15));
        btn25.setOnClickListener(v -> setTime(0, 25, btn25));
        btn30.setOnClickListener(v -> setTime(0, 30, btn30));
        btn60.setOnClickListener(v -> setTime(1, 0, btn60));

        // Pornește pastila de 25 default
        selectPill(btn25);

        // Logica pentru butonul de Aplicații (Meniul de jos)
        btnEditBlocklist.setOnClickListener(v -> {
            openAppsBottomSheet();
        });

        // Logica START/STOP
        btnStartFocus.setOnClickListener(v -> {
            boolean isFocusActive = prefs.getBoolean("isFocusActive", false);

            if (!isFocusActive) {
                // PORNIM FOCUSUL
                prefs.edit().putBoolean("isFocusActive", true).apply();
                updateButtonUI(true);

                initialSelectedTimeMax = selectedTimeInMillis;
                currentMillisLeft = selectedTimeInMillis;

                // Schimbăm UI-ul: Ascundem nucleul, controlul și butonul de aplicații
                viewFocusPulse.setVisibility(View.GONE);
                controlPanel.setVisibility(View.GONE);
                btnEditBlocklist.setVisibility(View.GONE);
                tvTimerString.setVisibility(View.VISIBLE);

                // Oprim animația de puls
                if (pulseAnimation != null) pulseAnimation.cancel();

                startTimer(prefs);
                Toast.makeText(getContext(), "Focus activat! Rămâi concentrat.", Toast.LENGTH_SHORT).show();
            } else {
                // OPRIM FOCUSUL MANUAL
                prefs.edit().putBoolean("isFocusActive", false).apply();
                updateButtonUI(false);
                stopTimer();

                // Schimbăm UI-ul la loc
                resetIdleUI();
                Toast.makeText(getContext(), "Focus oprit. Liber la navigare!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // --- METODE NOI ---

    private void populateStats() {
        tvFocusScore.setText("80%");

        // Preluăm numărul TOTAL de distragere evitate din DB
        new Thread(() -> {
            List<AppConfig> allConfigs = db.appDao().getAllConfigs();
            int totalIntercepts = 0;
            for (AppConfig config : allConfigs) {
                totalIntercepts += config.getInterceptCount();
            }
            final String distractionsText = String.valueOf(totalIntercepts);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvDistractionsScore.setText(distractionsText);
                    tvSavedTime.setText("2h");
                });
            }
        }).start();
    }

    private void setTime(int hours, int minutes, Button selectedPill) {
        selectedTimeInMillis = ((hours * 60L) + minutes) * 60 * 1000;
        selectPill(selectedPill);
    }

    private void selectPill(Button pill) {
        deselectPill(btn15);
        deselectPill(btn25);
        deselectPill(btn30);
        deselectPill(btn60);

        pill.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        pill.setTextColor(Color.BLACK);
    }

    private void deselectPill(Button pill) {
        pill.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#222222")));
        pill.setTextColor(Color.WHITE);
    }

    private void setupPulseAnimation() {
        pulseAnimation = new AlphaAnimation(1.0f, 0.6f);
        pulseAnimation.setDuration(1500);
        pulseAnimation.setRepeatMode(Animation.REVERSE);
        pulseAnimation.setRepeatCount(Animation.INFINITE);

        viewFocusPulse.startAnimation(pulseAnimation);
    }

    private void resetIdleUI() {
        viewFocusPulse.setVisibility(View.VISIBLE);
        controlPanel.setVisibility(View.VISIBLE);
        btnEditBlocklist.setVisibility(View.VISIBLE);
        tvTimerString.setVisibility(View.GONE);

        progressBar.setProgress(100);
        setupPulseAnimation();
    }

    private void startTimer(SharedPreferences prefs) {
        countDownTimer = new CountDownTimer(currentMillisLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentMillisLeft = millisUntilFinished;
                updateTimerText(millisUntilFinished);

                int progress = (int) (millisUntilFinished * 100 / initialSelectedTimeMax);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                prefs.edit().putBoolean("isFocusActive", false).apply();
                updateButtonUI(false);
                resetIdleUI();
                Toast.makeText(getContext(), "Bravo! Sesiunea de focus s-a terminat.", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        resetIdleUI();
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

    private void openAppsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_apps, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView rvBottomSheetApps = sheetView.findViewById(R.id.rvBottomSheetApps);
        rvBottomSheetApps.setLayoutManager(new LinearLayoutManager(requireContext()));

        new Thread(() -> {
            PackageManager pm = requireContext().getPackageManager();
            List<AppConfig> savedConfigs = db.appDao().getAllConfigs();

            android.content.Intent mainIntent = new android.content.Intent(android.content.Intent.ACTION_MAIN, null);
            mainIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
            List<android.content.pm.ResolveInfo> resolvedInfos = pm.queryIntentActivities(mainIntent, 0);

            List<AppInfo> appList = new ArrayList<>();

            for (android.content.pm.ResolveInfo resolveInfo : resolvedInfos) {
                String packageName = resolveInfo.activityInfo.packageName;

                if (packageName.equals(requireContext().getPackageName())) continue;

                boolean alreadyAdded = false;
                for (AppInfo addedApp : appList) {
                    if (addedApp.getPackageName().equals(packageName)) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (alreadyAdded) continue;

                String name = resolveInfo.loadLabel(pm).toString();
                android.graphics.drawable.Drawable icon = resolveInfo.loadIcon(pm);
                AppInfo appInfo = new AppInfo(name, packageName, icon);

                for (AppConfig config : savedConfigs) {
                    if (config.getPackageName().equals(packageName)) {
                        appInfo.setSelected(config.isBlocked());
                        appInfo.setInterceptCount(config.getInterceptCount());
                        break;
                    }
                }
                appList.add(appInfo);
            }

            java.util.Collections.sort(appList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    AppAdapter adapter = new AppAdapter(appList, db);
                    rvBottomSheetApps.setAdapter(adapter);
                });
            }
        }).start();

        bottomSheetDialog.show();
    }
}
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
import com.example.licenta20.data.DailyStat;
import com.example.licenta20.ui.adaptor.CardAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private AppDatabase db;
    private Button btnStartFocus;
    private View btnEditBlocklist;

    private TextView tvFocusScore, tvSavedTime, tvDistractionsScore;
    private TextView tvTimerString;
    private ProgressBar progressBar;
    private View viewFocusPulse;

    private int selectedTimeMinutes = 30;
    private static final int INCREMENT = 5;
    private static final int MIN_TIME = 5;

    private TextView tvTimerSelection;
    private Button btnMinusTime;
    private Button btnPlusTime;
    private View layoutTimerControls;

    private CountDownTimer countDownTimer;
    private long selectedTimeInMillis;
    private long currentMillisLeft;
    private long initialSelectedTimeMax;

    private AlphaAnimation pulseAnimation;

    private RecyclerView rvSetups;
    private RecyclerView rvSoundscapes;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvFocusScore = view.findViewById(R.id.tvFocusScore);
        tvSavedTime = view.findViewById(R.id.tvSavedTime);
        tvDistractionsScore = view.findViewById(R.id.tvDistractionsScore);
        tvTimerString = view.findViewById(R.id.tvTimerString);
        progressBar = view.findViewById(R.id.progressBar);
        viewFocusPulse = view.findViewById(R.id.viewFocusPulse);

        btnStartFocus = view.findViewById(R.id.btnStartTimer);
        btnEditBlocklist = view.findViewById(R.id.btnBlockApps);

        tvTimerSelection = view.findViewById(R.id.tvTimerSelection);
        btnMinusTime = view.findViewById(R.id.btnMinusTime);
        btnPlusTime = view.findViewById(R.id.btnPlusTime);
        layoutTimerControls = view.findViewById(R.id.layoutTimerControls);

        rvSetups = view.findViewById(R.id.rvSetups);
        rvSoundscapes = view.findViewById(R.id.rvSoundscapes);

        db = AppDatabase.getInstance(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("KairosPrefs", Context.MODE_PRIVATE);

        prefs.edit().putBoolean("isFocusActive", false).apply();
        updateButtonUI(false);
        progressBar.setProgress(100);

        populateStats();
        setupPulseAnimation();
        updateTimerSelectionDisplay();
        setupHorizontalLists();

        btnPlusTime.setOnClickListener(v -> {
            selectedTimeMinutes += INCREMENT;
            updateTimerSelectionDisplay();
        });

        btnMinusTime.setOnClickListener(v -> {
            if (selectedTimeMinutes > MIN_TIME) {
                selectedTimeMinutes -= INCREMENT;
                updateTimerSelectionDisplay();
            }
        });

        btnEditBlocklist.setOnClickListener(v -> openAppsBottomSheet());

        btnStartFocus.setOnClickListener(v -> {
            boolean isFocusActive = prefs.getBoolean("isFocusActive", false);

            if (!isFocusActive) {
                prefs.edit().putBoolean("isFocusActive", true).apply();
                updateButtonUI(true);

                selectedTimeInMillis = selectedTimeMinutes * 60 * 1000L;
                initialSelectedTimeMax = selectedTimeInMillis;
                currentMillisLeft = selectedTimeInMillis;

                viewFocusPulse.setVisibility(View.GONE);
                if(rvSetups != null) rvSetups.setVisibility(View.GONE);
                if(rvSoundscapes != null) rvSoundscapes.setVisibility(View.GONE);
                if(layoutTimerControls != null) layoutTimerControls.setVisibility(View.GONE);

                tvTimerString.setVisibility(View.VISIBLE);

                if (pulseAnimation != null) pulseAnimation.cancel();
                startTimer(prefs);
                Toast.makeText(getContext(), "Focus activat! Rămâi concentrat.", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit().putBoolean("isFocusActive", false).apply();
                updateButtonUI(false);
                stopTimer();
                resetIdleUI();
                Toast.makeText(getContext(), "Focus oprit. Liber la navigare!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void updateTimerSelectionDisplay() {
        if (selectedTimeMinutes < 60) {
            tvTimerSelection.setText(selectedTimeMinutes + "m");
        } else {
            int hours = selectedTimeMinutes / 60;
            int minutes = selectedTimeMinutes % 60;
            if (minutes == 0) {
                tvTimerSelection.setText(hours + "h");
            } else {
                tvTimerSelection.setText(hours + "h " + minutes + "m");
            }
        }
    }

    private void setupHorizontalLists() {
        // 1. Configurăm lista For You (Setups)
        if (rvSetups != null) {
            rvSetups.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            List<CardItem> setupsList = new ArrayList<>();
            setupsList.add(new CardItem("Laser Focus", "60m - Intense", "▶ Start", android.R.color.holo_blue_dark));
            setupsList.add(new CardItem("Deep Work", "120m - Flow state", "▶ Start", android.R.color.holo_purple));
            setupsList.add(new CardItem("Study Sprint", "45m - Exam prep", "▶ Start", android.R.color.holo_orange_dark));

            CardAdapter setupsAdapter = new CardAdapter(setupsList, new CardAdapter.OnItemClickListener() {
                @Override
                public void onActionClick(CardItem item) {
                    // Când apasă pe butonul mic "▶ Start"
                    startPresetTimer(item.getTitle());
                }

                @Override
                public void onCardClick(CardItem item) {
                    // Când apasă pe card în general (îl putem lăsa gol sau pune un mesaj)
                    Toast.makeText(getContext(), "Ai apăsat pe cardul " + item.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
            rvSetups.setAdapter(setupsAdapter);
        }

        // 2. Configurăm lista Soundscapes
        if (rvSoundscapes != null) {
            rvSoundscapes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            List<CardItem> sleepList = new ArrayList<>();
            sleepList.add(new CardItem("City Rain", "Ambient Noise", "▶ Play", android.R.color.darker_gray));
            sleepList.add(new CardItem("Ocean Waves", "Relaxing Water", "▶ Play", android.R.color.holo_blue_light));
            sleepList.add(new CardItem("Forest Night", "Crickets & Owls", "▶ Play", android.R.color.holo_green_dark));

            CardAdapter sleepAdapter = new CardAdapter(sleepList, new CardAdapter.OnItemClickListener() {
                @Override
                public void onActionClick(CardItem item) {
                    // Când apasă pe butonul mic "▶ Play"
                    Toast.makeText(getContext(), "Se pregătește sunetul: " + item.getTitle() + " 🎵", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCardClick(CardItem item) {
                    // Când apasă pe cardul de sunet
                    Toast.makeText(getContext(), "Setări pentru: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
            rvSoundscapes.setAdapter(sleepAdapter);
        }
    }

    // ==========================================
    // METODA NOUĂ PENTRU LOGICA BUTOANELOR
    // ==========================================
    private void startPresetTimer(String presetName) {
        int minutesToSet = 30; // Timpul default dacă nu găsește numele

        // Mapăm fiecare nume de card cu durata lui specifică în minute
        if (presetName.equals("Laser Focus")) {
            minutesToSet = 60;
        } else if (presetName.equals("Deep Work")) {
            minutesToSet = 120;
        } else if (presetName.equals("Study Sprint")) {
            minutesToSet = 45;
        }

        // Verificăm dacă nu cumva timer-ul merge deja
        SharedPreferences prefs = requireContext().getSharedPreferences("KairosPrefs", Context.MODE_PRIVATE);
        boolean isFocusActive = prefs.getBoolean("isFocusActive", false);

        if (isFocusActive) {
            Toast.makeText(getContext(), "Oprește sesiunea curentă mai întâi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Setăm timpul intern pe baza cardului ales
        selectedTimeMinutes = minutesToSet;
        updateTimerSelectionDisplay();

        // 2. MAGIC TRICK: Simulăm o apăsare pe butonul mare "Start Timer"
        // ca să declanșăm toate animațiile, ascunderea listelor și baza de date!
        btnStartFocus.performClick();

        Toast.makeText(getContext(), "Sesiunea " + presetName + " a început!", Toast.LENGTH_SHORT).show();
    }

    private void populateStats() {
        new Thread(() -> {
            List<AppConfig> allConfigs = db.appDao().getAllConfigs();
            int totalIntercepts = 0;
            for (AppConfig config : allConfigs) {
                totalIntercepts += config.getInterceptCount();
            }

            String today = getTodayDate();
            DailyStat todayStat = db.dailyStatDao().getStatForDate(today);

            final String distractionsText = String.valueOf(totalIntercepts);
            final String focusScoreText = (todayStat != null) ? todayStat.getFocusScore() + "%" : "100%";

            long totalTimeToday = (todayStat != null) ? todayStat.getTotalFocusTime() : 0;
            int hours = (int) (totalTimeToday / (1000 * 60 * 60));
            int mins = (int) ((totalTimeToday / (1000 * 60)) % 60);

            final String savedTimeText;
            if (hours > 0) {
                savedTimeText = hours + "h " + mins + "m";
            } else {
                savedTimeText = mins + "m";
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvDistractionsScore.setText(distractionsText);
                    tvFocusScore.setText(focusScoreText);
                    tvSavedTime.setText(savedTimeText);
                });
            }
        }).start();
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void saveSessionToDatabase(long timeFocusedInMillis) {
        new Thread(() -> {
            String today = getTodayDate();
            DailyStat todayStat = db.dailyStatDao().getStatForDate(today);

            if (todayStat == null) {
                todayStat = new DailyStat(today, timeFocusedInMillis, 0, 100);
            } else {
                long newTotalTime = todayStat.getTotalFocusTime() + timeFocusedInMillis;
                todayStat.setTotalFocusTime(newTotalTime);
            }

            db.dailyStatDao().insertOrUpdate(todayStat);

            if (isAdded()) {
                requireActivity().runOnUiThread(this::populateStats);
            }
        }).start();
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
        if(rvSetups != null) rvSetups.setVisibility(View.VISIBLE);
        if(rvSoundscapes != null) rvSoundscapes.setVisibility(View.VISIBLE);
        tvTimerString.setVisibility(View.GONE);
        if(layoutTimerControls != null) layoutTimerControls.setVisibility(View.VISIBLE);
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
                saveSessionToDatabase(initialSelectedTimeMax);
                Toast.makeText(getContext(), "Bravo! Sesiunea de focus s-a terminat.", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        resetIdleUI();
        long timeFocused = initialSelectedTimeMax - currentMillisLeft;
        if (timeFocused > 0) saveSessionToDatabase(timeFocused);
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
            btnStartFocus.setText("STOP TIMER");
            btnStartFocus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
            btnStartFocus.setTextColor(Color.WHITE);
        } else {
            btnStartFocus.setText("▶ START TIMER");
            btnStartFocus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3E456A")));
            btnStartFocus.setTextColor(Color.WHITE);
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
                    if (addedApp.getPackageName().equals(packageName)) { alreadyAdded = true; break; }
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

    // Această metodă se rulează AUTOMAT de fiecare dată când intri pe ecranul Home
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = requireContext().getSharedPreferences("KairosPrefs", Context.MODE_PRIVATE);
        boolean shouldAutoStart = prefs.getBoolean("autoStartTimer", false);
        boolean shouldOpenBlockList = prefs.getBoolean("openBlockListNow", false); // <-- VERIFICĂM COMANDA NOUĂ

        // Dacă am primit comanda de Timer din Setups...
        if (shouldAutoStart) {
            prefs.edit().putBoolean("autoStartTimer", false).apply();
            if (!prefs.getBoolean("isFocusActive", false)) {
                int mins = prefs.getInt("autoStartMinutes", 30);
                selectedTimeMinutes = mins;
                updateTimerSelectionDisplay();
                btnStartFocus.performClick();
            }
        }

        // Dacă am primit comanda "Deschide Block List" din Setups...
        if (shouldOpenBlockList) {
            // Ștergem comanda imediat
            prefs.edit().putBoolean("openBlockListNow", false).apply();
            // Deschidem fereastra cu lista de aplicații existentă!
            openAppsBottomSheet();
        }
    }

}
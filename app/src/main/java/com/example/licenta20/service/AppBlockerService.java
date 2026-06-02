package com.example.licenta20.service;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.licenta20.data.AppConfig;
import com.example.licenta20.data.AppDatabase;
import com.example.licenta20.data.BlockSetup;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppBlockerService extends AccessibilityService {

    private AppDatabase db;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        db = AppDatabase.getInstance(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                String currentApp = event.getPackageName().toString();

                if (currentApp.equals(getPackageName()) || currentApp.contains("launcher")) {
                    return;
                }

                checkAndBlockApp(currentApp);
            }
        }
    }

    private void checkAndBlockApp(String packageName) {
        new Thread(() -> {
            boolean shouldBlock = false;

            // 1. VERIFICARE: TIMER MANUAL (Prioritate)
            SharedPreferences prefs = getSharedPreferences("KairosPrefs", MODE_PRIVATE);
            boolean isFocusActive = prefs.getBoolean("isFocusActive", false);

            if (isFocusActive) {
                String activeBlockedAppsString = prefs.getString("activeSetupBlockedApps", "");
                if (activeBlockedAppsString != null && !activeBlockedAppsString.isEmpty()) {
                    List<String> blockedPackages = Arrays.asList(activeBlockedAppsString.split(","));
                    if (blockedPackages.contains(packageName)) {
                        shouldBlock = true;
                    }
                }
            }

            // 2. VERIFICARE: ORAR AUTOMAT (SCHEDULE)
            if (!shouldBlock) {
                List<BlockSetup> allSetups = db.blockSetupDao().getAllBlocksSync();
                if (allSetups != null) {
                    for (BlockSetup setup : allSetups) {

                        // IGNORĂM ORARUL DACĂ ESTE PUS PE PAUZĂ
                        if (setup.isPaused()) {
                            continue;
                        }

                        String blockedApps = setup.getBlockedApps();

                        if (blockedApps != null && blockedApps.contains(packageName)) {
                            if (isCurrentlyInSchedule(setup.getDaysActive(), setup.getTimeRange())) {
                                shouldBlock = true;
                                break;
                            }
                        }
                    }
                }
            }

            // 3. EXECUTARE BLOCARE ȘI STATISTICI
            if (shouldBlock) {
                performGlobalAction(GLOBAL_ACTION_HOME);

                AppConfig config = db.appDao().getConfigByPackage(packageName);
                if (config == null) {
                    config = new AppConfig(packageName, false);
                    db.appDao().insertOrUpdate(config);
                }
                db.appDao().incrementInterceptCount(packageName);
                Log.d("AppBlocker", "Interceptat! +1 pentru: " + packageName);
            }

        }).start();
    }

    // AICI E REPARATĂ ORA PENTRU ROMÂNIA (Locale.US)
    private boolean isCurrentlyInSchedule(String daysString, String timeRange) {
        if (daysString == null || timeRange == null || daysString.equals("Only Once") || daysString.isEmpty()) return false;

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        boolean isDayMatch = false;
        String[] activeDays = daysString.split(" ");
        for (String d : activeDays) {
            if (d.equals("M") && currentDay == Calendar.MONDAY) isDayMatch = true;
            if (d.equals("T") && currentDay == Calendar.TUESDAY) isDayMatch = true;
            if (d.equals("W") && currentDay == Calendar.WEDNESDAY) isDayMatch = true;
            if (d.equals("Th") && currentDay == Calendar.THURSDAY) isDayMatch = true;
            if (d.equals("F") && currentDay == Calendar.FRIDAY) isDayMatch = true;
            if (d.equals("S") && currentDay == Calendar.SATURDAY) isDayMatch = true;
            if (d.equals("Su") && currentDay == Calendar.SUNDAY) isDayMatch = true;
        }

        if (!isDayMatch) return false;

        try {
            String[] parts = timeRange.split(" - ");
            if (parts.length != 2) return false;

            // FOLOSIM Locale.US PENTRU AM/PM
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);

            Date startTime = sdf.parse(parts[0]);
            Date endTime = sdf.parse(parts[1]);

            String currentTimeStr = sdf.format(calendar.getTime());
            Date currentTime = sdf.parse(currentTimeStr);

            if (currentTime != null && startTime != null && endTime != null) {
                if (startTime.after(endTime)) {
                    return currentTime.after(startTime) || currentTime.before(endTime) || currentTime.equals(startTime) || currentTime.equals(endTime);
                } else {
                    return (currentTime.after(startTime) && currentTime.before(endTime)) || currentTime.equals(startTime) || currentTime.equals(endTime);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onInterrupt() {}
}
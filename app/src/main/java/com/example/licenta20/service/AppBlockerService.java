package com.example.licenta20.service;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.licenta20.data.AppConfig;
import com.example.licenta20.data.AppDatabase;

public class AppBlockerService extends AccessibilityService {

    private AppDatabase db;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Inițializăm baza de date când serviciul pornește
        db = AppDatabase.getInstance(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                String currentApp = event.getPackageName().toString();

                // Nu blocăm propria noastră aplicație sau launcher-ul (ecranul de pornire)
                if (currentApp.equals(getPackageName()) || currentApp.contains("launcher")) {
                    return;
                }

                checkAndBlockApp(currentApp);
            }
        }
    }

    private void checkAndBlockApp(String packageName) {
        // 1. Citim starea butonului de START/STOP salvată de HomeFragment
        SharedPreferences prefs = getSharedPreferences("KairosPrefs", MODE_PRIVATE);
        boolean isFocusActive = prefs.getBoolean("isFocusActive", false);

        // 2. Dacă Focusul NU este activ, ne oprim aici (liber la navigare)
        if (!isFocusActive) {
            return;
        }

        // 3. Dacă Focusul ESTE activ, verificăm în baza de date dacă aplicația e bifată
        new Thread(() -> {
            AppConfig config = db.appDao().getConfigByPackage(packageName);

            if (config != null && config.isBlocked()) {

                // --- AICI ESTE PARTEA NOUĂ ---
                // Salvăm interceptarea în baza de date! (+1 la numărătoare)
                db.appDao().incrementInterceptCount(packageName);
                Log.d("AppBlocker", "Interceptat! +1 pentru: " + packageName);
                // -----------------------------

                // Dacă e blocată, trimitem utilizatorul la "Acasă" (Home)
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
        }).start();
    }

    @Override
    public void onInterrupt() {}
}
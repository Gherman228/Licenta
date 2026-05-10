package com.example.licenta20.service;

import android.accessibilityservice.AccessibilityService;
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
        new Thread(() -> {
            // Căutăm în DB dacă aplicația este marcată ca blocată
            AppConfig config = db.appDao().getConfigByPackage(packageName);

            if (config != null && config.isBlocked()) {
                // Dacă e blocată, trimitem utilizatorul la "Acasă" (Home)
                performGlobalAction(GLOBAL_ACTION_HOME);

                // Opțional: Poți afișa un mesaj (pe thread-ul principal)
                /*
                new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(getApplicationContext(), "Kairos: Această aplicație este blocată!", Toast.LENGTH_SHORT).show());
                */
            }
        }).start();
    }

    @Override
    public void onInterrupt() {}
}
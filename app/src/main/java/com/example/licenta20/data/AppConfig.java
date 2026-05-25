package com.example.licenta20.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_configs")
public class AppConfig {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String packageName; // CNP-ul aplicației

    private boolean isBlocked;
    private int dailyLimitMinutes;
    private int currentStreak;     // Pentru streak-uri
    private int interceptCount;    // NOU: Pentru numărarea blocărilor (salvărilor)

    // Un singur constructor care le inițializează pe toate
    public AppConfig(@NonNull String packageName, boolean isBlocked) {
        this.packageName = packageName;
        this.isBlocked = isBlocked;
        this.dailyLimitMinutes = 0;
        this.currentStreak = 0;
        this.interceptCount = 0; // Pornește de la zero
    }

    // --- Getters și Setters ---

    @NonNull
    public String getPackageName() { return packageName; }
    public void setPackageName(@NonNull String packageName) { this.packageName = packageName; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public int getDailyLimitMinutes() { return dailyLimitMinutes; }
    public void setDailyLimitMinutes(int dailyLimitMinutes) { this.dailyLimitMinutes = dailyLimitMinutes; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getInterceptCount() { return interceptCount; }
    public void setInterceptCount(int interceptCount) { this.interceptCount = interceptCount; }
}
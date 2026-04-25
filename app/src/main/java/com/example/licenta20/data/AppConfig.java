package com.example.licenta20.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_configs")
public class AppConfig {
    @PrimaryKey(autoGenerate = false)
    @androidx.annotation.NonNull
    private String packageName; // CNP-ul aplicației

    private boolean isBlocked;
    private int dailyLimitMinutes; // Pentru setările de care ziceai
    private int currentStreak;     // Pentru streak-uri

    public AppConfig(String packageName, boolean isBlocked) {
        this.packageName = packageName;
        this.isBlocked = isBlocked;
        this.dailyLimitMinutes = 0;
        this.currentStreak = 0;
    }

    // Getters și Setters
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
    public int getDailyLimitMinutes() { return dailyLimitMinutes; }
    public void setDailyLimitMinutes(int dailyLimitMinutes) { this.dailyLimitMinutes = dailyLimitMinutes; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
}
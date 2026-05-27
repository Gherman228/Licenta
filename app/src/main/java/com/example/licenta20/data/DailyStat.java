package com.example.licenta20.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_stats")
public class DailyStat {

    @PrimaryKey
    @NonNull
    private String date; // Va fi cheia primară (ex: "2026-05-27")

    private long totalFocusTime; // Cât timp a stat în focus în milisecunde
    private int distractionsAvoided; // Câte aplicații a blocat în ziua respectivă
    private int focusScore; // Scorul calculat pe ziua respectivă (0-100%)

    // Constructor
    public DailyStat(@NonNull String date, long totalFocusTime, int distractionsAvoided, int focusScore) {
        this.date = date;
        this.totalFocusTime = totalFocusTime;
        this.distractionsAvoided = distractionsAvoided;
        this.focusScore = focusScore;
    }

    // Getteri și Setteri
    @NonNull
    public String getDate() { return date; }
    public void setDate(@NonNull String date) { this.date = date; }

    public long getTotalFocusTime() { return totalFocusTime; }
    public void setTotalFocusTime(long totalFocusTime) { this.totalFocusTime = totalFocusTime; }

    public int getDistractionsAvoided() { return distractionsAvoided; }
    public void setDistractionsAvoided(int distractionsAvoided) { this.distractionsAvoided = distractionsAvoided; }

    public int getFocusScore() { return focusScore; }
    public void setFocusScore(int focusScore) { this.focusScore = focusScore; }
}
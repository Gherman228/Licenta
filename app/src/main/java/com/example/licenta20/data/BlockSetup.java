package com.example.licenta20.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "block_setups")
public class BlockSetup {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;          // ex: "Laser Focus", "Deep Sleep"
    private String description;    // ex: "Your daily focus hour"
    private String timeRange;      // ex: "2:00 PM - 3:00 PM"
    private String daysActive;     // ex: "Weekdays", "Weekends", "Daily"
    private String category;
    private boolean isPaused = false;// ex: "FOCUS", "SLEEP", "HABIT"

    // Un identificator pentru imaginea/gradientul de fundal (vom folosi asta la UI mai târziu)
    private int imageResourceId;

    // Constructor, Getters și Setters
    public BlockSetup(String title, String description, String timeRange, String daysActive, String category, int imageResourceId) {
        this.title = title;
        this.description = description;
        this.timeRange = timeRange;
        this.daysActive = daysActive;
        this.category = category;
        this.imageResourceId = imageResourceId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public String getDaysActive() { return daysActive; }
    public void setDaysActive(String daysActive) { this.daysActive = daysActive; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getImageResourceId() { return imageResourceId; }
    public void setImageResourceId(int imageResourceId) { this.imageResourceId = imageResourceId; }

    // ADĂUGĂ ASTA UNDEVA LÂNGĂ CELELALTE VARIABILE
    private String blockedApps = "";

    // ADĂUGĂ ASTEA JOS, LÂNGĂ CEILALȚI GETTERI/SETTERI
    public String getBlockedApps() { return blockedApps; }
    public void setBlockedApps(String blockedApps) { this.blockedApps = blockedApps; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }
}
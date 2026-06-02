package com.example.licenta20.ui.home;

public class CardItem {

    private int id = -1;
    private String title;
    private String subtitle;
    private String actionText;
    private int colorResId;

    // Lista de aplicații pentru acest card
    private String blockedApps = "";
    // Starea de pauză (Vacation Mode)
    private boolean isPaused = false;

    public CardItem(String title, String subtitle, String actionText, int colorResId) {
        this.title = title;
        this.subtitle = subtitle;
        this.actionText = actionText;
        this.colorResId = colorResId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getActionText() { return actionText; }
    public int getColorResId() { return colorResId; }

    public String getBlockedApps() { return blockedApps; }
    public void setBlockedApps(String blockedApps) { this.blockedApps = blockedApps; }

    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }
}
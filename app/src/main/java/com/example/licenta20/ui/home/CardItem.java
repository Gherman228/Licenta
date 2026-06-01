package com.example.licenta20.ui.home;

public class CardItem {
    private String title;
    private String subtitle;
    private String actionText;
    private int colorResId; // Vom folosi culori până adaugi imagini în drawable

    public CardItem(String title, String subtitle, String actionText, int colorResId) {
        this.title = title;
        this.subtitle = subtitle;
        this.actionText = actionText;
        this.colorResId = colorResId;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getActionText() { return actionText; }
    public int getColorResId() { return colorResId; }
}
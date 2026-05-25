package com.example.licenta20.ui.home;


import android.graphics.drawable.Drawable;
public class AppInfo {

    //Initializam caracterele
        private String name;
        private String packageName;
        private Drawable icon;
        private boolean isSelected;
        private int interceptCount;


    public AppInfo(String name, String packageName, android.graphics.drawable.Drawable icon) {

        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.isSelected = false;
        this.interceptCount = 0;
}
        //Getter si setter pentru selectie
        public boolean isSelected() {return isSelected;}
        public void setSelected(boolean selected) { isSelected = selected; }


        //Getter existenti
        public String getName() { return name; }
        public String getPackageName() {return packageName;}
        public Drawable getIcon() {return icon;}
    public int getInterceptCount() {
        return interceptCount;
    }

    public void setInterceptCount(int interceptCount) {
        this.interceptCount = interceptCount;
    }
}

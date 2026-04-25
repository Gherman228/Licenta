package com.example.licenta20;


import android.graphics.drawable.Drawable;
public class AppInfo {

    //Initializam caracterele
        private String name;
        private String packageName;
        private Drawable icon;
        private boolean isSelected;


        public AppInfo(String name, String packageName, Drawable icon){

            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
            this.isSelected = false; // Arata ca nici o aplicatie nu-i selectata initial
}
        //Getter si setter pentru selectie
        public boolean isSelected() {return isSelected;}
        public void setSelected(boolean selected) { isSelected = selected; }


        //Getter existenti
        public String getName() { return name; }
        public String getPackageName() {return packageName;}
        public Drawable getIcon() {return icon;}
}

package com.bonovo.bonovohandle;

import android.graphics.drawable.Drawable;

public class AppItem {

    private String pkgName;
    private String title;
    private Drawable icon;
    private boolean isSelected;
    private int position;

    public AppItem(String title, Drawable icon) {
        this.title = title;
        this.icon = icon;
    }

    public AppItem(String pkgName, String title, Drawable icon, boolean isSelected) {
        this.pkgName = pkgName;
        this.title = title;
        this.icon = icon;
        this.isSelected = isSelected;
    }

    public String getPackageName() {
        return pkgName;
    }

    public void setPackageName(String packageName) {
        this.pkgName = pkgName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
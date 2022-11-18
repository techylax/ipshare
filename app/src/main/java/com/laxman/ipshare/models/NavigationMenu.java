package com.laxman.ipshare.models;

public class NavigationMenu {
    private String icon;
    private String title;
    private String path;

    public NavigationMenu(String icon, String title, String path) {
        this.icon = icon;
        this.title = title;
        this.path = path;
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }
}

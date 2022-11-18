package com.laxman.ipshare.models;

import androidx.fragment.app.Fragment;

public class TabItem {
    private Fragment fragment;
    private Integer image;
    private String title;

    public TabItem(Fragment fragment, Integer image, String title) {
        this.fragment = fragment;
        this.image = image;
        this.title = title;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public Integer getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }
}

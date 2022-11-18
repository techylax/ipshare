package com.laxman.ipshare.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.laxman.ipshare.models.TabItem;
import java.util.List;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private List<TabItem> tabItems;

    public PagerAdapter(@NonNull FragmentManager fm, List<TabItem> tabItems) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.tabItems = tabItems;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return tabItems.get(position).getFragment();
    }

    @Override
    public int getCount() {
        return tabItems.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabItems.get(position).getTitle();
    }
}
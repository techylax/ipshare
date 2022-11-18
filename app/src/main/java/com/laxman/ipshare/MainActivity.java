package com.laxman.ipshare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.laxman.ipshare.adapters.PagerAdapter;
import com.laxman.ipshare.models.DataModel;
import com.laxman.ipshare.models.TabItem;
import com.laxman.ipshare.ui.DownloadsFragment;
import com.laxman.ipshare.ui.HomeFragment;
import com.laxman.ipshare.ui.MoreFragments;
import com.laxman.ipshare.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements OnTabSelectedListener {

    private TabLayout tabLayout;
    private List<TabItem> tabItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.initClipboard(this);

        ViewPager viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabLayout);

        tabItems.add(new TabItem(new HomeFragment(), R.drawable.web, "Home"));
        tabItems.add(new TabItem(new DownloadsFragment(), R.drawable.downloads, "Downloads"));
        tabItems.add(new TabItem(new MoreFragments(), R.drawable.more, "More"));

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabItems);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);

        for (int i=0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                setTab(tab, tabItems.get(i).getTitle(), tabItems.get(i).getImage(),i==0);
            }
        }

        final WebServer webServer = new WebServer(this, 8080);

        DataModel dataModel = new ViewModelProvider(this).get(DataModel.class);

        Observer<Boolean> webServerObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean start) {
                if (start) {
                    try {
                        webServer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toasty.info(getApplicationContext(), "Port not available", Toasty.LENGTH_SHORT).show();
                    }
                } else {
                    webServer.stop();
                }
            }
        };

        dataModel.getWebServer().observe(this, webServerObserver);
    }

    private void setTab(TabLayout.Tab tab, String title, int image, boolean selected) {
        if (!selected) {
            tab.setCustomView(null);
            tab.setText(null);
            tab.setIcon(image);
            return;
        }

        View view = View.inflate(this, R.layout.tab_background, null);
        TextView textView = view.findViewById(R.id.tab_title);
        ImageView imageView = view.findViewById(R.id.tab_icon);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.poppins_medium);
        textView.setTypeface(typeface);
        tab.setCustomView(view);
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        textView.setText(title);
        imageView.setImageResource(image);
        textView.startAnimation(animation);
        imageView.startAnimation(animation);

    }

    @Override
    public void onTabSelected(TabLayout.Tab selectedTab) {
        for (int i=0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                setTab(tab, tabItems.get(i).getTitle(), tabItems.get(i).getImage(), selectedTab == tab);
            }
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
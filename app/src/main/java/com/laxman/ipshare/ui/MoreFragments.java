package com.laxman.ipshare.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;
import com.laxman.ipshare.App;
import com.laxman.ipshare.BuildConfig;
import com.laxman.ipshare.R;

public class MoreFragments extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_more, container, false);
        NavigationView navigationView = view.findViewById(R.id.more_menu);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (getActivity() == null) {
                    return false;
                }
                int id = item.getItemId();
                switch (id) {
                    case R.id.rate:
                        App.openGooglePlay(getActivity(), getActivity().getPackageName());
                        break;
                    case R.id.feedback:
                        App.emailIntent(getActivity(), "coderlaxman@gmail.com", "Feedback about "
                                + getString(R.string.app_name), "", "Email");
                        break;
                    case R.id.share:
                        App.shareApp(getActivity(), "Share " + getString(R.string.app_name),
                                "Download " + getString(R.string.app_name) + " to share files instantly " +
                                App.getAppUrl(getActivity().getPackageName()));
                        break;
                }
                return false;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        TextView versionName = headerView.findViewById(R.id.version_name);
        String buildName = " " + BuildConfig.VERSION_NAME;
        versionName.setText(buildName);
        return view;
    }
}

package com.laxman.ipshare.ui;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.laxman.ipshare.models.DataModel;
import com.laxman.ipshare.App;
import com.laxman.ipshare.R;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class HomeFragment extends Fragment {

    private List<String> ips = new ArrayList<>();
    private ToggleButton toggleServer;
    private DataModel dataModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_home, container, false);

        if (getActivity() == null) {
            return view;
        }

        ips.clear();
        ips.addAll(App.getIpAddress(8080));

        if (ips.size() == 0) {
            ips.add("http://127.0.0.1:8080");
        }

        Spinner urls = view.findViewById(R.id.urls);
        final ArrayAdapter arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, ips);
        urls.setAdapter(arrayAdapter);

        urls.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (getContext() != null) {
                    App.copyToClipboard(getContext(), ips.get(position));
                }
                return false;
            }
        });

        dataModel = new ViewModelProvider(getActivity()).get(DataModel.class);

        toggleServer = view.findViewById(R.id.toggle_server);
        toggleServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ips.clear();
                ips.addAll(App.getIpAddress(8080));
                if (ips.size() == 0) {
                    ips.add("http://127.0.0.1:8080");
                }
                arrayAdapter.notifyDataSetChanged();
                if (!App.checkStoragePermission(getActivity())) {
                    requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 0);
                    toggleServer.setChecked(false);
                } else {
                    dataModel.getWebServer().setValue(toggleServer.isChecked());
                }
            }
        });
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (getActivity() == null) {
            return;
        }
        if (App.checkStoragePermission(getActivity())) {
            toggleServer.setChecked(true);
            dataModel.getWebServer().setValue(toggleServer.isChecked());
        } else {
            Toasty.info(getActivity(), "Permission denied!").show();
        }
    }
}

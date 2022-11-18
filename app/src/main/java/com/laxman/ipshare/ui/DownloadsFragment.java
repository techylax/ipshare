package com.laxman.ipshare.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laxman.ipshare.adapters.DownloadsListAdapter;
import com.laxman.ipshare.models.DataModel;
import com.laxman.ipshare.models.FileItem;
import com.laxman.ipshare.ActionEvent;
import com.laxman.ipshare.App;
import com.laxman.ipshare.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class DownloadsFragment extends Fragment {
    private ConstraintLayout constraintLayout;
    private List<FileItem> fileItems = new ArrayList<>();
    private DownloadsListAdapter listAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloads, container, false);

        if (getActivity() == null) {
            return view;
        }

        constraintLayout = view.findViewById(R.id.list_container);

        RecyclerView recyclerView = view.findViewById(R.id.downloads_item);
        listAdapter = new DownloadsListAdapter(getActivity(), fileItems, new ActionEvent() {
            @Override
            public void openFile(String path) {
                if (getActivity() == null) {
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(path);
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        getActivity().getApplicationContext().getPackageName() + ".provider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setData(photoURI);
                startActivity(intent);
            }

            @Override
            public void clearFile(String path) {
                deleteFile(path);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(listAdapter);

        DataModel dataModel = new ViewModelProvider(getActivity()).get(DataModel.class);

        Observer<Boolean> listObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    listFiles();
                }
            }
        };

        dataModel.getDataChanged().observe(getActivity(), listObserver);

        listFiles();
        return view;
    }

    private void listFiles() {
        fileItems.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getContext() == null | getActivity() == null) {
                    return;
                }
                File fileDir = App.getAppDir(getContext());
                if (!fileDir.exists()) {
                    return;
                }

                File[] files = fileDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.isDirectory()) {
                            fileItems.add(new FileItem(file.getName(), file.getPath()));
                        }
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.notifyDataSetChanged();
                        if (fileItems.size() == 0) {
                            constraintLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.empty));
                        } else {
                            constraintLayout.setBackground(null);
                        }
                    }
                });
            }
        }).start();
    }

    private void deleteFile(String path) {
        final File file = new File(path);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.warning);
        builder.setTitle("Delete " + file.getName() + "?");
        builder.setMessage("This is permanent and cannot be undone.");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() == null) {
                            return;
                        }
                        final boolean bool = file.delete();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bool) {
                                    Toasty.success(getActivity(), "File deleted").show();
                                } else {
                                    Toasty.info(getActivity(), "Failed to delete file").show();
                                }
                                listFiles();
                                listAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();
            }
        });

        builder.show();
    }
}

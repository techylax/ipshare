package com.laxman.ipshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.laxman.ipshare.ActionEvent;
import com.laxman.ipshare.R;
import com.laxman.ipshare.models.FileItem;
import java.util.List;

public class DownloadsListAdapter extends RecyclerView.Adapter<DownloadsListAdapter.ItemViewHolder> {

    private Context context;
    private List<FileItem> fileItems;
    private ActionEvent actionEvent;
    public DownloadsListAdapter(@NonNull Context context, List<FileItem> fileItems, ActionEvent actionEvent) {
        this.context = context;
        this.fileItems = fileItems;
        this.actionEvent = actionEvent;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        final FileItem fileItem = fileItems.get(position);
        if (fileItem.getFileName() != null & fileItem.getFilePath() != null) {
            holder.fileName.setText(fileItem.getFileName());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionEvent.openFile(fileItem.getFilePath());
            }
        });

        holder.clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionEvent.clearFile(fileItem.getFilePath());
            }
        });

    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView fileName;
        ImageButton clear;
        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            fileName = itemView.findViewById(R.id.file_name);
            clear = itemView.findViewById(R.id.clear);
        }
    }
}

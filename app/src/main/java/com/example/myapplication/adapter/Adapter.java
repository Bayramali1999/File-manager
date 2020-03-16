package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.FileModel;
import com.example.myapplication.listener.FileItemClickListener;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<FileVH> {
    private List<FileModel> list;
    private boolean showExtension;
    private FileItemClickListener listener;
    private SelectionTracker<FileModel> selectionTracker;

    public Adapter(List<FileModel> files, boolean b,
                   FileItemClickListener clickListener) {
        this.list = files;
        this.showExtension = b;
        this.listener = clickListener;
    }

    @NonNull
    @Override
    public FileVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new FileVH(view, false, listener, showExtension);
    }

    @Override
    public void onBindViewHolder(@NonNull FileVH holder, int position) {
        FileModel file = list.get(position);
        boolean isSelected = selectionTracker.isSelected(file);
        holder.onBind(file, isSelected, position);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setSelectionTracker(SelectionTracker<FileModel> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }
}

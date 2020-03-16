package com.example.myapplication.tracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

import com.example.myapplication.data.FileModel;

import java.util.List;

public class MyFileKeyProvider extends ItemKeyProvider<FileModel> {
    private final List<FileModel> list;

    public MyFileKeyProvider(int scope, List<FileModel> list) {
        super(scope);
        this.list = list;
    }

    @Nullable
    @Override
    public FileModel getKey(int position) {
        return list.get(position);
    }

    @Override
    public int getPosition(@NonNull FileModel key) {
        return list.indexOf(key);
    }
}

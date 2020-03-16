package com.example.myapplication.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.FileModel;
import com.example.myapplication.listener.FileItemClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileVH extends RecyclerView.ViewHolder {
    private TextView tvName;
    private TextView tvSpace;
    private TextView tvCreatedDate;
    private ImageView fileIc;
    private boolean showExtension;
    private View v;
    private TextView tvExtension;
    private FileModel file;
    private View mainView;
    private FileItemClickListener listener;

    void onBind(FileModel file,
                boolean selected, int position) {
        this.file = file;
        if (position == 0) {
            itemView.setActivated(false);
            v.setVisibility(View.GONE);
            fileIc.setVisibility(View.VISIBLE);
            fileIc.setImageResource(R.drawable.ic_storage);
            tvName.setText(file.getName());
            tvName.setPadding(0, 16, 0, 0);
            tvSpace.setVisibility(View.GONE);
            tvCreatedDate.setVisibility(View.GONE);
            mainView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.fileIsClicked(null);
                        }
                    }
            );
        } else {
            String name = file.getName();
            if (showExtension) {
                tvName.setText(name);
            } else {
                setNameToItem(name);
            }
            setFileSpace(file);
            setFileCreatedDate(file);
            int drawable = setFileImage(name);

            if (drawable == 0) {
                fileIc.setVisibility(View.GONE);
                v.setVisibility(View.VISIBLE);
            } else {
                fileIc.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
                fileIc.setImageResource(drawable);
            }

            mainView.setActivated(selected);
            mainView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<FileModel> fileModel = new ArrayList<>();
                            fileModel.add(file);
                            listener.fileIsClicked(fileModel);
                        }
                    }
            );
        }
    }

    FileVH(@NonNull View itemView, boolean showExtension
            , FileItemClickListener listener,
           boolean extension) {
        super(itemView);
        //todo listener
        this.showExtension = extension;
        this.listener = listener;
        this.mainView = itemView;
        this.tvName = itemView.findViewById(R.id.tv_file_name);
        this.tvSpace = itemView.findViewById(R.id.tv_file_size);
        this.tvCreatedDate = itemView.findViewById(R.id.tv_file_date);
        this.fileIc = itemView.findViewById(R.id.iv_file);
        this.showExtension = showExtension;
        this.v = itemView.findViewById(R.id.lv_another);
        this.tvExtension = itemView.findViewById(R.id.tv_extension);
    }

    private int setFileImage(String name) {
        int dot = name.lastIndexOf(".");
        String fExt = name.substring(dot + 1);
        if (dot == -1) {
            fExt = "";
        }
        switch (fExt) {
            case "doc":
            case "docx":
                return R.drawable.ic_doc_mcs;
            case "ppt":
            case "pptx":
                return R.drawable.ic_ppt_mcs;
            case "xls":
            case "xlsx":
                return R.drawable.ic_xls_mcs;
            case "pdf":
                return R.drawable.ic_pdf_mcs;
            default: {
                if (fExt.length() > 3) {
                    fExt = fExt.substring(0, 3);
                }
                tvExtension.setText(fExt);
                return 0;
            }
        }
    }

    private void setFileCreatedDate(FileModel file) {
        SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyy");
        Date date = new Date(file.getLastModified());
        String modifiedDate = sd.format(date);
        tvCreatedDate.setText(modifiedDate);
    }

    private void setFileSpace(FileModel file) {
        long space = Long.parseLong(String.valueOf(file.getLength() / 1024));
        if (space >= 1024) {
            long mbSpace = space / 1024;
            tvSpace.setText(mbSpace + " MB");
        } else {
            tvSpace.setText(space + " KB");
        }
    }

    public ItemDetailsLookup.ItemDetails getItemDetails() {
        return new ItemDetailsLookup.ItemDetails<FileModel>() {
            @Override
            public int getPosition() {
                return getAdapterPosition();
            }

            @Nullable
            @Override
            public FileModel getSelectionKey() {
                return file;
            }
        };
    }

    private void setNameToItem(String nameToItem) {
        int dot = nameToItem.lastIndexOf(".");
        if (dot > -1) {
            tvName.setText(nameToItem.substring(0, dot));
        } else {
            tvName.setText(nameToItem);
        }
    }
}
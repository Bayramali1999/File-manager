package com.example.myapplication.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapter.Adapter;
import com.example.myapplication.data.FileModel;
import com.example.myapplication.listener.FileItemClickListener;
import com.example.myapplication.listener.NavigationUpClickListener;
import com.example.myapplication.tracker.MyFileKeyProvider;
import com.example.myapplication.tracker.MyFileLookup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileFragment
        extends Fragment
        implements ActionMode.Callback
        , NavigationUpClickListener {

    private static final int READ_REQ_CODE = 313;
    private static final int REQ_CODE_RESULT = 303;
    private RecyclerView rv;
    private Adapter fileAdapter;
    private List<FileModel> fileList;
    private FileItemClickListener clickListener;
    private SelectionTracker<FileModel> selectionTracker;
    private ActionMode actionMode = null;
    private List<FileModel> sendFiles = new ArrayList<>();
    private List<FileModel> originList = new ArrayList<>();
    private List<FileModel> list;
    private SearchView sv = null;
    private FileModel header = new FileModel("Browse other docs");

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_select_file, container, false);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_sort);
        MainActivity activity = (MainActivity) getActivity();
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(drawable);
        setUpListener();
        list = new ArrayList<>();
        fileList = new ArrayList<>();
        fileAdapter = new Adapter(list, false, clickListener);
        rv = view.findViewById(R.id.rv_list);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setArrowButtonOnToolbar();
        setHasOptionsMenu(true);
        rv.setAdapter(fileAdapter);
        givePermission();
        setUpSelectionTracker();
        fileAdapter.setSelectionTracker(selectionTracker);
        trackerAddObserver();
        setSearchView();
    }


    private void setUpListener() {
        clickListener = new FileItemClickListener() {
            @Override
            public void fileIsClicked(List<FileModel> file) {
                if (file == null) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent = Intent.createChooser(intent, "");
                    startActivityForResult(intent, REQ_CODE_RESULT);
                } else {
                    if (!selectionTracker.hasSelection()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendSelectedItems(file);
                            }
                        });
                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                        builder.setTitle("Send '" + file.get(0).getName() + "' to ... ?");
                        builder.create();
                        builder.show();
                    }
                }
            }
        };
    }

    private void sendSelectedItems(List<FileModel> file) {
        // todo SendItems
        for (FileModel fileModel : file) {
//            Log.e("tag", fileModel.getName());
        }

    }

    private void setSearchView() {
        MainActivity activity = (MainActivity) getActivity();
        if (sv != null) {
            if (activity.onSupportNavigateUp()) {
                sv.setIconified(true);
            }
        }
    }

    private void trackerAddObserver() {
        selectionTracker.addObserver(
                new SelectionTracker.SelectionObserver<FileModel>() {
                    @Override
                    public void onSelectionChanged() {
                        super.onSelectionChanged();
                        if ((!sendFiles.isEmpty()) && actionMode == null) {
                            MainActivity activity = (MainActivity) getActivity();
                            actionMode = activity.startSupportActionMode(FileFragment.this);
                        }
                        if (sendFiles.isEmpty() && actionMode != null) {
                            actionMode.finish();
                            actionMode = null;
                        }
                        if (!sendFiles.isEmpty() && actionMode != null) {
                            actionMode.setTitle(sendFiles.size() + " selected");
                        }
                    }

                    @Override
                    public void onItemStateChanged(@NonNull FileModel key, boolean selected) {
                        super.onItemStateChanged(key, selected);
                        if (selected) {
                            if (!key.equals(header)) {
                                sendFiles.add(key);
                            } else {
                                if (sendFiles.size() > 0)
                                    clickListener.fileIsClicked(null);
                            }
                        } else {
                            if (!key.equals(header)) {
                                sendFiles.remove(key);
                            } else {
                                if (sendFiles.size() > 0)
                                    clickListener.fileIsClicked(null);
                            }
                        }
                    }
                }
        );
    }

    private void setUpSelectionTracker() {
        selectionTracker = new SelectionTracker.Builder<FileModel>(
                "my-selection-id",
                rv,
                new MyFileKeyProvider(1, list),
                new MyFileLookup(rv),
                StorageStrategy.createParcelableStorage(FileModel.class)
        ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
        ).build();
    }

    private void setArrowButtonOnToolbar() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void givePermission() {
        String[] permissions = new String[1];
        permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission
                    (getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                readDataFromStorage();
            } else {
                requestPermissions(permissions, READ_REQ_CODE);
            }
        } else {
            readDataFromStorage();
        }
    }

    private void readDataFromStorage() {
        final List<FileModel> myFiles = new ArrayList<>();
        String download = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOWNLOADS;
        String document = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOCUMENTS;
        String root = Environment.getExternalStorageDirectory().toString();
        File fDown = new File(download);
        File fDoc = new File(document);
        File fRoot = new File(root);

        myFiles.addAll(readFileList(fDown));
        myFiles.addAll(myFiles.size(), readFileList(fDoc));
        myFiles.addAll(myFiles.size(), readFileList(fRoot));

        fileList.clear();
        originList.clear();
        fileList.addAll(myFiles);
        originList.addAll(myFiles);
        filesSortByDate();
    }

    private List<FileModel> readFileList(File fDown) {
        List<FileModel> myFiles = new ArrayList<>();
        if (fDown.exists()) {
            File[] files = fDown.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    myFiles.add(new FileModel(
                            file.getName(),
                            file.getPath(),
                            file.lastModified(),
                            file.length()));
                }
            }
        }
        return myFiles;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.sort_by_date);
        item.setChecked(true);
        MenuItem itemSearch = menu.findItem(R.id.search_text);
        sv = (SearchView) itemSearch.getActionView();
        sv.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchFileByName(sv);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_REQ_CODE && grantResults.length != 0) {
            readDataFromStorage();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search_text) {
            return true;
        } else if (id == R.id.sort_by_name) {
            filesSortByName();
            item.setChecked(true);
            return true;
        } else if (id == R.id.sort_by_date) {
            filesSortByDate();
            item.setChecked(true);
            return true;
        }

        return false;
    }

    private void filesSortByDate() {
        Collections.sort(fileList, new Comparator<FileModel>() {
            @Override
            public int compare(FileModel o1, FileModel o2) {
                return (String.valueOf(o2.getLastModified()))
                        .compareTo(String.valueOf(o1.getLastModified()));
            }
        });
        list.clear();
        list.add(header);
        list.addAll(fileList);
        fileAdapter.notifyDataSetChanged();
    }

    private void filesSortByName() {
        Collections.sort(fileList, new Comparator<FileModel>() {
            @Override
            public int compare(FileModel o1, FileModel o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        list.clear();
        list.add(header);
        list.addAll(fileList);
        fileAdapter.notifyDataSetChanged();
    }

    private void searchFileByName(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fileList.clear();
                list.clear();
                list.add(header);
                if (newText.isEmpty()) {
                    fileList.addAll(originList);
                    list.addAll(fileList);
                    fileAdapter.notifyDataSetChanged();
                } else {
                    for (FileModel file : originList) {
                        String name = file.getName().toLowerCase();
                        String text = newText.toLowerCase();
                        if (name.contains(text)) {
                            fileList.add(file);
                        }
                    }
                    list.addAll(fileList);
                    fileAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        sendSelectedItems(sendFiles);
        actionMode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        selectionTracker.clearSelection();
    }

    @Override
    public void navClicked() {
        sv.clearFocus();
        sv.setIconified(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_RESULT
                && Activity.RESULT_OK == resultCode
                && null != data) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                int count = clipData.getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = clipData.getItemAt(i).getUri();

                }
            }
        }
    }
}

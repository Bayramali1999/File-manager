package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.fragments.FileFragment;
import com.example.myapplication.listener.NavigationUpClickListener;

public class MainActivity extends AppCompatActivity {
    private static final int READ_REQ_CODE = 313;
    FileFragment fileFragment;
    private NavigationUpClickListener listener = null;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fileFragment = new FileFragment();
        listener = (NavigationUpClickListener) fileFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root, fileFragment)
                .commit();

    }

    @Override
    public boolean onSupportNavigateUp() {
        if (listener != null) {
            listener.navClicked();
        }
        return true;
    }
}

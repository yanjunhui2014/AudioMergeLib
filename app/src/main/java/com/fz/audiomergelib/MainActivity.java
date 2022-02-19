package com.fz.audiomergelib;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.fz.audiomergelib.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }

    @Override
    public void onClick(View v) {
        if (v == binding.btnAndroid) {
            startActivity(AndroidMergeActivity.createIntent(this));
        } else if (v == binding.btnFfmpeg) {

        }
    }

}
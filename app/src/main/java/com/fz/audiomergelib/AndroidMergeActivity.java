package com.fz.audiomergelib;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fz.audiomergelib.databinding.ActivityAndroidMergeBinding;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
public class AndroidMergeActivity extends AppCompatActivity {

    ActivityAndroidMergeBinding binding;

    public static Intent createIntent(Context context) {
        return new Intent(context, AndroidMergeActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAndroidMergeBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }

}

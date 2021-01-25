package com.tesseractmobile.pocketbot.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 9/14/16.
 */
public class SettingsActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }
}

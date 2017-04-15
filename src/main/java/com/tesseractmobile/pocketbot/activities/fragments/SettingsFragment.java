package com.tesseractmobile.pocketbot.activities.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

/**
 * Created by josh on 10/24/2015.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        findPreference("privacy").setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals("privacy")){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) getText(R.string.privacy_url)));
            startActivity(browserIntent);
            return true;
        }
        return false;
    }
}

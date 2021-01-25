package com.tesseractmobile.pocketbot.activities.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

/**
 * Created by josh on 10/18/2015.
 */
public class TextPreviewFragment extends CallbackFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private ListView mTextListView;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.text_preview, container, false);
        mTextListView = (ListView) view.findViewById(R.id.textList);
        updateViewVisibility(PocketBotSettings.isShowTextPreview(getActivity()));
        return view;
    }

    public ListView getListView(){
        return mTextListView;
    }

    @Override
    public void onStart() {
        super.onStart();
        PocketBotSettings.registerOnSharedPreferenceChangeListener(getActivity(), this);
    }

    @Override
    public void onStop() {
        super.onStop();
        PocketBotSettings.registerOnSharedPreferenceChangeListener(getActivity(), this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(PocketBotSettings.KEY_SHOW_TEXT_PREVIEW.equals(key)){
            final boolean showText = sharedPreferences.getBoolean(key, PocketBotSettings.DEFAULT_SHOW_TEXT_PREVIEW);
            updateViewVisibility(showText);
        }
    }

    private void updateViewVisibility(boolean showText) {
        if(showText){
            mTextListView.setVisibility(View.VISIBLE);
        } else {
            mTextListView.setVisibility(View.GONE);
        }
    }
}

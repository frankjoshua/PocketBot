package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.Robot;

/**
 * Created by josh on 11/28/2015.
 */
public class EmotionsFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.emotions_selector, null);
        listView = (ListView) view.findViewById(R.id.lvEmotions);
        //Add all emotions to the list view
        listView.setAdapter(new ArrayAdapter<Emotion>(getActivity(), android.R.layout.simple_list_item_1, Emotion.values()));
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //Set the robots emotion
        Emotion emotion = (Emotion) listView.getItemAtPosition(i);
        Robot.get().setEmotion(emotion);
        dismiss();
    }
}

package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.faces.EfimFace;

/**
 * Created by josh on 7/14/16.
 */
public class ScratchyFaceFragment extends EfimFaceFragment {
    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.scratchy_face, null);
        setFace(new EfimFace(view));
        return view;
    }

}

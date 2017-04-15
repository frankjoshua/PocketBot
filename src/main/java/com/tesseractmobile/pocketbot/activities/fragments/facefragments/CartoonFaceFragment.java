package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.faces.CartoonFace;

/**
 * Created by josh on 1/26/2016.
 */
public class CartoonFaceFragment extends EfimFaceFragment {

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.cartoon_face, null);
        setFace(new CartoonFace(view));
        return view;
    }

}

package com.tesseractmobile.pocketbot.activities.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 4/18/17.
 */

public class WizardFaceType extends WizardFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.wizard_face_type, container, false);
        view.findViewById(R.id.btnFace).setOnClickListener(this);
        view.findViewById(R.id.btnTelepresence).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnFace){
            getConfigWizard().setShowFace(true);
            done();
        }
        if(v.getId() == R.id.btnTelepresence){
            getConfigWizard().setUseTelepresence(true);
            done();
        }
    }
}

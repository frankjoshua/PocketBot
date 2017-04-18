package com.tesseractmobile.pocketbot.activities.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 4/17/17.
 */

public class WizardStepRos extends WizardFragment implements View.OnClickListener {

    private EditText editTextMasterUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.wizard_step_ros, container, false);
        view.findViewById(R.id.btnOk).setOnClickListener(this);
        editTextMasterUri = (EditText) view.findViewById(R.id.edMasterUri);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnOk){
            getConfigWizard().setRosMasterUri(editTextMasterUri.getText().toString());
            done();
        }
    }
}

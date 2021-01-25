package com.tesseractmobile.pocketbot.activities.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 4/16/17.
 */
public class WizardRemoteControl extends WizardFragment implements View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.wizard_step_remote, container, false);
        view.findViewById(R.id.btnRos).setOnClickListener(this);
        view.findViewById(R.id.btnRemote).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnRos){
            nextFragment(new WizardStepRos());
        }
        if(v.getId() == R.id.btnRemote){
            getConfigWizard().setOnRobot(false);
            done();
        }
    }
}

package com.tesseractmobile.pocketbot.activities.wizard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 4/16/17.
 */

public class WizardOnRobot extends WizardFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.wizard_step_on_robot, container, false);
        view.findViewById(R.id.btnWifi).setOnClickListener(this);
        view.findViewById(R.id.btnArduino).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnWifi) {
            getConfigWizard().setWifi(true);
            nextFragment(new WizardStepRos());
        }
        if(v.getId() == R.id.btnArduino){
            getConfigWizard().setUsb(true);
            done();
        }
    }
}

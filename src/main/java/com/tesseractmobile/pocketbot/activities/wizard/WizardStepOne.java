package com.tesseractmobile.pocketbot.activities.wizard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 4/15/17.
 */

public class WizardStepOne extends WizardFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.wizard_step_one, container, false);
        view.findViewById(R.id.btnOnRobot).setOnClickListener(this);
        view.findViewById(R.id.btnRemoteControl).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        final ConfigWizard configWizard = getConfigWizard();
        if(configWizard == null){
            //Can be null if exited Activity
            return;
        }
        if(v.getId() == R.id.btnOnRobot){
            configWizard.setOnRobot(true);
            nextFragment(new WizardOnRobot());
        } else if(v.getId() == R.id.btnRemoteControl){
            configWizard.setOnRobot(false);
            nextFragment(new WizardRemoteControl());
        }
    }
}

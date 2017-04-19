package com.tesseractmobile.pocketbot.activities.wizard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.kofigyan.stateprogressbar.StateProgressBar;
import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 4/15/17.
 */

public class WizardActivity extends FragmentActivity implements View.OnClickListener, ConfigWizard {

    private static final String STEP_ONE_FRAGMENT = "step_one_fragment";
    final ConfigWizard configWizard = new BaseConfigWizard();
    private StateProgressBar stateProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard);
        findViewById(R.id.btnSkip).setOnClickListener(this);
        stateProgressBar = (StateProgressBar) findViewById(R.id.your_state_progress_bar_id);
        stateProgressBar.setStateDescriptionData(new String[]{"DEVICE","CONTROL","ROS","4"});
        stateProgressBar.setMaxStateNumber(StateProgressBar.StateNumber.THREE);
        getFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, new WizardStepOne(), STEP_ONE_FRAGMENT).commit();
    }

    public static Intent getLaunchIntent(final Context context) {
        return new Intent(context, WizardActivity.class);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSkip){
            finish();
        }
    }

    @Override
    public void setOnRobot(boolean b) {
        configWizard.setOnRobot(b);
    }

    @Override
    public void setWifi(boolean b) {
        configWizard.setWifi(b);
    }

    @Override
    public void setUsb(boolean b) {
        configWizard.setUsb(b);
    }

    @Override
    public void applyConfig(Context context) {
        configWizard.applyConfig(context);
    }

    @Override
    public void setRosMasterUri(String s) {
        configWizard.setRosMasterUri(s);
    }

    @Override
    public void setShowFace(boolean b) {
        configWizard.setShowFace(b);
    }

    @Override
    public void setUseTelepresence(boolean b) {
        configWizard.setUseTelepresence(b);
    }

    @Override
    public void nextStep() {
        configWizard.nextStep();
        switch (stateProgressBar.getCurrentStateNumber()){
            case 0:
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                break;
            case 1:
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                break;
            case 2:
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                break;
            case 3:
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                break;
        }
    }
}

package com.tesseractmobile.pocketbot.activities.wizard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 4/15/17.
 */

public class WizardActivity extends FragmentActivity implements View.OnClickListener, ConfigWizard {

    private static final String STEP_ONE_FRAGMENT = "step_one_fragment";
    final ConfigWizard configWizard = new BaseConfigWizard();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard);
        findViewById(R.id.btnSkip).setOnClickListener(this);

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
}

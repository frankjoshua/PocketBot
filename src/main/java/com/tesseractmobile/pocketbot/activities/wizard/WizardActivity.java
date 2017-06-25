package com.tesseractmobile.pocketbot.activities.wizard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.kofigyan.stateprogressbar.StateProgressBar;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.LauncherActivity;

/**
 * Created by josh on 4/15/17.
 */

public class WizardActivity extends FragmentActivity implements View.OnClickListener, ConfigWizard {

    private static final String STEP_ONE_FRAGMENT = "step_one_fragment";
    final ConfigWizard configWizard = new BaseConfigWizard();
    private StateProgressBar stateProgressBar;
    private View loadingBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard);
        findViewById(R.id.btnSkip).setOnClickListener(this);
        loadingBar = findViewById(R.id.pbLoading);
        stateProgressBar = (StateProgressBar) findViewById(R.id.progressBar);
        stateProgressBar.setStateDescriptionData(new String[]{"DEVICE","CONTROL","ROS","FACE"});
        stateProgressBar.setMaxStateNumber(StateProgressBar.StateNumber.FOUR);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, new WizardStepOne(), STEP_ONE_FRAGMENT).commit();
        checkForPermissions();
    }

    /**
     * Check for Android M permissions
     */
    private void checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String[] permissionList = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            for(final String permission : permissionList){
                if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(permissionList, 0);
                }
            }
        }
    }

    public static Intent getLaunchIntent(final Context context) {
        return new Intent(context, WizardActivity.class);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSkip){
            done();
        }
    }

    public void done() {
        loadingBar.setVisibility(View.VISIBLE);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.wizard);
        disableLayouts(layout);
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                applyConfig(getApplicationContext());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //Restart the app by launching the Launcher Activity
                startActivity(LauncherActivity.getLaunchIntent(getApplicationContext()));
                finish();
            }
        }.execute();
    }

    private void disableLayouts(ViewGroup layout) {
        if(layout.getId() == R.id.pbLoading){
            return;
        }
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if(child instanceof ViewGroup){
                disableLayouts((ViewGroup) child);
            }
            child.setEnabled(false);
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
    public boolean isOnRobot() {
        return configWizard.isOnRobot();
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

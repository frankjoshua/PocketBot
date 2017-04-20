package com.tesseractmobile.pocketbot.activities.wizard;

import android.content.Context;

import com.tesseractmobile.pocketbot.activities.LauncherActivity;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceFragmentFactory;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceInfo;

/**
 * Created by josh on 4/17/17.
 */

public class BaseConfigWizard implements ConfigWizard {
    private boolean mOnRobot;
    private boolean mWifi;
    private boolean mUsb;
    private String mRosMasterUri = "http://localhost:11311";
    private boolean mShowFace;
    private boolean mUseTelepresence;

    @Override
    public void setOnRobot(boolean b) {
        mOnRobot = b;
    }

    @Override
    public void setWifi(boolean b) {
        mWifi = b;
    }

    @Override
    public void setUsb(boolean b) {
        mUsb = b;
    }

    @Override
    public void applyConfig(final Context context) {
        if(mRosMasterUri != null){
            PocketBotSettings.setRosMasterUri(context, mRosMasterUri);
        }
        //Set correct starting activity
        PocketBotSettings.setStartingActvityId(context, getStartingActivityId());
        //Set best starting face
        PocketBotSettings.setSelectedFace(context, getSelectedFace());
    }

    /**
     * @return face id based on settings
     */
    private int getSelectedFace() {
        if(mShowFace){
            return FaceFragmentFactory.ID_FACE_EFIM;
        }
        if(mUseTelepresence){
            return FaceFragmentFactory.ID_FACE_TELEPRESENCE;
        }
        return FaceFragmentFactory.ID_FACE_CONTROL;
    }

    /**
     *
     * @return correct starting activity based on settings
     */
    private int getStartingActivityId() {
        if(mUsb){
            return LauncherActivity.USB_ACTIVITY;
        }
        return LauncherActivity.ROS_ACTIVITY;
    }

    @Override
    public void setRosMasterUri(final String s) {
        mRosMasterUri = s;
    }

    @Override
    public void setShowFace(boolean b) {
        mShowFace = b;
    }

    @Override
    public void setUseTelepresence(boolean b) {
        mUseTelepresence = b;
    }

    @Override
    public void nextStep() {

    }
}

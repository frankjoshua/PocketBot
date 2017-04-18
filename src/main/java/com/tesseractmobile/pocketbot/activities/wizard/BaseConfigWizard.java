package com.tesseractmobile.pocketbot.activities.wizard;

import android.content.Context;

import com.tesseractmobile.pocketbot.activities.LauncherActivity;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

/**
 * Created by josh on 4/17/17.
 */

public class BaseConfigWizard implements ConfigWizard {
    private boolean mOnRobot;
    private boolean mWifi;
    private boolean mUsb;
    private String mRosMasterUri;

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
        PocketBotSettings.setRosMasterUri(context, mRosMasterUri);
        PocketBotSettings.setStartingActvityId(context, getStartingActivityId());
    }

    private int getStartingActivityId() {
        return LauncherActivity.ROS_ACTIVITY;
    }

    @Override
    public void setRosMasterUri(final String s) {
        mRosMasterUri = s;
    }
}

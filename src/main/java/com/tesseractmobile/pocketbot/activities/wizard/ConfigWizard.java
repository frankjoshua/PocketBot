package com.tesseractmobile.pocketbot.activities.wizard;

import android.content.Context;

/**
 * Created by josh on 4/17/17.
 */

interface ConfigWizard {
    void setOnRobot(boolean b);

    void setWifi(boolean b);

    void setUsb(boolean b);

    void applyConfig(Context context);

    void setRosMasterUri(String s);

    void setShowFace(boolean b);

    void setUseTelepresence(boolean b);

    void nextStep();

    boolean isOnRobot();
}

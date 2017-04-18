package com.tesseractmobile.pocketbot.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tesseractmobile.pocketbot.activities.wizard.WizardActivity;

/**
 * This is them main entry point for the PocketBot app
 * It just redirects to the correct start activity
 */
public class LauncherActivity extends Activity {

    //Activity Ids
    final public static int ACTIVITY_WIZARD = 0;
    final public static int USB_ACTIVITY = 1;
    final public static int ROS_ACTIVITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get start activity
        final int startingActivityId = PocketBotSettings.getStartingActivityId(this);
        startActivityById(this, startingActivityId);
        finish();
    }

    /**
     * Starts an activity based on id
     * @param context
     * @param startingActivityId
     */
    final static public void startActivityById(final Context context, final int startingActivityId) {
        switch (startingActivityId){
            case ACTIVITY_WIZARD:
                context.startActivity(WizardActivity.getLaunchIntent(context));
                break;
            case ROS_ACTIVITY:
                context.startActivity(AiFragmentActivity.getLaunchIntent(context));
                break;
            default:
                throw new IllegalArgumentException("No activity found for " + startingActivityId);
        }
    }

    public static Intent getLaunchIntent(final Context context) {
        return new Intent(context, LauncherActivity.class);
    }
}

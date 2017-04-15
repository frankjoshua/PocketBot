package com.tesseractmobile.tag;

import android.os.SystemClock;

import java.util.Random;

/**
 * Created by josh on 9/28/2015.
 */
public class TagGame {
    /* This is the state after IT and tagging a Not It */
    final static public int SAFE = 2;
    /** State after being tagged */
    final static public int IT = 1;
    /** State after detecting an IT */
    final static public int NOT_IT = 0;

    public static final int FREEZE_TIME = 5000;

    //Need a way to insure unique id
    final private int ID = new Random(SystemClock.uptimeMillis()).nextInt(65500 - 2) + 2;
    //Time since last state change
    private long mLastChange;
    private float mTagRange = 1.5f;
    //Start in bad state
    private int mState = Integer.MIN_VALUE;

    private final OnTagGameUpdateListener onTagGameUpdateListener;

    public TagGame(final OnTagGameUpdateListener onTagGameUpdateListener) {
        this.onTagGameUpdateListener = onTagGameUpdateListener;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        this.mState = state;
    }

    public void onMessageReceived(final int remoteState, final int remoteId, final double range) {
        final long timeElapsed = SystemClock.uptimeMillis() - mLastChange;
        //Check for tagged
        if(remoteState == TagGame.SAFE){
            //If you are tagged become It, if not already It
            if(remoteId == ID && getState() != TagGame.IT){
                updateBeacon(TagGame.IT, ID);
            }
        } else {
            if (getState() == TagGame.IT && range < mTagRange) {
                //If toggleIt and close
                if(remoteState == TagGame.NOT_IT && timeElapsed > TagGame.FREEZE_TIME) {
                    //Transmit the Id of who you tagged
                    updateBeacon(TagGame.SAFE, remoteId);
                }
            }  else if(remoteState == TagGame.IT && getState() != TagGame.NOT_IT){
                // No longer toggleIt
                updateBeacon(TagGame.NOT_IT, ID);
            }
        }
    }

    private void updateBeacon(int state, int id) {
        if(mState == state){
            return;
        }
        if (state == TagGame.IT || state == TagGame.SAFE || SystemClock.uptimeMillis() - mLastChange > TagGame.FREEZE_TIME) {
            mLastChange = SystemClock.uptimeMillis();
            setState(state);
        }
        onTagGameUpdateListener.onTagGameUpdate(state, id);
    }

    public String getFreezeTimeLeftString() {
        final long timeElapsed = SystemClock.uptimeMillis() - mLastChange;
        final long millis = TagGame.FREEZE_TIME - timeElapsed;
        final int seconds = (int) (Math.max(0, millis)) / 1000;
        final long microSeconds = timeElapsed % 100;
        if(millis > 0){
            return Integer.toString(seconds) + "." + Long.toString(microSeconds);
        } else {
            return "0";
        }
    }

    public void toggleIt() {
        if(mState != IT){
            updateBeacon(IT, ID);
        } else {
            updateBeacon(NOT_IT, ID);
        }
    }

    public int getId() {
        return ID;
    }

    static public interface OnTagGameUpdateListener {
        public void onTagGameUpdate(final int state, final int id);
    }
}

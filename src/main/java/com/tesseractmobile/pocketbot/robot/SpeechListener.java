package com.tesseractmobile.pocketbot.robot;

/**
 * Created by josh on 11/29/2015.
 */
public interface SpeechListener {

    /**
     * Called when the user speaks to the Robot
     * @param speech
     */
    void onSpeechIn(final String speech);

    /**
     * Called when the Robot speaks to the user
     * @param speech
     */
    void onSpeechOut(final String speech);
}

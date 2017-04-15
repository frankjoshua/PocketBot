package com.tesseractmobile.pocketbot.service;

/**
 * Created by josh on 8/25/2015.
 */
public interface VoiceRecognitionListener {
    void onVoiceRecognitionError(String text);

    /**
     * Execute commands based on input
     * @param text
     * @return true if input handled
     */
    boolean onProccessInput(String text);

    void onTextInput(String text);

    void onVoiceRecognitionStateChange(VoiceRecognitionState state);
}

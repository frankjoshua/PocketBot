package com.tesseractmobile.pocketbot.robot;

import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;

/**
 * Created by josh on 7/13/16.
 */
public interface VoiceRecognitionService {
    /**
     * Start Listening for user voice input
     */
    void startListening();

    /**
     * Register to be notified about voice events
     * @param voiceRecognitionListener
     */
    void registerVoiceRecognitionListener(VoiceRecognitionListener voiceRecognitionListener);
}

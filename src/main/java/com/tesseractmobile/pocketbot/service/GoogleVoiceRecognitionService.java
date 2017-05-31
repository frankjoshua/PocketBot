package com.tesseractmobile.pocketbot.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import com.tesseractmobile.pocketbot.robot.VoiceRecognitionService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 8/25/2015.
 */
public class GoogleVoiceRecognitionService extends BaseVoiceRecognitionService implements RecognitionListener{

    private  static final String TAG = GoogleVoiceRecognitionService.class.getSimpleName();

    private static final boolean HIDE_VOICE_PROMPT              = true;

    private SpeechRecognizer mSpeechRecognizer;
    private boolean              mHideVoicePrompt;
    private boolean doError;
    private boolean doEndOfSpeech;
    private boolean doBeginningOfSpeech;
    private long mSpeechRecognizerStartListeningTime;


    @Override
    public void onCreate() {
        super.onCreate();
        // Load settings
        mHideVoicePrompt = HIDE_VOICE_PROMPT;

        if (checkVoiceRecognition()) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService"));
            mSpeechRecognizer.setRecognitionListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpeechRecognizer.stopListening();
        mSpeechRecognizer.cancel();
        mSpeechRecognizer.destroy();
    }

    private boolean checkVoiceRecognition() {
        // Check if voice recognition is present
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            //getMouthView().setEnabled(false);
            error("Voice recognizer not present");
            Toast.makeText(this, "Voice recognizer not present", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (SpeechRecognizer.isRecognitionAvailable(this) == false) {
            error("I have no voice recognition service available");
            return false;
        }

        return true;
    }

    /**
     * @param prompt
     */
    private synchronized void lauchListeningIntent() {

        if(getState() != VoiceRecognitionState.READY){
            Log.d(TAG, "Unable to listen. State is " +  getState().toString());
            return;
        }
        Log.d(TAG, "Launching Voice Prompt.");
        setState(VoiceRecognitionState.STARTING_LISTENING);

        //Use Google Speech Recognizer
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Given an hint to the recognizer about what the user is going to error
        // There are two form of language model available
        // 1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
        // 2.LANGUAGE_MODEL_FREE_FORM : If not sure about the words or phrases
        // and its domain.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be
        // sorted where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2500);
        // Start the Voice recognizer activity for the result.
        if (mHideVoicePrompt) {
            mSpeechRecognizer.startListening(intent);
            mSpeechRecognizerStartListeningTime = System.currentTimeMillis();
        } else {
            //startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
            throw new UnsupportedOperationException();
        }
        // error("I'm Listening");
        // Uncomment for test speech
        // new BotTask().execute("Are you listening?");
    }


    @Override
    public synchronized void onResults(final Bundle results) {
        final ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        proccessSpeech(data);
        setState(VoiceRecognitionState.READY);
    }

    /**
     * @param data
     */
    private void proccessSpeech(final ArrayList<String> data) {
        if (data != null && data.size() > 0) {
            final String response = data.get(0);
            if (response != null) {
                onTextInput(response);
            } else {
                // Something went wrong
                error("Pardon? " + SPEECH_INSTRUTIONS);
            }
        } else {
            error("No data recieved.");
        }
    }

    @Override
    public void onPartialResults(final Bundle partialResults) {
        error("I only heard a little of what you said");
    }

    @Override
    public void onEvent(final int eventType, final Bundle params) {
        error("Event " + eventType);
    }

    @Override
    public void onReadyForSpeech(final Bundle params) {
        doError = true;
        doEndOfSpeech = true;
        doBeginningOfSpeech = true;
        setState(VoiceRecognitionState.READY_FOR_SPEECH);
        //setEmotion(Emotion.ACCEPTED);
    }

    @Override
    public void onBeginningOfSpeech() {
        if (doBeginningOfSpeech) {
            doBeginningOfSpeech = false;
            setState(VoiceRecognitionState.BEGINNING_OF_SPEECH);
        }
        //setEmotion(Emotion.AWARE);
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        if (doEndOfSpeech) {
            setState(VoiceRecognitionState.END_OF_SPEECH);
        }
        //Show joy
        //setEmotion(Emotion.JOY);
    }

    @Override
    public void onError(final int error) {
        Log.d(TAG, "Error in state " + getState().toString() + " error code " + Integer.toString(error));

        //Best practice: call setState() first then out put the error
        switch (error) {
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                setState(VoiceRecognitionState.READY);
                error("I didn't hear you. " + SPEECH_INSTRUTIONS);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                if(getState() == VoiceRecognitionState.END_OF_SPEECH){
                    long duration = System.currentTimeMillis() - mSpeechRecognizerStartListeningTime;
                    if (duration < 500){
                        //This is a Google Bug. Wait for the next error state.
                        Log.e(TAG, "Error after " + duration + ", continuing to wait for text match.");
                        return;
                    }
                    Log.e(TAG, "Error after " + duration + ", giving up hope of text match.");
                    setState(VoiceRecognitionState.READY);
                    //startListening();
                    error("I'm sorry, I could not understand you. " + SPEECH_INSTRUTIONS);
                } else {
                    //This should not happen but it does
                    //https://code.google.com/p/android/issues/detail?id=179293
                    Log.e(TAG, "Bad SpeechRecognizer.ERROR_NO_MATCH error in state " + getState());
                }
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                setState(VoiceRecognitionState.ERROR);
                error("I'm sorry, but my speech recognizer is busy. Who ever programmed me probably forgot to close the service properly.");
                break;
            default:
                setState(VoiceRecognitionState.ERROR);
                error("I had and unknown error in my speech system. The error code is " + error + ". I'm sorry that I can not be more helpful.");
                break;
        }

    }

    @Override
    public void startListening() {
        lauchListeningIntent();
    }

}

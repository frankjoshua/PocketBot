package com.tesseractmobile.pocketbot.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.tesseractmobile.pocketbot.robot.VoiceRecognitionService;

/**
 * Created by josh on 7/13/16.
 */
abstract public class BaseVoiceRecognitionService extends Service implements VoiceRecognitionService {

    protected static final String SPEECH_INSTRUTIONS = "Please wait for the beep before speaking.";//"Touch my mouth if you want to error something";
    private static final String TAG = BaseVoiceRecognitionService.class.getSimpleName();
    final private IBinder binder = new LocalBinder();
    private VoiceRecognitionListener mVoiceRecognitionListener;
    private VoiceRecognitionState mState = VoiceRecognitionState.READY;

    protected void error(final String text){
        final VoiceRecognitionListener voiceRecognitionListener = this.mVoiceRecognitionListener;
        if(voiceRecognitionListener != null){
            voiceRecognitionListener.onVoiceRecognitionError(text);
        }
    }


    protected void onTextInput(final String text){
        final VoiceRecognitionListener voiceRecognitionListener = this.mVoiceRecognitionListener;
        if(voiceRecognitionListener != null) {
            voiceRecognitionListener.onTextInput(text);
        }
    }

    protected void setState(final VoiceRecognitionState state){
        mState = state;
        final VoiceRecognitionListener voiceRecognitionListener = this.mVoiceRecognitionListener;
        if(voiceRecognitionListener != null) {
            voiceRecognitionListener.onVoiceRecognitionStateChange(state);
        }
        Log.d(TAG, mState.toString());
    }


    protected VoiceRecognitionState getState(){
        return mState;
    }

    public void registerVoiceRecognitionListener(final VoiceRecognitionListener voiceRecognitionListener){
        this.mVoiceRecognitionListener = voiceRecognitionListener;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public VoiceRecognitionService getService(){
            return BaseVoiceRecognitionService.this;
        }
    }

}

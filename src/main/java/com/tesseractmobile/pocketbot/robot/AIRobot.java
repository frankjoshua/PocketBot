package com.tesseractmobile.pocketbot.robot;

/**
 * Created by josh on 11/16/2015.
 */
abstract public class AIRobot extends BaseRobot {
    private AI mAI;

    private SpeechListener mSpeechListener;

    public AIRobot(final DataStore dataStore) {
        super(dataStore);
    }

    /**
     * Set the AI service to use
     * @param ai
     */
    public void setAI(final AI ai){
        mAI = ai;
    }

    @Override
    public boolean onProccessInput(final String text) {
        //Report to the SpeechListener
        if(mSpeechListener != null){
            mSpeechListener.onSpeechOut(text);
        }
        //Not handled completely
        return false;
    }

    @Override
    public void onTextInput(final String text) {
        mAI.input(text, null);
        //Report to the SpeechListener
        if(mSpeechListener != null){
            mSpeechListener.onSpeechIn(text);
        }
    }

    @Override
    public void registerSpeechListener(final SpeechListener speechListener){
        mSpeechListener = speechListener;
    }

    @Override
    public void unregisterSpeechListener(final SpeechListener speechListener){
        mSpeechListener = null;
    }

}

package com.tesseractmobile.pocketbot.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.tesseractmobile.pocketbot.robot.AI;
import com.tesseractmobile.pocketbot.robot.AIListener;
import com.tesseractmobile.pocketbot.robot.CommandContract;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.Robot;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import io.fabric.sdk.android.Fabric;

/**
 * Created by josh on 8/19/2015.
 */
public class AiFragmentActivity extends BaseFaceFragmentActivity implements AI {


    private AIDataService mAiDataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        final String token = PocketBotSettings.getApiAiToken(this);
        //final String key = PocketBotSettings.getApiAiKey(this);
        final AIConfiguration aiConfig = new AIConfiguration(token, AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
        mAiDataService = new AIDataService(this, aiConfig);
        Robot.get().setAI(this);
        setSensorDelay(120);
    }


    private void handleAiResponse(final AIResponse aiResponse){
        final Result result = aiResponse.getResult();
        final String action = result.getAction();
        if(action.equals(CommandContract.ACTION_ARDUINO)){
            throw new UnsupportedOperationException("Not implemeted");
            //sendData(result);
        } else if(action.equals(CommandContract.ACTION_MOVE)){
            final String direction = result.getStringParameter(CommandContract.PARAM_DIRECTION);
            final String measurement = result.getStringParameter(CommandContract.PARAM_MEASUREMENT);
            final int distance = result.getIntParameter(CommandContract.PARAM_DISTANCE);
            move(direction, measurement, distance);
        } else if(action.equals(CommandContract.ACTION_EMOTION)){
            emotion(result);
        } else if (action.equals(CommandContract.ACTION_SETTINGS)){
            final String previewSetting = result.getStringParameter(CommandContract.PARAM_PREVIEW, "false");
            final boolean shouldPreview = previewSetting.equalsIgnoreCase("true");
            PocketBotSettings.setShowPreview(this, shouldPreview);
        } else if (action.equals(CommandContract.ACTION_LAUNCH)){
            //Launch an app
            final String packageName = result.getStringParameter(CommandContract.PARAM_PACKAGE);
            final Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if(intent != null){
                //If found launch the app
                startActivity(intent);
            } else {
                //Find the app in the pay store if not installed
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                }
            }
        }
        final String speech = result.getFulfillment().getSpeech();
        if(speech.equals("")){
            super.doTextInput(result.getResolvedQuery());
        } else {
            Robot.get().listen(speech);
        }
    }

    private void emotion(Result result) {
        final String emotion = result.getStringParameter(CommandContract.PARAM_EMOTION);

        if(emotion.equals(CommandContract.EMOTION_ANGER)){
            setEmotion(Emotion.ANGER);
        } else if(emotion.equals(CommandContract.EMOTION_JOY)){
            setEmotion(Emotion.JOY);
        } else if(emotion.equals(CommandContract.EMOTION_ACCEPTED)){
            setEmotion(Emotion.ACCEPTED);
        } else if(emotion.equals(CommandContract.EMOTION_AWARE)){
            setEmotion(Emotion.AWARE);
        } else if(emotion.equals(CommandContract.EMOTION_SURPRISED)){
            setEmotion(Emotion.SUPRISED);
        } else if(emotion.equals(CommandContract.EMOTION_FEAR)){
            setEmotion(Emotion.FEAR);
        } else {
            Robot.get().say("I had a new emotion... I don't understand, " + emotion);
        }
    }

    protected void move(String direction, String measurement, int distance) {
        Robot.get().say("I have no body. I can't move");
    }

    @Override
    public void input(String input, AIListener aiListener) {
        if(input == null || input.equals("")){
            return;
        }
        //TODO: Disabled until http://stackoverflow.com/questions/39195771/zipexception-duplicate-entry-during-android-build
//        final AIRequest aiRequest = new AIRequest();
//        aiRequest.setQuery(input);
//
//        new AsyncTask<Void, Void, Void>(){
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    final AIResponse aiResponse = mAiDataService.request(aiRequest);
//                    // process response object here...
//                    handleAiResponse(aiResponse);
//
//
//                } catch (final AIServiceException e) {
//                    Robot.get().say("I had an unhandled error.");
//                }
//                return null;
//            }
//        }.execute();
    }
}

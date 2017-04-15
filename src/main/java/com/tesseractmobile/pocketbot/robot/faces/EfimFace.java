package com.tesseractmobile.pocketbot.robot.faces;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.views.EyeView;
import com.tesseractmobile.pocketbot.views.MouthView;
import com.tesseractmobile.pocketbot.views.MouthView.SpeechCompleteListener;

/**
 * Created by josh on 10/17/2015.
 */
public class EfimFace extends BaseFace implements RobotFace, OnClickListener{

    private MouthView mouthView;
    private EyeView mLeftEye;
    private EyeView mRightEye;

    private Emotion mEmotion = Emotion.JOY;

    private String mTempText;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mouthView.setText(mTempText);
        }
    };

    public EfimFace(final View view){

        //Init views
        mouthView = (MouthView) view.findViewById(R.id.mouthView);
        mLeftEye = (EyeView) view.findViewById(R.id.eyeViewLeft);
        mRightEye = (EyeView) view.findViewById(R.id.eyeViewRight);

        // Setup click listeners
        mLeftEye.setOnClickListener(this);
        mRightEye.setOnClickListener(this);
        mouthView.setOnClickListener(this);
    }

    @Override
    public void look(float x, float y, float z) {
        mLeftEye.look(x, y);
        mRightEye.look(x, y);
    }

    @Override
    public void say(final String text) {
         mTempText = text;
         mHandler.sendEmptyMessage(0);
    }

    @Override
    public void setOnSpeechCompleteListener(SpeechCompleteListener speechCompleteListener) {
        mouthView.setOnSpeechCompleteListener(speechCompleteListener);
    }

    @Override
    public void onClick(final View v) {
        final int viewId = v.getId();

        switch (viewId) {
            case R.id.eyeViewLeft:
                mRobotInterface.say("Ouch");
                fear();
                // finish();
                break;
            case R.id.eyeViewRight:
                //say("I'm going to kill you in my sleep... Oh wait, your sleep");
                mRobotInterface.say("Please don't poke my eye.");
                anger();
                break;
            case R.id.mouthView:
                mRobotInterface.listen();
                break;
        }
    }

    @Override
    public void setEmotion(final Emotion emotion) {
        if (mEmotion != emotion) {
            mEmotion = emotion;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    switch (emotion) {
                        case ACCEPTED:
                            mLeftEye.squint();
                            mRightEye.squint();
                            mouthView.smile();
                            break;
                        case SUPRISED:
                            mLeftEye.open();
                            mRightEye.open();
                            mLeftEye.blink();
                            mRightEye.blink();
                            mouthView.smile();
                            break;
                        case AWARE:
                            mLeftEye.open();
                            mRightEye.squint();
                            mouthView.nuetral();
                            break;
                        case JOY:
                            mLeftEye.wideOpenLeft();
                            mRightEye.wideOpenRight();
                            mouthView.smile();
                            break;
                        case FEAR:
                            fear();
                            mouthView.frown();
                            break;
                        case ANGER:
                            anger();
                            mouthView.frown();
                            break;
                        default:
                            mLeftEye.squint();
                            mRightEye.squint();
                            mouthView.frown();
                            say("I don't under stand the emotion " + emotion + ".");
                            break;
                    }
                }
            });
        }
    }

    /**
     * Set look to fearful
     */
    private void fear() {
        mLeftEye.squintLeft();
        mRightEye.squintRight();
    }

    /**
     * Set look to angry
     */
    private void anger() {
        mLeftEye.squintRight();
        mRightEye.squintLeft();
    }

//    public void sendJson(JSONObject jsonObject) {
//        final SensorData sensorData = mRobotInterface.getSensorData();
//        try {
//            //Read in JSON and send to the local robot
//            final float x = (float) jsonObject.getDouble(ControlFace.JOY1_X);
//            final float y = (float) jsonObject.getDouble(ControlFace.JOY1_Y);
//            final float z = (float) jsonObject.getDouble(ControlFace.JOY1_Z);
//            final boolean a = (boolean) jsonObject.getBoolean(ControlFace.JOY1_A);
//            final boolean b = (boolean) jsonObject.getBoolean(ControlFace.JOY1_B);
//            final int heading = (int) jsonObject.getInt(ControlFace.JOY1_HEADING);
//            //Update joystick 1
//            sensorData.setJoystick1(x, y, z, a, b, heading);
//            //Update joystick 2
//            final float x2 = (float) jsonObject.getDouble(ControlFace.JOY2_X);
//            final float y2 = (float) jsonObject.getDouble(ControlFace.JOY2_Y);
//            final float z2 = (float) jsonObject.getDouble(ControlFace.JOY2_Z);
//            final boolean a2 = (boolean) jsonObject.getBoolean(ControlFace.JOY2_A);
//            final boolean b2 = (boolean) jsonObject.getBoolean(ControlFace.JOY2_B);
//            final int heading2 = (int) jsonObject.getInt(ControlFace.JOY2_HEADING);
//            sensorData.setJoystick2(x2, y2, z2, a2, b2, heading2);
//            //Send data
//            mRobotInterface.sendSensorData(false);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

}

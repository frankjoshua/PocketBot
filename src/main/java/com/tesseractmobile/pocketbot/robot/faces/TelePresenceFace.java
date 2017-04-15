package com.tesseractmobile.pocketbot.robot.faces;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.views.MouthView;

import java.text.NumberFormat;

/**
 * Created by josh on 10/31/2015.
 */
public class TelePresenceFace extends BaseFace {

    private TextView mUserId;
    private NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private Handler mHandler = new Handler();

    public TelePresenceFace(View view) {
        numberFormat.setMinimumFractionDigits(2);
        mUserId = (TextView) view.findViewById(R.id.tvUserId);
    }

    @Override
    public void setEmotion(Emotion emotion) {

    }

    @Override
    public void look(float x, float y, float z) {

    }

    @Override
    public void say(String text) {

    }

    @Override
    public void setOnSpeechCompleteListener(MouthView.SpeechCompleteListener speechCompleteListener) {

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
//            mRobotInterface.sendSensorData(y == 0);
//            //Show joystick data in text view
//            final String data =  "JoyX: " + numberFormat.format(x) + " JoyY: " + numberFormat.format(y);// + " JoyZ: " + numberFormat.format(z);
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mUserId.setText(data);
//                }
//            });
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onControlReceived(SensorData.Control control) {
        super.onControlReceived(control);
        //Show joystick data in text view
        final String data =  "JoyX: " + numberFormat.format(control.joy1.X) + " JoyY: " + numberFormat.format(control.joy1.Y);// + " JoyZ: " + numberFormat.format(z);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mUserId.setText(data);
            }
        });
    }
}

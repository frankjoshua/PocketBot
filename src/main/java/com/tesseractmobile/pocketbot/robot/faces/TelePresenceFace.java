package com.tesseractmobile.pocketbot.robot.faces;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.model.Face;
import com.tesseractmobile.pocketbot.robot.model.Speech;
import com.tesseractmobile.pocketbot.views.MouthView;

import java.text.NumberFormat;

/**
 * Created by josh on 10/31/2015.
 */
public class TelePresenceFace extends BaseFace {

    private TextView mUserId;
    private NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private Handler mHandler = new Handler();

    public TelePresenceFace(final View view, final RobotInterface robotInterface) {
        setRobotInterface(robotInterface);
        numberFormat.setMinimumFractionDigits(2);
        mUserId = (TextView) view.findViewById(R.id.tvUserId);
    }

    @Override
    public void setEmotion(Emotion emotion) {

    }

    @Override
    public void look(Face face) {

    }

    @Override
    public void say(Speech text) {

    }

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

package com.tesseractmobile.pocketbot.robot.faces;

import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by josh on 10/25/2015.
 */
abstract public class BaseFace implements RobotFace{
    protected RobotInterface mRobotInterface;

    final public void setRobotInterface(final RobotInterface robotInterface){
        this.mRobotInterface = robotInterface;
        robotInterface.getEmotion().subscribe(new Observer<Emotion>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Emotion emotion) {
                setEmotion(emotion);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void onControlReceived(final SensorData.Control message) {
        final SensorData sensorData = mRobotInterface.getSensorData();
        sensorData.setControl(message);
        //Send data
        mRobotInterface.sendSensorData(false);

        //Look based on joystick 2
        look((float) message.joy2.X + 1, (float) message.joy2.Y + 1, 0);
    }
}

package com.tesseractmobile.pocketbot.robot.faces;

import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.model.Face;
import com.tesseractmobile.pocketbot.robot.model.Speech;

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
        robotInterface.getFaceSubject().subscribe(new Observer<Face>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Face face) {
                look(face);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        robotInterface.getSpeechSubject().subscribe(new Observer<Speech>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Speech speech) {
                say(speech);
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
        RobotInterface robotInterface = this.mRobotInterface;
        if(robotInterface == null){
            return;
        }
        final SensorData sensorData = robotInterface.getSensorData();
        sensorData.setControl(message);
        //Send data
        robotInterface.sendSensorData(false);

        //Look based on joystick 2
        final Face face = new Face((float) message.joy2.X + 1, (float) message.joy2.Y + 1, 0);
        look(face);
    }
}

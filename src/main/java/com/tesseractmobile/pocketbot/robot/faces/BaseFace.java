package com.tesseractmobile.pocketbot.robot.faces;

import com.tesseractmobile.pocketbot.robot.SensorData;

/**
 * Created by josh on 10/25/2015.
 */
abstract public class BaseFace implements RobotFace{
    protected RobotInterface mRobotInterface;

    final public void setRobotInterface(final RobotInterface robotInterface){
        this.mRobotInterface = robotInterface;
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

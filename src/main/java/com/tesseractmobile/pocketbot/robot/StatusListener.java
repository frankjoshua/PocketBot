package com.tesseractmobile.pocketbot.robot;

/**
 * Created by josh on 1/21/2016.
 */
public interface StatusListener {

    /**
     * Called when remote sensor data changes
     * @param sensorData
     */
    void onRemoteSensorUpdate(final SensorData sensorData);

}

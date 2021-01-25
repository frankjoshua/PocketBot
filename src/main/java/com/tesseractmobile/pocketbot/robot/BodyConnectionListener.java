package com.tesseractmobile.pocketbot.robot;

/**
 * Created by josh on 8/29/2015.
 */
public interface BodyConnectionListener {

    public void onBluetoothDeviceFound();

    public void onError(int i, String connected);

    public void onBodyConnected(BodyInterface bluetoothService);

    public void onRobotEvent(final RobotEvent robotEvent);
}

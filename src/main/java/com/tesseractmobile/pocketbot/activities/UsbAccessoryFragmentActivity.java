package com.tesseractmobile.pocketbot.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.service.BodyService;
import com.tesseractmobile.pocketbot.service.UsbConnectionService;

public class UsbAccessoryFragmentActivity extends AiFragmentActivity {

    private final ServiceConnection conn = new ServiceConnection() {

                                             @Override
                                             public void onServiceDisconnected(final ComponentName name) {

                                             }

                                             @Override
                                             public void onServiceConnected(final ComponentName name, final IBinder service) {
                                                 final BodyService bodyService = ((BodyService.LocalBinder) service).getService();
                                                 bodyService.registerBodyConnectionListener(Robot.get().getBodyConnectionListener());
                                                 //robotCommandInterface.reconnectRobot();
                                             }
                                         };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Speed up sensor data
        setSensorDelay(30);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Intent bindIntent = new Intent(this, UsbConnectionService.class);
        bindService(bindIntent, conn, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        unbindService(conn);
        super.onStop();
    }


}

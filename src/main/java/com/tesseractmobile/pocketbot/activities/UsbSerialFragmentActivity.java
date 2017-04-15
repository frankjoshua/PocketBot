package com.tesseractmobile.pocketbot.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.tesseractmobile.pocketbot.robot.AuthData;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.service.BodyService;
import com.tesseractmobile.pocketbot.service.UsbSerialService;

/**
 * Created by josh on 11/18/2015.
 */
public class UsbSerialFragmentActivity extends AiFragmentActivity {
    private final ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(final ComponentName name) {

        }

        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            final BodyService bodyService = ((BodyService.LocalBinder) service).getService();
            bodyService.registerBodyConnectionListener(Robot.get().getBodyConnectionListener());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the intent that started this activity
        final Intent intent = getIntent();
        final Uri data = intent.getData();
        if(data != null){
            final String robotId = data.getLastPathSegment();
            Log.d("USB", robotId);
            //Need to add this to list of allowed robots
            Robot.get().getDataStore().registerOnAuthCompleteListener(new DataStore.OnAuthCompleteListener() {
                @Override
                public void onAuthComplete(final AuthData authData) {
                    Robot.get().getDataStore().addRobot(robotId, false);
                }
            });
        }

        //Speed up sensor data
        setSensorDelay(20);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Intent bindIntent = new Intent(this, UsbSerialService.class);
        if (bindService(bindIntent, conn, Service.BIND_AUTO_CREATE) == false) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void onStop() {
        unbindService(conn);
        super.onStop();
    }
}

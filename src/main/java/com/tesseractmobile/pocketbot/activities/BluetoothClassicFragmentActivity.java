package com.tesseractmobile.pocketbot.activities;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.fragments.BluetoothDialog;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.service.BluetoothClassicService;
import com.tesseractmobile.pocketbot.service.BodyService;

/**
 * Created by josh on 12/7/2015.
 */
public class BluetoothClassicFragmentActivity extends AiFragmentActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    private ServiceConnection bluetoothServiceConnection;

    private boolean mBluetoothEnabled = false;
    private BluetoothClassicService blueToothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check if bluetooth is supported
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            startActivity(new Intent(this, AiFragmentActivity.class));
            finish();
            return;
        }
        //Check if bluetooth is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothEnabled = true;
        }

        findViewById(R.id.btnBluetooth).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnBluetooth){
            //Launch bluetooth dialog
            final BluetoothDialog bluetoothDialog = new BluetoothDialog();
            bluetoothDialog.show(getSupportFragmentManager(), "BLUETOOTH_DIALOG_FRAGMENT");
            bluetoothDialog.setData(blueToothService.getDeviceList());
        } else {
            super.onClick(view);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Bind to bluetooth service
        if(mBluetoothEnabled) {
            connectToBluetoothService();
        }
    }

    private void connectToBluetoothService() {
        bluetoothServiceConnection = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                blueToothService = (BluetoothClassicService) ((BodyService.LocalBinder) service).getService();
                blueToothService.registerBodyConnectionListener(Robot.get().getBodyConnectionListener());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        final Intent bluetoothBindIntent = new Intent(this, BluetoothClassicService.class);
        if(bindService(bluetoothBindIntent, bluetoothServiceConnection, Service.BIND_AUTO_CREATE) == false){
            throw new UnsupportedOperationException("Error binding to bluetooth service");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //Unbind from bluetooth service
        if(mBluetoothEnabled) {
            unbindService(bluetoothServiceConnection);
            bluetoothServiceConnection = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if(resultCode == Activity.RESULT_OK){
           mBluetoothEnabled = true;
           connectToBluetoothService();
       }
    }
}

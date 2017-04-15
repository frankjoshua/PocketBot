package com.tesseractmobile.pocketbot.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by josh on 12/7/2015.
 */
public class BluetoothClassicService extends BodyService {

    private final static String TAG = BluetoothClassicService.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    /** List of paired bluetooth devices */
    private ArrayList<BluetoothDevice> mPairedDevices = new ArrayList();
    /** Connected bluetooth device */
    private String mConnectedDevice;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Found " + device.getName() + "\n" + device.getAddress());
                // Add the name and address to an array adapter to show in a ListView
                mPairedDevices.add(device);

                mBluetoothAdapter.cancelDiscovery();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                Log.d(TAG, "Paired with " + device.getName() + "\n" + device.getAddress());
                mPairedDevices.add(device);
            }
        }

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        final String address = PocketBotSettings.getBluetoothDevice(this);
    }

    /**
     * List of paired and discovered devices
     * @return
     */
    public ArrayList<BluetoothDevice> getDeviceList() {
        return mPairedDevices;
    }

    @Override
    protected void bodyListenerRegistered() {
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    public void sendObject(Object object) {

    }

    @Override
    public void sendJson(String json) {

    }

    @Override
    public void sendBytes(byte[] bytes) {

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}

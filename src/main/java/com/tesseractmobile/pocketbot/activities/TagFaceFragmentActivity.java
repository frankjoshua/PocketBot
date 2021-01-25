package com.tesseractmobile.pocketbot.activities;

import android.bluetooth.le.AdvertiseSettings;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.tag.HeadingEstimate;
import com.tesseractmobile.tag.TagGame;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by josh on 9/30/2015.
 */
public class TagFaceFragmentActivity extends BluetoothFragmentActivity implements BeaconConsumer, TagGame.OnTagGameUpdateListener {

    private HeadingEstimate mHeadingEstimate = new HeadingEstimate();
    private BeaconManager beaconManager;
    private BeaconTransmitter beaconTransmitter;
    private Beacon beacon;

    private TagGame mTagGame;

    private Handler hander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            final double range = (double) msg.obj;
            final int remoteState = msg.arg1;
            final int remoteId = msg.arg2;

            //Update Tag game
            mTagGame.onMessageReceived(remoteState, remoteId, range);
            //Send new Data
            Robot.get().sendSensorData(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Only phones with API 18+ have Bluetooth LE
        if(Build.VERSION.SDK_INT < 18){
            return;
        }
        //If bluetooth scanning is off just exit
        //TODO ***** This needs to be improved
        if(PocketBotSettings.isUseBluetooth(this) == false){
            return;
        }
        mTagGame = new TagGame(this);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        // beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);
        beaconManager.setForegroundScanPeriod(100L);
        beaconManager.setForegroundBetweenScanPeriod(0L);
        beaconManager.setBackgroundMode(false);
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //Start transmitting
        //TODO Need to check if the phone supports this
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);

        createBeacon(TagGame.NOT_IT, mTagGame.getId());
    }

    /**
     * Sent state amd Id
     * If SAFE send the Id of who you tagged
     * @param type
     * @param id
     */
    private void createBeacon(final int type, final int id) {
        //TODO Need to check if the phone supports this
        //Log.d(TAG, "Creating Beacon, type " + Integer.toString(type) + " Id " + Integer.toString(id));
        beaconTransmitter.stopAdvertising();
        beacon = new Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
                .setId2(Integer.toString(type))
                .setId3(Integer.toString(id))
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[]{0l}))
                .build();
        beaconTransmitter.startAdvertising(beacon);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            public double mLastDistance;

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    RunningAverageRssiFilter.setSampleExpirationMilliseconds(1000);
                    final Beacon beacon = beacons.iterator().next();
                    //Log.i(TAG, "The first beacon I see is about " + beacon.getDistance() + " meters away.");
                    final double distance = beacon.getDistance();
                    final String transimitedId = beacon.getId3().toString();
                    //If our id is the same as the transmitted id we need to update now because we have been tagged
                    final boolean tagged = transimitedId.equals(Integer.toString(mTagGame.getId())) || mTagGame.getState() == TagGame.SAFE || mTagGame.getState() == TagGame.IT;
                    //Update if tagged or distance has changed
                    final double distanceChange = distance - mLastDistance;
                    if (tagged || Math.abs(distanceChange) > .001f) {
                        //Log.d(TAG, "Updating");
                        mLastDistance = distance;
                        //Update display
                        final Message obtain = Message.obtain();
                        obtain.obj = distance;
                        obtain.arg1 = Integer.parseInt(beacon.getId2().toString());
                        obtain.arg2 = Integer.parseInt(transimitedId);
                        hander.sendMessage(obtain);
                        //Update heading estimate
                        mHeadingEstimate.newData(Robot.get().getSensorData().getSensor().heading, distanceChange);
                        if(mTagGame.getState() == TagGame.IT) {
                            //Chase
                            //getRobotInterface().getSensorData().setDestHeading(mHeadingEstimate.getHeadingEstimate());
                            throw new UnsupportedOperationException("Not implemented!");
                        } else {
                            //Run Away
                            int direction = mHeadingEstimate.getHeadingEstimate() - 180;
                            if(direction < 0){
                                direction += 360;
                            }
                            //getRobotInterface().getSensorData().setDestHeading(direction);
                            throw new UnsupportedOperationException("Not implemented!");
                        }
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    @Override
    public void onTagGameUpdate(int state, int id) {
        if(state == TagGame.SAFE){
            Robot.get().say("Tag your it!");
        } else if (state == TagGame.IT){
            Robot.get().say("You got me!");
        }
        createBeacon(state, id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final BeaconManager beaconManager = this.beaconManager;
        if(beaconManager != null){
            beaconManager.unbind(this);
        }
    }
}

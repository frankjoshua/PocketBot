package com.tesseractmobile.pocketbot.robot;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tesseractmobile.pocketbot.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 3/27/2016.
 */
public class GoogleNearbyConnectionController implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Connections.MessageListener,
        RemoteTransmiter{

    private static final String TAG = GoogleNearbyConnectionController.class.getSimpleName();
    // Identify if the device is the host
    private boolean mIsHost = false;

    final GoogleApiClient mGoogleApiClient;
    private String mRemoteEndpointId;

    public GoogleNearbyConnectionController(final GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    /**
     * Start as a network Host
     * @param context
     */
    public void startAdvertising(final Context context, final GoogleApiClient googleApiClient) {
        // Identify that this device is the host
        mIsHost = true;

        // Advertising with an AppIdentifer lets other devices on the
        // network discover this application and prompt the user to
        // install the application.
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(context.getPackageName()));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        // The advertising timeout is set to run indefinitely
        // Positive values represent timeout in milliseconds
        long NO_TIMEOUT = 0L;

        String name = null;
        Nearby.Connections.startAdvertising(googleApiClient, name, appMetadata, NO_TIMEOUT,
                new Connections.ConnectionRequestListener() {
                    @Override
                    public void onConnectionRequest(String endpointId, String deviceId, byte[] payload) {
                        Log.d(TAG, "ConnectionRequest from " + endpointId);
                        Nearby.Connections.acceptConnectionRequest(mGoogleApiClient, endpointId, payload, GoogleNearbyConnectionController.this);
                    }
                }).setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                if (result.getStatus().isSuccess()) {
                    // Device is advertising
                    Log.d(TAG, "startAdvertising:onResult: SUCCESS");
                } else {
                    int statusCode = result.getStatus().getStatusCode();
                    // Advertising failed - see statusCode for more details
                }
            }
        });
    }

    /**
     * Look for host on the newtork
     * @param context
     */
    public void startDiscovery(final Context context, final GoogleApiClient googleApiClient) {

        String serviceId = context.getString(R.string.service_id);

        // Set an appropriate timeout length in milliseconds
        long DISCOVER_TIMEOUT = 1000L;

        // Discover nearby apps that are advertising with the required service ID.
        Nearby.Connections.startDiscovery(googleApiClient, serviceId, DISCOVER_TIMEOUT, new Connections.EndpointDiscoveryListener() {
            @Override
            public void onEndpointFound(final String endpointId, String deviceId,
                                        String serviceId) {
                Log.d(TAG, "Endpoint found " + endpointId);
                connectTo(endpointId);
            }

            @Override
            public void onEndpointLost(String s) {
                Log.w(TAG, "Endpoint lost " + s);
            }
        })
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            // Device is discovering
                            Log.d(TAG, "startDiscovery:onResult: SUCCESS");
                        } else {
                            int statusCode = status.getStatus().getStatusCode();
                            // Advertising failed - see statusCode for more details
                        }
                    }
                });

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed " + connectionResult.getErrorMessage());
    }

    private void connectTo(String endpointId) {
        // Send a connection request to a remote endpoint. By passing 'null' for
        // the name, the Nearby Connections API will construct a default name
        // based on device model such as 'LGE Nexus 5'.
        String myName = null;
        byte[] myPayload = null;
        Nearby.Connections.sendConnectionRequest(mGoogleApiClient, myName,
                endpointId, myPayload, new Connections.ConnectionResponseCallback() {
                    @Override
                    public void onConnectionResponse(String remoteEndpointId, Status status,
                                                     byte[] bytes) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "Connected to endpoint " + remoteEndpointId);
                            // Successful connection
                            mRemoteEndpointId = remoteEndpointId;
                            RemoteControl.get().setRemoteTransmitter(GoogleNearbyConnectionController.this);
                        } else {
                            // Failed connection
                            Log.d(TAG, "Failed to connected to endpoint: " + status.getStatusMessage());
                        }
                    }
                }, this);
    }

    public void sendMessage(final byte[] bytes){
        Nearby.Connections.sendUnreliableMessage(mGoogleApiClient, mRemoteEndpointId, bytes) ;
        Log.d(TAG, "sendMessage " + bytes.length);
    }

    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {
        Log.d(TAG, "onMessageReceived: " + s);
        //Convert from bytes to PocketBotMessage
        try {
            final PocketBotProtocol.PocketBotMessage data = PocketBotProtocol.PocketBotMessage.parseFrom(bytes);
            final SensorData sensorData = SensorData.fromPocketBotMessage(data);
            Robot.get().getSensorData().setControl(sensorData.getControl());
            Robot.get().sendSensorData(false);
        } catch (InvalidProtocolBufferException e) {

        }
    }

    @Override
    public void onDisconnected(String s) {
        Log.w(TAG, "Disconnected " + s);
    }

    @Override
    public void send(String uuid, Object object) {
        Log.d(TAG, "sending message: " + object.toString());
        //Put control object into a SensorData object to be sent
        final SensorData sensorData = new SensorData();
        final SensorData.Control control = (SensorData.Control) object;
        sensorData.setControl(control);
        //Convert to protobuf
        final PocketBotProtocol.PocketBotMessage data = SensorData.toPocketBotMessage(sensorData);
        //Send the data
        sendMessage(data.toByteArray());
    }


}

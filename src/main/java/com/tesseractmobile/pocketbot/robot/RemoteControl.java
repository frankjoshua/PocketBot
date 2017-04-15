package com.tesseractmobile.pocketbot.robot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.tesseractmobile.pocketbot.activities.KeepAliveThread;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.ArrayList;

/**
 * Created by josh on 12/1/2015.
 */
public class RemoteControl implements ChildEventListener, DataStore.OnAuthCompleteListener, KeepAliveThread.KeepAliveListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String CONTROL = "control";
    public static final String STATUS = "robot_status";
    public static final String DATA = "data";
    private static final String CONNECTED = "connected";
    private static final String TIMESTAMP = "time_stamp";
    private KeepAliveThread mKeepAliveThread;
    //private Pubnub pubnub = new Pubnub("pub-c-2bd62a71-0bf0-4d53-bf23-298fd6b34c3e", "sub-c-75cdf46e-83e9-11e5-8495-02ee2ddab7fe");
    private DatabaseReference mFirebaseListen;
    private DatabaseReference mFirebaseTransmit;

    /** the pubnub channel to listen to */
    private String id;
    /** Listen to control data from Remote */
    final private ArrayList<RemoteListener> mRemoteListeners = new ArrayList<RemoteListener>();
    /** Notify of status update from current connected robot */
    private StatusListener mStatusListener;

    /** Singleton */
    static private RemoteControl instance;
    private long mTimeStamp;
    private String mTransmitUUID;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mFirebaseStatus;
    final private SensorData mSensorData = new SensorData();
    /** Sends data to the remote robot */
    private RemoteTransmiter mRemoteTransmiter;

    private RemoteControl(final Context context, final DataStore dataStore, final String id){
        setId(dataStore, id);
        if(PocketBotSettings.isKeepAlive(context)){
            mKeepAliveThread = new KeepAliveThread(this, null);
            mKeepAliveThread.setName("RemoteKeepAliveThread");
            mKeepAliveThread.startThread();
        }
        PocketBotSettings.registerOnSharedPreferenceChangeListener(context, this);
        //Set firebase as default remote transmiter
        mRemoteTransmiter = new RemoteTransmiter() {
            @Override
            public void send(String uuid, Object object) {
                if(uuid != null) {
                    //Send to firebase
                    mFirebaseTransmit.child(uuid).child(CONTROL).child(DATA).setValue(object);
                    timeStamp(uuid);
                }
            }
        };
    }

    /**
     * Initialize the RemoteControl
     * @param id
     */
    static public void init(final Context context, final DataStore dataStore, final String id){
        if(instance == null){
            instance = new RemoteControl(context, dataStore, id);
        }
    }

    /**
     * Singleton class
     * @return
     */
    static public RemoteControl get(){
        return instance;
    }

    /**
     * Register to listen for remote control messages
     * @param remoteListener
     */
    public synchronized void registerRemoteListener(final RemoteListener remoteListener){
        mRemoteListeners.add(remoteListener);
    }

    /**
     * Stop listening to remote messages
     * @param remoteListener
     */
    public synchronized void unregisterRemoteListener(final RemoteListener remoteListener){
        mRemoteListeners.remove(remoteListener);
    }

    /**
     * The channel id to listen to
     * @param id
     */
    private void setId(final DataStore dataStore, final String id) {
        if(this.id != null){
            //Stop listening for firebase messages
            mFirebaseListen.removeEventListener(this);
            //Stop listening for auth registration
            dataStore.unregisterOnAuthCompleteListener(this);
        }
        //Set the ID
        this.id = id;
        //Listen for auth registration
        dataStore.registerOnAuthCompleteListener(this);
    }

    /**
     * Call when remote message is received
     * @param message
     */
    private synchronized void onObjectReceived(Object message) {
        for (RemoteListener remoteListener : mRemoteListeners) {
            remoteListener.onMessageReceived(message);
        }
    }

    /**
     * Call when connection is lost
     */
    private synchronized void onConnectionLost(){
        for (RemoteListener remoteListener : mRemoteListeners) {
            remoteListener.onConnectionLost();
        }
    }

    /**
     * Create a connection to the remote robot and start tracking status
     * @param uuid
     */
    public void connect(final String uuid, final StatusListener statusListener){
        mTransmitUUID = uuid;
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                final StatusListener listener = statusListener;
                if(listener != null){
                    //Read in sensor data
                    if(dataSnapshot.getKey().equals("sensor")) {
                        final SensorData.Sensor sensor = dataSnapshot.getValue(SensorData.Sensor.class);
                        mSensorData.setSensor(sensor);
                    } else if(dataSnapshot.getKey().equals("control")) {
                        final SensorData.Control control = dataSnapshot.getValue(SensorData.Control.class);
                        mSensorData.setControl(control);
                    } else if(dataSnapshot.getKey().equals("face")) {
                        final SensorData.Face face = dataSnapshot.getValue(SensorData.Face.class);
                        mSensorData.setFace(face.id);
                        mSensorData.setFace(face.X, face.Y, face.Z);
                    }
                    //Update listener
                    listener.onRemoteSensorUpdate(mSensorData);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError DatabaseError) {

            }
        };
        mFirebaseStatus.child(uuid).child(DataStore.SENSORS).addChildEventListener(mChildEventListener);
    }

    /**
     * Pass data to remote robot
     * @param object
     */
    public void send(final Object object) {
        final String uuid = mTransmitUUID;
        mRemoteTransmiter.send(uuid, object);
    }

    /**
     * Stop sending keep alive message
     */
    public void disconnect(){
        mFirebaseStatus.child(mTransmitUUID).child(DataStore.SENSORS).removeEventListener(mChildEventListener);
        mTransmitUUID = null;
    }

    void timeStamp(final String uuid){
        if(uuid != null) {
            //Set time stamp
            mFirebaseTransmit.child(uuid).child(CONNECTED).child(TIMESTAMP).setValue(ServerValue.TIMESTAMP);
        } else {
            //Set local time stamp
            mTimeStamp = SystemClock.uptimeMillis();
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        onObjectReceived(dataSnapshot.getValue(SensorData.Control.class));
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(final DatabaseError DatabaseError) {
        //Firebase Cancelled
    }


    @Override
    public void onAuthComplete(final AuthData authData) {
        //Listen for messages from firebase
        mFirebaseListen = FirebaseDatabase.getInstance().getReferenceFromUrl(DataStore.FIREBASE_URL).child(CONTROL).child(id);
        mFirebaseListen.child(CONTROL).addChildEventListener(this);
        //Setup transmitter
        mFirebaseTransmit = FirebaseDatabase.getInstance().getReferenceFromUrl(DataStore.FIREBASE_URL).child(CONTROL);
        //Listen for status updates
        mFirebaseStatus = FirebaseDatabase.getInstance().getReferenceFromUrl(DataStore.FIREBASE_URL).child(STATUS);
        //Listen for controler disconnect
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected == false) {
                    onConnectionLost();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        //Listen for remote disconnect
        mFirebaseListen.child(CONNECTED).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Get first time stamp
//                final Long timeStamp = dataSnapshot.getValue(Long.class);
//                if(Constants.LOGGING){
//                    Log.d("TimeStamp", "Control Timestamp: " + timeStamp.toString());
//                }
//                mTimeStamp = timeStamp;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //final Long timeStamp = dataSnapshot.getValue(Long.class);
                //Save current time
                if(Constants.LOGGING){
                    Log.d("TimeStamp", "Lag: " + getLag());
                }
                mTimeStamp = SystemClock.uptimeMillis();

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError DatabaseError) {

            }
        });
    }

    public DatabaseReference getControlRef() {
        return mFirebaseTransmit;
    }

    /**
     * Time since last remote command received
     * Does not indicate lost connection just the time since last update
     * @return
     */
    public long getLag() {
        return SystemClock.uptimeMillis() - mTimeStamp;
    }

    @Override
    public void onHeartBeat() {
        timeStamp(mTransmitUUID);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PocketBotSettings.KEY_KEEP_ALIVE)){
            if(sharedPreferences.getBoolean(key, PocketBotSettings.DEFAULT_KEEP_ALIVE)){
                mKeepAliveThread = new KeepAliveThread(this, null);
                mKeepAliveThread.setName("RemoteKeepAliveThread");
                mKeepAliveThread.startThread();
            } else {
                mKeepAliveThread.stopThread();
            }
        }
    }

    public void setRemoteTransmitter(final RemoteTransmiter remoteTransmiter) {
        mRemoteTransmiter = remoteTransmiter;
    }
}

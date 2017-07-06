package com.tesseractmobile.pocketbot.robot;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.ArrayList;

/**
 * Created by josh on 12/27/2015.
 */
public class DataStore implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ROBOTS = "robots";
    public static final String USERS = "users";
    public static final String API_VERSION = "betaV5";
    public static final String BASE_FIREBASE_URL = "https://pocketbot-1161.firebaseio.com/";
    public static final String FIREBASE_URL = BASE_FIREBASE_URL + API_VERSION + "/";
    public static final String AUTH_DATA = "auth_data";
    public static final String SETTINGS = "settings";
    public static final String LAST_ONLINE = "lastOnline";
    public static final String IS_CONNECTED = "isConnected";
    public static final String PREFS = "prefs";
    public static final String SENSORS = "sensors";
    public static final String SENSOR_DATA = "sensor_data";
    static private DataStore instance;
    private final Context mContext;

    /** Stores user and robot data */
    private DatabaseReference mFirebase;

    private AuthData mAuthData;

    private ArrayList<OnAuthCompleteListener> mOnAuthCompleteListeners = new ArrayList<OnAuthCompleteListener>();

    private FirebasePreferenceSync mFirebasePreferenceSync;
    private String mRobotId;

    private DataStore(final Context context){
        mFirebasePreferenceSync = new FirebasePreferenceSync(context);
        mContext = context;
    }

    static public DataStore init(final Context context){
        if(instance == null){
            instance = new DataStore(context);
        }
        return instance;
    }

//    static public DataStore get(){
//        return instance;
//    }

    /**
     * Signs in to Firebase using Google Auth
     * Also adds robot to list of allowed robots
     * @param robotId
     * @param authData
     */
    public void setAuthToken(final String robotId, final AuthData authData) {
        mFirebase = FirebaseDatabase.getInstance().getReferenceFromUrl(FIREBASE_URL);
        //Save Auth data -- Must be done first
        mAuthData = authData;
        setupUser(authData, robotId);
        setupRobot(robotId);
        //Start syncing preferences
        mFirebasePreferenceSync.start(getRobots());
        //Let everyone know we are logged in
        for (OnAuthCompleteListener onAuthCompleteListener : mOnAuthCompleteListeners) {
            onAuthCompleteListener.onAuthComplete(authData);
        }
        PocketBotSettings.registerOnSharedPreferenceChangeListener(mContext, DataStore.this);
    }

    private void setupRobot(final String robotId) {

        if(mRobotId != null){
            //Disconnect last robot
            final DatabaseReference lastRobotRef = getRobots().child(mRobotId);
            //Mark robot last connect status
            lastRobotRef.child(SETTINGS).child(LAST_ONLINE).setValue(ServerValue.TIMESTAMP);
            //Set robot online status
            lastRobotRef.child(SETTINGS).child(IS_CONNECTED).setValue(false);
            lastRobotRef.child(SETTINGS).child(IS_CONNECTED).setValue(false);
        }
        final DatabaseReference robotRef = getRobots().child(robotId);
        //Save ID
        mRobotId = robotId;
        //Mark robot last connect status
        robotRef.child(SETTINGS).child(LAST_ONLINE).onDisconnect().setValue(ServerValue.TIMESTAMP);
        //Set robot online status
        robotRef.child(SETTINGS).child(IS_CONNECTED).setValue(true);
        robotRef.child(SETTINGS).child(IS_CONNECTED).onDisconnect().setValue(false);
    }

    private void setupUser(final AuthData authData, final String robotId) {
        final DatabaseReference userRef = mFirebase.child(USERS).child(authData.getUid());
        //Setup User
        userRef.child(AUTH_DATA).setValue(true);
        //Save current robot to list of allowed robots
        addRobot(robotId, true);
        //Mark user last connect status
        userRef.child(LAST_ONLINE).onDisconnect().setValue(ServerValue.TIMESTAMP);

        //Set user online status
        userRef.child(SETTINGS).child(IS_CONNECTED).setValue(true);
        userRef.child(IS_CONNECTED).onDisconnect().setValue(false);
    }

    public void addRobot(final String robotId, final boolean isOwned) {
        final DatabaseReference userRef = mFirebase.child(USERS).child(mAuthData.getUid());
        userRef.child(ROBOTS).child(robotId).setValue(isOwned);
    }

    private DatabaseReference getRobots() {
        return mFirebase.child(ROBOTS);
    }

    /**
     * Returns a reference to the users robots
     * @return
     */
    public DatabaseReference getRobotListRef() {
        if(isLoggedIn()){
            return getRobots();
        }
        throw new UnsupportedOperationException();
    }

    public DatabaseReference getUserListRef() {
        if (isLoggedIn()){
            return mFirebase.child(USERS).child(mAuthData.getUid());
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Listen for completetion of user sign in
     * @param onAuthCompleteListener
     */
    public void registerOnAuthCompleteListener(OnAuthCompleteListener onAuthCompleteListener) {
        mOnAuthCompleteListeners.add(onAuthCompleteListener);
        if(isLoggedIn()){
            onAuthCompleteListener.onAuthComplete(mAuthData);
        }
    }

    /**
     * Stop listening to user sign ins
     * @param onAuthCompleteListener
     */
    public void unregisterOnAuthCompleteListener(OnAuthCompleteListener onAuthCompleteListener) {
        mOnAuthCompleteListeners.remove(onAuthCompleteListener);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if(key.equals(PocketBotSettings.KEY_ROBOT_ID)){
            setupRobot(sharedPreferences.getString(PocketBotSettings.KEY_ROBOT_ID, ""));
        }
    }

    public void deleteRobot(final String robot_id) {
        //Delete robot from user
        final DatabaseReference userRef = mFirebase.child(USERS).child(mAuthData.getUid());
        userRef.child(ROBOTS).child(robot_id).removeValue();
        //Delete robot
        getRobots().child(robot_id).removeValue();
        //Delete robot control
        mFirebase.child(RemoteControl.CONTROL).child(robot_id).removeValue();
        //Delete robot sensor data
        mFirebase.child(DataStore.SENSORS).child(robot_id).removeValue();
    }

    /**
     * Upload the sensor data to the cloud
     * @param sensorData
     */
    public void sendSensorData(final SensorData sensorData) {
        if(isLoggedIn()){
            mFirebase.child(RemoteControl.STATUS).child(mRobotId).child(SENSORS).setValue(sensorData);
        }
    }

    private boolean isLoggedIn() {
        return mAuthData != null && mRobotId != null;
    }

    public AuthData getAuthData() {
        return mAuthData;
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        mAuthData = null;
    }

    public interface OnAuthCompleteListener {
        void onAuthComplete(final AuthData authData);
    }

    /**
     * Syncs settings between Firebase and Sharedpreferences
     */
    private static class FirebasePreferenceSync implements SharedPreferences.OnSharedPreferenceChangeListener, ChildEventListener {
        private String mRobotId;
        private DatabaseReference mFirebase;
        private Context mContext;

        public FirebasePreferenceSync(final Context context) {
            this.mContext = context;
        }

        /**
         * Start syncing data
         * @param firebase
         */
        public void start(final DatabaseReference firebase){
            this.mFirebase = firebase;
            //Listen for data changes on selected robot
            mRobotId = PocketBotSettings.getRobotId(mContext);
            firebase.child(mRobotId).child(SETTINGS).child(PREFS).addChildEventListener(this);
            //Set inital preferences
            onSharedPreferenceChanged(PocketBotSettings.getSharedPrefs(mContext), PocketBotSettings.KEY_ROBOT_NAME);
            onSharedPreferenceChanged(PocketBotSettings.getSharedPrefs(mContext), PocketBotSettings.KEY_QB_ID);
            onSharedPreferenceChanged(PocketBotSettings.getSharedPrefs(mContext), PocketBotSettings.KEY_PASSWORD);
            mFirebase.child(mRobotId).child(SETTINGS).child(PREFS).child(PocketBotSettings.KEY_ROBOT_ID).setValue(mRobotId);
            //Register for preference changes
            PocketBotSettings.registerOnSharedPreferenceChangeListener(mContext, this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Update local RobotSettings object
            if(key.equals(PocketBotSettings.KEY_ROBOT_ID)){
                //Remove old event listener
                mFirebase.child(mRobotId).child(SETTINGS).child(PREFS).removeEventListener((ChildEventListener) this);
                //Get new Id
                mRobotId = sharedPreferences.getString(key, mRobotId);
                //Create new event listener
                mFirebase.child(mRobotId).child(SETTINGS).child(PREFS).addChildEventListener(this);
                return;
            }

            //Update setting to Firebase if robot id exist
            final String id = sharedPreferences.getString(PocketBotSettings.KEY_ROBOT_ID, "Error");
            if(id.equals("Error") == false){
                mFirebase.child(id).child(SETTINGS).child(PREFS).child(key).setValue(PocketBotSettings.getObject(sharedPreferences, key));
            }
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            snapshotToPreference(dataSnapshot);
        }

        private boolean snapshotToPreference(DataSnapshot dataSnapshot) {
            final Object value = dataSnapshot.getValue();
            final Class<?> valueClass = value.getClass();
            if(valueClass == String.class){
                String value1 = (String) value;
                //Check for blank string
                if(value1.equals("")){
                    return false;
                }
                return PocketBotSettings.getSharedPrefs(mContext).edit().putString(dataSnapshot.getKey(), value1).commit();
            } else if(valueClass == Boolean.class){
                return PocketBotSettings.getSharedPrefs(mContext).edit().putBoolean(dataSnapshot.getKey(), (Boolean) value).commit();
            } else if(valueClass == Long.class){
                return PocketBotSettings.getSharedPrefs(mContext).edit().putInt(dataSnapshot.getKey(), ((Long) value).intValue()).commit();
            } else {
                throw new UnsupportedOperationException("Unhandled class: " + valueClass.getSimpleName());
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            snapshotToPreference(dataSnapshot);
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

    }
}

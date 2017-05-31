package com.tesseractmobile.pocketbot.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.tesseractmobile.pocketbot.BuildConfig;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceFragmentFactory;
import com.tesseractmobile.pocketbot.robot.CommandContract;

import java.util.UUID;

/**
 * Created by josh on 9/10/2015.
 */
public class PocketBotSettings {

    //Keys
    public static final String KEY_SHOW_PREVIEW = CommandContract.PARAM_PREVIEW;
    public static final String KEY_SHOW_TEXT_PREVIEW = CommandContract.PARAM_TEXT_PREVIEW;
    public static final String KEY_USE_BLUETOOTH = CommandContract.PARAM_BLUETOOTH;
    public static final String KEY_SELECTED_FACE = "selected_face";
    public static final String KEY_FAST_TRACKING = "fast_tracking";
    public static final String KEY_BLUETOOTH_DEVICE = "bluetooth_device";
    public static final String KEY_ROBOT_ID = "r_id";
    public static final String KEY_ROBOT_NAME = "robot_name";
    public static final String KEY_QB_ID = "qb_id";
    public static final String KEY_API_AI_TOKEN = "api_ai_token";
    public static final String KEY_PASSWORD = "qb_password";
    public static final String KEY_LAST_ROBOT_ID = "user_id";
    public static final String KEY_API_AI_KEY = "api_ai_key";
    public static final String KEY_ALLOW_TELEPRESENCE = "allow_tele";
    private static final String KEY_AUTO_SIGN_IN = "auto_sign_in";
    public static final String KEY_KEEP_ALIVE = "keep_alive";
    //Defaults
    public static final boolean DEFAULT_SHOW_TEXT_PREVIEW = true;
    public static final boolean DEFAULT_SHOW_PREVIEW = true;
    public static final boolean DEFAULT_USE_BLUETOOTH = false;
    public static final int DEFAULT_FACE_ID = FaceFragmentFactory.ID_FACE_CONTROL;
    public static final String DEFAULT_API_AI_KEY = "1eca9ad4-74e8-4d3a-afea-7131df82d19b";//"1eca9ad4-74e8-4d3a-afea-7131df82d19b";
    public static final String DEFAULT_API_AI_TOKEN = "443dddf4747d4408b0e9451d4d53f201";//"443dddf4747d4408b0e9451d4d53f201";
    private static final boolean DEFAULT_AUTO_SIGN_IN = false;
    public static final boolean DEFAULT_KEEP_ALIVE = false;
    public static final String ROBOT_ID_NOT_SET = "NOT_SET";
    public static final String DEFAULT_ROBOT_NAME = "Robot-1";
    public static final String KEY_ROS_MASTER_URI = "ros_master_uri";
    private static final String KEY_STARTING_ACTIVITY_ID = "start_activity_id";


    /**
     * True if preview window should be shown
     * @param context
     * @return
     */
    static public boolean isShowPreview(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_SHOW_PREVIEW, DEFAULT_SHOW_PREVIEW);
    }

    /**
     * Set to true to show preview window
     * @param context
     * @param b
     * @return
     */
    static public boolean setShowPreview(final Context context, final boolean b){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_SHOW_PREVIEW, b).commit();
    }

    /**
     * True if preview window should be shown
     * @param context
     * @return
     */
    static public boolean isShowTextPreview(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_SHOW_TEXT_PREVIEW, DEFAULT_SHOW_TEXT_PREVIEW);
    }

    /**
     * Set to true to show preview window
     * @param context
     * @param b
     * @return
     */
    static public boolean setShowTextPreview(final Context context, final boolean b){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_SHOW_TEXT_PREVIEW, b).commit();
    }

    /**
     * True if preview window should be shown
     * @param context
     * @return
     */
    static public boolean isUseBluetooth(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_USE_BLUETOOTH, DEFAULT_USE_BLUETOOTH);
    }

    /**
     * Set to true to show preview window
     * @param context
     * @param b
     * @return
     */
    static public boolean setUseBluetooth(final Context context, final boolean b){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_USE_BLUETOOTH, b).commit();
    }

    /**
     *
     * @param context
     * @param faceId
     * @return
     */
    static public boolean setSelectedFace(final Context context, final int faceId){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_SELECTED_FACE, faceId).commit();
    }

    /**
     *
     * @param context
     * @return
     */
    static public int getSelectedFace(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_SELECTED_FACE, DEFAULT_FACE_ID);
    }

    public static boolean setPassword(final Context context, final String password) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_PASSWORD, password).commit();
    }

    /**
     * Returns save password or creates a new one if needed
     * @param context
     * @return
     */
    public static String getPassword(final Context context) {
        final String uuid = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PASSWORD, ROBOT_ID_NOT_SET);
        if(uuid.equals(ROBOT_ID_NOT_SET) || uuid.equals("")){
            //Set UUID
            final String newUUID = UUID.randomUUID().toString().replace("-", "");
            setPassword(context, newUUID);
            return newUUID;
        }
        return uuid;
    }

    /**
     * Listen for preference changes
     * @param context
     * @param onSharedPreferenceChangeListener
     */
    public static void registerOnSharedPreferenceChangeListener(final Context context, final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    /**
     * Stop listening for preference changes
     * @param context
     * @param onSharedPreferenceChangeListener
     */
    public static void unregisterOnSharedPreferenceChangeListener(final Context context, final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    /**
     * Return last user ID
     * @param context
     * @return
     */
    public static String getLastRobotId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LAST_ROBOT_ID, "");
    }

    /**
     * Save the UUID of the last robot connected
     * @param context
     * @param userId
     * @return
     */
    public static boolean setLastRobotId(Context context, String userId) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_LAST_ROBOT_ID, userId).commit();
    }

    public static String getRobotId(Context context) {
        final String uuid = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_ROBOT_ID, ROBOT_ID_NOT_SET);
        if(uuid.equals(ROBOT_ID_NOT_SET)){
            //Set UUID
            final String newUUID = UUID.randomUUID().toString();
            setRobotId(context, newUUID);
            return newUUID;
        }
        return uuid;
    }

    public static boolean setRobotName(Context context, String name) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_ROBOT_NAME, name).commit();
    }

    public static String getRobotName(Context context) {
        final String name = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_ROBOT_NAME, DEFAULT_ROBOT_NAME);
        return name;
    }

    public static boolean setRobotId(Context context, String userId) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_ROBOT_ID, userId).commit();
    }

    public static String getApiAiKey(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_API_AI_KEY, DEFAULT_API_AI_KEY);
    }

    public static boolean setApiAiKey(final Context context, final String key){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_API_AI_KEY, key).commit();
    }

    public static String getApiAiToken(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_API_AI_TOKEN, DEFAULT_API_AI_TOKEN);
    }

    public static boolean setApiAiToken(final Context context, final String token){
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_API_AI_TOKEN, token).commit();
    }

    public static boolean allowTelepresence(final Context context) {
        //Should be false by default in releases
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ALLOW_TELEPRESENCE, BuildConfig.DEBUG);
    }

    /**
     * Speeds up face tracking but turns off smile detection
     * @param context
     * @return
     */
    public static boolean getFastTrackingMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_FAST_TRACKING, true);
    }

    /**
     * Set the mac address of the connected bluetooth device
     * @param context
     * @param name
     * @return
     */
    public static boolean setBluetoothDevice(Context context, String name) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_BLUETOOTH_DEVICE, name).commit();
    }

    /**
     * Return the MAC address of the selected bluetooth device
     * @param context
     * @return "" if no device selected
     */
    public static String getBluetoothDevice(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_BLUETOOTH_DEVICE, "");
    }

    public static boolean setUseFastFaceTracking(Context context, boolean fastFaceTracking) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_FAST_TRACKING, fastFaceTracking).commit();
    }

    public static boolean setQuickBloxId(final Context context, int qbUserId) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_QB_ID, qbUserId).commit();
    }

    public static int getQuickBloxId(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_QB_ID, -1);
    }

    public static boolean isAutoSignIn(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_AUTO_SIGN_IN, DEFAULT_AUTO_SIGN_IN);
    }

    public static SharedPreferences getSharedPrefs(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean setAutoSignIn(final Context context, final boolean b) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_AUTO_SIGN_IN, b).commit();
    }

    public static boolean isKeepAlive(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_KEEP_ALIVE, DEFAULT_KEEP_ALIVE);
    }

    /**
     * Save the ROS master host
     * @param host
     */
    public static boolean setRosMasterUri(final Context context, final String host) {
        return getSharedPrefs(context).edit().putString(KEY_ROS_MASTER_URI, host).commit();
    }

    /**
     * Resturn ROS master Uri
     * @param context
     * @return
     */
    public static String getRosMasterUri(final Context context){
        //return null;
        return getSharedPrefs(context).getString(KEY_ROS_MASTER_URI, "");
    }

    /**
     * Get the id of the activity that should be launched at startup
     * @param context
     * @return
     */
    public static int getStartingActivityId(final Context context) {
        return getSharedPrefs(context).getInt(KEY_STARTING_ACTIVITY_ID, LauncherActivity.ACTIVITY_WIZARD);
    }

    /**
     * Set the id of the activity to launch on startup
     * @param context
     * @param startingActivityId
     * @return
     */
    public static boolean setStartingActvityId(final Context context, final int startingActivityId) {
        return getSharedPrefs(context).edit().putInt(KEY_STARTING_ACTIVITY_ID, startingActivityId).commit();
    }

    /**
     * Sets a setting by key
     * @param key
     * @param value
     */
    public static void setBooleanSetting(final Context context, final String key, final boolean value) {
        getSharedPrefs(context).edit().putBoolean(key, value).apply();
    }

    public static Object getObject(final SharedPreferences sharedPreferences, final String key) {
        if(key.equals(PocketBotSettings.KEY_SELECTED_FACE)){
            return sharedPreferences.getInt(key, 0);
        } else if (key.equals(PocketBotSettings.KEY_FAST_TRACKING)) {
            return sharedPreferences.getBoolean(key, false);
        } else if (key.equals(PocketBotSettings.KEY_SHOW_PREVIEW)) {
            return sharedPreferences.getBoolean(key, false);
        } else if (key.equals(PocketBotSettings.KEY_QB_ID)) {
            return sharedPreferences.getInt(key, -1);
        } else if (key.equals(PocketBotSettings.KEY_API_AI_KEY)) {
            return sharedPreferences.getString(key, "");
        } else if (key.equals(PocketBotSettings.KEY_ROS_MASTER_URI)) {
            return sharedPreferences.getString(key, "");
        } else if (key.equals(PocketBotSettings.KEY_API_AI_TOKEN)) {
            return sharedPreferences.getString(key, "");
        } else if (key.equals(PocketBotSettings.KEY_ROBOT_NAME)) {
            return sharedPreferences.getString(key, "");
        } else if (key.equals(PocketBotSettings.KEY_PASSWORD)) {
            return sharedPreferences.getString(key, "");
        } else if (key.equals(PocketBotSettings.KEY_SHOW_TEXT_PREVIEW)) {
            return sharedPreferences.getBoolean(PocketBotSettings.KEY_SHOW_TEXT_PREVIEW, true);
        } else if (key.equals(PocketBotSettings.KEY_USE_BLUETOOTH)) {
            return sharedPreferences.getBoolean(PocketBotSettings.KEY_USE_BLUETOOTH, true);
        } else if (key.equals(PocketBotSettings.KEY_ALLOW_TELEPRESENCE)) {
            return sharedPreferences.getBoolean(PocketBotSettings.KEY_ALLOW_TELEPRESENCE, true);
        } else if (key.equals(PocketBotSettings.KEY_LAST_ROBOT_ID)) {
            return sharedPreferences.getString(PocketBotSettings.KEY_LAST_ROBOT_ID, "");
        } else if (key.equals(PocketBotSettings.KEY_BLUETOOTH_DEVICE)) {
            return sharedPreferences.getString(PocketBotSettings.KEY_BLUETOOTH_DEVICE, "");
        } else if (key.equals(PocketBotSettings.KEY_ROBOT_ID)) {
            return sharedPreferences.getString(PocketBotSettings.KEY_ROBOT_ID, "");
        } else if (key.equals(PocketBotSettings.KEY_AUTO_SIGN_IN)) {
            return sharedPreferences.getBoolean(PocketBotSettings.KEY_AUTO_SIGN_IN, PocketBotSettings.DEFAULT_AUTO_SIGN_IN);
        } else if (key.equals(PocketBotSettings.KEY_KEEP_ALIVE)) {
            return sharedPreferences.getBoolean(PocketBotSettings.KEY_KEEP_ALIVE, PocketBotSettings.DEFAULT_KEEP_ALIVE);
        } else if (key.equals("App Restrictions")) {
            return sharedPreferences.getString(key, "");
        } else if (key.equals(KEY_STARTING_ACTIVITY_ID)) {
            return sharedPreferences.getInt(PocketBotSettings.KEY_STARTING_ACTIVITY_ID, LauncherActivity.ACTIVITY_WIZARD);
        } else {
            throw new UnsupportedOperationException("Unknown key: " + key);
        }
    }
}

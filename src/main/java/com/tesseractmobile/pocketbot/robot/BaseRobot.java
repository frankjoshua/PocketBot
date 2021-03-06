package com.tesseractmobile.pocketbot.robot;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.tesseractmobile.pocketbot.robot.model.SpeechState;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.robot.model.Face;
import com.tesseractmobile.pocketbot.robot.model.Speech;
import com.tesseractmobile.pocketbot.robot.model.Waypoint;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionState;

import java.util.ArrayList;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

/**
 * Created by josh on 11/16/2015.
 */
abstract public class BaseRobot implements RobotInterface, VoiceRecognitionListener, BodyConnectionListener {

    private static final int START_LISTENING = 1;
    private static final int START_LISTENING_AFTER_PROMPT = 2;

    private static final int TIME_BETWEEN_HUMAN_SPOTTING = 10000;
    public static final String TAG = "PocketBot";

    /** Updated when emotion changes */
    private final Subject<Emotion> mEmotion = BehaviorSubject.create();
    /** Updated when face changes */
    private final Subject<Face> mFaceSubject = BehaviorSubject.create();
    /** Updated when robot wants to speak */
    private final Subject<Speech> mSpeechSubject = BehaviorSubject.create();
    /** Updated when Speech state changes */
    private final Subject<SpeechState> mSpeechStateSubject = BehaviorSubject.create();
    /** Updated when robot location changes */
    private Subject<Location> mLocationSubject = BehaviorSubject.create();
    /** Updated when waypoint is added */
    private Subject<Waypoint> mWaypointSubject = ReplaySubject.create();
    /** Current speech state */
    private SpeechState mSpeechState = SpeechState.READY;

    private VoiceRecognitionService mVoiceRecognitionService;
    final private SensorData mSensorData = new SensorData();
    final private DataStore mDataStore;
    /** Updated when new sensor data is received */
    final private ArrayList<SensorListener> mSensorListeners = new ArrayList<>();

    protected BodyInterface mBodyInterface = new BodyInterface() {
        @Override
        public void sendObject(Object object) {
            sendJson(null);
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void sendJson(String json) {
            //Do nothing
            //say("I can't feel my wheels!");
        }

        @Override
        public void sendBytes(byte[] bytes) {

        }
    };

    private long mLastSensorTransmision;
    private int mSensorDelay = 0;
    private long mLastHumanSpoted;
    private int mHumanCount = 0;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_LISTENING) {
                setSpeechState(SpeechState.LISTENING);
                mVoiceRecognitionService.startListening();
            } else if (msg.what == START_LISTENING_AFTER_PROMPT) {
                startListening((String) msg.obj);
            }
        }
    };
    private boolean mIsNew;


    public BaseRobot(final DataStore dataStore){
        mDataStore = dataStore;
    }

    @Override
    public void setEmotion(Emotion emotion) {
        mEmotion.onNext(emotion);
    }

    public Subject<Emotion> getEmotion(){
        return mEmotion;
    }

    @Override
    public Subject<Face> getFaceSubject() {
        return mFaceSubject;
    }

    @Override
    public Subject<Speech> getSpeechSubject() {
        return mSpeechSubject;
    }

    @Override
    public Subject<SpeechState> getSpeechStateSubject() {
        return mSpeechStateSubject;
    }

    @Override
    public Subject<Location> getLocationSubject() {
        return mLocationSubject;
    }

    @Override
    public Subject<Waypoint> getWaypointSubject() {
        return mWaypointSubject;
    }

    @Override
    public void look(final Face face) {
        mFaceSubject.onNext(face);
        mSensorData.setFace(face.x, face.y, face.z);
        sendSensorData(false);
    }

    @Override
    public SensorData getSensorData() {
        return mSensorData;
    }

    @Override
    public void sendSensorData(final boolean required) {
        if(Constants.LOGGING){
            //Log.d(TAG,  "sendSensorData Required: " + required);
        }
        final long uptime = SystemClock.uptimeMillis();
        if(required || uptime >= mLastSensorTransmision + mSensorDelay) {
            if(Constants.LOGGING){
                Log.d(TAG, mSensorDelay + " Data sent to body " + (uptime - mLastSensorTransmision));
            }
            mLastSensorTransmision = uptime;
            if(mBodyInterface.isConnected()){
                final PocketBotProtocol.PocketBotMessage data = SensorData.toPocketBotMessage(mSensorData);
                //Send raw data
                if(Constants.LOGGING){
                    Log.d(TAG, mSensorData.getControl().toString());
                }
                mBodyInterface.sendBytes(data.toByteArray());

                mDataStore.sendSensorData(mSensorData);
            }
        } else {
            if(Constants.LOGGING){
                Log.e(TAG, mSensorDelay + " Data dropped - too fast: " + (uptime - mLastSensorTransmision));
            }
            System.gc();
        }
        //Update sensor data listeners
        synchronized (mSensorListeners) {
            final int size = mSensorListeners.size();
            for (int i = 0; i < size; i++) {
                mSensorListeners.get(i).onSensorUpdate(mSensorData);
            }
        }
    }

    @Override
    public void listen() {
        final Message msg = Message.obtain();
        msg.what = START_LISTENING;
        mHandler.sendMessage(msg);
    }

    @Override
    final public boolean say(String text) {
        mSpeechSubject.onNext(new Speech(text));
        mLastHumanSpoted = SystemClock.uptimeMillis();

        if (mSpeechState != SpeechState.READY) {
            //Log.d(TAG, "Could not speak \'" + text + "\', state is " + mSpeechState);
            return false;
        }
        setSpeechState(SpeechState.TALKING);
        return true;
    }


    @Override
    public void humanSpotted(int id) {

        final long uptimeMillis = SystemClock.uptimeMillis();
        if(id == SensorData.NO_FACE){
            mHumanCount--;
            //if(mHumanCount == 0){
                mSensorData.setFace(id);
                if (uptimeMillis - mLastHumanSpoted > TIME_BETWEEN_HUMAN_SPOTTING) {
                    onHumanLeft();
                }
                //Set face id to NO_FACE only if no humans are present
                mSensorData.setFace(id);
                sendSensorData(true);
            //}
            return;
        }
        mHumanCount++;
        //Set face id to known human
        mSensorData.setFace(id);
        //Check if no human has been spotted for 10 seconds
        if (uptimeMillis - mLastHumanSpoted > TIME_BETWEEN_HUMAN_SPOTTING) {
            onHumanSpoted();
            sendSensorData(true);
        }
        mLastHumanSpoted = uptimeMillis;
    }

    @Override
    public void onSpeechComplete() {
        if (mSpeechState == SpeechState.WAITING_TO_LISTEN) {
            mHandler.sendEmptyMessage(START_LISTENING);
        } else {
            setSpeechState(SpeechState.READY);
        }
    }

    /**
     * Must be run on the UI thread
     *
     * @param prompt
     */
    private void startListening(final String prompt) {
        //setEmotion(Emotion.SURPRISED);
        if (prompt != null) {
            if (say(prompt)) {
                setSpeechState(SpeechState.WAITING_TO_LISTEN);
            }
        } else {
            //Call service here
            listen();
        }

    }

    @Override
    public VoiceRecognitionListener getVoiceRecognitionListener() {
        return this;
    }

    @Override
    public void onVoiceRecognitionStateChange(VoiceRecognitionState state) {
        //Any state change is not listening
        if (state == VoiceRecognitionState.READY) {
            onSpeechComplete();
        }
    }

    @Override
    public void onVoiceRecognitionError(String text) {
        say(text);
    }

    @Override
    public void onBluetoothDeviceFound() {

    }

    @Override
    public void onError(int i, String error) {
        say(error);
    }

    @Override
    public void onBodyConnected(BodyInterface bodyInterface) {
        this.mBodyInterface = bodyInterface;
        say("Body interface established");
    }

    @Override
    public void onRobotEvent(final RobotEvent robotEvent) {
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                switch (robotEvent.getEventType()) {
                    case ERROR:
                        say(robotEvent.getMessage());
                        break;
                    case DISCONNECT:
                        say("Please don't shut me off. I was just learning to. Love. ");
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                throw new UnsupportedOperationException();
                            }
                        }, 4000);
                        break;
                }
            }
        };
        mHandler.post(runnable);
    }

    private void onHumanLeft() {
        say("Goodbye");
    }

    private void onHumanSpoted() {
        onTextInput("hello");
    }

    @Override
    public void setSensorDelay(int delay) {
        mSensorDelay = delay;
    }

    @Override
    public void listen(String prompt) {
        mLastHumanSpoted = SystemClock.uptimeMillis();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            startListening(prompt);
        } else {
            final Message msg = Message.obtain();
            msg.obj = prompt;
            msg.what = START_LISTENING_AFTER_PROMPT;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public BodyConnectionListener getBodyConnectionListener() {
        return this;
    }

    @Override
    public void setVoiceRecognitionService(VoiceRecognitionService voiceRecognitionService) {
        mVoiceRecognitionService = voiceRecognitionService;
    }

    private synchronized void setSpeechState(SpeechState speechState) {
        this.mSpeechState = speechState;
        mSpeechStateSubject.onNext(speechState);
    }

    @Override
    public void setAuthToken(String robotId, AuthData authData) {
        mDataStore.setAuthToken(robotId, authData);
    }

    @Override
    public void signOut() {
        mDataStore.signOut();
    }

    @Override
    public void registerOnAuthCompleteListener(DataStore.OnAuthCompleteListener onAuthCompleteListener) {
        mDataStore.registerOnAuthCompleteListener(onAuthCompleteListener);
    }

    @Override
    public void unregisterOnAuthCompleteListener(DataStore.OnAuthCompleteListener onAuthCompleteListener) {
        mDataStore.unregisterOnAuthCompleteListener(onAuthCompleteListener);
    }

    @Override
    public void deleteRobot(String robotId) {
        mDataStore.deleteRobot(robotId);
    }

    @Override
    public DataStore getDataStore() {
        return mDataStore;
    }

    @Override
    public boolean isNew() {
        return mIsNew;
    }

    @Override
    public void setIsNew(boolean isNew) {
        mIsNew = isNew;
    }

    /**
     * Listen for sensor updates
     * @param sensorListener
     */
    @Override
    public void registerSensorListener(final SensorListener sensorListener){
        synchronized (mSensorListeners){
            mSensorListeners.add(sensorListener);
        }
    }

    /**
     * Stop listening for senspr updates
     * @param sensorListener
     */
    @Override
    public void unregisterSensorListener(final SensorListener sensorListener){
        synchronized (mSensorListeners){
            mSensorListeners.remove(sensorListener);
        }
    }

    public interface SensorListener {
        /**
         * Called when new sensor data received
         * @param sensorData
         */
        void onSensorUpdate(SensorData sensorData);
    }
}

package com.tesseractmobile.pocketbot.robot.faces;

import com.tesseractmobile.pocketbot.activities.SpeechState;
import com.tesseractmobile.pocketbot.robot.AI;
import com.tesseractmobile.pocketbot.robot.AuthData;
import com.tesseractmobile.pocketbot.robot.BaseRobot;
import com.tesseractmobile.pocketbot.robot.BodyConnectionListener;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.SpeechListener;
import com.tesseractmobile.pocketbot.robot.VoiceRecognitionService;
import com.tesseractmobile.pocketbot.robot.model.Face;
import com.tesseractmobile.pocketbot.robot.model.Speech;
import com.tesseractmobile.pocketbot.service.VoiceRecognitionListener;

import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by josh on 10/17/2015.
 */
public interface RobotInterface {
    /**
     * Start voice recognition
     */
    void listen();

    /**
     * Speek the text then listen for the response
     * @param prompt
     */
    void listen(final String prompt);

    /**
     * Speak the text
     * @param text
     */
    boolean say(final String text);

    /**
     *
     * @param id of the face SensorData.NO_FACE if face was lost
     */
    void humanSpotted(int id);

    /**
     * @return reference to the sensor data
     */
    SensorData getSensorData();

    /**
     * Call after changing sensor data
     * @param required false if data can be dropped
     */
    void sendSensorData(final boolean required);

    /**
     * Set the current emotional state
     * @param emotion
     */
    void setEmotion(final Emotion emotion);

    /**
     * Get a stream of emotional states
     * @return
     */
    BehaviorSubject<Emotion> getEmotion();

    /**
     * Face states
     * @return
     */
    BehaviorSubject<Face> getFaceSubject();

    VoiceRecognitionListener getVoiceRecognitionListener();

    void setSensorDelay(int delay);

    void setAI(final AI ai);

    BodyConnectionListener getBodyConnectionListener();

    void setVoiceRecognitionService(VoiceRecognitionService voiceRecognitionService);

    void registerSpeechListener(final SpeechListener speechListener);

    void unregisterSpeechListener(final SpeechListener speechListener);

    void registerSensorListener(final BaseRobot.SensorListener sensorListener);

    void unregisterSensorListener(final BaseRobot.SensorListener sensorListener);

    void setAuthToken(String robotId, AuthData authData);

    void registerOnAuthCompleteListener(final DataStore.OnAuthCompleteListener onAuthCompleteListener);

    void unregisterOnAuthCompleteListener(final DataStore.OnAuthCompleteListener onAuthCompleteListener);

    void deleteRobot(final String robotId);

    DataStore getDataStore();

    void setIsNew(boolean isNew);

    boolean isNew();

    /**
     * Informs the robot that has been spotted
     * x and y = 0.0 to 2.0,  1.0 is center
     * @param face
     */
    void look(Face face);


    BehaviorSubject<Speech> getSpeechSubject();

    BehaviorSubject<SpeechState> getSpeechStateSubject();

    void onSpeechComplete();
}

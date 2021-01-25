package com.tesseractmobile.pocketbot.robot.faces;

import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.model.Face;
import com.tesseractmobile.pocketbot.robot.model.Speech;
import com.tesseractmobile.pocketbot.views.MouthView;

/**
 * Created by josh on 10/17/2015.
 */
public interface RobotFace {
    /**
     * Display this emotion
     * @param emotion
     */
    void setEmotion(Emotion emotion);

    /**
     * Direction to look in
     * @param face
     */
    void look(Face face);

    /**
     * Speak the text
     * @param text
     */
    void say(Speech text);

    void setRobotInterface(final RobotInterface robotInterface);

    void onDestroy();
}

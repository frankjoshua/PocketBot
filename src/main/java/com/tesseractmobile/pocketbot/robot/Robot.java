package com.tesseractmobile.pocketbot.robot;

import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

/**
 * Created by josh on 11/16/2015.
 */
public class Robot extends AIRobot {

    static private RobotInterface mRobot;

    private Robot(final DataStore dataStore){
        super(dataStore);
    };

    static public void init(final DataStore dataStore){
        if(mRobot == null){
            mRobot = new Robot(dataStore);
        }
    }

    static public RobotInterface get(){
        return mRobot;
    }


}

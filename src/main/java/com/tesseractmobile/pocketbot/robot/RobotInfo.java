package com.tesseractmobile.pocketbot.robot;



/**
 * Created by josh on 12/28/2015.
 */

public class RobotInfo {
    public Settings settings;


    public static class Settings{
        public boolean isConnected;
        public Prefs prefs;


        public static class Prefs{
            public int qb_id;
            public String robot_name;
            public String r_id;
        }
    }
}

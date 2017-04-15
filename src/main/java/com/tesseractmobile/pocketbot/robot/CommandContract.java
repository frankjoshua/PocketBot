package com.tesseractmobile.pocketbot.robot;

/**
 * Hold static variables that match the TargetRegistration.h
 * 
 * @author josh
 * 
 */
public class CommandContract {

    //Data protocol
    public static final int START_BYTE = 255;
    public static final int STOP_BYTE = START_BYTE;

    // Targets
    public final static byte TAR_SERVO_PAN           = (byte) 0xa;    // 10
    public final static byte TAR_SERVO_TILT          = (byte) 0xb;    // 11
    public static final byte TARGET_PING_CENTER      = (byte) 0x32;    // 50
    public static final byte TARGET_PING_LEFT        = (byte) 0x33;    // 51
    public static final byte TARGET_PING_RIGHT       = (byte) 0x34;    // 52
    public static final byte TARGET_PING_BACK        = (byte) 0x34;    // 53
    public static final byte TAR_MOTOR_LEFT          = (byte) 0x14;   // 20
    public static final byte TAR_MOTOR_RIGHT         = (byte) 0x15;   // 21
    public static final byte TARGET_GPS              = (byte) 0x5A;    // 90

    // Commands
    public static final byte CMD_PAUSE               = Byte.MIN_VALUE; //Not part of TargetRegistration.h
    public static final byte CMD_FORWARD             = (byte) 0xa;    // 10
    public static final byte CMD_BACKWARD            = (byte) 0xb;    // 11
    public static final byte CMD_LEFT                = (byte) 0xc;    // 12
    public static final byte CMD_RIGHT               = (byte) 0xd;    // 13
    public static final byte COMMAND_SET_WAYPOINT    = (byte) 0x15;   // 21
    public static final byte COMMAND_REMOVE_WAYPOINT = (byte) 0x16;   // 22
    public static final byte COMMAND_NEXT_WAYPOINT   = (byte) 0x17;   // 23
    public static final byte COMMAND_LAST_WAYPOINT   = (byte) 0x18;   // 24
    public static final byte COMMAND_GPS_LOCATION    = (byte) 0x5B;   // 91
    public static final byte COMMAND_GPS_HEADING     = (byte) 0x5C;   // 92
    public static final byte COMMAND_GPS_COMPASS     = (byte) 0x5D;   // 92

    //Actions from api.ai
    public static final String ACTION_MOVE = "move";
    public static final String ACTION_FLASH = "flash";
    public static final String ACTION_EMOTION = "emotion";
    public static final String ACTION_ARDUINO = "arduino";
    public static final String ACTION_SETTINGS = "settings";
    public static final String ACTION_LAUNCH = "launch";

    //Parameters from api.ai
    public static final String PARAM_DIRECTION = "direction";
    public static final String PARAM_MEASUREMENT = "measurement";
    public static final String PARAM_DISTANCE = "distance";
    public static final String PARAM_EMOTION = "emotion";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_TEXT_PREVIEW = "text_preview";
    public static final String PARAM_BLUETOOTH = "bluetooth";
    public static final String PARAM_PACKAGE = "package";

    //Emotion values from api.ai
    public static final String EMOTION_ANGER = "anger";
    public static final String EMOTION_JOY = "joy";
    public static final String EMOTION_FEAR = "fear";
    public static final String EMOTION_SURPRISED = "surprised";
    public static final String EMOTION_ACCEPTED = "accepted";
    public static final String EMOTION_AWARE = "aware";

}

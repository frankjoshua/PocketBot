package com.tesseractmobile.pocketbot.robot;

public class RobotEvent {

    public enum RobotEventType {
        ERROR, DISCONNECT
    }

    private String message;
    private RobotEventType eventType;

    public RobotEvent setMessage(final String message) {
        this.message = message;
        return this;
    }

    public RobotEvent setEventType(final RobotEventType eventType) {
        this.eventType = eventType;
        return this;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the eventType
     */
    public RobotEventType getEventType() {
        return eventType;
    }

    public static RobotEvent createDisconectEvent() {
        final RobotEvent robotEvent = new RobotEvent()
        .setEventType(RobotEventType.DISCONNECT);
        return robotEvent;
    }

    public static RobotEvent createErrorEvent(final String string) {
        final RobotEvent robotEvent = new RobotEvent()
        .setEventType(RobotEventType.ERROR)
        .setMessage(string);
        return robotEvent;
    }

   
}

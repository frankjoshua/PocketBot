package com.tesseractmobile.pocketbot.robot;

/**
 * Created by josh on 8/29/2015.
 */
public interface BodyInterface {

    /*
    * Converts an object to JSON then sends to the robot body
     */
    void sendObject(final Object object);

    /**
     *
     * @return true is body is connected
     */
    boolean isConnected();

    /**
     * Send JSON
     * @param json
     */
    void sendJson(String json);

    /**
     * Send bytes directly
     * @param bytes
     */
    void sendBytes(byte[] bytes);
}

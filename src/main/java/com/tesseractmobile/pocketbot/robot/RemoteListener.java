package com.tesseractmobile.pocketbot.robot;

/**
 * Created by josh on 12/1/2015.
 */
public interface RemoteListener {
    /**
     * Called when a remote message is received
     * Normally JSON
     * @param message
     */
    void onMessageReceived(Object message);

    /**
     * Called when connection is dropped
     * Possibly because of loss of internet
     */
    void onConnectionLost();

}

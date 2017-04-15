package com.tesseractmobile.pocketbot.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.tesseractmobile.pocketbot.robot.BodyConnectionListener;
import com.tesseractmobile.pocketbot.robot.BodyInterface;
import com.tesseractmobile.pocketbot.robot.RobotEvent;

/**
 * Created by josh on 9/4/2015.
 */
abstract public class BodyService extends Service implements BodyInterface {

    private final LocalBinder             binder                = new LocalBinder();
    private BodyConnectionListener mBodyConnectionListener;
    private boolean mBodyReady = false;

    /**
     * Listen for body connection events
     * @param bodyConnectionListener
     */
    public void registerBodyConnectionListener(final BodyConnectionListener bodyConnectionListener){
        this.mBodyConnectionListener = bodyConnectionListener;
        bodyListenerRegistered();
        if (mBodyReady) {
            mBodyConnectionListener.onBodyConnected(this);
        }
    }

    protected abstract void bodyListenerRegistered();

    /**
     * Stop listening for body connection events
     * @param bodyConnectionListener
     */
    public void unregisterBodyConnectionListener(final BodyConnectionListener bodyConnectionListener){
        this.mBodyConnectionListener = null;
    }

    protected void error(final int errorCode, final String errorMessage){
        final BodyConnectionListener bodyConnectionListener = this.mBodyConnectionListener;
        if(bodyConnectionListener != null){
            bodyConnectionListener.onError(errorCode, errorMessage);
        }
    }

    /**
     * Call when body is ready for connections
     */
    protected void bodyReady(){
        mBodyReady = true;
        final BodyConnectionListener bodyConnectionListener = this.mBodyConnectionListener;
        if(bodyConnectionListener != null){
            bodyConnectionListener.onBodyConnected(this);
        }
    }

    @Override
    public boolean isConnected() {
        return mBodyReady;
    }

    protected void robotEvent(final RobotEvent robotEvent){
        final BodyConnectionListener bodyConnectionListener = this.mBodyConnectionListener;
        if(bodyConnectionListener != null){
            bodyConnectionListener.onRobotEvent(robotEvent);
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public BodyService getService() {
            return BodyService.this;
        }
    }
}

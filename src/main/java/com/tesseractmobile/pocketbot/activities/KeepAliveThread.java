package com.tesseractmobile.pocketbot.activities;

import android.util.Log;

import com.tesseractmobile.pocketbot.robot.Constants;
import com.tesseractmobile.pocketbot.robot.RemoteControl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by josh on 1/16/2016.
 */
public class KeepAliveThread extends Thread{

    final private KeepAliveListener mKeepAliveListener;
    final private InternetAliveListener mInternetAliveListener;
    final private AtomicBoolean mRunning = new AtomicBoolean(false);
    private Thread mNetThread;

    public KeepAliveThread(final KeepAliveListener keepAliveListener, final InternetAliveListener internetAliveListener) {
        super("KeepAliveThread");
        if(keepAliveListener == null){
            throw new UnsupportedOperationException();
        }
        mKeepAliveListener = keepAliveListener;
        mInternetAliveListener = internetAliveListener;
        if(mInternetAliveListener != null) {
            mNetThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mRunning.get()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Keep Arduino awake
                        if (mRunning.get()) {
                            final long lag = RemoteControl.get().getLag();
                            if (Constants.LOGGING) {
                                Log.d(getName(), "Lag: " + lag);
                            }
                            if (lag > 500 || executeCommand() == false) {
                                mInternetAliveListener.onInternetTimeout();
                                Log.e(getName(), "Connection Lost, sending stop command!");
                            }
                        }
                    }
                }
            });
        }

    }

    @Override
    public void run() {
        while(mRunning.get()){
            //Keep Arduino awake
            mKeepAliveListener.onHeartBeat();
            if (Constants.LOGGING) {
                Log.d(getName(), "Triggering a sensor send");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Pings internet and return true if sucessful
     * Also false if error
     * @return
     */
    private boolean executeCommand(){
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();
            mIpAddrProcess.destroy();
            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            Log.d(getName(), " Exception:" + ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e(getName(), " Exception:" + e);
        }
        return false;
    }

    public void startThread() {
        Log.d(getName(), "Starting KeepAliveThread");
        mRunning.set(true);
        start();
        if(mNetThread != null){
            mNetThread.start();
        }
    }

    public void stopThread() {
        Log.d(getName(), "Stopping KeepAliveThread");
        mRunning.set(false);
    }

    public interface KeepAliveListener {
        /**
         * Send on a regular basis to wake up robot
         */
        void onHeartBeat();
    }

    public interface InternetAliveListener {
        /**
         * Called if internet ping times out
         */
        void onInternetTimeout();
    }
}

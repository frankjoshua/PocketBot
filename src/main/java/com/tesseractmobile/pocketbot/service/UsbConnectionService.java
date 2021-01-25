package com.tesseractmobile.pocketbot.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import com.google.gson.Gson;
import com.tesseractmobile.pocketbot.robot.BodyInterface;
import com.tesseractmobile.pocketbot.robot.RobotEvent;
import com.tesseractmobile.pocketbot.robot.SensorData;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class UsbConnectionService extends BodyService implements Runnable, BodyInterface{


    private static final String     ACTION_USB_PERMISSION = "com.tesseractmobile.pocketbot.action.USB_PERMISSION";
    private static final Gson GSON = new Gson();
    private UsbManager              mUsbManager;

    private PendingIntent           mPermissionIntent;

    private boolean                 mPermissionRequestPending;

    private ParcelFileDescriptor    mFileDescriptor;

    private UsbAccessory            mAccessory;

    private FileInputStream         mInputStream;

    private FileOutputStream        mOutputStream;

    private final BroadcastReceiver usbReciever           = new BroadcastReceiver() {

                                                              @Override
                                                              public void onReceive(final Context context, final Intent intent) {
                                                                  final String action = intent.getAction();
                                                                  //log(action);
                                                                  if (ACTION_USB_PERMISSION.equals(action)) {
                                                                      final UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

                                                                      if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                                                          if(accessory != null){
                                                                              //call method to set up accessory communication
                                                                              error(0, "Accessor null");
                                                                          }
                                                                      } else {
                                                                          error(0, "Permission denied");
                                                                      }
                                                                  } else {
                                                                      robotEvent(RobotEvent.createDisconectEvent());
                                                                  }
                                                              }

                                                          };



    private final LinkedList<Command> commandQueue = new LinkedList<Command>();
    private final AtomicBoolean run = new AtomicBoolean(true);
    private long lastCommandTime;

    @Override
    public void sendObject(Object data) {
        final Gson gson = GSON;
        final String s = gson.toJson(data);
        sendJson(s);
    }

    @Override
    public void sendJson(final String json){
        final Command command = new Command();
        command.jsonBytes = json.getBytes(Charset.forName("UTF-8"));
        synchronized (commandQueue){
            commandQueue.add(command);
        }
    }

    @Override
    public void sendBytes(byte[] bytes) {
        final Command command = new Command();
        command.jsonBytes = bytes;
        synchronized (commandQueue){
            commandQueue.add(command);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

        registerReceiver(usbReciever, filter);

    }

    @Override
    public void onDestroy() {
        closeAccessory();
        unregisterReceiver(usbReciever);
        run.set(false);
        super.onDestroy();
    }

    private void closeAccessory() {

        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (final IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }
    
    /**
     * 
     */
    private void connectAccessory() {
        final UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        final UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (mUsbManager) {
                    if (!mPermissionRequestPending) {
                        error(0, "Request Permission");
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            error(0, "Accessory == null");
        }
    }

    private void openAccessory(final UsbAccessory accessory) {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            final FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            startThread();
            bodyReady();
            //error(0, "All systems online.");
        } else {
            final String name = accessory != null ? accessory.toString() : "null";
            error(0, "accessory open fail: " + name);
        }
    }

    /**
     * 
     */
    protected void startThread() {
        lastCommandTime = System.currentTimeMillis();
        final Thread thread = new Thread(null, this, "UsbSerialService");
        thread.start();
    }

    private boolean sendCommand(byte[] buffer) {
        if (mOutputStream != null) {
            try {
                final byte[] message = SensorData.wrapData(buffer);
                //Write message
                for(int i = 0; i < message.length; i++){
                    mOutputStream.write(message[i]);
                }
                return true;
            } catch (final IOException e) {
                error(0, "write failed:" + e.getMessage());
            }
        } else {

            //Try to open Accessory
            if(mAccessory != null){
                error(0, "Send Command Failed: mOutputStream is null");
                openAccessory(mAccessory);
            } else {
                error(0, "Send Command Failed: mAccessory is null");
                connectAccessory();
            }
        }
        return false;
    }

    @Override
    public void run() {
        while (run.get()) {
            final long currentTimeMillis = System.currentTimeMillis();
            final long timeElapsed = currentTimeMillis - lastCommandTime;
            lastCommandTime = currentTimeMillis;
            //Read input Stream and Clear
//            final InputStream inputStream = mInputStream;
//            if (inputStream != null) {
//                final byte[] buffer = new byte[16384];
//                int ret = 0;
//                //log("Clearing Input Stream");
//                if (ret >= 0) {
//                    try {
//                        //Clear Buffer      
//                        ret = inputStream.read(buffer);
//                        //log("Clearing Input Stream: " + ret);
//                    } catch (final IOException e1) {
//                        ret = -1;
//                    }
//                }
//            }
            
            synchronized (commandQueue) {
                if (commandQueue.isEmpty() == false) {
                    final Command command = commandQueue.poll();
                    sendCommand(command.jsonBytes);
                }
            }
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        error(0, "Thread Exit");
    }

    @Override
    protected void bodyListenerRegistered() {
        connectAccessory();
    }


    public class Command {

        public byte[] jsonBytes;

        public Command(final byte[] jsonBytes){
            this.jsonBytes = jsonBytes;
        }
        
        public Command() {
            
        }
        
    }
}

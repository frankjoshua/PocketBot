package com.tesseractmobile.pocketbot.robot.faces;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.StatusListener;
import com.tesseractmobile.pocketbot.robot.model.Face;
import com.tesseractmobile.pocketbot.robot.model.Speech;
import com.tesseractmobile.pocketbot.views.JoystickView;
import com.tesseractmobile.pocketbot.views.MouthView;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosFragmentActivity;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.CameraControlLayer;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.text.NumberFormat;

/**
 * Created by josh on 10/25/2015.
 */
public class ControlFace extends BaseFace implements JoystickView.JoystickListener, StatusListener, RosFragmentActivity.NodeInitListener {

    private static final String MAP_FRAME = "map";
    private static final String ROBOT_FRAME = "base_link";

    /** remote message delay in millis */
    public static final int REMOTE_MAX_TRANSMIT_SPEED = 100;

    private final TextView mInputTextView;
    private final VisualizationView mVisualizationView;
    private final CameraControlLayer mCameraControlLayer;
    private NumberFormat numberFormat = NumberFormat.getNumberInstance();
    //Channel that the remote robot is listening on
    private String mRemoteRobotId;
    private long mLastUpdate = SystemClock.uptimeMillis();
    private String inputText;

    final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ControlFace.this.handleMessage(msg);
        }
    };

    private TextView mFaceData;
    private SensorData.Joystick mJoy1 = new SensorData.Joystick();
    private SensorData.Joystick mJoy2 = new SensorData.Joystick();

    public ControlFace(final View view, final RosFragmentActivity rosFragmentActivity, final RobotInterface robotInterface){
        setRobotInterface(robotInterface);
        numberFormat.setMinimumFractionDigits(2);
        mFaceData = (TextView) view.findViewById(R.id.tvFaceData);
        //Setup Joysticks
        ((JoystickView) view.findViewById(R.id.joyStick)).setJoystickListener(this);
        ((JoystickView) view.findViewById(R.id.joyStickLeft)).setJoystickListener(this);
        mInputTextView = (TextView) view.findViewById(R.id.tvInput);
        //Setup ROS Visualization view
        mVisualizationView = (VisualizationView) view.findViewById(R.id.visualization);
        mCameraControlLayer = new CameraControlLayer();
        mVisualizationView.onCreate(Lists.<Layer>newArrayList(mCameraControlLayer,
                new OccupancyGridLayer("map"), new LaserScanLayer("scan"), new RobotLayer(ROBOT_FRAME)));
        rosFragmentActivity.registerNodeInitListener(this);
        rosFragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVisualizationView.getCamera().jumpToFrame(ROBOT_FRAME);
            }
        });
    }

    private void handleMessage(Message msg) {
        final String data =  "H: " + mJoy2.heading + " X: " + numberFormat.format(mJoy1.X) + " Y: " + numberFormat.format(mJoy1.Y) + " A: " + mJoy1.A + " B: " + mJoy1.B;
        mFaceData.setText(data);
        if(inputText != null){
            mInputTextView.setText(inputText);
            mRobotInterface.onSpeechComplete();
        }
    }

    @Override
    public void setEmotion(Emotion emotion) {

    }

    @Override
    public void look(Face face) {

    }

    @Override
    public void say(final Speech speech) {
        inputText = speech.text;
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void onPositionChange(final JoystickView joystickView, float x, float y, float z, final boolean a, final boolean b) {
        final SensorData sensorData = mRobotInterface.getSensorData();

        if(joystickView.getId() == R.id.joyStick) {
            final int heading = headingFromPosition(x, y);
            sensorData.setJoystick1(x, y, z, a, b, heading);
            //Valus from sensor data are rounded so use those
            mJoy1.X = sensorData.getControl().joy1.X;
            mJoy1.Y = sensorData.getControl().joy1.Y;
            mJoy1.Z = sensorData.getControl().joy1.Z;
            mJoy1.A = sensorData.getControl().joy1.A;
            mJoy1.B = sensorData.getControl().joy1.B;
            mJoy1.heading = sensorData.getControl().joy1.heading;
        }

        if(joystickView.getId() == R.id.joyStickLeft) {
            final int heading = headingFromPosition(x, y);
            sensorData.setJoystick2(x, y, z, a, b, heading);
            //Valus from sensor data are rounded so use those
            mJoy2.X = sensorData.getControl().joy2.X;
            mJoy2.Y = sensorData.getControl().joy2.Y;
            mJoy2.Z = sensorData.getControl().joy2.Z;
            mJoy2.A = sensorData.getControl().joy2.A;
            mJoy2.B = sensorData.getControl().joy2.B;
            mJoy2.heading = sensorData.getControl().joy2.heading;
        }

        mRobotInterface.sendSensorData(false);
        updateRemote(false);
        mHandler.sendEmptyMessage(0);
    }

    private int headingFromPosition(float x, float y) {
        final double degrees;
        if(x != 0 && y != 0) {
            final double heading = Math.atan2(x, y) * 57;
            degrees = heading < 0 ? heading + 360 : heading;
        } else {
            degrees = 0;
        }
        return (int) Math.round(degrees);
    }

    /**
     * Send control data to remote robot
     * @param force true if data must be sent
     */
    private void updateRemote(boolean force) {
        if(force || SystemClock.uptimeMillis() - mLastUpdate > REMOTE_MAX_TRANSMIT_SPEED){
            //Make sure channel is set
            String channel = this.mRemoteRobotId;
            if(channel != null){
                //Mark the time
                mLastUpdate = SystemClock.uptimeMillis();
                //Send to the remote robot
                final SensorData sensorData = mRobotInterface.getSensorData();
                RemoteControl.get().send(sensorData.getControl());
            }
        }
    }

    @Override
    public void onFocusChange(final JoystickView joystickView, boolean hasFocus) {
        //Send message when user lets go of controls
        if(joystickView.getId() == R.id.joyStick && hasFocus == false) {
            final SensorData sensorData = mRobotInterface.getSensorData();
            sensorData.setJoystick1(0, 0, 0);
            mRobotInterface.sendSensorData(true);
            mJoy1.X = 0;
            mJoy1.Y = 0;
            mJoy1.Z = 0;
            mHandler.sendEmptyMessage(0);
            updateRemote(true);
        }
        if(joystickView.getId() == R.id.joyStickLeft && hasFocus == false) {
            final SensorData sensorData = mRobotInterface.getSensorData();
            sensorData.setJoystick2(0, 0, 0);
            mRobotInterface.sendSensorData(true);
            mJoy2.X = 0;
            mJoy2.Y = 0;
            mJoy2.Z = 0;
            mHandler.sendEmptyMessage(0);
            updateRemote(true);
        }
    }

    /**
     * Set the UUID of the remote robot to contorl
     * @param uuid null to disconnect
     */
    public void controlRemoteRobot(final String uuid){
        mRemoteRobotId = uuid;
        //Set disconnect command
        if(uuid != null){
            RemoteControl.get().getControlRef().child(uuid).child(RemoteControl.CONTROL).child(RemoteControl.DATA).child("joy1").onDisconnect().setValue(new SensorData.Joystick());
            RemoteControl.get().connect(uuid, this);
        } else {
            //Disconnect on null channel
            RemoteControl.get().disconnect();
        }
    }

    @Override
    public void onRemoteSensorUpdate(final SensorData sensorData) {
        //Received sensor data from remote robot
        final String text = "Heading: " + Integer.toString(sensorData.getSensor().heading) + " Battery " + Integer.toString(sensorData.getSensor().battery) + "%\n"
                + "JoyX: " + numberFormat.format(sensorData.getControl().joy1.X) + " JoyY: " + numberFormat.format(sensorData.getControl().joy1.Y);
        final Speech speech = new Speech(text);
        say(speech);
    }

    @Override
    public void onControlReceived(SensorData.Control message) {
        super.onControlReceived(message);

    }

    @Override
    public void onNodeInit(final NodeMainExecutor nodeMainExecutor, final URI masterUri) {

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                mVisualizationView.init(nodeMainExecutor);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                NodeConfiguration nodeConfiguration =
                        NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                                masterUri);
//                NtpTimeProvider ntpTimeProvider =
//                        new NtpTimeProvider(InetAddressFactory.newFromHostString("pool.ntp.org"),
//                                nodeMainExecutor.getScheduledExecutorService());
//                ntpTimeProvider.startPeriodicUpdates(1, TimeUnit.MINUTES);
//                nodeConfiguration.setTimeProvider(ntpTimeProvider);
                nodeMainExecutor.execute(mVisualizationView, nodeConfiguration);
                return null;
            }
        }.execute();

    }
}

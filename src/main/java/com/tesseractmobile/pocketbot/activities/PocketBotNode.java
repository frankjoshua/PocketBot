package com.tesseractmobile.pocketbot.activities;

import android.os.SystemClock;

import java.net.URI;

import com.tesseractmobile.pocketbot.robot.BaseRobot;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.SpeechListener;

import org.ros.address.InetAddressFactory;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

import geometry_msgs.Twist;
import sensor_msgs.Imu;
import std_msgs.String;


/**
 * Created by josh on 8/1/16.
 *
 * Creates nodes the publish information received by sensor data to ROS
 */
public class PocketBotNode implements NodeMain {

    public PocketBotNode(final NodeMainExecutor nodeMainExecutor, final URI masterUri) {
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                        masterUri);
        nodeMainExecutor
                .execute(this, nodeConfiguration.setNodeName("pocketbot"));
    }

    @Override
    public GraphName getDefaultNodeName() {
        return null;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        initFacePublisher(connectedNode);
        initTeleopPublisher(connectedNode);
        initImuPublisher(connectedNode);
        initVoicePublisher(connectedNode);
    }

    /**
     * Sends voice commands as raw text to ROS
     * @param connectedNode
     */
    private void initVoicePublisher(final ConnectedNode connectedNode) {
        final Publisher<String> aiPublisher = connectedNode.newPublisher("~ai_speech_out", String._TYPE);
        final Publisher<String> speechPublisher = connectedNode.newPublisher("~speech", String._TYPE);
        final String string = speechPublisher.newMessage();
        final String aiString = aiPublisher.newMessage();
        Robot.get().registerSpeechListener(new SpeechListener() {
            @Override
            public void onSpeechIn(java.lang.String speech) {
                string.setData(speech);
                speechPublisher.publish(string);
            }

            @Override
            public void onSpeechOut(java.lang.String speech) {
                aiString.setData(speech);
                aiPublisher.publish(aiString);
            }
        });
    }

    private void initImuPublisher(final ConnectedNode connectedNode) {
        final Publisher<Imu> publisher = connectedNode.newPublisher("~imu", Imu._TYPE);
        final Imu imu = publisher.newMessage();
        imu.setLinearAccelerationCovariance(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0});
        imu.setAngularVelocityCovariance(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0});
        imu.setOrientationCovariance(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0});
        imu.getHeader().setFrameId("/pocketbot");
        Robot.get().registerSensorListener(new BaseRobot.SensorListener() {
            @Override
            public void onSensorUpdate(final SensorData sensorData) {
                imu.getLinearAcceleration().setX(sensorData.getSensor().imu.linear_x);
                imu.getLinearAcceleration().setY(sensorData.getSensor().imu.linear_y);
                imu.getLinearAcceleration().setZ(sensorData.getSensor().imu.linear_z);

                imu.getAngularVelocity().setX(sensorData.getSensor().imu.angular_x);
                imu.getAngularVelocity().setY(sensorData.getSensor().imu.angular_y);
                imu.getAngularVelocity().setZ(sensorData.getSensor().imu.angular_z);

                imu.getOrientation().setW(sensorData.getSensor().imu.orientation_w);
                imu.getOrientation().setX(sensorData.getSensor().imu.orientation_x);
                imu.getOrientation().setY(sensorData.getSensor().imu.orientation_y);
                imu.getOrientation().setZ(sensorData.getSensor().imu.orientation_z);

                // Convert event.timestamp (nanoseconds uptime) into system time, use that as the header stamp
                //long time_delta_millis = System.currentTimeMillis() - SystemClock.uptimeMillis();
                imu.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));

                publisher.publish(imu);
                //throw new UnsupportedOperationException("Not implemented!");
            }
        });
    }

    private void initFacePublisher(final ConnectedNode connectedNode) {
        final Publisher<Twist> publisher = connectedNode.newPublisher("~face", Twist._TYPE);
        final Twist msg = publisher.newMessage();
        Robot.get().registerSensorListener(new BaseRobot.SensorListener() {
            @Override
            public void onSensorUpdate(final SensorData sensorData) {
                msg.getLinear().setX(sensorData.getFace().X);
                msg.getLinear().setY(sensorData.getFace().Y);
                msg.getLinear().setZ(sensorData.getFace().Z);
                publisher.publish(msg);
            }
        });
    }

    private void initTeleopPublisher(ConnectedNode connectedNode) {

        final Publisher<Twist> teleopPublisher = connectedNode.newPublisher("~cmd_vel", Twist._TYPE);
        final Twist msg = teleopPublisher.newMessage();
        Robot.get().registerSensorListener(new BaseRobot.SensorListener() {
            @Override
            public void onSensorUpdate(final SensorData sensorData) {
                final double angularZ = -sensorData.getControl().joy1.X;
                final double linearX = sensorData.getControl().joy1.Y;
                final double angularX = sensorData.getControl().joy2.X;
                final double angularY = sensorData.getControl().joy2.Y;
                //Check for changes
                if(angularZ != msg.getAngular().getZ() || linearX != msg.getLinear().getX()
                        || angularX != msg.getAngular().getX() || angularY != msg.getAngular().getY()) {
                    msg.getAngular().setZ(angularZ);
                    msg.getAngular().setX(angularX);
                    msg.getAngular().setY(angularY);
                    msg.getLinear().setX(linearX);
                    teleopPublisher.publish(msg);
                }
            }
        });
    }

    @Override
    public void onShutdown(Node node) {

    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }
}

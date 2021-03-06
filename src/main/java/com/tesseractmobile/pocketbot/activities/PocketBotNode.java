package com.tesseractmobile.pocketbot.activities;

import android.location.Location;
import android.os.SystemClock;

import java.net.URI;
import java.util.UUID;

import com.google.android.gms.maps.model.LatLng;
import com.tesseractmobile.pocketbot.robot.BaseRobot;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.model.Speech;
import com.tesseractmobile.pocketbot.robot.model.TextInput;
import com.tesseractmobile.pocketbot.robot.model.Waypoint;

import org.ros.address.InetAddressFactory;
import org.ros.internal.message.RawMessage;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

import geometry_msgs.Twist;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import sensor_msgs.Imu;
import std_msgs.Header;
import std_msgs.String;
import std_msgs.Float32;
import sensor_msgs.NavSatFix;


/**
 * Created by josh on 8/1/16.
 *
 * Creates nodes the publish information received by sensor data to ROS
 */
public class PocketBotNode implements NodeMain {

    public final static java.lang.String NODE_PREFIX = "pocketbot";

    public PocketBotNode(final NodeMainExecutor nodeMainExecutor, final URI masterUri) {
        try {
            final NodeConfiguration nodeConfiguration =
                    NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                            masterUri);
            nodeMainExecutor
                    .execute(this, nodeConfiguration.setNodeName(NODE_PREFIX + "_" + UUID.randomUUID().toString().substring(0, 4)));
        } catch (Exception e){
            //Could not find non loopback device
        }
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
        initEmotionPublisher(connectedNode);
        initLocationPublisher(connectedNode);
        initWaypointPublisher(connectedNode);
        initHeadingPublisher(connectedNode);
    }

    private void initHeadingPublisher(final ConnectedNode connectedNode) {
        final Publisher<Float32> headingPublisher = connectedNode.newPublisher("/" + NODE_PREFIX + "/heading", Float32._TYPE);
        final Float32 heading = headingPublisher.newMessage();
        Robot.get().registerSensorListener(new BaseRobot.SensorListener() {
            @Override
            public void onSensorUpdate(SensorData sensorData) {
                final int newHeading = sensorData.getSensor().heading;
                //Check for new heading
                if(Math.abs(newHeading - heading.getData()) > 1){
                    //Publish new heading
                    heading.setData(newHeading);
                    headingPublisher.publish(heading);
                }
            }
        });
    }

    private void initWaypointPublisher(final ConnectedNode connectedNode) {
        final Publisher<NavSatFix> waypointPublisher = connectedNode.newPublisher("/" + NODE_PREFIX + "/waypoint", NavSatFix._TYPE);

        Robot.get().getWaypointSubject()
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Waypoint>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Waypoint waypoint) {
                final NavSatFix fix = waypointPublisher.newMessage();
                fix.getHeader().setSeq(waypoint.sequence);
                fix.getHeader().setStamp(Time.fromMillis(SystemClock.uptimeMillis()));
                fix.setLatitude(waypoint.position.latitude);
                fix.setLongitude(waypoint.position.longitude);
                waypointPublisher.publish(fix);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void initLocationPublisher(final ConnectedNode connectedNode) {
        final Publisher<NavSatFix> locationPublisher = connectedNode.newPublisher("/" + NODE_PREFIX + "/fix", NavSatFix._TYPE);
        final NavSatFix fix = locationPublisher.newMessage();
        Robot.get().getLocationSubject().subscribe(new Observer<Location>() {
            int seqence = 0;
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Location location) {
                fix.setLatitude(location.getLatitude());
                fix.setLongitude(location.getLongitude());
                fix.setAltitude(location.getAltitude());
                final double secs = SystemClock.uptimeMillis() / 1000.0d;
                fix.getHeader().setStamp(new Time(secs));
                fix.getHeader().setSeq(++seqence);
                fix.getHeader().setFrameId(NODE_PREFIX);
                fix.getStatus().setStatus((byte) 0);
                fix.getStatus().setService((short) 1);
                fix.setPositionCovariance(new double[]{
                        location.getAccuracy(), location.getSpeed(), 0,
                        0, 0, 0,
                        0, 0, 0});
                locationPublisher.publish(fix);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * Sends emotional state to ROS
     * @param connectedNode
     */
    private void initEmotionPublisher(final ConnectedNode connectedNode) {
        final Publisher<String> emotionPublisher = connectedNode.newPublisher("/" + NODE_PREFIX + "/emotion", String._TYPE);
        final String emotionString = emotionPublisher.newMessage();
        Robot.get().getEmotion().subscribe(new Observer<Emotion>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Emotion emotion) {
                emotionString.setData(emotion.toString());
                emotionPublisher.publish(emotionString);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * Sends voice commands as raw text to ROS
     * @param connectedNode
     */
    private void initVoicePublisher(final ConnectedNode connectedNode) {
        final Publisher<String> aiPublisher = connectedNode.newPublisher("/" + NODE_PREFIX + "/ai_speech_out", String._TYPE);
        final Publisher<String> speechPublisher = connectedNode.newPublisher("/" + NODE_PREFIX + "/speech", String._TYPE);
        final String string = speechPublisher.newMessage();
        final String aiString = aiPublisher.newMessage();
        Robot.get().getTextInputSubject().subscribe(new Observer<TextInput>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull TextInput textInput) {
                string.setData(textInput.text);
                speechPublisher.publish(string);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        Robot.get().getSpeechSubject().subscribe(new Observer<Speech>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Speech speech) {
                aiString.setData(speech.text);
                aiPublisher.publish(aiString);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

    }

    private void initImuPublisher(final ConnectedNode connectedNode) {
        final Publisher<Imu> publisher = connectedNode.newPublisher("/" + NODE_PREFIX + "/imu", Imu._TYPE);
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
        final Publisher<Twist> publisher = connectedNode.newPublisher("/" + NODE_PREFIX + "/face", Twist._TYPE);
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

        final Publisher<Twist> teleopPublisher = connectedNode.newPublisher("/" + NODE_PREFIX + "/cmd_vel", Twist._TYPE);
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

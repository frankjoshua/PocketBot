package com.tesseractmobile.pocketbot.robot;



import java.math.BigDecimal;
import java.security.PermissionCollection;

/**
 * Data in this class is sent to the Arduino
 * It is all converted to JSON so unused fields will still be accessed
 *
 * Created by josh on 9/13/2015.
 */
public class SensorData {

    /** Face left or no face detected */
    public static final int NO_FACE = -1;

    private SensorData.Sensor sensor = new SensorData.Sensor();
    private SensorData.Face face = new SensorData.Face();
    private SensorData.Control control = new SensorData.Control();

    //private long time;
    public void setFace(int id) {
        face.id = id;
        update();
    }

    public void setFace(float x, float y, float z) {
        face.X = x;
        face.Y = y;
        face.Z = z;
        update();
    }

    public void setHeading(int heading) {
        sensor.heading = heading;
        update();
    }

    private void update() {
        //Uptime, don't excede max int value on uno
        //time = SystemClock.uptimeMillis() % 2147483647;
    }


    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace the numbers of decimals
     * @return
     */
    public static double round(double d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public void setProximity(boolean proximity) {
        sensor.proximity = proximity;
        update();
    }

    public void setJoystick1(final float x, final float y, final float z) {
        control.joy1.X = round(x, 2);
        control.joy1.Y = round(y, 2);
        control.joy1.Z = round(z, 2);
        update();
    }

    public void setJoystick2(final float x, final float y, final float z) {
        control.joy2.X = round(x, 2);
        control.joy2.Y = round(y, 2);
        control.joy2.Z = round(z, 2);
        update();
    }

    public void setJoystick1(final float x, final float y, final float z, final boolean a, final boolean b, final int heading) {
        setJoystick1(x, y, z);
        control.joy1.A = a;
        control.joy1.B = b;
        control.joy1.heading = heading;
        update();
    }

    public void setJoystick2(final float x, final float y, final float z, final boolean a, final boolean b, final int heading) {
        control.joy2.X = round(x, 2);
        control.joy2.Y = round(y, 2);
        control.joy2.Z = round(z, 2);
        control.joy2.A = a;
        control.joy2.B = b;
        control.joy2.heading = heading;
        update();
    }

    //Builders
    final static private PocketBotProtocol.PocketBotMessage.Builder messageBuilder = PocketBotProtocol.PocketBotMessage.newBuilder();
    final static private PocketBotProtocol.Face.Builder faceBuilder = PocketBotProtocol.Face.newBuilder();
    final static private PocketBotProtocol.Control.Builder controlBuilder = PocketBotProtocol.Control.newBuilder();
    final static private PocketBotProtocol.Sensor.Builder sensorBuilder = PocketBotProtocol.Sensor.newBuilder();
    final static private PocketBotProtocol.Gps.Builder gpsBuilder = PocketBotProtocol.Gps.newBuilder();
    final static private PocketBotProtocol.Joystick.Builder joyStickBuilder = PocketBotProtocol.Joystick.newBuilder();

    public static PocketBotProtocol.PocketBotMessage toPocketBotMessage(final SensorData sensorData) {


        //Objects
        final PocketBotProtocol.Face face = faceBuilder
                .setId(sensorData.getFace().id)
                .setX(sensorData.getFace().X)
                .setY(sensorData.getFace().Y)
                .setZ(sensorData.getFace().Z)
                .build();
        final PocketBotProtocol.Joystick joy1 = joyStickBuilder
                .setA(sensorData.getControl().joy1.A)
                .setB(sensorData.getControl().joy1.B)
                .setHeading(sensorData.getControl().joy1.heading)
                .setX((float) sensorData.getControl().joy1.X)
                .setY((float) sensorData.getControl().joy1.Y)
                .setZ((float) sensorData.getControl().joy1.Z)
                .build();
        final PocketBotProtocol.Joystick joy2 = joyStickBuilder
                .setA(sensorData.getControl().joy2.A)
                .setB(sensorData.getControl().joy2.B)
                .setHeading(sensorData.getControl().joy2.heading)
                .setX((float) sensorData.getControl().joy2.X)
                .setY((float) sensorData.getControl().joy2.Y)
                .setZ((float) sensorData.getControl().joy2.Z)
                .build();
        final PocketBotProtocol.Control control = controlBuilder
                .setJoy1(joy1)
                .setJoy2(joy2)
                .build();
        final PocketBotProtocol.Gps gps = gpsBuilder
                .setLat(sensorData.getSensor().gps.lat)
                .setLon(sensorData.getSensor().gps.lon)
                .build();
        final PocketBotProtocol.Sensor sensor = sensorBuilder
                .setHeading(sensorData.getSensor().heading)
                .setProximity(sensorData.getSensor().proximity)
                .setGps(gps)
                .build();

        //Message
        final PocketBotProtocol.PocketBotMessage pocketBotMessage = messageBuilder
                .setFace(face)
                .setControl(control)
                .setSensor(sensor)
                .build();

        return pocketBotMessage;
    }

    public Control getControl() {
        return control;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Face getFace() {
        return face;
    }

    /**
     * Wrap message in the format that is expected by the Arduino
     * @param data
     * @return
     */
    public static byte[] wrapData(byte[] data) {
        for(int i = 0; i < data.length; i++){
            if(data[i] == CommandContract.START_BYTE){
                throw new UnsupportedOperationException("Bad byte");
            }
        }
        //Create message to be sent
        final byte[] message = new byte[data.length + 2];
        //Add start byte
        message[0] = (byte) CommandContract.START_BYTE;
        //Add data
        System.arraycopy(data, 0, message, 1, data.length);
        //Add stop byte
        message[message.length - 1] = (byte) CommandContract.STOP_BYTE;
        return message;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public void setSensor(final Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Create a new SensorData object from a PocketBotProtocol.PocketBotMessage
     * @param data
     * @return
     */
    public static SensorData fromPocketBotMessage(final PocketBotProtocol.PocketBotMessage data) {
        final SensorData sensorData = new SensorData();
        copyControl(data.getControl(), sensorData.getControl());
        copySensor(data.getSensor(), sensorData.getSensor());
        copyFace(data.getFace(), sensorData.getFace());
        return sensorData;
    }

    private static void copyFace(final PocketBotProtocol.Face fromFace, final Face toFace) {
        toFace.id = fromFace.getId();
        toFace.X = (float) round(fromFace.getX(), 2);
        toFace.Y = (float) round(fromFace.getY(), 2);
        toFace.Z = (float) round(fromFace.getZ(), 2);
    }

    private static void copySensor(final PocketBotProtocol.Sensor fromSensor, final Sensor toSensor) {
        toSensor.heading = fromSensor.getHeading();
        toSensor.proximity = fromSensor.getProximity();
        copyGps(fromSensor.getGps(), toSensor.gps);
    }

    private static void copyGps(final PocketBotProtocol.Gps fromGps, final Gps toGps) {
        toGps.lat = fromGps.getLat();
        toGps.lon = fromGps.getLon();
    }

    private static void copyControl(final PocketBotProtocol.Control fromControl, final Control toControl) {
        copyJoyStick(fromControl.getJoy1(), toControl.joy1);
        copyJoyStick(fromControl.getJoy2(), toControl.joy2);
    }

    private static void copyJoyStick(final PocketBotProtocol.Joystick fromJoy, final Joystick toJoy) {
        toJoy.X = round(fromJoy.getX(), 2);
        toJoy.Y = round(fromJoy.getY(), 2);
        toJoy.Z = round(fromJoy.getZ(), 2);
        toJoy.A = fromJoy.getA();
        toJoy.B = fromJoy.getB();
        toJoy.heading = fromJoy.getHeading();
    }

    static public class Gps {
        public float lat;
        public float lon;
    }

    static public class Joystick {
        public boolean A;
        public boolean B;
        public int heading;
        public double X;
        public double Y;
        public double Z;
    }

    static public class Face {
        public int id = NO_FACE;
        public float X;
        public float Y;
        public float Z;
    }

    static public class Sensor {
        public int heading;
        public boolean proximity;
        public SensorData.Gps gps = new SensorData.Gps();
        public SensorData.Imu imu = new SensorData.Imu();
        public int battery;
        /** Time stamp in millis */
        public long timestamp;
    }


    static public class Control {
        public SensorData.Joystick joy1 = new SensorData.Joystick();
        public SensorData.Joystick joy2 = new SensorData.Joystick();

        @Override
        public String toString() {
            final String toString = "X: " + Double.toString(joy1.X) + " Y: " + Double.toString(joy1.Y);
            return toString;
        }
    }

    static public class Imu {
        public float linear_x;
        public float linear_y;
        public float linear_z;
        public float angular_x;
        public float angular_y;
        public float angular_z;
        public float orientation_w;
        public float orientation_x;
        public float orientation_y;
        public float orientation_z;

        /**
         * Update linearVaules
         * @param values 3 floats {x,y,z}
         */
        public void updateLinearVelocity(final float[] values) {
            linear_x = values[0];
            linear_y = values[1];
            linear_z = values[2];
        }

        /**
         * Update Angular Velocity
         * @param values 3 floats {x,y,z}
         */
        public void updateAngularVelocity(final float[] values) {
            angular_x = values[0];
            angular_y = values[1];
            angular_z = values[2];
        }

        /**
         * Update orientation
         * @param quaternion 4 floats {x,x,y,z}
         */
        public void updateOrientation(final float[] quaternion) {
            orientation_w = quaternion[0];
            orientation_x = quaternion[1];
            orientation_y = quaternion[2];
            orientation_z = quaternion[3];
        }
    }
}

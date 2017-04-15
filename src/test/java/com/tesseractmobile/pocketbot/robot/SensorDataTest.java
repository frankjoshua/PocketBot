package com.tesseractmobile.pocketbot.robot;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by josh on 1/14/2016.
 */
public class SensorDataTest {

    private SensorData mSensorData;

    @Before
    public void setUp() throws Exception {
        mSensorData = new SensorData();
    }

    @Test
    public void testSetFace() throws Exception {
        mSensorData.setFace(-1);
        assertEquals(-1, mSensorData.getFace().id);
        mSensorData.setFace(0);
        assertEquals(0, mSensorData.getFace().id);
        mSensorData.setFace(1);
        assertEquals(1, mSensorData.getFace().id);
    }
}
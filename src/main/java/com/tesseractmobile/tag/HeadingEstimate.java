package com.tesseractmobile.tag;

import android.os.SystemClock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by josh on 9/30/2015.
 */
public class HeadingEstimate {

    private static long DATA_TIMEOUT = 10000;
    private Queue<HeadingData> mDataQueue = new LinkedList<HeadingData>();
    private int mLastEstimateHeading = 0;

    public synchronized void newData(int heading, double distanceChange) {
        if(distanceChange > 0){
            heading += 180;
            if(heading > 360){
                heading -= 360;
            }
        }
        final long timeStamp = SystemClock.uptimeMillis();
        mDataQueue.add(new HeadingData(heading, (int) (Math.abs(distanceChange) / 0.1d), timeStamp));

        //Remove stale data
        Iterator<HeadingData> iterator = mDataQueue.iterator();
        while(iterator.hasNext()){
            final HeadingData headingData = iterator.next();
            if(timeStamp - headingData.mTimeStamp > DATA_TIMEOUT){
                iterator.remove();
            }
        }
    }

    public synchronized int getHeadingEstimate() {
        //int dataUnitCount = 0;
        //int headingDataSum = 0;
        double sin_sum = 0;
        double cos_sum = 0;
        for(HeadingData headingData : mDataQueue){
            final double heading = headingData.mHeading;
            for(int i = 0; i < headingData.mDistanceChange; i++){
                sin_sum += Math.sin(heading);
                cos_sum += Math.cos(heading);
            }
        }
        if(sin_sum == 0) {
            //Catch divide by 0 error
            return mLastEstimateHeading;
        }
        //Convert to degrees and round
        mLastEstimateHeading = ((int) Math.round(Math.toDegrees(Math.atan2(sin_sum, cos_sum))) + 360) % 360;
        return mLastEstimateHeading;
    }

    private class HeadingData {
        /** Heading in radians */
        final private double mHeading;
        final private int mDistanceChange;
        final private long mTimeStamp;

        public HeadingData(final int heading, final int distanceChange, final long timeStamp) {
            mHeading = Math.toRadians(heading);
            mDistanceChange = distanceChange;
            mTimeStamp = timeStamp;
        }
    }
}

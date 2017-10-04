package com.tesseractmobile.pocketbot.robot.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by josh on 10/3/17.
 */

public class Waypoint {
    public final LatLng position;
    public final int sequence;

    public Waypoint(final LatLng position, final int sequence) {
        this.position = position;
        this.sequence = sequence;
    }
}

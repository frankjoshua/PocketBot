package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Robot;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by josh on 7/23/17.
 */

public class MapFaceFragment extends MapFragment implements OnMapReadyCallback {

    public static final int DEFAULT_ZOOM = 17;
    private LatLng lastLatLng;

    private Marker currentRobotLocation;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        //Request Map
        getMapAsync(this);
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        final LatLng stLouis = new LatLng(38.649, -90.219);
        //Create new android marker
        currentRobotLocation = googleMap.addMarker(new MarkerOptions().position(stLouis).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_adb_black_48dp)).title("Current Location"));

        //Add move to current location
        Robot.get().getLocationSubject()
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(location ->
                    new LatLng(location.getLatitude(), location.getLongitude())
                ).subscribe(latLng -> setMarkerCurrent(googleMap, latLng));

        //Listen for map clicks
        googleMap.setOnMapClickListener(latLng -> {
            Robot.get().getWaypointSubject().onNext(latLng);
        });

        //Update map when new markers appear
        Robot.get().getWaypointSubject()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(latLng -> addMarker(googleMap, latLng));

        //Listen for marker clicks
        googleMap.setOnMarkerClickListener(marker -> {
            Robot.get().getWaypointSubject().onNext(marker.getPosition());
            return true;
        });
    }

    /**
     * Set the current position of the robot
     * @param googleMap
     * @param latLng
     * @return
     */
    private LatLng setMarkerCurrent(final GoogleMap googleMap, final LatLng latLng) {
        currentRobotLocation.setPosition(latLng);
        if(googleMap.getCameraPosition().zoom < DEFAULT_ZOOM){
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
        return latLng;
    }

    private LatLng addMarker(GoogleMap googleMap, LatLng latLng) {
        final Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                .title(latLng.toString()));
        addMarker(marker);
        if(googleMap.getCameraPosition().zoom < DEFAULT_ZOOM){
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
        if(lastLatLng != null){
            googleMap.addPolyline(new PolylineOptions().add(lastLatLng).add(latLng));
        } else {
            //First line is from the robot
            googleMap.addPolyline(new PolylineOptions().add(currentRobotLocation.getPosition()).add(latLng));
        }
        //Save lat point
        lastLatLng = latLng;
        return latLng;
    }

    private void addMarker(final Marker marker) {

    }
}

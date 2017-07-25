package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Robot;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by josh on 7/23/17.
 */

public class MapFaceFragment extends MapFragment implements OnMapReadyCallback {

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
        currentRobotLocation = googleMap.addMarker(new MarkerOptions().position(stLouis).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_adb_black_48dp)));

        //Add move to current location
        Robot.get().getLocationSubject()
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(location ->
                    new LatLng(location.getLatitude(), location.getLongitude())
                ).map( latLng -> addMarkerCurrent(googleMap, latLng)).subscribe();

//        LatLng stLouis = new LatLng(38.649, -90.219);
//        addMarker(googleMap, stLouis);
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stLouis, 13));
        //Listen for map clicks
        googleMap.setOnMapClickListener(latLng -> addMarker(googleMap, latLng));
    }

    private LatLng addMarkerCurrent(final GoogleMap googleMap, final LatLng latLng) {
        currentRobotLocation.setPosition(latLng);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
        return latLng;
    }

    private LatLng addMarker(GoogleMap googleMap, LatLng latLng) {
        googleMap.addMarker(new MarkerOptions().position(latLng)
                .title(latLng.toString()));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
        if(lastLatLng != null){
            googleMap.addPolyline(new PolylineOptions().add(lastLatLng).add(latLng));
        }
        //Save lat point
        lastLatLng = latLng;
        return latLng;
    }
}

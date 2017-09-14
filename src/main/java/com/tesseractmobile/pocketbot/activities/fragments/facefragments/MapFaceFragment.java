package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Robot;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by josh on 7/23/17.
 */

public class MapFaceFragment extends MapFragment implements OnMapReadyCallback {

    public static final int DEFAULT_ZOOM = 16;
    private LatLng lastLatLng;

    private Marker currentRobotLocation;
    private Button button;
    private List<Marker> markers = new ArrayList<>();
    private List<Polyline> polyLines = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        //Request Map
        getMapAsync(this);
        button = new Button(layoutInflater.getContext());
        button.setText("Clear Waypoints");

        View mapView = super.onCreateView(layoutInflater, viewGroup, bundle);
        RelativeLayout view = new RelativeLayout(getActivity());
        view.addView(mapView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        view.addView(button, params);
        // working with view
        return view;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        // Sets the map type to be "hybrid"
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //Get St. Louis lat long
        final LatLng stLouis = new LatLng(38.649, -90.219);
        //Create new android marker
        final Bitmap bitmap = changeBitmapColor(BitmapFactory.decodeResource(getResources(), R.drawable.ic_adb_black_48dp));
        final BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);//BitmapDescriptorFactory.fromResource(R.drawable.ic_adb_black_48dp);
        currentRobotLocation = googleMap.addMarker(new MarkerOptions().position(stLouis).icon(bitmapDescriptor).title("Current Location"));

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

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for(Marker marker : markers){
                    marker.remove();
                }
                markers.clear();
                for(Polyline polyline : polyLines){
                    polyline.remove();
                }
                polyLines.clear();
            }
        });
    }

    private static Bitmap changeBitmapColor(final Bitmap sourceBitmap){
        float[] colorTransform = {
                0, 1f, 0, 0, 0,
                0, 0, 0.1f, 0, 0,
                0, 0, 0, 0.30f, 0,
                0, 0, 0, 1f, 0};

        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f); //Remove Colour
        colorMatrix.set(colorTransform); //Apply the Red

        final ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        final Paint paint = new Paint();
        paint.setColorFilter(colorFilter);

        final Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap.copy(Bitmap.Config.ARGB_8888, true), 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());

        final Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, paint);

        return resultBitmap;
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
            savePolyLine(googleMap.addPolyline(new PolylineOptions().add(lastLatLng).add(latLng)));
        } else {
            //First line is from the robot
            savePolyLine(googleMap.addPolyline(new PolylineOptions().add(currentRobotLocation.getPosition()).add(latLng)));
        }
        //Save lat point
        lastLatLng = latLng;
        return latLng;
    }

    private void savePolyLine(final Polyline polyline) {
        polyLines.add(polyline);
    }

    private void addMarker(final Marker marker) {
        markers.add(marker);
    }
}

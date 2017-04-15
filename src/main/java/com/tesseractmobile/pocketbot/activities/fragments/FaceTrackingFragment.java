package com.tesseractmobile.pocketbot.activities.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.views.CameraSourcePreview;
import com.tesseractmobile.pocketbot.views.FaceGraphic;
import com.tesseractmobile.pocketbot.views.GraphicOverlay;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * Created by josh on 10/18/2015.
 */
public class FaceTrackingFragment extends CallbackFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    public int PREVIEW_WIDTH = 240;
    public int PREVIEW_HEIGHT = 320;

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private RobotInterface mRobotInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        PREVIEW_HEIGHT = (int) getResources().getDimension(R.dimen.height_camera_preview);
        PREVIEW_WIDTH = (int) getResources().getDimension(R.dimen.width_camera_preview);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.camera_preview, container, false);
        mPreview = (CameraSourcePreview) view.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) view.findViewById(R.id.faceOverlay);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            startFaceDetection();
        } else {
            requestCameraPermission();
        }
        //Listen for settings changes
        PocketBotSettings.registerOnSharedPreferenceChangeListener(getActivity(), this);
        updateView(PocketBotSettings.isShowPreview(getActivity()));
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {

        final Activity thisActivity = getActivity();

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    private void startFaceDetection() {
        try {
            final FaceDetector detector;
            if(PocketBotSettings.getFastTrackingMode(getActivity())) {
                detector = new FaceDetector.Builder(getActivity().getApplicationContext())
                        .setTrackingEnabled(true)
                        .setMode(FaceDetector.FAST_MODE)
                        //.setProminentFaceOnly(true)
                        .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                        .setLandmarkType(FaceDetector.NO_LANDMARKS)
                        .build();

            } else {
                detector = new FaceDetector.Builder(getActivity().getApplicationContext())
                        .setTrackingEnabled(true)
                        .setMode(FaceDetector.ACCURATE_MODE)
                        //.setProminentFaceOnly(true)
                        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .build();
            }
            detector.setProcessor(new MultiProcessor.Builder<Face>(new GraphicFaceTrackerFactory()).build());
//        detector.setProcessor(
//                new LargestFaceFocusingProcessor(
//                        detector,
//                        new GraphicFaceTracker(mGraphicOverlay)));

            mCameraSource = new CameraSource.Builder(getActivity().getApplicationContext(), detector)
                    .setRequestedPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(15.0f)
                    .build();
            mPreview.setDrawingCacheEnabled(true);
            mPreview.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            mPreview.layout(0, 0, mPreview.getMeasuredWidth(), mPreview.getMeasuredHeight());
            mPreview.start(mCameraSource, mGraphicOverlay);
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        final CameraSource cameraSource = mCameraSource;
        if(cameraSource != null){
            cameraSource.stop();
        }
        PocketBotSettings.unregisterOnSharedPreferenceChangeListener(getActivity(), this);
    }

    public void setRobotInterface(RobotInterface mRobotInterface) {
        this.mRobotInterface = mRobotInterface;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PocketBotSettings.KEY_SHOW_PREVIEW)){
            final boolean showPreview = sharedPreferences.getBoolean(key, false);
            updateView(showPreview);
        } else if(key.equals(PocketBotSettings.KEY_FAST_TRACKING)){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final CameraSource cameraSource = mCameraSource;
                    if(cameraSource != null){
                        cameraSource.stop();
                    }
                    startFaceDetection();
                }
            });
        }
    }

    private void updateView(boolean showPreview) {
        if(showPreview){
            mPreview.setVisibility(View.VISIBLE);
        } else {
            mPreview.setVisibility(View.GONE);
        }
        mPreview.invalidate();
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        final XYZ xyz = new XYZ();
        private boolean mTakingPicture = false;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        @Override
        public void onNewItem(int id, Face item) {
            FaceTrackingFragment.getCenter(xyz, item, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            mFaceGraphic.setmXyz(xyz);
            mFaceGraphic.setId(id);
            mRobotInterface.humanSpotted(id);
        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face item) {
            //Face position has changed
            FaceTrackingFragment.getCenter(xyz, item, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(item);
            mRobotInterface.look(xyz.x, xyz.y, xyz.z);

//            if(mTakingPicture == false) {
//                mTakingPicture = true;
//                mPreview.buildDrawingCache(true);
//                Bitmap  bitmap = mPreview.getSurfaceView().getDrawingCache();
//                if(bitmap == null){
//                    return;
//                }
//
//                FirebaseStorage storage = FirebaseStorage.getInstance();
//                StorageReference storageRef = storage.getReferenceFromUrl("gs://pocketbot-1161.appspot.com");
//                StorageReference image = storageRef.child(UUID.randomUUID().toString());
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byte[] byteArray = stream.toByteArray();
//                image.putBytes(byteArray);
//                mTakingPicture = false;
////                mCameraSource.takePicture(new CameraSource.ShutterCallback() {
////                    @Override
////                    public void onShutter() {
////
////                    }
////                }, new CameraSource.PictureCallback() {
////                    @Override
////                    public void onPictureTaken(byte[] bytes) {
////                        try {
////                            // convert byte array into bitmap
////                            Bitmap loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
////                                    bytes.length);
////
////                            FirebaseStorage storage = FirebaseStorage.getInstance();
////                            StorageReference storageRef = storage.getReferenceFromUrl("gs://pocketbot-1161.appspot.com");
////                            StorageReference image = storageRef.child(UUID.randomUUID().toString());
////                            image.putBytes(bytes);
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        }
////                        mTakingPicture = false;
////                    }
////                });
//            }
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);
            //Face missed for a frame or more
            mOverlay.remove(mFaceGraphic);
            mRobotInterface.look(1.0f, 1.0f, 1.0f);
            mRobotInterface.humanSpotted(-1);
        }

        @Override
        public void onDone() {
            //Called when face is lost
            super.onDone();
        }
    }

    /**
     * Updates XYZ with current face info
     * @param xyz
     * @param face
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    static public XYZ getCenter(XYZ xyz, final Face face, final int viewWidth, final int viewHeight){
        //Center horizontal
        final float centerX = face.getPosition().x + face.getWidth() / 2;
        //Above center for vertical (Look into eyes instead of face)
        final float centerY = face.getPosition().y + face.getHeight() / 2;
        //Log.d("PocketBot", Float.toString(centerX));
        float cx = centerX / viewWidth;
        float cy = centerY / viewHeight;

        xyz.x = 2 - cx * 2f;
        xyz.y = cy * 2f;
        xyz.z = face.getHeight() / viewHeight;

        return xyz;
    }

    public static class XYZ {
        public float x,y,z;
    }
}

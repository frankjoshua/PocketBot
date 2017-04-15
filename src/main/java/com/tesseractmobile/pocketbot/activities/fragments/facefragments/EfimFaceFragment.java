package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.RemoteListener;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.faces.EfimFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

/**
 * Created by josh on 10/18/2015.
 */
public class EfimFaceFragment extends FaceFragment implements RemoteListener {

    private RobotFace mRobotFace;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RemoteControl.get().registerRemoteListener(this);
    }

    @Override
    public void onDestroy() {
        RemoteControl.get().unregisterRemoteListener(this);
        super.onDestroy();
    }

    @Override
    public RobotFace getRobotFace(final RobotInterface robotInterface) {
        mRobotFace.setRobotInterface(robotInterface);
        return mRobotFace;
    }

    @Override
    public boolean isUseFaceTracking() {
        return true;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.robot_face, null);
        setFace(new EfimFace(view));
        return view;
    }

    @Override
    public void onMessageReceived(Object message) {
        ((EfimFace) mRobotFace).onControlReceived((SensorData.Control) message);
    }

    @Override
    public void onConnectionLost() {
        //Send stop to face
        ((EfimFace) mRobotFace).onControlReceived(new SensorData.Control());
    }

    protected void setFace(final RobotFace face){
        mRobotFace = face;
    }
}
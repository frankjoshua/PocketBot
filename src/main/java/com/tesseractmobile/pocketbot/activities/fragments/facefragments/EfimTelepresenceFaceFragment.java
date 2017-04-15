package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.AuthData;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.RemoteListener;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.faces.EfimFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

/**
 * Created by josh on 10/18/2015.
 */
public class EfimTelepresenceFaceFragment extends QuickBloxFragment implements RemoteListener{

    private static final String TAG = EfimTelepresenceFaceFragment.class.getSimpleName();
    private EfimFace mRobotFace;

    @Override
    public RobotFace getRobotFace(final RobotInterface robotInterface) {
        mRobotFace.setRobotInterface(robotInterface);
        return mRobotFace;
    }

    @Override
    public boolean isUseFaceTracking() {
        return false;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.robot_face, null);
        mRobotFace = new EfimFace(view);
        final View btnSignIn = view.findViewById(R.id.sign_in_button);
        final Activity activity = getActivity();
        final View progressBar = view.findViewById(R.id.pbSignIn);
        btnSignIn.setVisibility(View.VISIBLE);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                btnSignIn.setVisibility(View.INVISIBLE);
                ((View.OnClickListener) activity).onClick(view);
            }
        });
        Robot.get().registerOnAuthCompleteListener(new DataStore.OnAuthCompleteListener() {
            @Override
            public void onAuthComplete(final AuthData authData) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSignIn.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Listen to remote messages
        RemoteControl.get().registerRemoteListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop listening to remote messages
        RemoteControl.get().unregisterRemoteListener(this);
    }


    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {

    }

    @Override
    void onQBSetup(QBUser user) {
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

}

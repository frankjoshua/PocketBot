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
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.faces.EfimFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by josh on 10/18/2015.
 */
public class EfimTelepresenceFaceFragment extends QuickBloxFragment{

    private static final String TAG = EfimTelepresenceFaceFragment.class.getSimpleName();
    private EfimFace mRobotFace;
    private Disposable mControlDisposable;

    @Override
    public boolean isUseFaceTracking() {
        return false;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.robot_face, null);
        mRobotFace = new EfimFace(view, Robot.get());
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
        RemoteControl.get().getControlSubject().subscribe(new Observer<SensorData.Control>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mControlDisposable = d;
            }

            @Override
            public void onNext(@NonNull SensorData.Control control) {
                mRobotFace.onControlReceived(control);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                mRobotFace.onControlReceived(new SensorData.Control());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop listening to remote messages
        final Disposable controlDisposable = this.mControlDisposable;
        if(controlDisposable != null && !controlDisposable.isDisposed()){
            controlDisposable.dispose();
        }
    }


    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {

    }

    @Override
    void onQBSetup(QBUser user) {
    }


}

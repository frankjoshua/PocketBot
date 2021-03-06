package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.RTCGLVideoView;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.AuthData;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.robot.faces.TelePresenceFace;

import org.webrtc.VideoRenderer;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by josh on 10/31/2015.
 */
public class TelepresenceFaceFragment extends QuickBloxFragment{


    private RobotFace mRobotFace;
    private RTCGLVideoView mRemoteVideoView;
    private TextView mUserId;
    private Handler mHandler = new Handler();
    private Disposable mControlDisplosable;

    @Override
    public boolean isUseFaceTracking() {
        return false;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view;
        view = inflater.inflate(R.layout.face_telepresence, null);
        mRemoteVideoView = (RTCGLVideoView) view.findViewById(R.id.remoteVideoView);
        mUserId = (TextView) view.findViewById(R.id.tvUserId);
        mRobotFace = new TelePresenceFace(view, Robot.get());
        final View progressBar = view.findViewById(R.id.pbSignIn);
        final View btnSignIn = view.findViewById(R.id.sign_in_button);
        final Activity activity = getActivity();
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
        //Listen for remote messages
        RemoteControl.get().getControlSubject().subscribe(new Observer<SensorData.Control>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mControlDisplosable = d;
            }

            @Override
            public void onNext(@NonNull SensorData.Control control) {
                ((TelePresenceFace) mRobotFace).onControlReceived(control);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                //Send stop to face
                ((TelePresenceFace) mRobotFace).onControlReceived(new SensorData.Control());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop listening for remote messages
        final Disposable controlDisposable = mControlDisplosable;
        if(controlDisposable != null && !controlDisposable.isDisposed()){
            controlDisposable.dispose();
        }
    }

    @Override
    protected void onQBSetup(final QBUser user) {
        final Integer userId = user.getId();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUserId.setText("Waiting for call: " + Integer.toString(userId));
            }
        });
    }


    static public void fillVideoView(RTCGLVideoView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.addRenderer(new VideoRenderer(remoteRenderer ?
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.MAIN) :
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.SECOND)));
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRemoteVideoView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onLocalVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack) {

    }

    @Override
    public void onRemoteVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //Setup Remote video
                fillVideoView(mRemoteVideoView, qbrtcVideoTrack, true);
                mRemoteVideoView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onStateChanged(QBRTCSession qbrtcSession, BaseSession.QBRTCSessionState qbrtcSessionState) {

    }
}

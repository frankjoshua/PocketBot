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
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.RTCGLVideoView;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.AuthData;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.RemoteListener;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.robot.faces.TelePresenceFace;

import org.webrtc.VideoRenderer;

/**
 * Created by josh on 10/31/2015.
 */
public class TelepresenceFaceFragment extends QuickBloxFragment implements RemoteListener {


    private RobotFace mRobotFace;
    private RTCGLVideoView mRemoteVideoView;
    private TextView mUserId;
    private Handler mHandler = new Handler();

    @Override
    public RobotFace getRobotFace(RobotInterface robotInterface) {
        mRobotFace.setRobotInterface(robotInterface);
        return mRobotFace;
    }

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
        mRobotFace = new TelePresenceFace(view);
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
        RemoteControl.get().registerRemoteListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop listening for remote messages
        RemoteControl.get().unregisterRemoteListener(this);
    }

    @Override
    protected void onQBSetup(final QBUser user) {
        final Integer userId = user.getId();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUserId.setText(Integer.toString(userId));
            }
        });
    }

    @Override
    public void onRemoteVideoTrackReceive(final QBRTCSession qbrtcSession, final QBRTCVideoTrack qbrtcVideoTrack, final Integer integer) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //Setup Remote video
                fillVideoView(mRemoteVideoView, qbrtcVideoTrack, true);
                mRemoteVideoView.setVisibility(View.VISIBLE);
            }
        });

    }

    static public void fillVideoView(RTCGLVideoView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.addRenderer(new VideoRenderer(remoteRenderer ?
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.MAIN) :
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.SECOND)));
    }

    @Override
    public void onMessageReceived(Object message) {
        ((TelePresenceFace) mRobotFace).onControlReceived((SensorData.Control) message);
    }

    @Override
    public void onConnectionLost() {
        //Send stop to face
        ((TelePresenceFace) mRobotFace).onControlReceived(new SensorData.Control());
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

}

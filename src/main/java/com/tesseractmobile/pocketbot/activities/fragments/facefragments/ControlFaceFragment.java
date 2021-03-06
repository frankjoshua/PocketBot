package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.RTCGLVideoView;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.activities.fragments.RobotSelectionDialog;
import com.tesseractmobile.pocketbot.robot.Constants;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.RobotInfo;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.faces.BaseFace;
import com.tesseractmobile.pocketbot.robot.faces.ControlFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;

import org.ros.android.RosFragmentActivity;
import org.ros.android.view.visualization.VisualizationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by josh on 10/25/2015.
 */
public class ControlFaceFragment extends QuickBloxFragment implements View.OnClickListener, RobotSelectionDialog.OnRobotSelectedListener {

    private RobotFace mRobotFace;
    private RTCGLVideoView mRemoteVideoView;
    private VisualizationView mVisualizationView;
    private ImageButton mConnectButton;
    private RemoteState mRemoteState = RemoteState.NOT_CONNECTED;
    private QBRTCSession mSession;

    private Handler mHandler = new Handler();
    private Disposable mControlDisplosable;

    @Override
    public boolean isUseFaceTracking() {
        return false;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.face_control, container, false);
        mConnectButton = (ImageButton) view.findViewById(R.id.btnConnect);
        mConnectButton.setOnClickListener(this);
        mRemoteVideoView = (RTCGLVideoView) view.findViewById(R.id.remoteVideoView);
        mVisualizationView = (VisualizationView) view.findViewById(R.id.visualization);
        mRobotFace = new ControlFace(view, (RosFragmentActivity) getActivity(), Robot.get());
        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnConnect){
            if(mRemoteState == RemoteState.NOT_CONNECTED){
                //Show selection dialog
                final RobotSelectionDialog robotSelectionDialog = new RobotSelectionDialog();
                robotSelectionDialog.setOnlyUserRobots(false);
                robotSelectionDialog.setSignInOnClickListener((View.OnClickListener) getActivity());
                robotSelectionDialog.show(getActivity().getFragmentManager(), "ROBOT_SELECTION_DIALOG");
                robotSelectionDialog.setOnRobotSelectedListener(this);
            } else if (mRemoteState == RemoteState.CONNECTED){
                disconnect();
            }
        }
    }

    private void disconnect() {
        if(mSession != null){
            mSession.hangUp(null);
            setRemoteState(RemoteState.NOT_CONNECTED);
        }
        ((ControlFace) mRobotFace).controlRemoteRobot(null);
    }

    private void connectToRemoteRobot(final int remoteNumber, final String remoteRobotId) {
        final Activity activity = getActivity();
        if(activity == null){
            //Called after activity closed just return
            return;
        }
        setRemoteState(RemoteState.CONNECTING);
        List<Integer> opponents = new ArrayList<Integer>();
        opponents.add(remoteNumber); //12345 - QBUser ID

        //Set user information
        // User can set any string key and value in user info
        // Then retrieve this data from sessions which is returned in callbacks
        // and parse them as he wish
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("key", "value");

        //Init session
        mSession = QBRTCClient.getInstance(activity).createNewSessionWithOpponents(opponents, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
        newSessionCreated(mSession);
        //Start call
        mSession.startCall(userInfo);

        //Connect to remote robot
        ((ControlFace) mRobotFace).controlRemoteRobot(remoteRobotId);

        //Save UserId
        PocketBotSettings.setLastRobotId(activity, remoteRobotId);

    }

    private void setRemoteState(final RemoteState newState) {
        this.mRemoteState = newState;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (newState){
                    case CONNECTING:
                        mConnectButton.setEnabled(false);
                        break;
                    case CONNECTED:
                        mConnectButton.setEnabled(true);
                        break;
                    case NOT_CONNECTED:
                        mConnectButton.setEnabled(true);
                        break;
                }
            }
        });
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
                ((BaseFace) mRobotFace).onControlReceived(control);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                //Send stop to face
                ((BaseFace) mRobotFace).onControlReceived(new SensorData.Control());
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
    void onQBSetup(QBUser user) {
        //Do Nothing
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        super.onCallAcceptByUser(qbrtcSession, integer, map);
        //Update state
        setRemoteState(RemoteState.CONNECTED);
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRemoteVideoView.setVisibility(View.GONE);
                mVisualizationView.setVisibility(View.VISIBLE);
                setRemoteState(RemoteState.NOT_CONNECTED);
            }
        });
    }

    @Override
    public void onRobotSelected(RobotInfo.Settings robotinfo) {
        final Context context =  getActivity();
        if(context == null){
            //If context is null user has probably exited the app
            return;
        }
        if(robotinfo.prefs.r_id.equals(PocketBotSettings.getRobotId(context))){
            Toast.makeText(context, "You must select a remote robot", Toast.LENGTH_LONG).show();
            //Allow if testing
            if(Constants.LOGGING == false){
                return;
            }
        }
        connectToRemoteRobot(robotinfo.prefs.qb_id, robotinfo.prefs.r_id);
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
                TelepresenceFaceFragment.fillVideoView(mRemoteVideoView, qbrtcVideoTrack, true);
                mRemoteVideoView.setVisibility(View.VISIBLE);
                mVisualizationView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStateChanged(QBRTCSession qbrtcSession, BaseSession.QBRTCSessionState qbrtcSessionState) {

    }

    private enum RemoteState {
        NOT_CONNECTED, CONNECTED, CONNECTING
    }

}

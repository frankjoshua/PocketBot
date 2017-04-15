package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.tesseractmobile.pocketbot.BuildConfig;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.AuthData;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.Robot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by josh on 11/1/2015.
 */
abstract public class QuickBloxFragment extends FaceFragment implements QBRTCClientSessionCallbacks, QBRTCClientVideoTracksCallbacks, QBRTCSessionConnectionCallbacks, QBRTCSignalingCallback, DataStore.OnAuthCompleteListener {

    private QBChatService chatService;
    private QBRTCSession mCurrentRTCSession;
    private Handler mHandler = new Handler();

    @Override
    public void onStart() {
        super.onStart();
        Robot.get().registerOnAuthCompleteListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Robot.get().unregisterOnAuthCompleteListener(this);
        if(mCurrentRTCSession != null){
            mCurrentRTCSession.hangUp(mCurrentRTCSession.getUserInfo());
            mCurrentRTCSession.removeSessionCallbacksListener(this);
            mCurrentRTCSession.removeVideoTrackCallbacksListener(this);
            mCurrentRTCSession.removeSignalingCallback(this);
        }
    }

    protected void signIn(final Context context) {
        //Get the username and password
        final String login = PocketBotSettings.getRobotId(context);
        final String password = PocketBotSettings.getPassword(context);
        //Create a user
        final QBUser user = new QBUser(login, password);
        //If no password sign up the user
        if(password.equals("")){
            signUpUser(user, context);
            return;
        }
        QBUsers.signIn(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                //Save the id so other robots can use it to call
                PocketBotSettings.setQuickBloxId(context, qbUser.getId());

                //After signin continue to set up call
                setUpQB(user, context);
            }

            @Override
            public void onError(QBResponseException e) {
                //error
                if (e.getErrors().get(0).equals("Unauthorized")) {
                    //User has no account, so sign them up
                    signUpUser(user, context);
                } else {
                    //Unhandled Error
                    error(e.getErrors().toString());
                }
            }
        });

    }

    /**
     * Creates a new user account
     * @param user
     */
    private void signUpUser(final QBUser user, final Context context) {
        QBUsers.signUp(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                signIn(context);
            }

            @Override
            public void onError(QBResponseException e) {
                error(user.getLogin().toString() + " SignUp: " + e.getErrors().toString());
            }
        });

    }

    /**
     * Shows a dialog with the error string
     * @param s
     */
    private void error(String s) {
        showDialog(s);
    }

    private void setUpQB(final QBUser user, final Context context) {


        //Chat service is used to start WebRTC signaling
        QBChatService.getInstance().login(user, new QBEntityCallback<QBUser>() {

            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                postLogin(context, user);
            }

            @Override
            public void onError(QBResponseException e) {
                if (e.getErrors().get(0).equals("You have already logged in chat")) {
                    postLogin(context, user);
                } else {
                    //error
                    error("Login: " + e.getErrors().toString());
                }
            }
        });
    }

    private void postLogin(final Context context, QBUser user) {
        //Set up WebRTC Signaling
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager()
                .addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
                    @Override
                    public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                        if (!createdLocally) {
                            QBRTCClient.getInstance(context).addSignaling((QBWebRTCSignaling) qbSignaling);
                        }
                    }
                });

        QBRTCClient.getInstance(context).addSessionCallbacksListener(QuickBloxFragment.this);
        QBRTCConfig.setMaxOpponentsCount(6);
        QBRTCConfig.setDisconnectTime(30);
        QBRTCConfig.setAnswerTimeInterval(30l);
        QBRTCConfig.setDebugEnabled(BuildConfig.DEBUG);
        //Ready for calls
        QBRTCClient.getInstance(context).prepareToProcessCalls();
        //showDialog("Listening for calls");
        //Let extended classes know that calls are ready
        onQBSetup(user);
    }

    abstract void onQBSetup(QBUser user);

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer, QBRTCSignalException e) {
        showDialog("Disconnected!");
    }

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {

    }

    @Override
    final public void onReceiveNewSession(final QBRTCSession qbrtcSession) {
        newSessionCreated(qbrtcSession);

        //Answer calls
        final Map<String,String> userInfo = new HashMap<String,String>();
        userInfo.put("Key", "Value");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                qbrtcSession.acceptCall(userInfo);
            }
        });


    }

    /**
     * This must be called when a new session is created
     * @param qbrtcSession
     */
    protected void newSessionCreated(QBRTCSession qbrtcSession) {
        //Save the current session
        mCurrentRTCSession = qbrtcSession;
        //Listen for video
        qbrtcSession.addVideoTrackCallbacksListener(QuickBloxFragment.this);
        qbrtcSession.addSessionCallbacksListener(QuickBloxFragment.this);
        qbrtcSession.addSignalingCallback(QuickBloxFragment.this);
        //Debugging
        //showDialog("New Session Received");
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        showDialog("The remote robot did not answer.");
    }

    private void showDialog(final String error) {
        final Activity activity = getActivity();
        if(activity != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle("Remote Error");
                    alertDialog.setMessage(error);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            });
        }
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        showDialog("Call Rejected");
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        //showDialog("Session Closed");
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {

    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack) {

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        //showDialog("Call Accepted");
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        //showDialog("The remote robot disconnected");
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        //showDialog("Connected to user");
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
        showDialog(e.toString());
    }

    @Override
    public void onAuthComplete(final AuthData authData) {
        signIn(getActivity());
    }
}

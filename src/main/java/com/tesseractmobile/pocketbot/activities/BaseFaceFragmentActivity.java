package com.tesseractmobile.pocketbot.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotThought;
import com.google.code.chatterbotapi.ChatterBotType;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.fragments.CallbackFragment;
import com.tesseractmobile.pocketbot.activities.fragments.EmotionsFragment;
import com.tesseractmobile.pocketbot.activities.fragments.FaceTrackingFragment;
import com.tesseractmobile.pocketbot.activities.fragments.RobotSelectionDialog;
import com.tesseractmobile.pocketbot.activities.fragments.TextPreviewFragment;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceFragment;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceFragmentFactory;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceInfo;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceListFragment;
import com.tesseractmobile.pocketbot.activities.wizard.WizardActivity;
import com.tesseractmobile.pocketbot.robot.AuthData;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.Emotion;
import com.tesseractmobile.pocketbot.robot.GoogleNearbyConnectionController;
import com.tesseractmobile.pocketbot.robot.GoogleSignInController;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.RobotInfo;
import com.tesseractmobile.pocketbot.robot.SensorControler;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.model.Speech;
import com.tesseractmobile.pocketbot.robot.model.TextInput;

import org.ros.android.MasterChooser;
import org.ros.android.RosFragmentActivity;
import org.ros.node.NodeMainExecutor;

import java.net.URI;

import io.fabric.sdk.android.Fabric;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BaseFaceFragmentActivity extends RosFragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener, KeepAliveThread.KeepAliveListener, KeepAliveThread.InternetAliveListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = BaseFaceFragmentActivity.class.getSimpleName();

    public static final String FRAGMENT_FACE_TRACKING = "FACE_TRACKING";
    public static final String FRAGMENT_FACE = "FACE";
    public static final String FRAGMENT_PREVIEW = "PREVIEW";

    private static final int RC_REQUEST_INVITE = 2;
    private static final int RC_SIGN_IN = 3;

    private SpeechAdapter mSpeechAdapter;

    private boolean mFaceTrackingActive;

    private SignInButton mSignInButton;

    private KeepAliveThread mKeepAliveThread;

    /** Receives sensor data and forwards it to the robot */
    private SensorControler mSensorControler;
    /** Handles Google Authentication */
    private GoogleSignInController mGoogleSignInController;

    /** For finding local network devices */
    private GoogleNearbyConnectionController mGoogleNearbyConnectionController;
    private DrawerLayout mDrawerLayout;
    private Disposable mSpeechInDisposable;
    private Disposable mSpeechOutDisposable;

    public BaseFaceFragmentActivity(){
        super("PocketBot", "PocketBot");
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.main);

        //Check for needed Android permissions
        checkForPermissions();

        if(Robot.get().isNew()){
            //After login show the robot selection dialog
            Robot.get().registerOnAuthCompleteListener(new DataStore.OnAuthCompleteListener() {
                @Override
                public void onAuthComplete(final AuthData authData) {
                    showRobotSelectionDialog();
                }
            });
        }

        //Setup face
        switchFace(PocketBotSettings.getSelectedFace(this));

        //Listen for sensor events
        mSensorControler = new SensorControler(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Allow user to control the volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        updateUI();

        //Show drawer to user
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        peekDrawer(mDrawerLayout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                drawerView.bringToFront();
                getWindow().getDecorView().requestLayout();
                getWindow().getDecorView().invalidate();
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        //Setup Google Sign in
        mGoogleSignInController = new GoogleSignInController(this);
        final View headerView = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
        mSignInButton = (SignInButton) headerView.findViewById(R.id.sign_in_button);
        mSignInButton.setSize(SignInButton.SIZE_STANDARD);
        mSignInButton.setScopes(mGoogleSignInController.getScopeArray());
        mSignInButton.setOnClickListener(this);
        if (PocketBotSettings.isAutoSignIn(this)) {
            mGoogleSignInController.startSignin(this, RC_SIGN_IN);
            //throw new UnsupportedOperationException();
        }
        //Setup click listeners
        headerView.findViewById(R.id.tvRobotId).setOnClickListener(this);
        headerView.findViewById(R.id.ivRobotIcon).setOnClickListener(this);
        headerView.findViewById(R.id.tvRobotName).setOnClickListener(this);

        //Start Nearby devices controller when authenticated
        Robot.get().registerOnAuthCompleteListener(new DataStore.OnAuthCompleteListener() {
            @Override
            public void onAuthComplete(AuthData authData) {
                startGoogleNearbyDevicesService(mGoogleSignInController.getGoogleApiClient());
            }
        });

    }

    /**
     * Check for Android M permissions
     */
    private void checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String[] permissionList = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            for(final String permission : permissionList){
                if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(permissionList, 0);
                    return;
                }
            }
        }
    }

    /**
     * Start searching for devices on the local network
     */
    private void startGoogleNearbyDevicesService(final GoogleApiClient googleApiClient){
        if(googleApiClient.isConnected() == false){
            return;
        }
        //Look for nearby Pocketbots
        mGoogleNearbyConnectionController = new GoogleNearbyConnectionController(googleApiClient);
        //Look for nearby devices
        mGoogleNearbyConnectionController.startAdvertising(this, googleApiClient);
        mGoogleNearbyConnectionController.startDiscovery(this, googleApiClient);
    }

    @Override
    public void onHeartBeat() {
        //Keep Arduino awake
        Robot.get().sendSensorData(false);
    }

    @Override
    public void onInternetTimeout() {
        Robot.get().getSensorData().setControl(new SensorData.Control());
        Robot.get().sendSensorData(true);
    }

    private void updateUI() {
        //Setup Robot Id
        final View navigationView = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
        final TextView tvRobotId = (TextView) navigationView.findViewById(R.id.tvRobotId);
        //tvRobotId.setText(PocketBotSettings.getRobotId(this));
        final WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        final String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        tvRobotId.setText(ip);
        //Setup Robot Name
        final TextView tvRobotName = (TextView) navigationView.findViewById(R.id.tvRobotName);
        tvRobotName.setText(PocketBotSettings.getRobotName(this));
    }



    protected void peekDrawer(final DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        }, 3000);
//        final long downTime = SystemClock.uptimeMillis();
//        final long eventTime = SystemClock.uptimeMillis() + 100;
//        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 0, 100, 0);
//        drawerLayout.dispatchTouchEvent(motionEvent);
//        motionEvent.recycle();
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                final long downTime = SystemClock.uptimeMillis();
//                final long eventTime = SystemClock.uptimeMillis() + 100;
//                MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, 0, 100, 0);
//                drawerLayout.dispatchTouchEvent(motionEvent);
//                motionEvent.recycle();
//            }
//        }, (long) (2 * DateUtils.SECOND_IN_MILLIS));
    }

    private void setupTextPreview(final TextPreviewFragment textPreviewFragment) {
        //Setup list view for text
        mSpeechAdapter = new SpeechAdapter(BaseFaceFragmentActivity.this);
        ListView listView = textPreviewFragment.getListView();
        listView.setAdapter(mSpeechAdapter);
    }

    /**
     * Will direct user to play store to update Google Play Services if needed
     * @return true if google play services is available
     */
    private boolean checkGooglePlayServices() {
        final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, GooglePlayServicesUtil.getErrorString(status));

            // ask user to update google play services.
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 1);
            dialog.show();
            return false;
        } else {
            Log.i(TAG, GooglePlayServicesUtil.getErrorString(status));
            // google play services is updated.
            //your code goes here...
            return true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Listen for preference changes
        PocketBotSettings.registerOnSharedPreferenceChangeListener(this, this);
        //Listen for speech to update preview
        Robot.get().getTextInputSubject().subscribe(new Observer<TextInput>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                mSpeechInDisposable = d;
            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull TextInput textInput) {
                addTextToList(textInput.text, false);
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        Robot.get().getSpeechSubject().subscribe(new Observer<Speech>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                mSpeechOutDisposable = d;
            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull Speech speech) {
                addTextToList(speech.text, true);
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        //Keep alive thread
        if(PocketBotSettings.isKeepAlive(this)) {
            mKeepAliveThread = new KeepAliveThread(this, this);
            mKeepAliveThread.startThread();
        }

    }

    @Override
    protected void init(final NodeMainExecutor nodeMainExecutor) {
        //This is run from an acync task so update the ui from a runnable
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final String masterUri = getMasterUri().toString();
                final String finalUri;
                if(masterUri.contains("android")){
                    WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                    String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                    finalUri = "http://" + ip + ":11311";
                } else {
                    finalUri = "ROS URI: " + masterUri;
                }
                TextView tvId = (TextView) findViewById(R.id.tvRobotId);
                if(tvId != null){
                    tvId.setText(finalUri);
                }
            }
        });
        //Create main PocketBot nodes
        new PocketBotNode(nodeMainExecutor, getMasterUri());
    }

    @Override
    public void onStop() {
        super.onStop();
        //Listen for preference changes
        PocketBotSettings.unregisterOnSharedPreferenceChangeListener(this, this);
        //Stop listening for speech to update preview
        final Disposable inDisposeable = mSpeechInDisposable;
        if(inDisposeable != null && !inDisposeable.isDisposed()){
            inDisposeable.dispose();
        }
        final Disposable outDisposeable = mSpeechOutDisposable;
        if(outDisposeable != null && !outDisposeable.isDisposed()){
            outDisposeable.dispose();
        }
        //Keep alive thread
        if(PocketBotSettings.isKeepAlive(this)) {
            mKeepAliveThread.stopThread();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorControler.onResume(this, Robot.get());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorControler.onPause(this);
    }

    /**
     * Set the face to mimic the emotional state
     *
     * @param emotion
     */
    final public void setEmotion(final Emotion emotion) {
        Robot.get().setEmotion(emotion);
    }

    private void addTextToList(final String text, final boolean isPocketBot) {

        final SpeechAdapter speechAdapter = this.mSpeechAdapter;
        if(speechAdapter != null){
            speechAdapter.addText(text, isPocketBot);
        }
    }

    protected void doTextInput(String input) {
        new BotTask().execute(input);
    }


    /**
     * Change active face
     * @param faceId
     */
    private void switchFace(int faceId){
        final FragmentManager supportFragmentManager = getFragmentManager();
        final FragmentTransaction ft = supportFragmentManager.beginTransaction();
        final FaceFragment faceFragment = FaceFragmentFactory.getFaceFragment(faceId);
        final boolean isUseFaceTracking = faceFragment.isUseFaceTracking() && checkGooglePlayServices();

        if(supportFragmentManager.findFragmentByTag(FRAGMENT_FACE) != null){
            ft.replace(R.id.faceView, faceFragment, FRAGMENT_FACE);
        } else {
            ft.add(R.id.faceView, faceFragment, FRAGMENT_FACE);
        }

        //Check if face tracking is used
        if(isUseFaceTracking){
            if(mFaceTrackingActive == false) {
                mFaceTrackingActive = true;
                final TextPreviewFragment textPreviewFragment = new TextPreviewFragment();
                final FaceTrackingFragment faceTrackingFragment = new FaceTrackingFragment();
                //Create FaceTrackingFragment
                ft.add(R.id.overlayView, faceTrackingFragment, FRAGMENT_FACE_TRACKING);
                //Create Preview Fragment
                ft.add(R.id.topOverlayView, textPreviewFragment, FRAGMENT_PREVIEW);
                //Set up a listener for when the view is created
                if (textPreviewFragment != null) {
                    textPreviewFragment.setOnCompleteListener(new CallbackFragment.OnCompleteListener() {

                        @Override
                        public void onComplete() {
                            setupTextPreview(textPreviewFragment);
                        }
                    });
                }
                if (faceTrackingFragment != null) {
                    faceTrackingFragment.setRobotInterface(Robot.get());
                }
            }
        } else {
            if(mFaceTrackingActive) {
                mFaceTrackingActive = false;
                final Fragment faceTrackingFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_FACE_TRACKING);
                if (faceFragment != null) {
                    ft.remove(faceTrackingFragment);
                }
                final Fragment previewFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_PREVIEW);
                if (previewFragment != null) {
                    ft.remove(previewFragment);
                }
            }
        }
        //Commit changes
        ft.commitAllowingStateLoss();

        //Set icon and background for header view
        final View headerView = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
        final ImageView robotIcon = (ImageView) headerView.findViewById(R.id.ivRobotIcon);
        final FaceInfo faceInfo = FaceFragmentFactory.getFaceInfo(faceId);
        robotIcon.setImageResource(faceInfo.icon);
        //headerView.setBackgroundResource(faceInfo.background);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(PocketBotSettings.KEY_SELECTED_FACE.equals(key)){
            final int faceId = sharedPreferences.getInt(key, PocketBotSettings.DEFAULT_FACE_ID);
            switchFace(faceId);
        } else if(PocketBotSettings.KEY_KEEP_ALIVE.equals(key)) {
            final boolean keepAlive = sharedPreferences.getBoolean(key, PocketBotSettings.DEFAULT_KEEP_ALIVE);
            if(keepAlive){
                //Start the thread
                mKeepAliveThread = new KeepAliveThread(this, this);
                mKeepAliveThread.startThread();
            } else {
                //Stop the thread
                final KeepAliveThread keepAliveThread = this.mKeepAliveThread;
                if(keepAliveThread != null){
                    keepAliveThread.stopThread();
                }
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI();
                }
            });
        }
    }

    /**
     * Time between data sent to the robot
     * @param i in millis
     */
    protected void setSensorDelay(int i) {
        Robot.get().setSensorDelay(i);
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        switch (id){
            case R.id.tvRobotName:
            case R.id.ivRobotIcon:
                showRobotSelectionDialog();
                break;
            case R.id.sign_in_button:
                mGoogleSignInController.startSignin(this, RC_SIGN_IN);
                break;
            case R.id.tvRobotId:
                startActivityForResult(new Intent(this, MasterChooser.class), RosFragmentActivity.MASTER_CHOOSER_REQUEST_CODE);
                break;
            default:
                throw new UnsupportedOperationException("Not implemented");
        }
    }

    private void launchFeedback() {
        //Launch user feedback website
        String url = "https://feedback.userreport.com/eb1b841d-4f55-44a7-9432-36e77efefb77/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void sendInvite() {
        Robot.get().registerOnAuthCompleteListener(new DataStore.OnAuthCompleteListener() {
            @Override
            public void onAuthComplete(final AuthData authData) {
                final String displayName = authData.getDisplayName();
                final String robotName = PocketBotSettings.getRobotName(BaseFaceFragmentActivity.this);
                final String message = displayName + " is giving you control of " + robotName;
                Intent intent = new AppInviteInvitation.IntentBuilder("PocketBot Control Invite")
                        .setMessage(message)
                        .setDeepLink(Uri.parse("http://pocketbot.tesseractmobile.com/usbserialfragment/" + PocketBotSettings.getRobotId(BaseFaceFragmentActivity.this)))
                        //.setCustomImage(Uri.parse((String) authData.getProviderData().get("profileImageURL")))
                        //.setCallToActionText("invitation_cta")
                        .setEmailHtmlContent("<html><body>"
                                + "<a href=\"%%APPINVITE_LINK_PLACEHOLDER%%\">" + "Click here to start controlling " + robotName + ".<br><br>"
                                + "<img src=\"" + authData.getProfileImageURL() + "\" height=\"130\" width=\"130\"/></a>"
                                + "</body></html>")
                        .setEmailSubject("PocketBot Control Invite from " + displayName)
                        .build();
                startActivityForResult(intent, RC_REQUEST_INVITE);
            }
        });
    }


    private void showRobotSelectionDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RobotSelectionDialog robotSelectionDialog = new RobotSelectionDialog();
                robotSelectionDialog.setOnlyUserRobots(true);
                robotSelectionDialog.setSignInOnClickListener(BaseFaceFragmentActivity.this);
                robotSelectionDialog.show(getFragmentManager(), "ROBOT_SELECTION_DIALOG");
                robotSelectionDialog.setOnRobotSelectedListener(new RobotSelectionDialog.OnRobotSelectedListener() {
                    @Override
                    public void onRobotSelected(RobotInfo.Settings model) {
                        PocketBotSettings.setRobotId(BaseFaceFragmentActivity.this, model.prefs.r_id);
                        //Check if name should be set
                        if (model.prefs.robot_name.equals(PocketBotSettings.DEFAULT_ROBOT_NAME)) {
                            //Launch name change dialog
                            //Open draw
                            //Tell user to change the robot name
                            Toast.makeText(BaseFaceFragmentActivity.this, "You should change your robot name.", Toast.LENGTH_LONG).show();
                            ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case RC_SIGN_IN:
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    mGoogleSignInController.handleSignInResult(this, result);
                    break;
                case RC_REQUEST_INVITE:
                    String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                    Log.d(TAG, "Sent " + ids.length + " Invitations");
                    break;
                case RosFragmentActivity.MASTER_CHOOSER_REQUEST_CODE:
                    //save the ros master uri
                    PocketBotSettings.setRosMasterUri(this, nodeMainExecutorService.getMasterUri().toString());
                    break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Close the drawer
        mDrawerLayout.closeDrawer(GravityCompat.START);
        //Handle the click
        final int id = item.getItemId();
        if(id == R.id.nav_faces){
            //Launch face selection dialog
            new FaceListFragment().show(getFragmentManager(), "FACE_DIALOG");
            return true;
        } else if(id == R.id.nav_emotions){
            new EmotionsFragment().show(getFragmentManager(), "EMOTION_DIALOG");
            return true;
        } else if(id == R.id.nav_settings){
            //Check for Settings fragment
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if(id == R.id.nav_share){
            sendInvite();
            return true;
        } else if(id == R.id.nav_feedback){
            launchFeedback();
            return true;
        } else if(id == R.id.nav_wizard){
            startActivity(WizardActivity.getLaunchIntent(this));
            finish();
            return true;
        }
        throw new UnsupportedOperationException("Not implemented! " + item.toString());
    }

    private class BotTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {

            // Create a chat bot
            final ChatterBotFactory factory = new ChatterBotFactory();
            ChatterBot bot1;
            try {
                // final ChatterBot bot2 =
                bot1 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
                // bot1 = factory.create(ChatterBotType.CLEVERBOT);
                // bot1 = factory.create(ChatterBotType.JABBERWACKY);
            } catch (final Exception e) {
                Robot.get().say("There was an error loading ChatterBotFactory()");
                return null;
            }
            final ChatterBotSession bot1session = bot1.createSession();
            String response = null;
            try {
                // Get the response from the chat bot
                final ChatterBotThought chatterBotThought = new ChatterBotThought();
                chatterBotThought.setText(params[0]);
                chatterBotThought.setEmotions(new String[]{"Happy"});
                final ChatterBotThought responseThought = bot1session.think(chatterBotThought);
                // Check for emotions
                final String[] emotions = responseThought.getEmotions();
                if (emotions != null) {
                    response = emotions[0];
                }
                if (response == null) {
                    // No emotions use words
                    // Strip HTML
                    final String cleanedResponce = Html.fromHtml(responseThought.getText()).toString();
                    response = cleanedResponce;
                } else {
                    response = "I feel somthing. It might be " + response + ".";
                }
            } catch (final Exception e) {
                // Tell user what went wrong
                Robot.get().say("Error in BotTask.doInBackground");
                Log.e("BotTask", e.toString());
                return null;
            }
            if (response.length() != 0) {
                //Speak the text and listen for a response
                Robot.get().listen(response);
            } else {
                Robot.get().say("I can't think of anything to say.");
            }

            return null;
        }
    }

    @Override
    public URI getSavedMasterUri() {
        final String savedHost = PocketBotSettings.getRosMasterUri(this);
        if(savedHost == null || savedHost.equals("")){
            return null;
        } else {
            final URI rosUri = URI.create(savedHost);
            return  rosUri;
        }
    }
}

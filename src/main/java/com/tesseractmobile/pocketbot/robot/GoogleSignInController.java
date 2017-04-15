package com.tesseractmobile.pocketbot.robot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.io.IOException;

/**
 * Created by josh on 3/27/2016.
 */
public class GoogleSignInController implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private static final String TAG = GoogleSignInController.class.getSimpleName();

    private final GoogleSignInOptions gso;
    /* Client used to interact with Google APIs. */
    private final GoogleApiClient mGoogleApiClient;
    private final FirebaseAuth mAuth;


    public GoogleSignInController(final FragmentActivity fragmentActivity) {
       gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("547931764939-du412euoccilddpl427nc33u5lp0s0vd.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(fragmentActivity)
                //.enableAutoManage(fragmentActivity, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Starts the sign in or signs out if already signed in
     * @param fragmentActivity
     * @param requestCode
     */
    public void startSignin(final FragmentActivity fragmentActivity, final int requestCode) {
        if(mGoogleApiClient.isConnected()){
            FirebaseAuth.getInstance().signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    mGoogleApiClient.disconnect();
                    Toast.makeText(fragmentActivity, "Signed Out!", Toast.LENGTH_LONG);
                }
            });
        } else {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            fragmentActivity.startActivityForResult(signInIntent, requestCode);
        }
    }

    public void handleSignInResult(final Context context, final GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(context, acct);
            //getGoogleOAuthTokenAndLogin(context, acct.getEmail());
        } else {
            // Signed out, show unauthenticated UI.
            throw new UnsupportedOperationException();
        }
    }

    private void firebaseAuthWithGoogle(final Context context, final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        // ...
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(context, "Could not sign in: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        final AuthData authData = new AuthData(acct);
                        onTokenReceived(context, authData);
                    }
                });
    }


    private Context mContext;
    private AuthData mAuthData;
    private void onTokenReceived(final Context context, final AuthData authData){
        mAuthData = authData;
        mContext = context;

        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle result) {
        if(mGoogleApiClient.isConnected()) {
            Robot.get().setAuthToken(PocketBotSettings.getRobotId(mContext), mAuthData);
            Toast.makeText(mContext, "Google Sign-In Complete", Toast.LENGTH_LONG).show();
            //mSignInButton.setEnabled(true);
            //Auto sign in next time
            PocketBotSettings.setAutoSignIn(mContext, true);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //I don't think anything needs to be done here - this would be rare
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {
        //I don't think anything needs to be done here - this would be rare
    }

    public Scope[] getScopeArray() {
        return gso.getScopeArray();
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}

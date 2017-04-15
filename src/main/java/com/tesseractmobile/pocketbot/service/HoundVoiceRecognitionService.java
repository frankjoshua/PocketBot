package com.tesseractmobile.pocketbot.service;

import android.location.LocationManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.android.sdk.VoiceSearch;
import com.hound.android.sdk.VoiceSearchInfo;
import com.hound.android.sdk.VoiceSearchListener;
import com.hound.android.sdk.audio.SimpleAudioByteStreamSource;
import com.hound.android.sdk.util.HoundRequestInfoFactory;
import com.hound.core.model.sdk.HoundRequestInfo;
import com.hound.core.model.sdk.HoundResponse;
import com.hound.core.model.sdk.PartialTranscript;
import com.tesseractmobile.pocketbot.robot.Constants;
import com.tesseractmobile.pocketbot.robot.Robot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by josh on 7/13/16.
 */
public class HoundVoiceRecognitionService extends BaseVoiceRecognitionService implements VoiceSearchListener {

    private VoiceSearch voiceSearch;
    private JsonNode lastConversationState;
    private String mInputText = new String();

    @Override
    public void startListening() {
        if (voiceSearch != null) {
            return; // We are already searching
        }

        /**
         * Example of using the VoiceSearch.Builder to configure a VoiceSearch object
         * which is then use to run the voice search.
         */
        voiceSearch = new VoiceSearch.Builder()
                .setRequestInfo( getHoundRequestInfo() )
                .setAudioSource( new SimpleAudioByteStreamSource() )
                .setClientId( Constants.HOUND_CLIENT_ID)
                .setClientKey( Constants.HOUND_CLIENT_KEY)
                .setListener( this )
                .build();

        // Kickoff the search. This will start listening from the microphone and streaming
        // the audio to the Hound server, at the same time, waiting for a response which will be passed
        // back as a result to the voiceListener registered above.
        voiceSearch.start();
        setState(VoiceRecognitionState.STARTING_LISTENING);
    }

    /**
     * Helper method called from the startSearch() method below to fill out user information
     * needed in the HoundRequestInfo query object sent to the Hound server.
     *
     * @return
     */
    private HoundRequestInfo getHoundRequestInfo() {
        final HoundRequestInfo requestInfo = HoundRequestInfoFactory.getDefault(this);

        // Client App is responsible for providing a UserId for their users which is meaningful to the client.
        requestInfo.setUserId("User ID");
        // Each request must provide a unique request ID.
        requestInfo.setRequestId(UUID.randomUUID().toString());
        // Providing the user's location is useful for geographic queries, such as, "Show me restaurants near me".
        //setLocation( requestInfo, locationManager.getLastKnownLocation( LocationManager.PASSIVE_PROVIDER ));

        // for the first search lastConversationState will be null, this is okay.  However any future
        // searches may return us a conversation state to use.  Add it to the request info when we have one.
        requestInfo.setConversationState( lastConversationState );
        return requestInfo;
    }

    @Override
    public void onTranscriptionUpdate(PartialTranscript transcript) {
        mInputText = transcript.getPartialTranscript();
    }

    @Override
    public void onResponse(HoundResponse response, VoiceSearchInfo info) {
        setState(VoiceRecognitionState.READY);
        voiceSearch = null;

        if (!response.getResults().isEmpty()) {
            // Save off the conversation state.  This information will be returned to the server
            // in the next search. Note that at some point in the future the results CommandResult list
            // may contain more than one item. For now it does not, so just grab the first result's
            // conversation state and use it.
            lastConversationState = response.getResults().get(0).getConversationState();
            Robot.get().say(response.getResults().get(0).getSpokenResponseLong());
        }
        onTextInput(mInputText);
//        try {
//            //onTextInput(new JSONObject(info.getContentBody()).toString(4));
//        } catch (JSONException e) {
//            //onTextInput("Error in speech result");
//        }
    }

    @Override
    public void onError(Exception e, VoiceSearchInfo info) {
        voiceSearch = null;
    }

    @Override
    public void onAbort(VoiceSearchInfo info) {
        voiceSearch = null;
    }

    @Override
    public void onRecordingStopped() {

    }
}

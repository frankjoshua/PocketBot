package com.tesseractmobile.pocketbot.activities;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.Robot;

import java.util.UUID;

public class FirebaseFaceFragmentActivity extends BaseFaceFragmentActivity {

    private static final String CHILD_PATH = "chat";
    private DatabaseReference mFirebaseRef;
    private String userId;
    boolean firstResponce;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Read User Id
        userId = getPreferences(MODE_PRIVATE).getString("uuid", UUID.randomUUID().toString());
        //Save User Id
        getPreferences(MODE_PRIVATE).edit().putString("uuid", userId).commit();

        mFirebaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(DataStore.FIREBASE_URL).child(CHILD_PATH);

        mFirebaseRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                Chat chat = null;
                for (final DataSnapshot childSnapshot: snapshot.getChildren()) {
                    chat = childSnapshot.getValue(Chat.class);
                }
                if(chat != null){
                    if(!chat.user.equals(userId)){
                        //Ignore first response
                        if(firstResponce){
                            Robot.get().say(chat.text);
                        } else {
                            firstResponce = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(final DatabaseError error) {
            }

        });
    }

    @Override
    public void doTextInput(final String input) {
        final Chat chat = new Chat(userId, input);
        mFirebaseRef.push().setValue(chat);
    }
}

package com.pharos.webrtc.SignallingServer;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignallingClient {

    private DatabaseReference dbRef;

    public SignallingClient(DatabaseReference dbRef) {
        this.dbRef = dbRef;

        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void doLogin(String username){
        // create room with the name of the user
    }


    public void doOffer(String username,String offer){
        // send offer request with the name of the user
    }


    public void doAnswer(String username, String answer){
        // create answer with the name of the user
    }


    public void doCandidate(String username , String candidate){
        // create and add candidate with the name of the user
    }


    public void doHangup(String username){
        // end the call

    }

}

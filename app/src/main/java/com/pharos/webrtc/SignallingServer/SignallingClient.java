package com.pharos.webrtc.SignallingServer;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

//firebase is used as the Signalling client here REALTIME DATABSE

public class SignallingClient {


    private static SignallingClient instance;
    private String roomName = null;
    public boolean isChannelReady = false;
    public boolean isInitiator = false;
    public boolean isStarted = false;

    private DatabaseReference dbRef;
    private SignalingInterface callback;


    public static SignallingClient getInstance() {
        if (instance == null) {
            instance = new SignallingClient();
        }
        if (instance.roomName == null) {
            //set the room name here
            instance.roomName = "pharos";
        }
        return instance;
    }

    public void doLogin(String username) {
        // create room with the name of the user

    }


    public void doOffer(String username, String offer) {
        // send offer request with the name of the user
    }


    public void doAnswer(String username, String answer) {
        // create answer with the name of the user
    }


    public void doCandidate(String username, String candidate) {
        // create and add candidate with the name of the user
    }


    public void doHangup(String username) {
        // end the call

    }


    public interface SignalingInterface {
        void onRemoteHangUp(String msg);

        void onOfferReceived(JSONObject data);

        void onAnswerReceived(JSONObject data);

        void onIceCandidateReceived(JSONObject data);

        void onTryToStart();

        void onCreatedRoom();

        void onJoinedRoom();

        void onNewPeerJoined();
    }

}

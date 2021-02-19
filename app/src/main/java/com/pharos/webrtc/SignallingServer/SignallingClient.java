package com.pharos.webrtc.SignallingServer;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.HashMap;
//firebase is used as the Signalling client here REALTIME DATABSE

public class SignallingClient {

    private static SignallingClient instance;
    private static DatabaseReference dbRef;
    private String roomName = null,userName=null;
    public boolean isChannelReady = false;
    public boolean isInitiator = false;
    public boolean isStarted = false;

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


    public void init(SignalingInterface signalingInterface,String name){

        this.callback=signalingInterface;
        this.userName=name;
        isChannelReady=true;
        isInitiator=true;
        dbRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomName);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    callback.onJoinedRoom();
                    if (!snapshot.child("sender").getValue().equals(name)) {
                        callback.onNewPeerJoined();
                        isChannelReady=true;
                        //TODO on ICe Candidate Recived

                        if (snapshot.child("type").getValue().equals("candidate")) {
                            String data = snapshot.child("data").getValue().toString();
                            try {
                                callback.onIceCandidateReceived(new JSONObject(data));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                        //on TODO Answer Recived....
                        else if (snapshot.child("type").getValue().equals("amswer")) {
                            String data = snapshot.child("data").getValue().toString();
                            try {
                                callback.onAnswerReceived(new JSONObject(data));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                        //TODO on Offer recived....
                        else if (snapshot.child("type").getValue().equals("offer")) {
                            String data = snapshot.child("data").getValue().toString();
                            try {
                                callback.onOfferReceived(new JSONObject(data));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        else {
                            callback.onRemoteHangUp(name);
                        }
                    }
                }
                else {
                    callback.onCreatedRoom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    public void sendMessage(SessionDescription message){

        if (message.type.canonicalForm().equals("offer")){
            doOffer(userName,message.description);
        }
        else if (message.type.canonicalForm().equals("answer")){
            doAnswer(userName,message.description);
        }

    }

    public void doOffer(String username, String description) {
        // send offer request with the name of the user
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("type","offer");
        hashMap.put("sdp",description);
        hashMap.put("sender",username);

        dbRef.child(username).updateChildren(hashMap);

    }


    public void doAnswer(String username, String description) {
        // create answer with the name of the user

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("type","answer");
        hashMap.put("sdp",description);
        hashMap.put("sender",username);

        dbRef.child(username).updateChildren(hashMap);

    }


    public void sendIceCandidate(IceCandidate iceCandidate) {
        // create and add candidate with the name of the user

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("type","candidate");
        hashMap.put("label",iceCandidate.sdpMLineIndex);
        hashMap.put("id",iceCandidate.sdpMid);
        hashMap.put("candidate",iceCandidate.sdp);
        hashMap.put("sender",userName);

        dbRef.child(userName).updateChildren(hashMap);

    }


    public void doHangup(String username) {
        // end the call

        callback.onRemoteHangUp(username);

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

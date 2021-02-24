package com.pharos.webrtc.SignallingServer;

import android.content.Context;
import android.util.Log;
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
//firebase is used as the Signalling client here REALTIME DATABASE
public class SignallingClient {

    private static SignallingClient instance;
    private static DatabaseReference dbRef;
    private String roomName = null, userName = null;
    public boolean isChannelReady = false;
    public boolean isInitiator = false;
    public boolean isStarted = false;
    private Context mContext;

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


    public void init(SignalingInterface signalingInterface, String name, Context context) {

        this.callback = signalingInterface;
        this.userName = name;
        isChannelReady = true;
        isInitiator = true;
        this.mContext = context;
        dbRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomName);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    callback.onJoinedRoom();

                    for (DataSnapshot user : snapshot.getChildren()) {
                        String Uname = (String) user.child("sender").getValue();
                        String type = (String) user.child("type").getValue();
                        Log.v("paresh", type+"type reciveede in firebase");
                        if (!Uname.equals(name)) {
                            callback.onNewPeerJoined();
                            isChannelReady = true;
                            if (type.equals("candidate")) {

                                String data = user.child("sdp").getValue().toString();
                                Integer label = Integer.parseInt(user.child("label").getValue().toString());
                                String id = user.child("id").getValue().toString();
                                callback.onIceCandidateReceived(data, label, id);

                            }
                            else if (type.equals("amswer")) {

                                    String data = user.child("sdp").getValue().toString();
                                        callback.onAnswerReceived(data);

                            }
                            else if (type.equals("offer")) {
                                    String data = user.child("sdp").getValue().toString();
                                        callback.onOfferReceived(data);

                            } else {
                                callback.onRemoteHangUp(name);
                            }
                        }
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    public void sendMessage(SessionDescription message) {

        if (message.type.canonicalForm().equals("offer")) {
            doOffer(userName, message.description);
        } else if (message.type.canonicalForm().equals("answer")) {
            doAnswer(userName, message.description);
        }
    }

    public void doOffer(String username, String description) {
        // send offer request with the name of the user
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", "offer");
        hashMap.put("sdp", description);
        hashMap.put("sender", username);
        dbRef.child(username).updateChildren(hashMap);

    }


    public void doAnswer(String username, String description) {
        // create answer with the name of the user
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", "answer");
        hashMap.put("sdp", description);
        hashMap.put("sender", username);

        dbRef.child(username).updateChildren(hashMap);

    }


    public void sendIceCandidate(IceCandidate iceCandidate) {
        // create and add candidate with the name of the user

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", "candidate");
        hashMap.put("label", iceCandidate.sdpMLineIndex);
        hashMap.put("id", iceCandidate.sdpMid);
        hashMap.put("sdp", iceCandidate.sdp);
        hashMap.put("sender", userName);

        dbRef.child(userName).updateChildren(hashMap);

    }


    public void doHangup(String username) {
        // end the call
        dbRef.child(username).removeValue();
        dbRef=null;

    }

    public interface SignalingInterface {
        void onRemoteHangUp(String msg);

        void onOfferReceived(String data);

        void onAnswerReceived( String sdp);

        void onIceCandidateReceived(String data, int label, String id);

        void onTryToStart();

        void onCreatedRoom();

        void onJoinedRoom();

        void onNewPeerJoined();
    }

}

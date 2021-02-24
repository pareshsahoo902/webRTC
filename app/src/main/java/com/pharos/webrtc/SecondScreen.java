package com.pharos.webrtc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pharos.webrtc.Observer.CustomPeerConnectionObserver;
import com.pharos.webrtc.Observer.CustomSdpObserver;
import com.pharos.webrtc.SignallingServer.SignallingClient;
import com.pharos.webrtc.VC.VideoCallActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static org.webrtc.SessionDescription.*;

public class SecondScreen extends AppCompatActivity implements SignallingClient.SignalingInterface {

    private static final int RC_CALL = 111;
    public static final int VIDEO_RESOLUTION_WIDTH = 852;
    public static final int VIDEO_RESOLUTION_HEIGHT = 480;
    private static final String TAG = "SecondScreen";
    public static final int FPS = 30;
    private String roomID = null, name = null;
    private Boolean mute = true, videoOn = false, front_cam = true;
    private ImageView rotate_cam, mute_unmute, videoon_off, hangup, menu;

    PeerConnection localpeer;
    private EglBase rootEglBase;

    private PeerConnectionFactory peerConnectionFactory;

    MediaConstraints audioConstraints;
    MediaConstraints videoConstraints;
    MediaConstraints sdpConstraints;
    VideoSource videoSource;
    VideoTrack localVideoTrack;
    AudioSource audioSource;
    AudioTrack localAudioTrack;
    SurfaceTextureHelper surfaceTextureHelper;
    VideoTrack videoTrack;

    private SurfaceViewRenderer surfaceView1, remotesurface;
    private TextView local_peername, remote_peername, roomidText;

    boolean gotUserMedia;
    List<PeerConnection.IceServer> peerIceServers = new ArrayList<>();

    final int ALL_PERMISSIONS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video_calling_activity);
        roomID = getIntent().getStringExtra("roomid");
        name = getIntent().getStringExtra("name");


        init();
        clickers();
        start();

    }

    private void init() {
        mute_unmute = findViewById(R.id.togglemic);
        videoon_off = findViewById(R.id.toggle_video);
        hangup = findViewById(R.id.hangup);
        rotate_cam = findViewById(R.id.flip_cam);
        surfaceView1 = findViewById(R.id.surface_view);
        remotesurface = findViewById(R.id.surfacenew);
        remote_peername = findViewById(R.id.namepeer);
//        menu = findViewById(R.id.menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    private void clickers() {
        mute_unmute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mute == true) {
                    mute_unmute.setImageResource(R.drawable.unmute);
                    mute = false;
                    unmuteAudio();
                } else {
                    mute_unmute.setImageResource(R.drawable.mute);
                    mute = true;
                    muteAudio();
                }
            }
        });

        videoon_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoOn == true) {
                    videoon_off.setImageResource(R.drawable.novideo);
                    videoOn = false;
                    turnOffVideo();
                } else {
                    videoon_off.setImageResource(R.drawable.videoon);
                    videoOn = true;
                    turnOnVideo();
                }
            }
        });

        hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Toast.makeText(SecondScreen.this, "Call Ended.", Toast.LENGTH_SHORT).show();
            }
        });

        rotate_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SecondScreen.this, "Roatate cam!", Toast.LENGTH_SHORT).show();
                rotateCamera();
            }
        });

    }

    private void muteAudio() {
        //TODO mute audiostream on button click
    }

    private void unmuteAudio() {
        //TODO enable audiostream on button click

    }

    private void turnOnVideo() {
        //TODO enable videostream on button click

    }

    private void turnOffVideo() {
        //TODO disable videostream on button click

    }

    private void rotateCamera() {
        //TODO change camera on button click

        if (front_cam) {
            front_cam = false;
        } else {
            front_cam = true;
        }

    }


    private void initVideos() {

        rootEglBase = EglBase.create();
        surfaceView1.init(rootEglBase.getEglBaseContext(), null);
        surfaceView1.setZOrderMediaOverlay(true);

        rootEglBase = EglBase.create();
        remotesurface.init(rootEglBase.getEglBaseContext(), null);
        remotesurface.setZOrderMediaOverlay(true);
    }

    @AfterPermissionGranted(RC_CALL)
    private void start() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
            initVideos();
            getIceServers();
            Log.v("paresh", "peerConnection Init");

            SignallingClient.getInstance().init(this, name, getApplicationContext());

            //Initialize PeerConnectionFactory globals.
            PeerConnectionFactory.InitializationOptions initializationOptions =
                    PeerConnectionFactory.InitializationOptions.builder(this)
                            .createInitializationOptions();
            PeerConnectionFactory.initialize(initializationOptions);

            //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                    rootEglBase.getEglBaseContext(), true, true);
            DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
            peerConnectionFactory = PeerConnectionFactory.builder()
                    .setOptions(options)
                    .setVideoEncoderFactory(defaultVideoEncoderFactory)
                    .setVideoDecoderFactory(defaultVideoDecoderFactory)
                    .createPeerConnectionFactory();

            //VideoCapturer instance.
            VideoCapturer videoCapturerAndroid;
            videoCapturerAndroid = createCameraCapturer(new Camera1Enumerator(true));

            //Create MediaConstraints .
            audioConstraints = new MediaConstraints();
            videoConstraints = new MediaConstraints();

            if (videoCapturerAndroid != null) {
                surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
                videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid.isScreencast());
                videoCapturerAndroid.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());

            }
            localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);


            //AudioSource instance
            audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
            localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

            if (videoCapturerAndroid != null) {
                videoCapturerAndroid.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
            }
            //add our renderer to the VideoTrack.
            Log.v("paresh", "added local feed in camera");

            localVideoTrack.addSink(remotesurface);

            remotesurface.setMirror(true);
            surfaceView1.setMirror(true);

            if (SignallingClient.getInstance().isInitiator) {
                onTryToStart();
            }

        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }

    private void getIceServers() {
        Log.v("paresh", "getting ice server");
        peerIceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

    }


    @Override
    public void onTryToStart() {

        runOnUiThread(() -> {
            if (!SignallingClient.getInstance().isStarted && localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
                createPeerConnection();
                Log.v("paresh", "trying to start");
                Log.v("paresh", localpeer.toString());
                if (SignallingClient.getInstance().isInitiator) {
                    doCall();
                }
            }
        });

    }


    //create peer connection
    private void createPeerConnection() {
        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(peerIceServers);
        SignallingClient.getInstance().isStarted = true;
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.ALL;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        Log.v("paresh", "created peer connection");

        localpeer = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver("localPeerCreation") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                //add ice candiadate
                Log.v("paresh", "generated ice candidate");
                onIceCandidateReceived(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                Log.v("paresh", "Remote stream recived");
                gotRemoteStream(mediaStream);
            }
        });

        addStreamToLocalPeer();

    }

    private void addStreamToLocalPeer() {
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localAudioTrack);
        stream.addTrack(localVideoTrack);
        Log.v("paresh", "adding stream to local peer");
        localpeer.addStream(stream);
    }

    private void doCall() {
        sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        Log.v("paresh", "Creating offer-------------------------------------------");

        localpeer.createOffer(new CustomSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                localpeer.setLocalDescription(new CustomSdpObserver(), sessionDescription);
                Log.v("paresh", "SignallingClient emit ");
                SignallingClient.getInstance().sendMessage(sessionDescription);

            }
        }, sdpConstraints);
    }

    public void onIceCandidateReceived(IceCandidate iceCandidate) {
        //we have received ice candidate. We can set it to the other peer.
        Log.v("paresh", "Sending Ice Candidate-----------------------------------------------------------------------");

        SignallingClient.getInstance().sendIceCandidate(iceCandidate);
    }


    @Override
    public void onOfferReceived(String data) {
        runOnUiThread(() -> {
            if (!SignallingClient.getInstance().isInitiator && !SignallingClient.getInstance().isStarted) {
                onTryToStart();
            }
            Log.v("paresh", "setting remote description=-----------------------------------------");
            localpeer.setRemoteDescription(new CustomSdpObserver(), new SessionDescription(Type.OFFER, data));
            doAnswer();

        });
    }

    private void doAnswer() {
        localpeer.createAnswer(new CustomSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                Log.v("paresh", "creating answer=------------------------------------------------------------------");
                localpeer.setLocalDescription(new CustomSdpObserver(), sessionDescription);
                Log.v("paresh", "sending answer--------------------------------------------------------------------");
                SignallingClient.getInstance().sendMessage(sessionDescription);

            }
        }, new MediaConstraints());
    }

    @Override
    public void onAnswerReceived(String sdp) {
        Log.v("paresh", "set remote description on Answer recived ==============-===================================");

        localpeer.setRemoteDescription(new CustomSdpObserver(),
                new SessionDescription(Type.ANSWER, sdp));

    }

    @Override
    public void onIceCandidateReceived(String data, int label, String id) {
        localpeer.addIceCandidate(new IceCandidate(id, label, data));
        Log.v("paresh", "Recived and adding ice candidate===============----------------------==============================");

    }

    private void gotRemoteStream(MediaStream stream) {
        //we have remote video stream. add to the renderer.
        final VideoTrack videoTrack = stream.videoTracks.get(0);
        AudioTrack audioTrack = stream.audioTracks.get(0);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v("paresh", "adding remote stream to surface view =================--------------------===========");

                    videoTrack.addSink(surfaceView1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onRemoteHangUp(String msg) {
        SignallingClient.getInstance().doHangup(name);
        Toast.makeText(this, "No one there in the room !", Toast.LENGTH_SHORT).show();

    }

    //capturing video from the front or back cam...
    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();


        Logging.v("paresh", "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                front_cam = true;
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                surfaceView1.setMirror(true);
                if (videoCapturer != null) {
                    return videoCapturer;

                }
            }
        }

        Logging.v("paresh", "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                front_cam = false;
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                surfaceView1.setMirror(false);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }

        }


        return null;
    }


    @Override
    public void onCreatedRoom() {
        Toast.makeText(this, "Room created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onJoinedRoom() {
        Toast.makeText(this, "room joined!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNewPeerJoined() {
        Toast.makeText(this, "new member added !", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onRemoteHangUp("hangup");
        localpeer.close();
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
    }
}
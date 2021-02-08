package com.pharos.webrtc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pharos.webrtc.Observer.CustomPeerConnectionObserver;
import com.pharos.webrtc.Observer.CustomSdpObserver;
import com.pharos.webrtc.SignallingServer.SignallingClient;
import com.pharos.webrtc.VC.VideoCallActivity;

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

public class SecondScreen extends AppCompatActivity  implements  SignallingClient.SignalingInterface  {

    private static final int RC_CALL = 111;
    public static final int VIDEO_RESOLUTION_WIDTH = 852;
    public static final int VIDEO_RESOLUTION_HEIGHT = 480;
    private static final String TAG = "SecondScreen";
    public static final int FPS = 30;
    private String roomID = "null", name = "null";
    private Boolean mute = true, videoOn = false, front_cam = true;
    private ImageView rotate_cam, mute_unmute, videoon_off, hangup, menu;

    private PeerConnection localpeer;
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

    private SurfaceViewRenderer surfaceView1,remotesurface;
    private TextView local_peername,remote_peername,roomidText;


    boolean gotUserMedia;
    List<PeerConnection.IceServer> peerIceServers = new ArrayList<>();

    final int ALL_PERMISSIONS_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_calling_activity);
        roomID = getIntent().getStringExtra("roomid");
        local_peername = findViewById(R.id.local_name);

        name = getIntent().getStringExtra("name");
        roomidText = findViewById(R.id.roomid);
        roomidText.setText("Room :  " + roomID);
        local_peername.setText("Me:" + name);


        init();
        clickers();
        start();

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

    private void init() {
        mute_unmute = findViewById(R.id.togglemic);
        videoon_off = findViewById(R.id.toggle_video);
        hangup = findViewById(R.id.hangup);
        rotate_cam = findViewById(R.id.flip_cam);
        surfaceView1 = findViewById(R.id.surface_view1);
        remotesurface = findViewById(R.id.remotesurfaceview);
        remote_peername = findViewById(R.id.peer_name);
        menu = findViewById(R.id.menu);
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

            //Initialize PeerConnectionFactory globals.
            PeerConnectionFactory.InitializationOptions initializationOptions =
                    PeerConnectionFactory.InitializationOptions.builder(this)
                            .createInitializationOptions();
            PeerConnectionFactory.initialize(initializationOptions);

            //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                    rootEglBase.getEglBaseContext(), true,  true);
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
            localVideoTrack.addSink(surfaceView1);

            surfaceView1.setMirror(true);

        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }

    private void getIceServers() {

        peerIceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        peerIceServers.add(new PeerConnection.IceServer("stun1.l.google.com:19302"));
        peerIceServers.add(new PeerConnection.IceServer("stun2.l.google.com:19302"));
        peerIceServers.add(new PeerConnection.IceServer("stun3.l.google.com:19302"));
    }


    //create peer connection
    private void createPeerConnection(){
        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(peerIceServers);

        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        localpeer = peerConnectionFactory.createPeerConnection(rtcConfig,new CustomPeerConnectionObserver("localPeerCreation"){
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                Toast.makeText(SecondScreen.this, "Recived remote stream!", Toast.LENGTH_SHORT).show();
            }
        });


        addStreamToLocalPeer();

    }

    private void addStreamToLocalPeer() {
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localAudioTrack);
        stream.addTrack(localVideoTrack);
        localpeer.addStream(stream);
    }


    //capturing video from the front or back cam...
    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();


        Logging.d(TAG, "Looking for front facing cameras.");
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

        Logging.d(TAG, "Looking for front facing cameras.");
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
    public void onRemoteHangUp(String msg) {

    }

    @Override
    public void onOfferReceived(JSONObject data) {

    }

    @Override
    public void onAnswerReceived(JSONObject data) {

    }

    @Override
    public void onIceCandidateReceived(JSONObject data) {

    }

    @Override
    public void onTryToStart() {

        runOnUiThread(() -> {
            if (!SignallingClient.getInstance().isStarted && localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
                createPeerConnection();
                SignallingClient.getInstance().isStarted = true;
                if (SignallingClient.getInstance().isInitiator) {
                    doCall();
                }
            }
        });

    }

    private void doCall() {
        sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        localpeer.createOffer(new CustomSdpObserver(){
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                //TODO add local stream in local peer.
            }
        },sdpConstraints);
    }



    @Override
    public void onCreatedRoom() {


    }

    @Override
    public void onJoinedRoom() {

    }

    @Override
    public void onNewPeerJoined() {

    }
}
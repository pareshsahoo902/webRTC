package com.pharos.webrtc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.pharos.webrtc.VC.VideoCallActivity;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText roomID, name;
    private Button startCall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roomID = findViewById(R.id.roomtextid);
        name = findViewById(R.id.nametext);
        startCall = findViewById(R.id.startCall);

        startCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondScreen.class)
                        .putExtra("roomid", roomID.getText().toString())
                        .putExtra("name", name.getText().toString()));
            }
        });

    }
}
package com.example.esp32camapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnConfig, btnVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnConfig = findViewById(R.id.btnConfig);
        btnVideo = findViewById(R.id.btnVideo);

        btnConfig.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ConfigActivity.class);
            startActivity(intent);
        });

        btnVideo.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
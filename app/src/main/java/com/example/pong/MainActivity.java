package com.example.pong;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity {

    public static int difficultyLevel = 0;

    // Initializing Variables
    RadioGroup difficulty;
    Button play;
    Intent game;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Full Screen the app
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Variables
        difficulty = findViewById(R.id.difficulty);
        play = findViewById(R.id.play);
        game = new Intent(this, GameActivity.class);

        // App settings
        difficulty.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.easy) {
                difficultyLevel = 0;
            } else if (checkedId == R.id.medium) {
                difficultyLevel = 1;
            } else if (checkedId == R.id.hard) {
                difficultyLevel = 2;
            } else if (checkedId == R.id.perfect) {
                difficultyLevel = 3;
            }
        });
        difficulty.check(R.id.easy);
        play.setOnClickListener(v -> startActivity(game));

        // Lock the app to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // ! DELETE AFTER TESTING
        startActivity(game);


    }
}
// TODO
// 1. Create Splash Screen
// 5. Improve Welcome Screen colors/design
// 6. Improve Splash Screen colors/design

// ! Extras
// 1. App name
// 2. Full Screen
// 3. Menu Locked to Portrait
// 4. Difficulty Levels
// 5. Game locked to landscape
// 6. Splash Screen

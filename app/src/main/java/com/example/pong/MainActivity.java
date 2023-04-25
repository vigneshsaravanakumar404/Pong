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
        play = findViewById(R.id.play);
        game = new Intent(this, GameActivity.class);

        // Start Game
        play.setOnClickListener(v -> startActivity(game));

        // Lock the app to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // ! Delete Afterwards
        startActivity(game);


    }
}
// TODO
// 1. Improve Splash Screen
// 5. Improve Start Screen
// 7. Button to Fire


// ! Extras
// 1. App name
// 2. Complete Full Screen
// 3. Menu Locked to Portrait
// 4. Difficulty Levels
// 5. Game locked to landscape
// 6. Splash Screen
// 7. Paddle hit offset Calculations
// 8. Game Speed Changes on click
// 9. AI

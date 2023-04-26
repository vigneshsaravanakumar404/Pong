package com.example.pong;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    Button play;
    Intent game;

    TextView scores;

    int previousPlayerScore = GameActivity.playerScore;
    int previousComputerScore = GameActivity.computerScore;

    @SuppressLint({"SourceLockedOrientationActivity", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Objects.requireNonNull(getSupportActionBar()).hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);


        play = findViewById(R.id.play);
        scores = findViewById(R.id.textView);
        game = new Intent(this, GameActivity.class);
        play.setOnClickListener(v -> startActivity(game));
        play.setBackgroundColor(getResources().getColor(R.color.white));


        // if the score is not o then create a textview and display the score
        if (previousPlayerScore != 0 || previousComputerScore != 0) {
            scores.setText("Player: " + previousPlayerScore + "\t\tComputer: " + previousComputerScore);
            scores.setTextColor(getResources().getColor(R.color.white));
            scores.setTextSize(30);

            if (GameActivity.broken) {
                Toast.makeText(getApplicationContext(), "Computer Wins!", Toast.LENGTH_SHORT).show();
            } else if (previousPlayerScore > previousComputerScore) {
                Toast.makeText(getApplicationContext(), "Player Wins!", Toast.LENGTH_SHORT).show();
            } else if (previousPlayerScore < previousComputerScore) {
                Toast.makeText(getApplicationContext(), "Computer Wins!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "It's a Tie!", Toast.LENGTH_SHORT).show();
            }
        }


    }
}

// TODO
// 9. An Extra


// ! Extras
// 1. App name
// 2. Complete Full Screen
// 3. Menu Locked to Portrait
// 5. Game locked to landscape
// 6. Splash Screen
// 7. Paddle hit offset Calculations
// 8. Game Speed Changes on click
// 9. AI

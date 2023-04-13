package com.example.pong;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static int difficultyLevel = 0;

    // Initializing Variables
    RadioGroup difficulty;
    Button play;
    Intent game;

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
        play.setOnClickListener(v -> {

            startActivity(game);
        });


    }
}
// TODO
// 1. Create Splash Screen
// 2. Create Main Menu
// 3. Set App Icon
// 4. Set app Name
// 5. Improve Welcome Screen

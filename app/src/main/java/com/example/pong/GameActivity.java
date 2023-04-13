package com.example.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    // Variables
    GameSurface gameSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        // Hide the action bar and make the activity full screen
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        getWindow().setFlags(0x04000000, 0x04000000);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    // Pause and resume the game
    @Override
    protected void onPause() {
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurface.resume();
    }

    // Game Surface
    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener {

        // Variables
        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        int ballX = 0;
        int x = 200;
        String sensorOutput = "This is test";
        Paint paintProperty;

        int screenWidth;
        int screenHeight;

        public GameSurface(Context context) {
            super(context);

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

            holder = getHolder();
            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth = sizeOfScreen.y;
            screenHeight = sizeOfScreen.x;
            paintProperty = new Paint();
            paintProperty.setTextSize(100);

        }

        @Override
        public void run() {
            while (running) {
                if (!holder.getSurface().isValid())
                    continue;
                Canvas canvas = holder.lockCanvas();

                canvas.drawRGB(255, 0, 0);

                canvas.drawText(sensorOutput, x, 200, paintProperty);

                ballX += 1;

                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume() {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException ignored) {
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}




package com.example.pong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    //Code from this program has been used from Beginning Android Games
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(gameSurface);

        // Hide the action bar and make the activity full screen
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        getWindow().setFlags(0x04000000, 0x04000000);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);


    }

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


    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener {


        // Variables
        Thread gameThread, accelerometerThread;
        final SurfaceHolder holder;
        volatile boolean running = false;
        int ballX = 0;
        final int x = 200;
        final String sensorOutput = "This is test";
        final Paint paintProperty;
        final int screenWidth;
        final int screenHeight;
        int ballY = 0;
        SensorManager sensorManager;
        Sensor accelerometerSensor;


        public GameSurface(Context context) {
            super(context);

            // Accelerometer Declaration
            Log.d("REACHED", "Accelerometer Declaration");
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

            holder = getHolder();
            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;
            paintProperty = new Paint();
            paintProperty.setTextSize(100);


        }

        @Override
        public void run() {
            while (running) {
                if (!holder.getSurface().isValid()) continue;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Canvas canvas = holder.lockCanvas();

                        canvas.drawRGB(255, 0, 0);

                        canvas.drawText(sensorOutput, x, 200, paintProperty);

                        // draw a circle with color blue
                        paintProperty.setColor(0xff0000ff);
                        canvas.drawCircle(ballX, ballY, 50, paintProperty);

                        holder.unlockCanvasAndPost(canvas);


                    }
                });
            }
        }


        // Thread Processing
        public void resume() {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @SuppressWarnings("InfiniteLoopStatement")
        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException ignored) {
                }
            }
        }

        // Detect Accelerometer Changes and translate them to ball movement
        @Override
        public void onSensorChanged(SensorEvent event) {


            // TODO CREATE A THREAD FOR THIS
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // calculate the magnitude of acceleration
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z * 0.0001);

            // calculate the proportion of movement for the ball
            float movementX = x / acceleration;
            float movementY = y / acceleration;

            // adjust the ball position based on movement proportion
            ballX -= (int) (movementX * 50 * 0.0001);
            ballY += (int) (movementY * 50 * 0.0001);

            // make sure the ball does not go out of the screen
            if (ballX < 0) {
                ballX = 0;
            } else if (ballX > screenWidth) {
                ballX = screenWidth;
            }

            if (ballY < 0) {
                ballY = 0;
            } else if (ballY > screenHeight) {
                ballY = screenHeight;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
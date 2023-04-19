package com.example.pong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class GameActivity extends AppCompatActivity {


    // Variables
    GameSurface gameSurface;
    int ballSpeedX = 10;
    int ballSpeedY = 5;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);


        // Hide the action bar and make the activity full screen
        ActionBar actionBar = getSupportActionBar();

        setContentView(gameSurface);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().setFlags(0x04000000, 0x04000000);
        assert actionBar != null;
        actionBar.hide();
        Objects.requireNonNull(getSupportActionBar()).hide();


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


        final SurfaceHolder holder;
        final Paint paintProperty;
        final int screenWidth;
        final int screenHeight;
        // Variables
        Thread gameThread;
        volatile boolean running = false;
        int ballX, ballY = 0;
        int paddleX;
        SensorManager sensorManager;
        Sensor accelerometerSensor;

        // Declare the ball bitmap
        Bitmap ballBitmapScaled = BitmapFactory.decodeResource(getResources(), R.drawable.pinpongball);
        Bitmap ballScaled = Bitmap.createScaledBitmap(ballBitmapScaled, 50, 50, false);
        Rect paddleRect;


        public GameSurface(Context context) {
            super(context);

            // Accelerometer Declaration
            Log.d("REACHED", "Accelerometer Declaration");
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_STATUS_ACCURACY_LOW, SensorManager.SENSOR_DELAY_GAME);

            holder = getHolder();
            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;
            paddleX = screenWidth / 2 - 100;
            paintProperty = new Paint();

            paddleRect = new Rect(paddleX, screenHeight - 100, paddleX + 200, screenHeight - 50);


        }

        @Override
        public void run() {
            while (running) {
                if (!holder.getSurface().isValid()) continue;

                Canvas canvas = holder.lockCanvas();

                // Background
                canvas.drawRGB(0, 0, 0);
                paintProperty.setColor(0xffffffff);


                // Center Line
                canvas.drawRect(0, screenHeight / 2 - 1, screenWidth, screenHeight / 2 + 1, paintProperty);


                // Border
                canvas.drawRect(0, 0, screenWidth, 10, paintProperty);
                canvas.drawRect(0, 0, 10, screenHeight, paintProperty);
                canvas.drawRect(0, screenHeight - 10, screenWidth, screenHeight, paintProperty);
                canvas.drawRect(screenWidth - 10, 0, screenWidth, screenHeight, paintProperty);

                // Paddle Code
                canvas.drawRect(paddleRect, paintProperty);

                // Update the ball's position
                ballX += ballSpeedX;
                ballY += ballSpeedY;

                // Check for collisions with the walls
                if (ballX < 11) {
                    ballX = 11;
                    ballSpeedX *= -1;
                } else if (ballX > screenWidth - 200 - 11) {
                    ballX = screenWidth - 200 - 11;
                    ballSpeedX *= -1;
                }
                if (ballY < 11) {
                    ballY = 11;
                    ballSpeedY *= -1;
                } else if (ballY > screenHeight - 50 - 11) {
                    ballY = screenHeight - 50 - 11;
                    ballSpeedY *= -1;
                }

                // Check for collisions with the paddle, on collision reverse the ball's direction and offset the x velocity based on the location of the collision
                if (ballX > paddleX && ballX < paddleX + 200 && ballY > screenHeight - 100 - 11) {
                    ballSpeedY *= -1;
                    ballY = screenHeight - 100 - 11;
                    if (ballX < paddleX + 50) {
                        ballSpeedX = -100;
                    } else if (ballX > paddleX + 150) {
                        ballSpeedX = 100;
                    } else {
                        ballSpeedX = 0;
                    }
                }
                canvas.drawBitmap(ballScaled, ballX, ballY, paintProperty);

                holder.unlockCanvasAndPost(canvas);
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

            float x = event.values[0];
            float y = event.values[1];

            if (Math.abs(x) >= 0.5 || Math.abs(y) >= 0.5) {
                // calculate the proportion of movement for the ball based in the acceleration
                float movementX = x / ((float) Math.sqrt(x * x + y * y));

                // adjust the ball position based on movement proportion
                paddleX -= (int) (movementX * 50 * 0.65);

                // make sure the ball does not go out of the screen
                paddleX = Math.max(11, Math.min(screenWidth - 200 - 11, paddleX));
            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}



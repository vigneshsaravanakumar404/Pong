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
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class GameActivity extends AppCompatActivity {


    // Variables
    GameSurface gameSurface;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);

        setContentView(gameSurface);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getWindow().setFlags(0x04000000, 0x04000000);
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


    // Main Game Code
    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener {

        final SurfaceHolder holder;
        final Paint paintProperty;
        final int screenWidth, screenHeight;
        final float MAX_BOUNCE_ANGLE = 60f;
        float ballSpeedX, ballSpeedY = 10;
        volatile boolean running = false;
        int ballX, ballY, paddleX;
        Thread gameThread;
        SensorManager sensorManager;
        Sensor accelerometerSensor;
        Bitmap ballBitmapScaled = BitmapFactory.decodeResource(getResources(), R.drawable.pinpongball);
        Bitmap ballScaled = Bitmap.createScaledBitmap(ballBitmapScaled, 50, 50, false);
        Rect paddleRect;


        public GameSurface(Context context) {
            super(context);

            // Accelerometer Declaration
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

            holder = getHolder();
            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;
            paddleX = screenWidth / 2 - 100;
            ballX = screenWidth / 2;
            ballY = screenHeight / 2;
            paddleRect = new Rect(paddleX, screenHeight - 100, paddleX + 200, screenHeight - 50);
            paintProperty = new Paint();
        }


        @Override
        public void run() {
            while (running) {
                if (!holder.getSurface().isValid()) continue;

                Canvas canvas = holder.lockCanvas();

                // Background
                canvas.drawRGB(0, 0, 0);

                // Draw a line through the center of the screen horizontally
                paintProperty.setColor(0xffffffff);
                canvas.drawLine(0, screenHeight / 2, screenWidth, screenHeight / 2, paintProperty);

                // Draw a white border
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
                } else if (ballX > screenWidth - ballScaled.getWidth() - 11) {
                    ballX = screenWidth - ballScaled.getWidth() - 11;
                    ballSpeedX *= -1;
                }

                if (ballY < 11) {
                    ballY = 11;
                    ballSpeedY *= -1;
                } else if (ballY > screenHeight - ballScaled.getHeight() - 11) {
                    ballY = screenHeight - ballScaled.getHeight() - 11;
                    ballSpeedY *= -1;
                }


                // Some math to calculate the angle of the bounce based on where the ball hit the paddle
                if (Rect.intersects(paddleRect, new Rect(ballX, ballY, ballX + ballScaled.getWidth(), ballY + ballScaled.getHeight()))) {

                    float ballSpeed = (float) Math.sqrt(ballSpeedX * ballSpeedX + ballSpeedY * ballSpeedY);
                    float ballDirection = (float) Math.atan2(-ballSpeedY, ballSpeedX);
                    float hitPosition = (ballX + ballScaled.getWidth() / 2f - paddleRect.centerX()) / (paddleRect.width() / 2f);
                    float bounceAngle;

                    if (Math.abs(hitPosition) < 0.2f) {
                        bounceAngle = 0f;
                    } else {
                        bounceAngle = hitPosition * MAX_BOUNCE_ANGLE;
                    }

                    ballDirection += bounceAngle * Math.PI / 180f;
                    ballSpeedX = (float) (ballSpeed * Math.cos(ballDirection));
                    ballSpeedY = (float) (-ballSpeed * Math.sin(ballDirection));
                    ballY = paddleRect.top - ballScaled.getHeight() - 1;
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

            float ax = event.values[0];
            paddleX -= ax * 5;
            if (paddleX < 0) {
                paddleX = 0;
            } else if (paddleX > screenWidth - 200) {
                paddleX = screenWidth - 200;
            }
            paddleRect.set(paddleX, screenHeight - 100, paddleX + 200, screenHeight - 50);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}


package com.example.pong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
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

        final SensorManager sensorManager;
        volatile boolean running = false;
        int ballX, ballY, paddleX;
        Thread gameThread;
        final Sensor accelerometerSensor;
        final Bitmap ballBitmapScaled = BitmapFactory.decodeResource(getResources(), R.drawable.pinpongball);
        final Bitmap ballScaled = Bitmap.createScaledBitmap(ballBitmapScaled, 50, 50, false);
        final Rect paddleRect;
        final MediaPlayer paddleCollision = MediaPlayer.create(GameActivity.this, R.raw.paddlecollsion);
        final MediaPlayer backgroundMusic = MediaPlayer.create(GameActivity.this, R.raw.backgroundmusic);
        float ballSpeedX, ballSpeedY = 25;
        MediaPlayer wallCollision = MediaPlayer.create(GameActivity.this, R.raw.wallcollision);


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

            // Background Music
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.25f, 0.25f);
            backgroundMusic.start();

            // When the screen Is tapped everything is double the speed for 5 seconds
            setOnTouchListener((v, event) -> {
                ballSpeedX *= 2;
                ballSpeedY *= 2;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ballSpeedX /= 2;
                ballSpeedY /= 2;
                return false;
            });

        }


        @Override
        public void run() {
            paintProperty.setColor(Color.GRAY);
            paintProperty.setTextSize(100);

            long startTime = System.currentTimeMillis();
            long countdownTime = 15000; // 15 seconds
            while (running) {
                if (!holder.getSurface().isValid()) continue;

                Canvas canvas = holder.lockCanvas();
                if (canvas == null) continue;

                // Background
                canvas.drawColor(Color.BLACK);

                // Draw a line through the center of the screen horizontally 2 pixels wide
                paintProperty.setColor(Color.WHITE);
                paintProperty.setStrokeWidth(2);
                canvas.drawLine(0, screenHeight / 2, screenWidth, screenHeight / 2, paintProperty);

                // Create a countdown timer and display it on the screen in the center pause the game when it reaches 0, do this in an AsyncTask


                // Draw a white border
                int borderWidth = 10;
                paintProperty.setColor(Color.WHITE);
                canvas.drawRect(0, 0, screenWidth, borderWidth, paintProperty);
                canvas.drawRect(0, borderWidth, borderWidth, screenHeight - borderWidth, paintProperty);
                canvas.drawRect(0, screenHeight - borderWidth, screenWidth, screenHeight, paintProperty);
                canvas.drawRect(screenWidth - borderWidth, borderWidth, screenWidth, screenHeight - borderWidth, paintProperty);

                // Paddle Code
                paddleRect.set(paddleX, screenHeight - 100, paddleX + 200, screenHeight - 50);
                paintProperty.setColor(Color.WHITE);
                canvas.drawRect(paddleRect, paintProperty);

                // Update the ball's position
                ballX += ballSpeedX;
                ballY += ballSpeedY;

                // Check for collisions with the walls
                int ballWidth = ballScaled.getWidth();
                int ballHeight = ballScaled.getHeight();
                int wallBorder = 11;
                if (ballX < wallBorder) {
                    ballX = wallBorder;
                    ballSpeedX *= -1;
                    wallCollision.start();
                } else if (ballX > screenWidth - ballWidth - wallBorder) {
                    ballX = screenWidth - ballWidth - wallBorder;
                    ballSpeedX *= -1;
                    wallCollision.start();
                }
                if (ballY < wallBorder) {
                    ballY = wallBorder;
                    ballSpeedY *= -1;
                    wallCollision.start();
                } else if (ballY > screenHeight - ballHeight - wallBorder) {
                    ballY = screenHeight - ballHeight - wallBorder;
                    ballSpeedY *= -1;
                    wallCollision.start();
                }

                Rect ballRect = new Rect(ballX, ballY, ballX + ballWidth, ballY + ballHeight);
                if (Rect.intersects(paddleRect, ballRect)) {
                    paddleCollision.start();

                    float ballSpeed = (float) Math.sqrt(ballSpeedX * ballSpeedX + ballSpeedY * ballSpeedY);
                    float ballDirection = (float) Math.atan2(-ballSpeedY, ballSpeedX);
                    float hitPosition = (ballX + ballWidth / 2f - paddleRect.centerX()) / (paddleRect.width() / 2f);

                    float centerAngle = 180f;
                    float edgeAngle = 120f;
                    float hitAngle;

                    if (hitPosition < 0) {
                        // Ball bounces to the left
                        hitAngle = centerAngle + (hitPosition + 0.5f) * (centerAngle - edgeAngle);
                        ballSpeedX = -Math.abs(ballSpeedX);
                    } else if (hitPosition > 0) {
                        // Ball bounces to the right
                        hitAngle = centerAngle + (hitPosition - 0.5f) * (edgeAngle - centerAngle);
                        ballSpeedX = Math.abs(ballSpeedX);
                    } else { // hitPosition == 0
                        hitAngle = centerAngle;
                    }

                    ballDirection += hitAngle * Math.PI / 180f;
                    ballSpeedX = (float) (ballSpeed * Math.cos(ballDirection));
                    ballSpeedY = (float) (-ballSpeed * Math.sin(ballDirection));
                    ballY = paddleRect.top - ballHeight - 1;
                }


                canvas.drawBitmap(ballScaled, ballX, ballY, paintProperty);


                holder.unlockCanvasAndPost(canvas);

                // if the absolute y velocity of the ball is less than 0.5, the ball is moving too slow, so speed it up
                if (Math.abs(ballSpeedY) < 3) {
                    ballSpeedY *= 1.1;
                }
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
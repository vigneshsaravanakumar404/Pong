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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

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

        final int screenWidth, screenHeight;
        final SurfaceHolder holder;
        final Paint paintProperty;


        final SensorManager sensorManager;
        final Sensor accelerometerSensor;
        final Bitmap ballBitmapScaled = BitmapFactory.decodeResource(getResources(), R.drawable.pinpongball);
        final Bitmap ballScaled = Bitmap.createScaledBitmap(ballBitmapScaled, 50, 50, false);
        final Rect playerPaddleRect, computerPaddleRect;
        final MediaPlayer paddleCollision = MediaPlayer.create(GameActivity.this, R.raw.paddlecollsion);
        final MediaPlayer backgroundMusic = MediaPlayer.create(GameActivity.this, R.raw.backgroundmusic);
        float ballSpeedX = 0, ballSpeedY = 25, computerPaddleSpeed = 0;

        volatile boolean running = false;

        int ballX, ballY, playerPaddleX, computerPaddleX;
        int remainingTime = 1500;
        int playerScore, computerScore = 0;

        Thread gameThread;
        MediaPlayer wallCollision = MediaPlayer.create(GameActivity.this, R.raw.wallcollision);


        @SuppressLint("ClickableViewAccessibility")
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
            playerPaddleX = screenWidth / 2 - 100;
            computerPaddleX = screenWidth / 2 - 100;
            ballX = screenWidth / 2;
            ballY = screenHeight / 2;
            playerPaddleRect = new Rect(playerPaddleX, screenHeight - 100, playerPaddleX + 200, screenHeight - 50);
            computerPaddleRect = new Rect(computerPaddleX, 50, computerPaddleX + 200, 100);
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

            new CountdownTask().execute();

            while (running) {
                if (!holder.getSurface().isValid()) continue;

                Canvas canvas = holder.lockCanvas();
                if (canvas == null) continue;


                // Background
                canvas.drawColor(Color.BLACK);


                // Countdown Timer
                paintProperty.setColor(Color.RED);
                paintProperty.setTextSize(50);
                String timeString = String.valueOf(remainingTime);
                float textWidth = paintProperty.measureText(timeString);
                canvas.drawText(timeString, screenWidth / 2 - textWidth / 2, screenHeight / 2 - 25, paintProperty);
                canvas.save();
                canvas.rotate(180, screenWidth / 2, screenHeight / 2);
                canvas.drawText(timeString, screenWidth / 2 - textWidth / 2, screenHeight / 2 - 25, paintProperty);
                canvas.restore();


                // Draw a line through the center of the screen horizontally 2 pixels wide
                paintProperty.setColor(Color.WHITE);
                paintProperty.setStrokeWidth(2);
                canvas.drawLine(0, screenHeight / 2, screenWidth, screenHeight / 2, paintProperty);


                paintProperty.setTextSize(100);
                paintProperty.setColor(Color.GRAY);
                // Display the computerScore in the middle of the top half of the screen
                String computerScoreString = String.valueOf(computerScore);
                float computerScoreWidth = paintProperty.measureText(computerScoreString);
                canvas.drawText(computerScoreString, screenWidth / 2 - computerScoreWidth / 2, screenHeight / 4 - 50, paintProperty);

                // Display the playerScore in the middle of the bottom half of the screen
                String playerScoreString = String.valueOf(playerScore);
                float playerScoreWidth = paintProperty.measureText(playerScoreString);
                canvas.drawText(playerScoreString, screenWidth / 2 - playerScoreWidth / 2, screenHeight / 4 * 3 - 50, paintProperty);


                // Draw a white border
                int borderWidth = 10;
                paintProperty.setColor(Color.WHITE);
                canvas.drawRect(0, 0, screenWidth, borderWidth, paintProperty);
                canvas.drawRect(0, borderWidth, borderWidth, screenHeight - borderWidth, paintProperty);
                canvas.drawRect(0, screenHeight - borderWidth, screenWidth, screenHeight, paintProperty);
                canvas.drawRect(screenWidth - borderWidth, borderWidth, screenWidth, screenHeight - borderWidth, paintProperty);

                // Paddle Code
                playerPaddleRect.set(playerPaddleX, screenHeight - 100, playerPaddleX + 200, screenHeight - 50);
                computerPaddleRect.set(computerPaddleX, 50, computerPaddleX + 200, 100);
                paintProperty.setColor(Color.WHITE);
                canvas.drawRect(playerPaddleRect, paintProperty);
                canvas.drawRect(computerPaddleRect, paintProperty);

                // Update the ball's position
                ballX += ballSpeedX;
                ballY += ballSpeedY;
                computerPaddleX += computerPaddleSpeed;

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
                    playerScore++;
                    ballX = screenWidth / 2;
                    ballY = screenHeight / 2;
                    ballSpeedX = 0;
                    ballSpeedY = 25;
                    if (Math.random() < 0.5) ballSpeedY *= -1;
                } else if (ballY > screenHeight - ballHeight - wallBorder) {
                    ballY = screenHeight - ballHeight - wallBorder;
                    ballSpeedY *= -1;
                    wallCollision.start();
                    computerScore++;
                    ballX = screenWidth / 2;
                    ballY = screenHeight / 2;
                    ballSpeedX = 0;
                    ballSpeedY = 25;
                    if (Math.random() < 0.5) ballSpeedY *= -1;
                }

                Rect ballRect = new Rect(ballX, ballY, ballX + ballWidth, ballY + ballHeight);
                if (Rect.intersects(playerPaddleRect, ballRect)) {
                    paddleCollision.start();

                    float ballSpeed = (float) Math.sqrt(ballSpeedX * ballSpeedX + ballSpeedY * ballSpeedY);
                    float ballDirection = (float) Math.atan2(-ballSpeedY, ballSpeedX);
                    float hitPosition = (ballX + ballWidth / 2f - playerPaddleRect.centerX()) / (playerPaddleRect.width() / 2f);

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
                    ballY = playerPaddleRect.top - ballHeight - 1;


                }
                if (Rect.intersects(computerPaddleRect, ballRect)) {
                    paddleCollision.start();

                    float ballSpeed = (float) Math.sqrt(ballSpeedX * ballSpeedX + ballSpeedY * ballSpeedY);
                    float ballDirection = (float) Math.atan2(ballSpeedY, ballSpeedX); // adjust for top paddle

                    float hitPosition = (ballX + ballWidth / 2f - computerPaddleRect.centerX()) / (computerPaddleRect.width() / 2f);

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
                    ballSpeedY = (float) (ballSpeed * Math.sin(ballDirection)); // adjust for top paddle
                    ballY = computerPaddleRect.bottom + 1; // adjust for top paddle

                }

                // Computer paddle code
                float computerPaddleCenterX = computerPaddleX + computerPaddleRect.width() / 2f;
                if (Rect.intersects(computerPaddleRect, ballRect)) {
                    paddleCollision.start();

                    float timeToOtherSide = (screenWidth - ballX - ballWidth) / ballSpeedX;
                    float targetY = ballY + ballSpeedY * timeToOtherSide;
                    if (computerPaddleCenterX < screenWidth / 2f) {
                        computerPaddleSpeed = Math.min(10, screenWidth / 2f - computerPaddleCenterX);
                    } else if (computerPaddleCenterX > screenWidth / 2f) {
                        computerPaddleSpeed = Math.max(-10, screenWidth / 2f - computerPaddleCenterX);
                    } else {
                        computerPaddleSpeed = 0;
                    }

                    if (ballSpeedY > 0) {
                        // If the ball is moving towards the bottom of the screen, move the computer paddle to the center of the screen
                        float targetX = screenWidth / 2f;
                        if (computerPaddleCenterX < targetX) {
                            computerPaddleSpeed = Math.min(10, targetX - computerPaddleCenterX);
                        } else if (computerPaddleCenterX > targetX) {
                            computerPaddleSpeed = Math.max(-10, targetX - computerPaddleCenterX);
                        } else {
                            computerPaddleSpeed = 0;
                        }
                    }
                } else {
                    if (ballX + ballWidth / 2 < computerPaddleCenterX - 20) {
                        computerPaddleSpeed = Math.max(-10, computerPaddleSpeed - 1);
                    } else if (ballX + ballWidth / 2 > computerPaddleCenterX + 20) {
                        computerPaddleSpeed = Math.min(10, computerPaddleSpeed + 1);
                    } else {
                        computerPaddleSpeed = 0;
                    }

                    if (ballSpeedY < 0) {
                        // If the ball is moving towards the top of the screen, move the computer paddle to the intersection point of the ball and the paddle
                        float timeToIntersection = (computerPaddleRect.top - ballY) / ballSpeedY;
                        float targetX = ballX + ballSpeedX * timeToIntersection;
                        if (computerPaddleCenterX < targetX) {
                            computerPaddleSpeed = Math.min(10, targetX - computerPaddleCenterX);
                        } else if (computerPaddleCenterX > targetX) {
                            computerPaddleSpeed = Math.max(-10, targetX - computerPaddleCenterX);
                        } else {
                            computerPaddleSpeed = 0;
                        }
                    } else if (ballSpeedY > 0 && ballY + ballHeight >= screenHeight - computerPaddleRect.height() / 2f) {
                        // If the ball is moving towards the bottom of the screen, center the computer paddle
                        float targetX = screenWidth / 2f;
                        if (computerPaddleCenterX < targetX) {
                            computerPaddleSpeed = Math.min(10, targetX - computerPaddleCenterX);
                        } else if (computerPaddleCenterX > targetX) {
                            computerPaddleSpeed = Math.max(-10, targetX - computerPaddleCenterX);
                        } else {
                            computerPaddleSpeed = 0;
                        }
                    }
                }
                computerPaddleX = (int) Math.max(0, Math.min(screenWidth - computerPaddleRect.width(), computerPaddleX + computerPaddleSpeed));


                canvas.drawBitmap(ballScaled, ballX, ballY, paintProperty);


                holder.unlockCanvasAndPost(canvas);

                // if the absolute y velocity of the ball is less than 0.5, the ball is moving too slow, so speed it up
                if (Math.abs(ballSpeedY) < 3) {
                    ballSpeedY *= 1.1;
                }
            }
        }


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
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            float ax = event.values[0];
            playerPaddleX -= ax * 5;
            if (playerPaddleX < 0) {
                playerPaddleX = 0;
            } else if (playerPaddleX > screenWidth - 200) {
                playerPaddleX = screenWidth - 200;
            }
            playerPaddleRect.set(playerPaddleX, screenHeight - 100, playerPaddleX + 200, screenHeight - 50);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        private class CountdownTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {
                while (remainingTime > 0) {
                    try {
                        Thread.sleep(1000); // Wait for 1 second
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    remainingTime--;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                remainingTime = 0;
                running = false; // Pause the game when the countdown is finished

                if (playerScore > computerScore) {
                    Toast.makeText(getContext(), "You Win!", Toast.LENGTH_SHORT).show();
                } else if (playerScore < computerScore) {
                    Toast.makeText(getContext(), "You Lose!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Tie!", Toast.LENGTH_SHORT).show();
                }

            }

        }

    }
}
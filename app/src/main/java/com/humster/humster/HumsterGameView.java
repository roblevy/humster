package com.humster.humster;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.os.Message;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;

// See this for more info:
// https://developer.android.com/guide/topics/graphics/2d-graphics.html#on-surfaceview
class HumsterGameView extends SurfaceView implements SurfaceHolder.Callback {
        class HumsterGameThread extends Thread {
            private SurfaceHolder mSurfaceHolder;
            private Context mContext;
            private Renderer mRenderer;
            private Handler mParentHandler; // Handler from whatever started this thread
            private DecimalFormat mFloatFormat; // For displaying floats
            private static final float SEMITONES_ON_SCREEN = 12;
            private static final int MIN_RECOGNISED_FREQUENCY = 50;
            private static final float X_FORCE = 100f;
            private static final float Y_FORCE = 12500f;
            private static final float MAX_Y_SPEED = 70f;
            private static final float MAX_X_SPEED = 50f;
            private static final float Y_STOPPING_DISTANCE = 20f; // TODO: Make this relative to surface size?
            private static final float MASS = 100;
            private static final float X_DRAG_COEFFICIENT = 0.1f;
            private static final float OFF_TRACK_DRAG_COEFFICIENT = 0.8f;
            private static final int HUMSTER_GAME_INVALID_Y = -999;
            private static final float DANGER_LEVEL_INCREASE = 0.12f;
            private static final float SPEED_SCORE_MULTIPLIER = 0.005f;
            private static final float SPEED_SCORE_EXPONENT = 1.5f; // Score += Speed ^ this
            private static final float JUMP_VELOCITY = 5f;
            private static final float GRAVITY = 0.3f;
            private static final float CHARACTER_XOFFSET = 250f;
            private float y, z, xF, yF, xV, yV, zV, targetY;
            private int mHeight = 1080; // Take a guess. This will be updated later
            private final Notes mNotes = new Notes();
            private Level mCurrentLevel;
            private float mCentralNote = 12f;
            private float mDistance;
            private float mLevelEndDistance;
            private float mFractionOffTrack = 0;
            private float mDangerLevel = 0;
            private float mScore = 0;
            private float mStartingScore = 0;
            private int mLevelNumber;

            private boolean mRun = false;
            private boolean mReset = false;
            private boolean mCalibrate = false;
            private boolean mLevelCompleted = false;
            // Ensures drawing is only attempted when it's allowed, using
            // a synchronized block ni the run() method of this thread.
            private final Object mRunLock = new Object();

            /** Constructor for the HumsterGameThread */
            public HumsterGameThread(SurfaceHolder surfaceHolder, Context context,
                                     Handler handler) {
                Log.i(LOG_TAG, "Constructing HumsterGameThread");
                mParentHandler = handler; // Used to send messages to parent object
                // Capture the drawing surface
                mSurfaceHolder = surfaceHolder;
                Resources res = getResources();
                BitmapDrawable drawable = (BitmapDrawable) res.getDrawable(R.drawable.background);
                Bitmap bm = drawable.getBitmap();
                Log.i(LOG_TAG, "Background: " + bm);
                mRenderer = new Renderer(bm);

                mFloatFormat = new DecimalFormat("00000.0;-0000.0");
                mFloatFormat.setRoundingMode(RoundingMode.HALF_UP);

                mContext = context;
            }

            boolean instantiateLevel(int levelNumber) {
                reset();
                mCurrentLevel = new Level(mContext, levelNumber); // context helps Level get JSON file
                boolean levelBuilt = mCurrentLevel.levelIsBuilt();
                if (levelBuilt) {
                    mLevelNumber = levelNumber;
                    mRenderer.createLevel(mCurrentLevel);
                    mLevelEndDistance = mCurrentLevel.getEndDistance();
                } else {
                    mLevelCompleted = true;
                }
                return levelBuilt;
            }

            void setStartingScore(int score) {
                mStartingScore = score;
            }

            private void reset() {
                mDistance = 0;
                mDangerLevel = 0;
                mScore = mStartingScore;
                xF = yF = xV = yV = zV = z = 0;
                y = targetY = HUMSTER_GAME_INVALID_Y; // This is an instantiation
                mReset = false;
            }

            public void setReset(boolean reset) {
                mReset = reset;
            }

            public void setCalibrate(boolean calibrate) {
                mCalibrate = calibrate;
            }

            @Override
            public void run() {
                while (mRun) {
                    Canvas c = null;
                    try {
                        c = mSurfaceHolder.lockCanvas(null);
                        synchronized (mSurfaceHolder) {
                            // Critical section. Do not allow mRun to be set false
                            // until we are sure all canvas draw operations are
                            // complete.
                            //
                            // If mRun has been toggled false, inhibit canvas
                            // operations.
                            synchronized (mRunLock) {
                                if (mRun) doDraw(c);
                            }
                        }
                    } finally {
                        // do this in a finally so that if an exception is thrown
                        // during the above, we don't leave the Surface in an
                        // inconsistent state
                        if (c != null) {
                            mSurfaceHolder.unlockCanvasAndPost(c);
                        }
                    }
                    Message doneLoop = Message.obtain();
                    doneLoop.what = HUMSTER_MSG_DRAW_DONE;
                    Bundle msgData = new Bundle();
                    String msg =
//                            " z: " + mFloatFormat.format(z) +
                            " distance: " + (int) mDistance +
                            " x-f: " + mFloatFormat.format(xF) +
//                            "y:" + mFloatFormat.format(y) +
//                            " reset: " + mReset +
                            " score: " + (int) mScore +
                            " danger: " + mFloatFormat.format(mDangerLevel);
                    msgData.putString("message", msg);
                    doneLoop.obj = msgData;
                    mParentHandler.sendMessage(doneLoop);
                }
            }

            /** This is where the drawing is done */
            private void doDraw(Canvas canvas) {
                if ((int) y == HUMSTER_GAME_INVALID_Y) y = targetY = canvas.getHeight() / 2;
                mRenderer.setDistance(mDistance);
                mRenderer.renderBackground(canvas);
                mRenderer.renderLevel(canvas, CHARACTER_XOFFSET, mHeight - y, z);
                mRenderer.renderPointer(canvas, mHeight - targetY);
                mFractionOffTrack = 1 - mRenderer.getFractionOfCharacterOnTrack();
                if (levelIsComplete()) nextLevel();
            }

            private boolean levelIsComplete() {
                return mDistance + CHARACTER_XOFFSET > mLevelEndDistance;
            }

            /** Let the thread's parent view know the level is complete */
            private void nextLevel() {
                if (!mLevelCompleted) {
                    mLevelCompleted = true;
                    Message nextLevelMsg = Message.obtain();
                    nextLevelMsg.what = HUMSTER_MSG_NEXT_LEVEL;
                    Bundle payload = new Bundle();
                    payload.putInt("levelNumber", mLevelNumber);
                    payload.putInt("score", (int) mScore);
                    nextLevelMsg.obj = payload;
                    mParentHandler.sendMessage(nextLevelMsg);
                }
            }

            /**
             * Used to signal the thread whether it should be running or not;
             * Passing true allows the thread to run; passing false will shut it
             * down if it's already running; Calling start() after this was most
             * recently called with false will result in an immediate shutdown.
             *
             * @param b true to run, false to shut down
             */
            public void setRunning(boolean b) {
                // Do not allow mRun to be modified while any canvas operations
                // are potentially in-flight. See doDraw().
                synchronized (mRunLock) {
                    mRun = b;
                }
            }

            public void setSurfaceSize(int w, int h) {
                mHeight = h;
                mRenderer.height = h;
                mRenderer.width = w;
            }

            void setFrequency(float freq) {
                if (mReset) {
                    reset();
                }
                if (freq > MIN_RECOGNISED_FREQUENCY) {
                    // A frequency has been detected
                    setY(freq);
                } else {
                    // No frequency has been detected
                    stopApplyingXForce();
                }
                updateGameState();
            }

            void setY(float freq) {
                float noteNumber = mNotes.frequency_to_note_id(freq);
                if (mCalibrate) {
                    calibrate(noteNumber);
                } else {
                    float minNote = mCentralNote - (SEMITONES_ON_SCREEN / 2f);
                    float maxNote = minNote + SEMITONES_ON_SCREEN;
                    float scaled = (noteNumber - minNote) / (maxNote - minNote);
                    if (scaled > 0 && scaled < 1) {
                        // A valid pitch has been detected. Go!
                        applyXForce();
                    } else {
                        stopApplyingXForce();
                    }
                    // Now force scaled between zero and one and set target
                    scaled = Math.min(Math.max(scaled, 0), 1);
                    targetY = (int) (mHeight * scaled);
                }
            }

            void updateGameState() {
                updatePhysics();
                setDangerLevel();
                setScore();
            }

            void calibrate(float centralNoteNumber) {
                if ((centralNoteNumber - (SEMITONES_ON_SCREEN / 2)) > 0) {
                    mCentralNote = centralNoteNumber;
                }
            }

            /** Danger mounts when the character is off the track. But only when not jumping
             *  and only between 0 and 1. */
            void setDangerLevel() {
                float dangerLevel = mDangerLevel;
                float rate = (mFractionOffTrack - 0.5f) * DANGER_LEVEL_INCREASE;
                if (rate < 0) {
                    // We lose danger faster than we gain it
                    rate *= 2.5f;
                } else {
                    // If we going to penalise, only do it when character is on the ground
                    if (z > 1) {
                        rate = 0;
                    }
                }
                dangerLevel += rate;
                dangerLevel = Math.max(0, Math.min(1, dangerLevel)); // force between 0 and 1
                mRenderer.setCharacterColour(dangerLevel);
                mDangerLevel = dangerLevel;
            }

            /** You can only jump if you're on the ground and on the track */
            void jump() {
                if (mFractionOffTrack < 0.5 && onTheGround()) {
                    zV = JUMP_VELOCITY;
                }
            }

            boolean onTheGround() {
                return Math.abs(z) < 1e-6;
            }

            void updatePhysics() {
                // z is fairly easy
                zV -= GRAVITY;
                float nextZ = z + zV;
                if (nextZ < 0) {
                    z = 0;
                    zV = -zV * 0.5f;
                } else {
                    z = nextZ;
                }
                // Use dv = (forward force - drag force) / m * dt
                // and assume drag force = v^2.
                // At terminal velocity, forward force = drag force
                // so for e.g. t.v. = 4 we need forward force = 16
                float dragForce = X_DRAG_COEFFICIENT;
                if (onTheGround()) {
                    // There's also "off-track drag" to account for here
                    dragForce += mFractionOffTrack * OFF_TRACK_DRAG_COEFFICIENT;
                }
                xV += (xF - dragForce * xV * xV) / MASS;
                if (xV < 0) xV = 0; // Due to rounding errors it's possible to go backwards!
                xV = Math.min(xV, MAX_X_SPEED);
                mDistance += xV; // x-velocity
                // y movement uses the Arrival behaviour described here:
                // http://www.red3d.com/cwr/papers/1999/gdc99steer.pdf
                float targetOffset = targetY - y;
                float distance = Math.abs(targetOffset);
                if (Math.abs(distance) > 1E-8) {
                    if (distance > Y_STOPPING_DISTANCE) {
                        yF = (targetY - y) / mHeight * Y_FORCE;
                    } else {
                        // We are inside the stopping radius
                        float rampedSpeed = MAX_Y_SPEED * (distance / Y_STOPPING_DISTANCE);
                        float clippedSpeed = Math.min(rampedSpeed, MAX_Y_SPEED);
                        float desiredVelocity = clippedSpeed / distance * targetOffset;
                        yF = (desiredVelocity - yV);
                    }
                } else {
                    yF = 0;
                }
                if (yV > 0) {
                    yV = (yF - X_DRAG_COEFFICIENT * yV * yV) / MASS;
                } else {
                    yV = (yF + X_DRAG_COEFFICIENT * yV * yV) / MASS;
                }
                if (Math.abs(yV) > MAX_Y_SPEED) {
                    if (yV < 0) {
                        yV = -MAX_Y_SPEED;
                    } else {
                        yV = MAX_Y_SPEED;
                    }
                }
                y += yV;
            }

            void applyXForce() {
                xF = X_FORCE;
            }

            void stopApplyingXForce() {
                xF = 0;
            }

            void setScore() {
                float dangerMultiplier = (0.1f - mDangerLevel) * 10f; // Between -4 and 1
                float speedScore = (float) Math.pow(xV, SPEED_SCORE_EXPONENT);
                speedScore *= SPEED_SCORE_MULTIPLIER;
                mScore += speedScore * dangerMultiplier;
            }
        }

    protected static final String LOG_TAG = GameActivity.LOG_TAG;
    int mState;
    Handler mParentHandler;
    Handler mHandler;
    TextView messageTextView;
    HumsterGameThread mGameThread;
    public static final int HUMSTER_MSG_DRAW_DONE = 1;
    public static final int HUMSTER_MSG_NEXT_LEVEL = 2;
    public static final int HUMSTER_MSG_SET_FREQUENCY = 3;

    /** Constructor for the HumsterGameView; Launches SingGame thread
     * and registers that we'd like to hear about changes to it*/
    public HumsterGameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHandler = new GameActivityHandler();

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        mGameThread = new HumsterGameThread(holder, context, new GameThreadHandler());

        setFocusable(true); // make sure we get key events
    }

    /** True if levelNumber was succesfully set up */
    public boolean startLevel(int levelNumber) {
        Log.i(LOG_TAG, "Starting level: " + levelNumber
                + " with score " + mGameThread.mStartingScore);
        boolean levelInstantiated = mGameThread.instantiateLevel(levelNumber);
        return levelInstantiated;
    }

    public void setScore(int score) {
        mGameThread.setStartingScore(score);
    }

    private void levelComplete(Message m) {
        Log.i(LOG_TAG, "levelComplete requested in HumsterGameView");
        mGameThread.setRunning(false);
        try {
            mGameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message msgNextLevel = Message.obtain(m);
        mParentHandler.sendMessage(msgNextLevel);
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void setMessageTextView(TextView tv) {
        messageTextView = tv;
    }

    public void setMessageHandler(Handler handler) {
        mParentHandler = handler;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle a touch event
        int motionAction = event.getAction();
        switch (motionAction & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mGameThread.jump();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // Two fingers
                mGameThread.setReset(true);
                mGameThread.setCalibrate(true);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // Release two fingers
                mGameThread.setReset(false);
                mGameThread.setCalibrate(false);
        }
        return true;
    }

    /** Time to implement the SurfaceHolder.Callback methods */
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(LOG_TAG, "Game surface created");
        mGameThread.setRunning(true);
        if (mGameThread.getState() == Thread.State.NEW) {
            mGameThread.start(); // TODO: Change this to implement Handler/Message stuff
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        /** Called immediately after any structural changes (format or size)
         * have been made to the surface. */
        Log.i(LOG_TAG, "Surface set to " + width + "x" + height);
        mGameThread.setSurfaceSize(width, height);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(LOG_TAG, "Game surface destroyed");
        mGameThread.setRunning(false);
    }

    /** Handler to receive messages from the parent activity */
    class GameActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HUMSTER_MSG_SET_FREQUENCY:
                    Bundle payload = (Bundle) msg.obj;
                    float frequency = payload.getFloat("frequency");
                    if (mGameThread.mRun) {
                        mGameThread.setFrequency(frequency);
                    }
                    break;
            }
        }
    }

    class GameThreadHandler extends Handler {
        @Override
        public void handleMessage(Message m) {
            // Handle messages coming back from the HumsterGameThread
            switch(m.what) {
                case HUMSTER_MSG_DRAW_DONE:
                    // Thread has finished a draw cycle
                    Bundle b = new Bundle((Bundle) m.obj);
                    messageTextView.setText(b.getString("message"));
                    break;
                case HUMSTER_MSG_NEXT_LEVEL:
                    // Pass the message up the chain to the parent Activity
                    levelComplete(m);
            }
        }
    }
}

package com.humster.humster;

import android.app.Activity;
import android.os.RemoteException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Messenger;
import android.os.Message;
import android.content.ServiceConnection;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.util.Log;
import android.widget.TextView;
import android.view.View;

public class GameActivity extends Activity {

    public static final String LOG_TAG = "Humster";
    Messenger mAudioServiceMessenger; // Initialised by mAudioServiceConnection.onServiceConnected
    final Messenger mAudioServiceMessageHandler;
    Handler mGameViewHandler;

    /** A handle to the View in which the game is running. */
    private HumsterGameView mHumsterGameView;

    boolean mBound = false;

    public GameActivity() {
        mAudioServiceMessenger = null;
        mAudioServiceMessageHandler = new Messenger(new AudioServiceMessageHandler());
    }

    /** Handler for start_game button */
    public void startGame(View view) {
    }

    @Override
    public void onCreate(Bundle previousState) {
        super.onCreate(previousState);
        Intent intent = getIntent();
        int level = intent.getIntExtra("levelNumber", 1);
        int score = intent.getIntExtra("score", 0);
        Log.i(LOG_TAG, "GameActivity created");
        setContentView(R.layout.activity_game);
        // get handles to the HumsterGameView and its HumsterGameThread
        mHumsterGameView = (HumsterGameView) findViewById(R.id.humster_game_view);
        mHumsterGameView.setMessageTextView((TextView) findViewById(R.id.message));
        mHumsterGameView.setMessageHandler(new GameMessageHandler());
        mGameViewHandler = mHumsterGameView.getHandler();
        mHumsterGameView.setScore(score);
        if (!mHumsterGameView.startLevel(level)) {
            gameCompleted(score);
        }
    }

    public void gameCompleted(int score) {
        Intent gameCompleteIntent = new Intent(this, GameCompleteActivity.class);
        gameCompleteIntent.putExtra("score", score);
        startActivity(gameCompleteIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "GameActivity started. Binding AudioService");
        Intent intent = new Intent(this, AudioService.class);
        mAudioServiceConnection = new AudioServiceConnection();
        bindService(intent, mAudioServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    // TODO: Maybe stop mHumsterGameThread? It certainly needs resetting?
    public void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "GameActivity stopped");
        unbindAudioService();
    }

    private void unbindAudioService() {
        Log.i(LOG_TAG, "Unbinding audio service");
        Message message = Message.obtain(null, AudioService.MSG_STOP_RECORDING);
        try {
            mAudioServiceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        stopService(new Intent(this, AudioService.class));
    }

    public void levelCompleted(Bundle levelInfo) {
        int levelNumber = levelInfo.getInt("levelNumber");
        int score = levelInfo.getInt("score");
        Log.i(LOG_TAG, "Level " + levelNumber + " completed");
        Log.i(LOG_TAG, "Score: " + score);
        Intent intent = new Intent(this, LevelCompleteActivity.class);
        intent.putExtra("levelNumber", levelNumber);
        intent.putExtra("score", score);
        startActivity(intent);
    }

    public void setFrequency(float frequency) {
        Message msg = Message.obtain();
        msg.what = HumsterGameView.HUMSTER_MSG_SET_FREQUENCY;
        Bundle payload = new Bundle();
        payload.putFloat("frequency", frequency);
        msg.obj = payload;
        mGameViewHandler.sendMessage(msg);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    class AudioServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(LOG_TAG, "Audio service connected");
            mBound = true;
            // mAudioServiceMessenger now has an IBinder which should contain a reference
            // to the service's Messenger, which in turn, should contain a
            // reference to the service's Handler.
            mAudioServiceMessenger = new Messenger(service);
            Message message = Message.obtain(null, AudioService.MSG_REPLYTO);
            message.replyTo = mAudioServiceMessageHandler;
            // Send message to service containing our messenger so the service
            // knows who to reply to
            try {
                mAudioServiceMessenger.send(message);
                Log.i(LOG_TAG, "MSG_REPLYTO sent to service");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // Now send a message to start recording
            message = Message.obtain(null, AudioService.MSG_START_RECORDING);
            try {
                mAudioServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // And finally a message to start processing audio
            message = Message.obtain(null, AudioService.MSG_START_PROCESSING);
            try {
                mAudioServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(LOG_TAG, "Audio service disconnecting");
            mBound = false;
            Message message = Message.obtain(null,
                    AudioService.MSG_STOP_RECORDING);
            try {
                mAudioServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mAudioServiceMessenger = null;
        }
    }
    private ServiceConnection mAudioServiceConnection;

    /** Handler for messages coming from the AudioService
     * containing pitch data. */
    class AudioServiceMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AudioService.MSG_PROCESSED_AUDIO:
                    Bundle bundle = (Bundle) msg.obj;
                    float pitch = bundle.getFloat("pitch");
                    setFrequency(pitch);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    class GameMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HumsterGameView.HUMSTER_MSG_NEXT_LEVEL:
                    levelCompleted((Bundle) msg.obj);
                    break;
            }
        }
    }
}

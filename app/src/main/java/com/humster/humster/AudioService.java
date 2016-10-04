package com.humster.humster;

import java.lang.IllegalStateException;
import java.util.LinkedList;

import android.app.Service;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Messenger;
import android.os.Message;
import android.os.RemoteException;
import android.content.Intent;
import android.util.Log;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.filters.LowPassFS;

public class AudioService extends Service {

    /** This messenger contains a reference to the AudioServiceMessageHandler so
     * whatever binds to this services knows where to send messages */
    final Messenger mInMessenger;

    //private AudioRecord mRecorder;
    private Messenger mOutMessenger = null;
    protected AudioDispatcher mDispatcher = null;
    protected SilenceDetector mSilenceDetector;
    private LinkedList<Float> mSmoothingFIFO;
    private static final String LOG_TAG = GameActivity.LOG_TAG;
    static final int MSG_REPLYTO = 1;
    static final int MSG_START_RECORDING = 2;
    static final int MSG_START_PROCESSING = 3;
    static final int MSG_STOP_RECORDING = 4;
    static final int MSG_PROCESSED_AUDIO = 5;
    static final int SAMPLING_RATE = 22050;
    static final float NO_PITCH = -1f;
    static final int PITCH_DETECTION_SMOOTHING_LENGTH = 10;

    PitchDetectionHandler pdh = new PitchDetectionHandler() {
        @Override
        public void handlePitch(PitchDetectionResult result, AudioEvent e) {
            Float pitchInHz = NO_PITCH;
            if (!silence()) {
                pitchInHz = result.getPitch();
            }
            addToFIFO(pitchInHz);
            sendValue(movingAverage(mSmoothingFIFO));
        }
    };

    void addToFIFO(Float val) {
        mSmoothingFIFO.removeFirst();
        mSmoothingFIFO.add(val);
    }

    boolean silence() {
        return mSilenceDetector.currentSPL() < SilenceDetector.DEFAULT_SILENCE_THRESHOLD;
    }

    float movingAverage(LinkedList<Float> fifo) {
        int n = 0;
        double sum = 0;
        for (int i = 0; i < fifo.size(); i++) {
            Float element = fifo.get(i);
            if (!element.equals(NO_PITCH)) {
                sum += element;
                n += 1;
            }
        }
        if (n > 0) {
            return (float) (sum / (float) n);
        } else {
            return -1;
        }
    }

    public AudioService() {
        mInMessenger = new Messenger(new IncomingHandler());
        mSmoothingFIFO = new LinkedList<>();
        for (int i = 0; i < PITCH_DETECTION_SMOOTHING_LENGTH; i++) {
            mSmoothingFIFO.add(NO_PITCH);
        }
    }


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REPLYTO:
                    Log.i(LOG_TAG, "REPLYTO received");
                    // The message may contain a replyTo Handler
                    mOutMessenger = msg.replyTo;
                    break;
                case MSG_START_RECORDING:
                    Log.i(LOG_TAG, "START_RECORDING received");
                    try {
                        setupAudio();
                    } catch (IllegalStateException e) {
                        Log.i(LOG_TAG, "Failed to set up audio");
                    }
                    break;
                case MSG_START_PROCESSING:
                    Log.i(LOG_TAG, "START_PROCESSING received");
                    new Thread(mDispatcher, "Audio Dispatcher").start();
                    break;
                case MSG_STOP_RECORDING:
                    Log.i(LOG_TAG, "STOP_RECORDING received");
                    break;
            }
        }
    }

    private void setupAudio() throws IllegalStateException {
        mDispatcher =
            AudioDispatcherFactory.fromDefaultMicrophone(SAMPLING_RATE,1024,0);
        AudioProcessor pitchProcessor = new
            PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                    SAMPLING_RATE, 1024, pdh);
        mDispatcher.addAudioProcessor(pitchProcessor);
        // Check for silence (with a threshold)
        double threshold = SilenceDetector.DEFAULT_SILENCE_THRESHOLD;
        mSilenceDetector = new SilenceDetector(threshold, true);
        mDispatcher.addAudioProcessor(mSilenceDetector);
        // Sounds should last 1/4 of a second to 'register'
        // This doesn't seem to work yet.
        LowPassFS lowPassFilter = new LowPassFS(4, SAMPLING_RATE);
        mDispatcher.addAudioProcessor(lowPassFilter);
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "AudioService onCreate() called");
    }

    @Override
    public IBinder onBind(Intent intent) {
//        int N = AudioRecord.getMinBufferSize(44100,
//                AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        /* AudioRecord(int audioSource, int sampleRateInHz, int channelConfig,
         * int audioFormat, int bufferSizeInBytes)*/
        Log.i(LOG_TAG, "AudioService bound");
        return mInMessenger.getBinder(); // Creates an IBinder with a reference to mAudioServiceMessenger's handler
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "AudioService being destroyed");
        super.onDestroy();
    }

    private void sendValue(float val) {
        Bundle bundle = new Bundle();
        bundle.putFloat("pitch", val);
        /** Message.obtain(Handler h, int what, Object obj) */
        Message msg = Message.obtain(null, MSG_PROCESSED_AUDIO, bundle);
        try {
            mOutMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

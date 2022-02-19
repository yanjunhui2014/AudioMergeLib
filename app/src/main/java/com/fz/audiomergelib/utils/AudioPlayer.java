package com.fz.audiomergelib.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import androidx.annotation.WorkerThread;

public class AudioPlayer {
    private static final String TAG = AudioPlayer.class.getSimpleName();
    private static final int DEFAULT_SAMPLE_RATE = 44100;

    private static AudioPlayer sInstance;

    private AudioTrack mAudioTrack;

    private AudioPlayer() {

    }

    public static AudioPlayer getInstance() {
        if (sInstance == null) {
            synchronized (AudioPlayer.class) {
                if (sInstance == null) {
                    sInstance = new AudioPlayer();
                }
            }
        }
        return sInstance;
    }

    public synchronized void init() {
        init(DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

    public synchronized void init(int sampleRate, int channelConfig, int audioFormat) {
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        } else {
            int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfig,
                    audioFormat, minBufferSize, AudioTrack.MODE_STREAM);
            mAudioTrack.play();
        }
    }

    @WorkerThread
    public synchronized boolean play(byte[] data, int size) {
        if (mAudioTrack != null) {
            try {
                int ret = mAudioTrack.write(data, 0, size);
                switch (ret) {
                    case AudioTrack.ERROR_INVALID_OPERATION:
                        Log.w(TAG, "play fail: ERROR_INVALID_OPERATION");
                        return false;
                    case AudioTrack.ERROR_BAD_VALUE:
                        Log.w(TAG, "play fail: ERROR_BAD_VALUE");
                        return false;
                    case AudioManager.ERROR_DEAD_OBJECT:
                        Log.w(TAG, "play fail: ERROR_DEAD_OBJECT");
                        return false;
                    default:
                        return true;
                }
            } catch (IllegalStateException e) {
                Log.w(TAG, "play fail: " + e.getMessage());
                return false;
            }
        }
        Log.w(TAG, "play fail: null mAudioTrack");
        return false;
    }

    public synchronized void release() {
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }
}

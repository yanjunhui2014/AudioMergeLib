package com.fz.audiomergelib.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
public class DubServiceImpl implements DubService{
    private static final String TAG = DubServiceImpl.class.getSimpleName();

    private static final int BUFFER_SIZE = 2048;

    private boolean mIsStopPlay = false;
    private boolean mIsPausePlay = false;
    private final Object mLock = new Object();

    private RxAudioRecorder mRxAudioRecorder;

    private LinkedBlockingQueue<RxAudioRecorder> mRecorderQueue = new LinkedBlockingQueue<>(16);

    @Override
    public Observable<AudioData> rxAudioRecord(final int sampleRate, final int channelConfig,
                                               final int audioFormat) {
        return rxAudioRecord(sampleRate, channelConfig, audioFormat, BUFFER_SIZE);
    }

    @Override
    public Observable<AudioData> rxAudioRecord(int sampleRate, int channelConfig, int audioFormat, int bufferSize) {
        synchronized (mLock) {
            stopAudioRecord();
            if (mRxAudioRecorder == null) {
                mRxAudioRecorder = new RxAudioRecorder(sampleRate, channelConfig, audioFormat, bufferSize);
                mRecorderQueue.add(mRxAudioRecorder);
            }
        }
        return mRxAudioRecorder.record();
    }

    @Override
    public Observable<AudioData> rxAudioRecord(int audioSource, int sampleRate, int channelConfig, int audioFormat, int bufferSize) {
        synchronized (mLock) {
            stopAudioRecord();
            if (mRxAudioRecorder == null) {
                mRxAudioRecorder = new RxAudioRecorder(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
                mRecorderQueue.add(mRxAudioRecorder);
            }
        }
        return mRxAudioRecorder.record();
    }

    @Override
    public void stopAudioRecord() {
        if (mRxAudioRecorder != null) {
            mRxAudioRecorder.stopRecord();
            mRxAudioRecorder = null;
        }
        stopAllAudioRecord();
    }

    @Override
    public Observable<AudioData> rxAudioPlay(final String audioPath, final int sampleRate,
                                             final int channelConfig, final int audioFormat) {
        mIsStopPlay = false;
        mIsPausePlay = false;
        return Observable.create(new ObservableOnSubscribe<AudioData>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<AudioData> emitter) throws IOException {
                if (TextUtils.isEmpty(audioPath)) {
                    emitter.onError(new Throwable("audioPath is null"));
                    return;
                }
                File outFile = new File(audioPath);
                if (!outFile.exists()) {
                    emitter.onError(new Throwable("audioFile is not exists"));
                    return;
                }
                try {
                    AudioPlayer.getInstance().init(sampleRate, channelConfig, audioFormat);
                    FileInputStream inputStream = new FileInputStream(outFile);
                    int read;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    while ((read = inputStream.read(buffer)) > 0) {
                        if (mIsStopPlay) {
                            break;
                        }
                        if (mIsPausePlay) {
                            synchronized (mLock) {
                                try {
                                    mLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }
                        if (!AudioPlayer.getInstance().play(buffer, read)) {
                            break;
                        }
                        emitter.onNext(new AudioData(buffer, read));
                    }
                    inputStream.close();
                    AudioPlayer.getInstance().release();
                    emitter.onComplete();
                } catch (IllegalStateException e) {
                    emitter.onError(e);
                }
            }
        });
    }

    @Override
    public void stopPlayAudio() {
        synchronized (mLock) {
            mIsPausePlay = false;
            mLock.notifyAll();
        }
        mIsStopPlay = true;
        AudioPlayer.getInstance().release();
    }

    @Override
    public void pausePlayAudio() {
        mIsPausePlay = true;
    }

    @Override
    public void resumePlayAudio() {
        synchronized (mLock) {
            mIsPausePlay = false;
            mLock.notifyAll();
        }
    }

    private void stopAllAudioRecord() {
        try {
            synchronized (mLock) {
                while (mRecorderQueue.size() > 0) {
                    RxAudioRecorder recorder = mRecorderQueue.take();
                    if (recorder != null) {
                        recorder.stopRecord();
                        recorder = null;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

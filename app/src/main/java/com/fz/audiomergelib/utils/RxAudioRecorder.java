package com.fz.audiomergelib.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class RxAudioRecorder {

    private static final String TAG = RxAudioRecorder.class.getSimpleName();

    private final AudioRecord mAudioRecord;

    private final byte[] mByteBuffer;
    private final short[] mShortBuffer;
    private final int mByteBufferSize;
    private final int mShortBufferSize;
    private final int mAudioFormat;
    private boolean mIsRecording;

    public RxAudioRecorder(int sampleRate, int channelConfig, int audioFormat,
                           int bufferSize) {
        this(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize);
    }

    public RxAudioRecorder(int audioSource, int sampleRate, int channelConfig, int audioFormat,
                           int bufferSize) {
        mAudioFormat = audioFormat;
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, mAudioFormat);
        mByteBufferSize = bufferSize;
        mShortBufferSize = mByteBufferSize / 2;
        mByteBuffer = new byte[mByteBufferSize];
        mShortBuffer = new short[mShortBufferSize];
        mAudioRecord = new AudioRecord(audioSource, sampleRate, channelConfig,
                audioFormat, Math.max(minBufferSize, bufferSize));
        mIsRecording = false;
    }

    public Observable<AudioData> record() {
        return Observable.create(new ObservableOnSubscribe<AudioData>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<AudioData> emitter) {
                if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
                    emitter.onError(new NoRecordPermissionException());
                    mAudioRecord.release();
                } else if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                        return;
                    }
                    try {
                        mAudioRecord.startRecording();
                    } catch (IllegalStateException e) {
                        emitter.onError(e);
                        mAudioRecord.release();
                        return;
                    }
                    mIsRecording = true;
                    while (mIsRecording) {
                        int ret;
                        if (mAudioFormat == AudioFormat.ENCODING_PCM_16BIT) {
                            ret = mAudioRecord.read(mShortBuffer, 0, mShortBufferSize);
                            if (ret > 0) {
                                long v = 0;
                                // 将 buffer 内容取出，进行平方和运算
                                for (short aMShortBuffer : mShortBuffer) {
                                    v += aMShortBuffer * aMShortBuffer;
                                }
                                // 平方和除以数据总长度，得到音量大小。
                                double mean = v / (double) ret;
                                double volume = 10 * Math.log10(mean);
                                AudioData audioData = AudioData.obtain();
                                audioData.setData(short2byte(mShortBuffer, ret, mByteBuffer));
                                audioData.setSize(ret * 2);
                                audioData.setVolume(volume);
                                emitter.onNext(audioData);
                            } else {
                                emitter.onError(new Throwable("ret = " + ret));
                                stopRecord();
                                mAudioRecord.release();
                                break;
                            }
                        } else {
                            ret = mAudioRecord.read(mByteBuffer, 0, mByteBufferSize);
                            if (ret > 0) {
                                AudioData audioData = AudioData.obtain();
                                audioData.setData(mByteBuffer);
                                audioData.setSize(ret);
                                emitter.onNext(audioData);
                            } else {
                                emitter.onError(new Throwable("ret = " + ret));
                                stopRecord();
                                mAudioRecord.release();
                                break;
                            }
                        }
                    }
                    emitter.onComplete();
                    mAudioRecord.release();
                }
            }
        });
    }

    public void stopRecord() {
        mIsRecording = false;
    }

    public void release() {
        mAudioRecord.release();
    }

    private byte[] short2byte(short[] sData, int size, byte[] bData) {
        if (size > sData.length || size * 2 > bData.length) {
            Log.w(TAG, "size > sData.length || size * 2 > bData.length");
        }
        for (int i = 0; i < size; i++) {
            bData[i * 2] = (byte) (sData[i] & 0x00FF);
            bData[i * 2 + 1] = (byte) (sData[i] >> 8);
        }

        return bData;
    }
}

package com.fz.audiomergelib.utils;

import io.reactivex.Observable;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
public interface DubService {


    Observable<AudioData> rxAudioRecord(int sampleRate, int channelConfig, int audioFormat);

    Observable<AudioData> rxAudioRecord(int sampleRate, int channelConfig, int audioFormat, int bufferSize);

    Observable<AudioData> rxAudioRecord(int audioSource, int sampleRate, int channelConfig, int audioFormat, int bufferSize);

    void stopAudioRecord();

    Observable<AudioData> rxAudioPlay(String audioPath, int sampleRate, int channelConfig,
                                      int audioFormat);

    void stopPlayAudio();

    void pausePlayAudio();

    void resumePlayAudio();

}

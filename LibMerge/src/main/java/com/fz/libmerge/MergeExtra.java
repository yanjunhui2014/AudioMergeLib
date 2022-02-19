package com.fz.libmerge;

import androidx.annotation.Keep;

import java.util.List;

/**
 * Title：合并参数
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
@Keep
public class MergeExtra {
    public static final int SAMPLE_RATE = 44100;

    /**
     * 背景音pcm路径
     */
    public String bgPcmPath;

    /**
     * 录音路劲集合
     */
    public List<String> recordPcmPathList;

    /**
     * 录音开始时间集合（单位ms）
     */
    public List<Long> recordStartTimeList;

    /**
     * 录音结束时间集合 (单位ms,可选)
     */
    public List<Long> recordEndTimeList;

    /**
     * 输出路径
     */
    public String outputPath;

    /**
     * 采样率，默认44100
     */
    public int sampleRate = SAMPLE_RATE;

    public MergeExtra(String bgPcmPath, List<String> recordPcmPathList, List<Long> recordStartTimeList, String outputPath) {
        this.bgPcmPath = bgPcmPath;
        this.recordPcmPathList = recordPcmPathList;
        this.recordStartTimeList = recordStartTimeList;
        this.outputPath = outputPath;
    }

}

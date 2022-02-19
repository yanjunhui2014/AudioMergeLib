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

    public String bgPcmPath;
    public List<String> recordPcmPathList;
    public List<Long> recordStartTimeList;
    public String outputPath;
    public int sampleRate = SAMPLE_RATE;

    public MergeExtra(String bgPcmPath, List<String> recordPcmPathList, List<Long> recordStartTimeList, String outputPath) {
        this.bgPcmPath = bgPcmPath;
        this.recordPcmPathList = recordPcmPathList;
        this.recordStartTimeList = recordStartTimeList;
        this.outputPath = outputPath;
    }

}

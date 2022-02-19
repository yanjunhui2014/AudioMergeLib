package com.fz.libmerge;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
public class AndroidMergeService implements MergeService {

    RandomAccessFile rafBg;
    RandomAccessFile rafRecord;
    RandomAccessFile rafMix;

    @Override
    public void merge(@NonNull MergeExtra extra, @NonNull MergeCallback callback) {
        Objects.requireNonNull(extra.bgPcmPath);
        Objects.requireNonNull(extra.recordPcmPathList);
        Objects.requireNonNull(extra.recordStartTimeList);
        Objects.requireNonNull(extra.outputPath);

        if (extra.recordPcmPathList.size() != extra.recordStartTimeList.size()) {
            throw new IllegalArgumentException("recordPcmPathList的长度需要与recordStartTimeList一致");
        }

        if (MergeUtils.isExists(extra.bgPcmPath)) {
            callback.onFail("合成失败：背景音文件不存在");
            return;
        }
        if (MergeUtils.isExists(extra.outputPath)) {
            callback.onFail("合成失败：输出路径为空");
            return;
        }

        try {
            final String mixPath = extra.bgPcmPath + ".mix";
            MergeUtils.copyFile(extra.bgPcmPath, mixPath);

            rafBg = new RandomAccessFile(extra.bgPcmPath, "rw");
            rafMix = new RandomAccessFile(mixPath, "rw");

            for (int i = 0; i < extra.recordPcmPathList.size(); i++) {
                final String recordPcm = extra.recordPcmPathList.get(i);
                final float startTime = extra.recordStartTimeList.get(i) / 1000.0f;

                int result = writePcmFile(recordPcm, rafMix, getPcmOffsetByStartTime(extra.sampleRate, startTime));
                if (result != 0) {
                    callback.onFail("合成失败：音频文件合并失败");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private synchronized int writePcmFile(String input, RandomAccessFile rafPcm, long offset) {
        try {
            rafPcm.seek(offset);

            InputStream is = new FileInputStream(input);
            byte[] bytes = new byte[1024];
            while (is.read(bytes) != -1) {
                rafPcm.write(bytes);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private long getPcmOffsetByStartTime(int sampleRate, float startTime) {
        Float offset = startTime * (sampleRate * 16 / 8.0f);
        if (offset.longValue() % 16 == 0) {
            return offset.longValue();
        } else {
            return offset.longValue() + (16 - offset.longValue() % 16);
        }
    }

}

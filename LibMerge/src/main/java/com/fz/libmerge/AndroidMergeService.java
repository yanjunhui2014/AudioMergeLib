package com.fz.libmerge;

import android.text.TextUtils;
import android.util.Log;

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
    private static final String TAG = AndroidMergeService.class.getSimpleName();

    RandomAccessFile rafBg;
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

        if (!MergeUtils.isExists(extra.bgPcmPath)) {
            callback.onFail("合成失败：背景音文件不存在");
            return;
        }
        if (TextUtils.isEmpty(extra.outputPath)) {
            callback.onFail("合成失败：outputPath路径为空");
            return;
        }
        if (MergeUtils.isExists(extra.outputPath)) {
            callback.onFail("合成失败：outputPath文件已存在");
            return;
        }

        try {
            MergeUtils.copyFile(extra.bgPcmPath, extra.outputPath);

            rafBg = new RandomAccessFile(extra.bgPcmPath, "rw");
            rafMix = new RandomAccessFile(extra.outputPath, "rw");

            for (int position = 0; position < extra.recordPcmPathList.size(); position++) {
                final String recordPcm = extra.recordPcmPathList.get(position);
                final float startTime = extra.recordStartTimeList.get(position) / 1000.0f;

                long offset = getPcmOffsetByStartTime(extra.sampleRate, startTime);
                long end = getPcmOffsetByStartTime(extra.sampleRate, startTime + 5.0f);

                rafMix.seek(offset);
                FileInputStream fis = new FileInputStream(recordPcm);
                long recordSize = fis.getChannel().size();
                int diff = (int) (end - offset - recordSize);
                Log.i(TAG, "preview diff = " + diff);
                int dataSize = 1024 * 5;
                byte[] data = new byte[dataSize];
                while (fis.read(data) != -1) {
                    rafMix.write(data);
                }
                fis.close();
                if (diff > 0) {
                    rafBg.seek(offset + recordSize);
                    int count = diff / dataSize;
                    if (count == 0) {
                        rafBg.read(data, 0, diff);
                        rafMix.write(data, 0, diff);
                    } else {
                        for (int i = 0; i < count; i++) {
                            rafBg.read(data);
                            rafMix.write(data);
                        }
                        if (diff % dataSize != 0) {
                            rafBg.read(data, 0, diff % dataSize);
                            rafMix.write(data, 0, diff % dataSize);
                        }
                    }
                }
            }

            MergeUtils.close(rafBg);
            MergeUtils.close(rafMix);

            callback.onMergeSuc(extra.outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

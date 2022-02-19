package com.fz.audiomergelib;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fz.audiomergelib.databinding.ActivityAndroidMergeBinding;
import com.fz.audiomergelib.utils.AudioData;
import com.fz.audiomergelib.utils.DubServiceImpl;
import com.fz.libmerge.MergeCallback;
import com.fz.libmerge.MergeExtra;
import com.fz.libmerge.MergeHelper;
import com.fz.libmerge.MergeService;
import com.fz.libmerge.MergeUtils;
import com.fz.libmerge.ServiceType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
public class AndroidMergeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AndroidMergeActivity.class.getSimpleName();

    public final static int SAMPLE_RATE = 44100;
    public final static int BIT_RATE = 64000;
    public final static int CHANNELS = 1;

    ActivityAndroidMergeBinding binding;
    private DubServiceImpl dubService;
    private MergeService mergeService;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private RandomAccessFile mRandomAccessRecord;

    private String rootPath;
    private String outputPath;

    public static Intent createIntent(Context context) {
        return new Intent(context, AndroidMergeActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAndroidMergeBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        rootPath = getExternalFilesDir("androidMerge").getAbsolutePath();
        Log.d(TAG, "rootPath == " + rootPath);
        outputPath = rootPath + "/output.pcm";
        Log.d(TAG, "outputPath == " + outputPath);

        binding.btnPlayOne.setOnClickListener(this);
        binding.btnPlayTwo.setOnClickListener(this);
        binding.btnPlayThree.setOnClickListener(this);
        binding.btnRecordOne.setOnClickListener(this);
        binding.btnRecordTwo.setOnClickListener(this);
        binding.btnRecordThree.setOnClickListener(this);
        binding.btnStartMerge.setOnClickListener(this);
        binding.btnPlayMergeFile.setOnClickListener(this);
        binding.btnRecordClear.setOnClickListener(this);

        dubService = new DubServiceImpl();
        mergeService = MergeHelper.getInstance().getMergeService(ServiceType.ANDROID);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        }
        refreshPlayUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dubService.stopPlayAudio();
        dubService.stopAudioRecord();
        dubService = null;
    }

    @Override
    public void onClick(View v) {
        if (v == binding.btnRecordClear) {
            MergeUtils.delFile(getPathByPosition(1));
            MergeUtils.delFile(getPathByPosition(2));
            MergeUtils.delFile(getPathByPosition(3));
            refreshPlayUi();
            binding.btnStartMerge.setVisibility(View.GONE);
        } else if (v == binding.btnRecordOne) {
            recordByPosition(1);
        } else if (v == binding.btnRecordTwo) {
            recordByPosition(2);
        } else if (v == binding.btnRecordThree) {
            recordByPosition(3);
        } else if (v == binding.btnPlayOne) {
            playRecordByPosition(1);
        } else if (v == binding.btnPlayTwo) {
            playRecordByPosition(2);
        } else if (v == binding.btnPlayThree) {
            playRecordByPosition(3);
        } else if (v == binding.btnStartMerge) {
            final String bgPcmPath = rootPath + "/bg_1440725921119.pcm";
            if (!MergeUtils.isExists(bgPcmPath)) {
                try {
                    InputStream is = getResources().getAssets().open("bg_1440725921119.pcm");
                    if (!MergeUtils.copyFile(is, bgPcmPath)) {
                        Toast.makeText(this, "bgPcm文件拷贝异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "bgPcm文件读取异常", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            List<String> recordPaths = new ArrayList<>();
            recordPaths.add(getPathByPosition(1));
            recordPaths.add(getPathByPosition(2));
            recordPaths.add(getPathByPosition(3));

            for (int i = 0; i < recordPaths.size(); i++) {
                if (!MergeUtils.isExists(recordPaths.get(i))) {
                    Toast.makeText(this, "没有完成第" + (i + 1) + "句录音", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            List<Long> timeList = new ArrayList<>();
            timeList.add(3000L);
            timeList.add(10000L);
            timeList.add(18000L);

            MergeUtils.delFile(outputPath);
            MergeExtra extra = new MergeExtra(bgPcmPath, recordPaths, timeList, outputPath);
            extra.sampleRate = SAMPLE_RATE;

            mergeService.merge(extra, new MergeCallback() {
                @Override
                public void onMergeSuc(String outputPath) {
                    Toast.makeText(AndroidMergeActivity.this, "合并成功", Toast.LENGTH_SHORT).show();
                    binding.btnPlayMergeFile.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFail(String msg) {
                    Log.e(TAG, "合并失败:" + msg);
                    Toast.makeText(AndroidMergeActivity.this, "合并失败:" + msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (v == binding.btnPlayMergeFile) {
            playRecordByPath(outputPath);
        }
    }

    private void refreshPlayUi() {
        for (int i = 1; i < 4; i++) {
            String file = getPathByPosition(i);
            if (MergeUtils.isExists(file)) {
                if (i == 1) {
                    binding.btnPlayOne.setVisibility(View.VISIBLE);
                } else if (i == 2) {
                    binding.btnPlayTwo.setVisibility(View.VISIBLE);
                } else if (i == 3) {
                    binding.btnPlayThree.setVisibility(View.VISIBLE);
                }
            } else {
                if (i == 1) {
                    binding.btnPlayOne.setVisibility(View.GONE);
                } else if (i == 2) {
                    binding.btnPlayTwo.setVisibility(View.GONE);
                } else if (i == 3) {
                    binding.btnPlayThree.setVisibility(View.GONE);
                }
            }
        }
    }

    private void recordByPosition(int position) {
        try {
            File recordFile = new File(getPathByPosition(position));
            if (recordFile.exists()) {
                recordFile.delete();
            }
            mRandomAccessRecord = new RandomAccessFile(recordFile.getAbsoluteFile(), "rw");

            dubService.rxAudioRecord(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT)
                    .flatMap(new Function<AudioData, ObservableSource<Integer>>() {
                        @Override
                        public ObservableSource<Integer> apply(final AudioData audioData) {
                            try {
                                mRandomAccessRecord.write(audioData.getData());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return Observable.just(audioData.getSize());
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(Integer size) {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "record onComplete");
                            try {
                                mRandomAccessRecord.close();
                                mRandomAccessRecord = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            refreshPlayUi();
                        }
                    });

            binding.getRoot().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dubService.stopAudioRecord();
                }
            }, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playRecordByPosition(int position) {
        final String path = getPathByPosition(position);
        playRecordByPath(path);
    }

    private void playRecordByPath(final String path) {
        if (MergeUtils.isExists(path)) {
            long currentTime = System.currentTimeMillis();
            dubService.rxAudioPlay(path, SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<AudioData>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(AudioData audioData) {
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {
                            System.out.println("播放时间：" + (System.currentTimeMillis() - currentTime));
                        }
                    });
        } else {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    private String getPathByPosition(int position) {
        return rootPath + "/record_" + position + ".pcm";
    }

}

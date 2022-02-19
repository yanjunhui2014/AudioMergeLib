package com.fz.libmerge;

/**
 * Title：
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
public class MergeHelper {

    private static MergeHelper instance;

    private AndroidMergeService androidMergeService;

    private MergeHelper() {
    }

    public static MergeHelper getInstance() {
        if (instance == null) {
            synchronized (MergeHelper.class) {
                instance = new MergeHelper();
            }
        }
        return instance;
    }

    public MergeService getMergeService(@ServiceType int serviceType) {
        if (serviceType == ServiceType.ANDROID) {
            return new AndroidMergeService();
        } else if (serviceType == ServiceType.FFMPEG) {
            return new FFmpegMergeService();
        }
        return null;
    }


}

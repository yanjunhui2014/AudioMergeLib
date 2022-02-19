package com.fz.libmerge;

/**
 * Title：合并回调
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
public interface MergeCallback {

    void onMergeSuc(String outputPath);

    void onFail(String msg);

}

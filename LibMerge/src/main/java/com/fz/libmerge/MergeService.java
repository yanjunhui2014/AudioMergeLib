package com.fz.libmerge;

import androidx.annotation.NonNull;

/**
 * Title：合并服务
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
public interface MergeService {

    void merge(@NonNull MergeExtra extra, @NonNull MergeCallback callback);

}

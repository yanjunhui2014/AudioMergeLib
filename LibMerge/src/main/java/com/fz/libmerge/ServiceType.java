package com.fz.libmerge;

import androidx.annotation.IntDef;

/**
 * Title：服务实现类型
 * Describe：
 * Remark：
 * <p>
 * Created by Milo
 * E-Mail : 303767416@qq.com
 * 2022/2/19
 */
@IntDef({ServiceType.ANDROID, ServiceType.FFMPEG})
public @interface ServiceType {
    int ANDROID = 0;
    int FFMPEG = 1;
}

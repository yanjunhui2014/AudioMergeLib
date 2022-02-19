package com.fz.audiomergelib.utils;

/**
 * @author zhl
 * @date 2018/12/26
 */
public class NoRecordPermissionException extends Exception {

    @Override
    public String getMessage() {
        return "录音初始化失败，请检查录音权限";
    }
}

package com.arcsoft.arcfacedemo.util.face.constants;

/**
 * 人脸识别中可能出现的状态
 * @author
 */
public @interface RequestFeatureStatus {
    /**
     * 默认状态
     */
    int DEFAULT = -1;
    /**
     * 处理中
     */
    int SEARCHING = 0;
    /**
     * 识别成功
     */
    int SUCCEED = 1;
    /**
     * 待重试
     */
    int TO_RETRY = 2;
    /**
     * 识别失败
     */
    int FAILED = 3;
}

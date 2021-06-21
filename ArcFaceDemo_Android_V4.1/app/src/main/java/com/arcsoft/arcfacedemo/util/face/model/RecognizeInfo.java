package com.arcsoft.arcfacedemo.util.face.model;

import com.arcsoft.arcfacedemo.util.face.constants.RequestFeatureStatus;
import com.arcsoft.face.LivenessInfo;

/**
 * 单个人脸（faceId）识别过程中的信息
 */
public class RecognizeInfo {
    /**
     * 用于记录人脸识别相关状态
     */
    private int recognizeStatus = RequestFeatureStatus.TO_RETRY;
    /**
     * 用于记录人脸特征提取出错重试次数
     */
    private int extractErrorRetryCount;
    /**
     * 用于存储活体值
     */
    private int liveness = LivenessInfo.UNKNOWN;
    /**
     * 用于存储活体检测出错重试次数
     */
    private int livenessErrorRetryCount;
    /**
     * 用户姓名，用于显示
     */
    private String name;
    /**
     * 特征等活体的lock
     */
    private Object waitLock = new Object();

    public int getRecognizeStatus() {
        return recognizeStatus;
    }

    public void setRecognizeStatus(int recognizeStatus) {
        this.recognizeStatus = recognizeStatus;
    }

    public void setLiveness(int liveness) {
        this.liveness = liveness;
    }

    public int increaseAndGetExtractErrorRetryCount() {
        return ++extractErrorRetryCount;
    }

    public int getLiveness() {
        return liveness;
    }

    public int increaseAndGetLivenessErrorRetryCount() {
        return ++livenessErrorRetryCount;
    }

    public void setExtractErrorRetryCount(int extractErrorRetryCount) {
        this.extractErrorRetryCount = extractErrorRetryCount;
    }

    public void setLivenessErrorRetryCount(int livenessErrorRetryCount) {
        this.livenessErrorRetryCount = livenessErrorRetryCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getWaitLock() {
        return waitLock;
    }

    public int getExtractErrorRetryCount() {
        return extractErrorRetryCount;
    }

    public int getLivenessErrorRetryCount() {
        return livenessErrorRetryCount;
    }
}
package com.arcsoft.arcfacedemo.util.debug;

import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;

/**
 * 异常分析界面，一些识别相关信息的回调
 */
public interface DebugInfoCallback {
    /**
     * track/extract/liveness结果回调
     *
     * @param errorType 异常类型，可能是人脸追踪错误/特征提取错误/活体检测结果
     * @param nv21      处理的图像数据
     * @param fileName  文件名
     */
    void onNormalErrorOccurred(int errorType, byte[] nv21, String fileName);

    /**
     * 比对失败的回调
     *
     * @param nv21                 处理的图像数据
     * @param fileName             文件名
     * @param recognizeFeatureName 预览画面中人脸提取的特征数据文件名
     * @param registerFeatureName  注册照的对应的特征数据文件名
     * @param recognizeFaceFeature 预览画面中人脸提取的特征数据
     * @param faceEntity           相似度最高者的数据库中的人脸信息
     */
    void onCompareFailed(byte[] nv21, String fileName, String recognizeFeatureName, String registerFeatureName, byte[] recognizeFaceFeature, FaceEntity faceEntity);

    void onRecognizePass(String performanceInfo);

    void onSavePerformanceInfo(String performanceInfo);
}

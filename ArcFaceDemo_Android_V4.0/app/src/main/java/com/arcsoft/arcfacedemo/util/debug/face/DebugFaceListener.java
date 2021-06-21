package com.arcsoft.arcfacedemo.util.debug.face;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacedemo.util.face.constants.LivenessType;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;

/**
 * 人脸处理回调
 */
public interface DebugFaceListener {
    /**
     * 当出现异常时执行
     *
     * @param e 异常信息
     */
    void onFail(Exception e);


    /**
     * 请求人脸特征后的回调
     *
     * @param nv21        处理的NV21图像数据
     * @param faceFeature 人脸特征
     * @param trackId     人脸trackId
     * @param faceInfo    人脸信息
     * @param frCost        耗时
     * @param errorCode   错误码
     */
    void onFaceFeatureInfoGet(byte[] nv21, @Nullable FaceFeature faceFeature, Integer trackId, FaceInfo faceInfo, long frCost, long fqCost, Integer errorCode);

    /**
     * 请求活体检测后的回调
     *  @param nv21         处理的NV21图像数据
     * @param livenessInfo 活体检测结果
     * @param trackId      人脸Id（相当于请求码）
     * @param faceInfo      人脸信息
     * @param cost         耗时
     * @param errorCode    错误码
     * @param livenessType 活体检测类型
     */
    void onFaceLivenessInfoGet(byte[] nv21, @Nullable LivenessInfo livenessInfo, Integer trackId, FaceInfo faceInfo, long cost, Integer errorCode, LivenessType livenessType);
}

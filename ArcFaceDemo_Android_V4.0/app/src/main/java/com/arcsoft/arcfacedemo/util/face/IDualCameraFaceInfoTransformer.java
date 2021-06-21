package com.arcsoft.arcfacedemo.util.face;

import com.arcsoft.face.FaceInfo;

/**
 * 设置双目识别时，将RGB Camera帧数据检测到的人脸信息用于IR Camera帧数据活体检测时的转换方式
 */
public interface IDualCameraFaceInfoTransformer {
    /**
     * 将RGB Camera帧数据检测到的人脸信息用于IR Camera帧数据活体检测时的转换方式
     *
     * @param faceInfo RGB Camera帧数据检测到的人脸信息
     * @return 转换后，用于IR活体检测的FaceInfo
     */
    FaceInfo transformFaceInfo(FaceInfo faceInfo);
}

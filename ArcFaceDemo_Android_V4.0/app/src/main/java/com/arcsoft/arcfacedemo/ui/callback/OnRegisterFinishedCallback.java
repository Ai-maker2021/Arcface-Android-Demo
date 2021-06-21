
package com.arcsoft.arcfacedemo.ui.callback;

import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;

/**
 * 实时注册的结果回调
 */
public interface OnRegisterFinishedCallback {
    /**
     * 注册结束的回调
     *
     * @param facePreviewInfo 注册的人脸信息
     * @param success         是否成功
     */
    void onRegisterFinished(FacePreviewInfo facePreviewInfo, boolean success);
}
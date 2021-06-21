package com.arcsoft.arcfacedemo.util.face.facefilter;


import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;

import java.util.List;

/**
 * 人脸识别区域过滤器：
 * 仅保留人脸区域在{@link FaceRecognizeAreaFilter#validArea}中的人脸。（基于View位置判断）
 */
public class FaceRecognizeAreaFilter implements FaceRecognizeFilter {
    private static final String TAG = "FaceRecognizeAreaFilter";
    private Rect validArea;

    public FaceRecognizeAreaFilter(Rect validArea) {
        this.validArea = validArea;
    }

    @Override
    public void filter(List<FacePreviewInfo> facePreviewInfoList) {
        for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
            if (!facePreviewInfo.isQualityPass()) {
                continue;
            }
            facePreviewInfo.setQualityPass(validArea.contains(facePreviewInfo.getRgbTransformedRect()));
        }
    }
}

package com.arcsoft.arcfacedemo.util.face.facefilter;

import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;

import java.util.List;

/**
 * 人脸尺寸过滤器：
 * 仅保留人脸宽度大于{@link FaceSizeFilter#horizontalSize}，且人脸高度大于{@link FaceSizeFilter#verticalSize}的人脸。
 */
public class FaceSizeFilter implements FaceRecognizeFilter {
    private int horizontalSize;
    private int verticalSize;

    private static final String TAG = "FaceSizeFilter";

    public FaceSizeFilter(int horizontalSize, int verticalSize) {
        this.horizontalSize = horizontalSize;
        this.verticalSize = verticalSize;
    }

    @Override
    public void filter(List<FacePreviewInfo> facePreviewInfoList) {
        for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
            if (!facePreviewInfo.isQualityPass()) {
                continue;
            }
            if (facePreviewInfo.getFaceInfoRgb() != null) {
                Rect rgbRect = facePreviewInfo.getFaceInfoRgb().getRect();
                Rect irRect = facePreviewInfo.getFaceInfoIr() == null ? null : facePreviewInfo.getFaceInfoIr().getRect();
                boolean rgbRectValid = rgbRect == null || (rgbRect.width() > horizontalSize && rgbRect.height() > verticalSize);
                boolean irRectValid = irRect == null || (irRect.width() > horizontalSize && irRect.height() > verticalSize);
                facePreviewInfo.setQualityPass(rgbRectValid && irRectValid);
            }
        }
    }
}

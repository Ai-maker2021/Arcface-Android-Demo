package com.arcsoft.arcfacedemo.util.face.model;

import android.graphics.Rect;

import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.MaskInfo;

/**
 * 人脸追踪时的信息
 */
public class FacePreviewInfo {
    /**
     * RGB人脸信息，包括人脸框和人脸角度
     */
    private FaceInfo faceInfoRgb;
    /**
     * IR人脸信息，包括人脸框和人脸角度
     */
    private FaceInfo faceInfoIr;
    /**
     * 可见光成像对应的用于FaceRectView绘制的Rect
     */
    private Rect rgbTransformedRect;
    /**
     * 红外成像对应的用于FaceRectView绘制的Rect
     */
    private Rect irTransformedRect;
    private int rgbLiveness = LivenessInfo.UNKNOWN;
    private int irLiveness = LivenessInfo.UNKNOWN;
    private float imageQuality = 0f;
    /**
     * 识别区域是否合法
     */
    private boolean recognizeAreaValid;
    /**
     * 基于{@link FaceInfo#getFaceId()}的一个偏移值，可理解为SDK截至目前检测到的人次，唯一性同faceId
     */
    private int trackId;
    /**
     * 整体质量是否通过，包括人脸大小、角度、移动速度等
     */
    private boolean qualityPass = true;

    /**
     * 是否戴口罩
     */
    private int mask;

    private Rect foreRect;

    public Rect getForeRect() {
        return foreRect;
    }

    public void setForeRect(Rect foreRect) {
        this.foreRect = foreRect;
    }

    public FacePreviewInfo(FaceInfo faceInfoRgb, int trackId) {
        this.faceInfoRgb = faceInfoRgb;
        this.trackId = trackId;
    }

    public FaceInfo getFaceInfoRgb() {
        return faceInfoRgb;
    }

    public void setFaceInfoRgb(FaceInfo faceInfoRgb) {
        this.faceInfoRgb = faceInfoRgb;
    }


    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public void setRgbTransformedRect(Rect rgbTransformedRect) {
        this.rgbTransformedRect = rgbTransformedRect;
    }

    public void setIrTransformedRect(Rect irTransformedRect) {
        this.irTransformedRect = irTransformedRect;
    }

    public Rect getRgbTransformedRect() {
        return rgbTransformedRect;
    }

    public Rect getIrTransformedRect() {
        return irTransformedRect;
    }

    public boolean isRecognizeAreaValid() {
        return recognizeAreaValid;
    }

    public void setRecognizeAreaValid(boolean recognizeAreaValid) {
        this.recognizeAreaValid = recognizeAreaValid;
    }

    public void setFaceInfoIr(FaceInfo faceInfoIr) {
        this.faceInfoIr = faceInfoIr;
    }

    public FaceInfo getFaceInfoIr() {
        return faceInfoIr;
    }

    public int getRgbLiveness() {
        return rgbLiveness;
    }

    public void setRgbLiveness(int rgbLiveness) {
        this.rgbLiveness = rgbLiveness;
    }

    public int getIrLiveness() {
        return irLiveness;
    }

    public void setIrLiveness(int irLiveness) {
        this.irLiveness = irLiveness;
    }

    public void setImageQuality(float imageQuality) {
        this.imageQuality = imageQuality;
    }

    public float getImageQuality() {
        return imageQuality;
    }

    public boolean isQualityPass() {
        return qualityPass;
    }

    public void setQualityPass(boolean qualityPass) {
        this.qualityPass = qualityPass;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }
}

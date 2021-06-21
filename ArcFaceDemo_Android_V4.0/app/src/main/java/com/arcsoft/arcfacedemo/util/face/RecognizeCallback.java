package com.arcsoft.arcfacedemo.util.face;

import com.arcsoft.arcfacedemo.ui.model.CompareResult;

public interface RecognizeCallback {
    /**
     * 识别结果回调
     *
     * @param compareResult 比对结果
     * @param liveness      活体值
     * @param similarPass   是否通过（依据设置的阈值）
     */
    void onRecognized(CompareResult compareResult, Integer liveness, boolean similarPass);

    /**
     * 提示文字变更的回调
     */
    void onNoticeChanged(String notice);
}

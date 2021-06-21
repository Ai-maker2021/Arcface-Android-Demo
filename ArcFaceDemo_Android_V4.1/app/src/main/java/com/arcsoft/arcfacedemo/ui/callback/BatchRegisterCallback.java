package com.arcsoft.arcfacedemo.ui.callback;

/**
 * 批量注册的回调
 */
public interface BatchRegisterCallback {
    /**
     * 批量注册过程中的回调
     *
     * @param current 当前已处理的数量
     * @param failed  处理失败的数量
     * @param total   处理总数
     */
    void onProcess(int current, int failed, int total);

    /**
     * 批量注册结束的回调
     *
     * @param current 当前已处理的数量
     * @param failed  处理失败的数量
     * @param total   处理总数
     * @param errMsg  错误消息
     */
    void onFinish(int current, int failed, int total, String errMsg);
}

package com.arcsoft.arcfacedemo.util.debug.model;

import android.os.Build;

import androidx.annotation.NonNull;

public class BasicInfo {
    private String cpu;
    private String memory;
    private String appId;
    private String sdkKey;
    private String activeKey;
    private String sdkVersion;

    public BasicInfo(String cpu, String memory, String appId, String sdkKey, String activeKey, String sdkVersion) {
        this.cpu = cpu;
        this.memory = memory;
        this.appId = appId;
        this.sdkKey = sdkKey;
        this.activeKey = activeKey;
        this.sdkVersion = sdkVersion;
    }

    @NonNull
    @Override
    public String toString() {
        return "cpu:" + cpu + "\r\n" +
                "memory:" + memory + "\r\n" +
                "appId:" + appId + "\r\n" +
                "sdkKey:" + sdkKey + "\r\n" +
                "activeKey:" + activeKey + "\r\n" +
                "sdkVersion:" + sdkVersion;
    }

    public String getBasicInfoString() {
        return this.toString() + "\r\n" +
                "androidVersion:" + Build.VERSION.RELEASE + "\r\n" +
                "brand:" + Build.BRAND + "\r\n" +
                "board:" + Build.BOARD + "\r\n" +
                "model:" + Build.MODEL + "\r\n" +
                "device:" + Build.DEVICE;
    }
}

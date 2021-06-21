package com.arcsoft.arcfacedemo.util.debug;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DeviceInfoUtil {
    /**
     * 获取CPU型号，即/proc/cpuinfo中的Hardware字段
     *
     * @return CPU型号
     */
    public static String getCpuModel() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("/proc/cpuinfo");
            Properties properties = new Properties();
            properties.load(fis);
            String cpu = (String) properties.get("Hardware");
            return cpu == null ? "unknown" : cpu;
        } catch (IOException e) {
            return "unknown";
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

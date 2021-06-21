package com.arcsoft.arcfacedemo.util;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.imageutil.ArcSoftImageUtilError;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ErrorCodeUtil {
    /**
     * 将ArcFace错误码转换为对应的错误码常量名，便于理解
     * TODO:目前每次都遍历，如果使用频繁，建议将Field缓存处理，避免每次都反射
     *
     * @param code 错误码
     * @return 错误码常量名
     */
    public static String arcFaceErrorCodeToFieldName(int code) {
        Field[] declaredFields = ErrorInfo.class.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            try {
                if (Modifier.isFinal(declaredField.getModifiers()) && ((int) declaredField.get(ErrorInfo.class)) == code) {
                    return declaredField.getName();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return "unknown error";
    }
    /**
     * 将ArcSoftImageUtil错误码转换为对应的错误码常量名，便于理解
     * TODO:目前每次都遍历，如果使用频繁，建议将Field缓存处理，避免每次都反射
     *
     * @param code 错误码
     * @return 错误码常量名
     */
    public static String imageUtilErrorCodeToFieldName(int code) {
        Field[] declaredFields = ArcSoftImageUtilError.class.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            try {
                if (Modifier.isFinal(declaredField.getModifiers()) && ((int) declaredField.get(ArcSoftImageUtilError.class)) == code) {
                    return declaredField.getName();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return "unknown error";
    }
}

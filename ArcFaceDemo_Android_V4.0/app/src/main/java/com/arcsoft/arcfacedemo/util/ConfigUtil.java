package com.arcsoft.arcfacedemo.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.Constants;
import com.arcsoft.face.enums.DetectFaceOrientPriority;

/**
 * 配置项设置，注意，{@link SharedPreferences}对象需要使用{@link PreferenceManager#getDefaultSharedPreferences(Context)}，
 * 以确保和{@link androidx.preference.PreferenceFragmentCompat}操作同一个xml。
 */
public class ConfigUtil {
    /**
     * 识别阈值
     */
    private static final float RECOMMEND_RECOGNIZE_THRESHOLD = 0.80f;
    /**
     * 可见光活体检测阈值
     */
    private static final float RECOMMEND_RGB_LIVENESS_THRESHOLD = 0.50f;
    /**
     * 红外活体检测阈值
     */
    private static final float RECOMMEND_IR_LIVENESS_THRESHOLD = 0.70f;
    /**
     * 图像质量检测阈值：未戴口罩，且在人脸识别场景下
     */
    public static final float IMAGE_QUALITY_NO_MASK_RECOGNIZE_THRESHOLD = 0.49f;
    /**
     * 图像质量检测阈值：未戴口罩，且在人脸注册场景下
     */
    public static final float IMAGE_QUALITY_NO_MASK_REGISTER_THRESHOLD = 0.63f;
    /**
     * 图像质量检测阈值：戴口罩，且在人脸识别场景下
     */
    public static final float IMAGE_QUALITY_MASK_RECOGNIZE_THRESHOLD = 0.29f;

    /**
     * 人脸大小限制
     */
    private static final int RECOMMEND_FACE_SIZE_LIMIT = 160;
    /**
     * 上下帧人脸移动像素数限制
     */
    private static final int RECOMMEND_FACE_MOVE_LIMIT = 20;
    /**
     * 默认最大人脸检测数量
     */
    private static final int DEFAULT_MAX_DETECT_FACE_NUM = 1;
    /**
     * 默认人脸大小占比
     */
    private static final int DEFAULT_SCALE = 16;
    /**
     * 默认相机分辨率
     */
    private static final String DEFAULT_PREVIEW_SIZE = "1280x720";


    /**
     * 获取String类型的preference
     *
     * @param context      上下文
     * @param keyRes       key的Id
     * @param defaultValue 默认值
     * @return preference值
     */
    private static String getString(Context context, @StringRes int keyRes, String defaultValue) {
        if (context == null) {
            return defaultValue;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(keyRes);
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * 获取boolean类型的preference
     *
     * @param context      上下文
     * @param keyRes       key的Id
     * @param defaultValue 默认值
     * @return preference值
     */
    private static boolean getBoolean(Context context, @StringRes int keyRes, boolean defaultValue) {
        if (context == null) {
            return defaultValue;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(keyRes);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * 获取int类型的preference
     *
     * @param context      上下文
     * @param keyRes       key的Id
     * @param defaultValue 默认值
     * @return preference值
     */
    private static int getInt(Context context, @StringRes int keyRes, int defaultValue) {
        if (context == null) {
            return defaultValue;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(keyRes);
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * 获取float类型的preference
     *
     * @param context      上下文
     * @param keyRes       key的Id
     * @param defaultValue 默认值
     * @return preference值
     */
    private static float getFloat(Context context, @StringRes int keyRes, float defaultValue) {
        if (context == null) {
            return defaultValue;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(keyRes);
        return sharedPreferences.getFloat(key, defaultValue);
    }

    /**
     * 保存int类型的preference
     *
     * @param context  上下文
     * @param keyRes   key的Id
     * @param newValue key对应的value
     * @return 是否保存成功
     */
    private static boolean commitInt(Context context, @StringRes int keyRes, int newValue) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.edit()
                .putInt(context.getString(keyRes), newValue)
                .commit();
    }

    /**
     * 保存String类型的preference
     *
     * @param context  上下文
     * @param keyRes   key的Id
     * @param newValue key对应的value
     * @return 是否保存成功
     */
    private static boolean commitString(Context context, @StringRes int keyRes, String newValue) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.edit()
                .putString(context.getString(keyRes), newValue)
                .commit();
    }

    /**
     * 设置截至目前已track到的人脸数
     *
     * @param context          上下文
     * @param trackedFaceCount 截至目前已track到的人脸数
     * @return 是否保存成功
     */
    public static boolean setTrackedFaceCount(Context context, int trackedFaceCount) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.edit()
                .putInt(context.getString(R.string.preference_track_face_count), trackedFaceCount)
                .commit();
    }

    /**
     * 获取到截至目前已track到的人脸数
     *
     * @param context 上下文
     * @return 之前已track到的人脸数
     */
    public static int getTrackedFaceCount(Context context) {
        return getInt(context, R.string.preference_track_face_count, 0);
    }

    /**
     * 获取VIDEO模式人脸检测角度优先级
     *
     * @param context 上下文
     * @return VIDEO模式人脸检测角度优先级
     */
    public static DetectFaceOrientPriority getFtOrient(Context context) {
        if (context == null) {
            return DetectFaceOrientPriority.ASF_OP_ALL_OUT;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return DetectFaceOrientPriority.valueOf(sharedPreferences.getString(context.getString(R.string.preference_choose_detect_degree), DetectFaceOrientPriority.ASF_OP_ALL_OUT.name()));
    }

    /**
     * TODO: 该Demo基于单人脸识别实现，若想使用多人脸识别，请将 return true 改成 return getBoolean，并修改相关配置项的preference.xml和业务代码
     *
     * 获取识别界面是否保留最大人脸
     *
     * @param context 上下文
     * @return 别界面是否保留最大人脸
     */
    public static boolean isKeepMaxFace(Context context) {
//        return getBoolean(context, R.string.preference_recognize_keep_max_face, false);
        return true;
    }

    /**
     * 获取是否限制人脸识别区域
     *
     * @param context 上下文
     * @return 是否限制人脸识别区域
     */
    public static boolean isRecognizeAreaLimited(Context context) {
        return getBoolean(context, R.string.preference_recognize_limit_recognize_area, false);
    }

    /**
     * 视频人脸比对界面中，获取最大的人脸检测数量
     *
     * @param context 上下文
     * @return 最大的人脸检测数量
     */
    public static int getRecognizeMaxDetectFaceNum(Context context) {
        try {
            return Integer.parseInt(getString(context, R.string.preference_recognize_max_detect_num, String.valueOf(DEFAULT_MAX_DETECT_FACE_NUM)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return DEFAULT_MAX_DETECT_FACE_NUM;
    }

    /**
     * 视频人脸比对界面中，获取预先设置的scale值
     *
     * @param context 上下文
     * @return scale值
     */
    public static int getRecognizeScale(Context context) {
        try {
            return Integer.parseInt(getString(context, R.string.preference_recognize_scale_value, String.valueOf(DEFAULT_SCALE)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return DEFAULT_SCALE;
    }

    /**
     * 获取双目水平成像偏移量
     *
     * @param context 上下文
     * @return 双目水平偏移量
     */
    public static int getDualCameraHorizontalOffset(Context context) {
        return getInt(context, R.string.preference_dual_camera_offset_horizontal, 0);
    }

    /**
     * 获取双目垂直成像偏移量
     *
     * @param context 上下文
     * @return 双目水平偏移量
     */
    public static int getDualCameraVerticalOffset(Context context) {
        return getInt(context, R.string.preference_dual_camera_offset_vertical, 0);
    }

    /**
     * 视频人脸比对界面中，获取预先设置的识别阈值
     *
     * @param context 上下文
     * @return 识别阈值
     */
    public static float getRecognizeThreshold(Context context) {
        return Float.parseFloat(getString(context, R.string.preference_recognize_threshold, String.valueOf(RECOMMEND_RECOGNIZE_THRESHOLD)));
    }

    public static float getRgbLivenessThreshold(Context context) {
        return Float.parseFloat(getString(context, R.string.preference_rgb_liveness_threshold, String.valueOf(RECOMMEND_RGB_LIVENESS_THRESHOLD)));
    }

    public static float getIrLivenessThreshold(Context context) {
        return Float.parseFloat(getString(context, R.string.preference_ir_liveness_threshold, String.valueOf(RECOMMEND_IR_LIVENESS_THRESHOLD)));
    }

    public static float getImageQualityNoMaskRecognizeThreshold(Context context) {
        return Float.parseFloat(getString(context, R.string.preference_image_quality_no_mask_recognize_threshold,
                String.valueOf(IMAGE_QUALITY_NO_MASK_RECOGNIZE_THRESHOLD)));
    }

    public static float getImageQualityNoMaskRegisterThreshold(Context context) {
        return Float.parseFloat(getString(context, R.string.preference_image_quality_no_mask_register_threshold,
                String.valueOf(IMAGE_QUALITY_NO_MASK_REGISTER_THRESHOLD)));
    }

    public static float getImageQualityMaskRecognizeThreshold(Context context) {
        return Float.parseFloat(getString(context, R.string.preference_image_quality_mask_recognize_threshold,
                String.valueOf(IMAGE_QUALITY_MASK_RECOGNIZE_THRESHOLD)));
    }

    public static int getFaceSizeLimit(Context context) {
        return Integer.parseInt(getString(context, R.string.preference_recognize_face_size_limit, String.valueOf(RECOMMEND_FACE_SIZE_LIMIT)));
    }

    public static int getFaceMoveLimit(Context context) {
        return Integer.parseInt(getString(context, R.string.preference_recognize_move_pixel_limit, String.valueOf(RECOMMEND_FACE_MOVE_LIMIT)));
    }

    public static String getLivenessDetectType(Context context) {
        return getString(context, R.string.preference_liveness_detect_type, context.getString(R.string.value_liveness_type_rgb));
    }


    public static boolean isEnableImageQualityDetect(Context context) {
        return getBoolean(context, R.string.preference_enable_image_quality_detect, true);
    }

    public static boolean isEnableFaceSizeLimit(Context context) {
        return getBoolean(context, R.string.preference_enable_face_size_limit, false);
    }

    public static boolean isEnableFaceMoveLimit(Context context) {
        return getBoolean(context, R.string.preference_enable_face_move_limit, false);
    }

    public static boolean isSwitchCamera(Context context) {
        return getBoolean(context, R.string.preference_switch_camera, false);
    }

    public static String getPreviewSize(Context context) {
        return getString(context, R.string.preference_dual_camera_preview_size, DEFAULT_PREVIEW_SIZE);
    }

    public static String getRgbCameraAdditionalRotation(Context context) {
        return getString(context, R.string.preference_rgb_camera_rotation, "0");
    }

    public static String getIrCameraAdditionalRotation(Context context) {
        return getString(context, R.string.preference_ir_camera_rotation, "0");
    }

    public static String getAppId(Context context) {
        return getString(context, R.string.preference_app_id, Constants.APP_ID);
    }

    public static String getSdkKey(Context context) {
        return getString(context, R.string.preference_sdk_key, Constants.SDK_KEY);
    }

    public static String getActiveKey(Context context) {
        return getString(context, R.string.preference_active_key, Constants.ACTIVE_KEY);
    }

    public static boolean commitAppId(Context context, String appId) {
        return commitString(context, R.string.preference_app_id, appId);
    }

    public static boolean commitSdkKey(Context context, String sdkKey) {
        return commitString(context, R.string.preference_sdk_key, sdkKey);
    }

    public static boolean commitActiveKey(Context context, String activeKey) {
        return commitString(context, R.string.preference_active_key, activeKey);
    }


    public static boolean isDrawRgbRectHorizontalMirror(Context context) {
        return getBoolean(context, R.string.preference_draw_rgb_rect_horizontal_mirror, false);
    }

    public static boolean isDrawIrRectHorizontalMirror(Context context) {
        return getBoolean(context, R.string.preference_draw_ir_rect_horizontal_mirror, false);
    }

    public static boolean isDrawRgbRectVerticalMirror(Context context) {
        return getBoolean(context, R.string.preference_draw_rgb_rect_vertical_mirror, false);
    }

    public static boolean isDrawIrRectVerticalMirror(Context context) {
        return getBoolean(context, R.string.preference_draw_ir_rect_vertical_mirror, false);
    }

    public static boolean isDrawRgbPreviewHorizontalMirror(Context context) {
        return getBoolean(context, R.string.preference_rgb_preview_horizontal_mirror, false);
    }

    public static boolean isDrawIrPreviewHorizontalMirror(Context context) {
        return getBoolean(context, R.string.preference_ir_preview_horizontal_mirror, false);
    }

}

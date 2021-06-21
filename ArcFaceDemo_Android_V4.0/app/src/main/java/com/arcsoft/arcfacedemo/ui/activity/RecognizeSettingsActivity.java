package com.arcsoft.arcfacedemo.ui.activity;

import android.Manifest;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.preference.RecognizeSettingsPreferenceFragment;
import com.arcsoft.arcfacedemo.preference.ThresholdPreference;
import com.arcsoft.arcfacedemo.preference.ChooseDetectDegreeListPreference;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.camera.DualCameraHelper;

import java.util.HashMap;

public class RecognizeSettingsActivity extends BaseActivity {
    private static final int ACTION_REQUEST_CAMERA = 1;
    private static final String[] NEEDED_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_settings);
        enableBackIfActionBarExists();
        if (checkPermissions(NEEDED_PERMISSIONS)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_CAMERA);
        }
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        super.afterRequestPermission(requestCode, isAllGranted);
        if (!isAllGranted) {
            showLongToast(getString(R.string.permission_denied));
            return;
        }
        if (requestCode == ACTION_REQUEST_CAMERA) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    /**
     * 设置人脸识别属性相关的Fragment，具体配置项见 R.xml.preferences_recognize 文件
     */
    public static class SettingsFragment extends RecognizeSettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
        // 检测角度相关的 Preference
        ChooseDetectDegreeListPreference chooseDetectDegreePreference;

        // 最大人脸检测数量相关 Preference
//        ListPreference maxDetectFaceNumPreference;

        // 识别时人脸尺寸比例相关 Preference
        ListPreference scalePreference;
        // 活体检测方式 Preference
        ListPreference livenessTypePreference;

        private Boolean canOpenDualCamera = null;

        ThresholdPreference imageQualityNoMaskRecognizeThresholdPreference;
        ThresholdPreference imageQualityNoMaskRegisterThresholdPreference;
        ThresholdPreference imageQualityMaskRecognizeThresholdPreference;

        ThresholdPreference similarThresholdPreference;
        ThresholdPreference rgbLivenessThresholdPreference;
        ThresholdPreference irLivenessThresholdPreference;

//        AdjustableIntegerPreference faceMoveLimitPreference;
//        AdjustableIntegerPreference faceSizeLimitPreference;


        // 用于显示检测角度用的 map，KEY - VALUE如下
        // ASF_OP_0_ONLY -> 仅0度
        // ....
        // ASF_OP_ALL_OUT -> 全角度
        HashMap<String, String> ftOrientMap;
        HashMap<String, String> livenessDescriptionMap;

        String[] livenessDetectTypeValueArray;
        String[] livenessDetectTypeArray;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_recognize, rootKey);
            initData();
            initPreference();
        }

        private void initData() {
            canOpenDualCamera = DualCameraHelper.canOpenDualCamera();

            ftOrientMap = new HashMap<>();
            livenessDescriptionMap = new HashMap<>();
            String[] ftValuesArray = getResources().getStringArray(R.array.recognize_orient_priority_values);
            String[] ftDescArray = getResources().getStringArray(R.array.recognize_orient_priority_desc);
            for (int i = 0; i < ftValuesArray.length; i++) {
                ftOrientMap.put(ftValuesArray[i], ftDescArray[i]);
            }

            livenessDetectTypeValueArray = getResources().getStringArray(canOpenDualCamera ? R.array.liveness_detect_type_with_ir_values : R.array.liveness_detect_type_no_ir_values);
            livenessDetectTypeArray = getResources().getStringArray(canOpenDualCamera ? R.array.liveness_detect_with_ir_types : R.array.liveness_detect_no_ir_types);

            for (int i = 0; i < livenessDetectTypeValueArray.length; i++) {
                livenessDescriptionMap.put(livenessDetectTypeValueArray[i], livenessDetectTypeArray[i]);
            }

        }

        private void initPreference() {
            chooseDetectDegreePreference = findPreference(getString(R.string.preference_choose_detect_degree));
            chooseDetectDegreePreference.setOnPreferenceChangeListener(this);
            chooseDetectDegreePreference.setEntries(R.array.recognize_orient_priority_desc);
            chooseDetectDegreePreference.setEntryValues(R.array.recognize_orient_priority_values);

//            maxDetectFaceNumPreference = findPreference(getString(R.string.preference_recognize_max_detect_num));
//            maxDetectFaceNumPreference.setOnPreferenceChangeListener(this);

            scalePreference = findPreference(getString(R.string.preference_recognize_scale_value));
            scalePreference.setOnPreferenceChangeListener(this);

            similarThresholdPreference = findPreference(getString(R.string.preference_recognize_threshold));
            similarThresholdPreference.setOnPreferenceChangeListener(this);

//            faceMoveLimitPreference = findPreference(getString(R.string.preference_recognize_move_pixel_limit));
//            faceMoveLimitPreference.setOnPreferenceChangeListener(this);
//
//            faceSizeLimitPreference = findPreference(getString(R.string.preference_recognize_face_size_limit));
//            faceSizeLimitPreference.setOnPreferenceChangeListener(this);

            imageQualityNoMaskRecognizeThresholdPreference = findPreference(getString(R.string.preference_image_quality_no_mask_recognize_threshold));
            imageQualityNoMaskRecognizeThresholdPreference.setOnPreferenceChangeListener(this);

            imageQualityNoMaskRegisterThresholdPreference = findPreference(getString(R.string.preference_image_quality_no_mask_register_threshold));
            imageQualityNoMaskRegisterThresholdPreference.setOnPreferenceChangeListener(this);

            imageQualityMaskRecognizeThresholdPreference = findPreference(getString(R.string.preference_image_quality_mask_recognize_threshold));
            imageQualityMaskRecognizeThresholdPreference.setOnPreferenceChangeListener(this);

            livenessTypePreference = findPreference(getString(R.string.preference_liveness_detect_type));
            livenessTypePreference.setOnPreferenceChangeListener(this);

            rgbLivenessThresholdPreference = findPreference(getString(R.string.preference_rgb_liveness_threshold));
            rgbLivenessThresholdPreference.setOnPreferenceChangeListener(this);


            irLivenessThresholdPreference = findPreference(getString(R.string.preference_ir_liveness_threshold));
            irLivenessThresholdPreference.setOnPreferenceChangeListener(this);

            livenessTypePreference.setEntries(livenessDetectTypeArray);
            livenessTypePreference.setEntryValues(livenessDetectTypeValueArray);

            String livenessType = ConfigUtil.getLivenessDetectType(getContext());
            livenessTypePreference.callChangeListener(livenessType);

            String recognizeThreshold = String.format("%.2f", ConfigUtil.getRecognizeThreshold(getContext()));
            similarThresholdPreference.setText(recognizeThreshold);

            String rgbLivenessThreshold = String.format("%.2f", ConfigUtil.getRgbLivenessThreshold(getContext()));
            rgbLivenessThresholdPreference.setText(rgbLivenessThreshold);

            String irLivenessThreshold = String.format("%.2f", ConfigUtil.getIrLivenessThreshold(getContext()));
            irLivenessThresholdPreference.setText(irLivenessThreshold);

            String imageQualityNoMaskRecognizeThreshold =
                    String.format("%.2f", ConfigUtil.getImageQualityNoMaskRecognizeThreshold(getContext()));
            imageQualityNoMaskRecognizeThresholdPreference.setText(imageQualityNoMaskRecognizeThreshold);

            String imageQualityNoMaskRegisterThreshold =
                    String.format("%.2f", ConfigUtil.getImageQualityNoMaskRegisterThreshold(getContext()));
            imageQualityNoMaskRegisterThresholdPreference.setText(imageQualityNoMaskRegisterThreshold);

            String imageQualityMaskRecognizeThreshold =
                    String.format("%.2f", ConfigUtil.getImageQualityMaskRecognizeThreshold(getContext()));
            imageQualityMaskRecognizeThresholdPreference.setText(imageQualityMaskRecognizeThreshold);

//            faceSizeLimitPreference.setText(String.valueOf(ConfigUtil.getFaceSizeLimit(getContext())));

//            faceMoveLimitPreference.setText(String.valueOf(ConfigUtil.getFaceMoveLimit(getContext())));


        }

        /**
         * 在配置项的值发生变更时，修改界面提示
         *
         * @param preference 配置项，如检测角度配置、最大人脸检测数量配置
         * @param newValue   配置项对应的值
         * @return 是否确认修改
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == livenessTypePreference) {
                notifyLivenessTypeChanged(preference, newValue.toString());
            }
            return true;
        }


        private void notifyLivenessTypeChanged(Preference preference, String livenessTypeKey) {
            if (livenessTypeKey.equals(getString(R.string.value_liveness_type_rgb))) {
                rgbLivenessThresholdPreference.setEnabled(true);
                irLivenessThresholdPreference.setEnabled(false);
            } else if (livenessTypeKey.equals(getString(R.string.value_liveness_type_ir))) {
                rgbLivenessThresholdPreference.setEnabled(false);
                irLivenessThresholdPreference.setEnabled(true);
            } else {
                rgbLivenessThresholdPreference.setEnabled(false);
                irLivenessThresholdPreference.setEnabled(false);
            }
        }
    }
}
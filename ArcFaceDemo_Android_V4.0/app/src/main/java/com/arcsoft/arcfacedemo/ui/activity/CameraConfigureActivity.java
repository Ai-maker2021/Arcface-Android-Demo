package com.arcsoft.arcfacedemo.ui.activity;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.databinding.ActivityCameraConfigureBinding;
import com.arcsoft.arcfacedemo.ui.model.PreviewConfig;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.arcfacedemo.util.FaceRectTransformer;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.util.camera.DualCameraHelper;
import com.arcsoft.arcfacedemo.util.camera.glsurface.CameraGLSurfaceView;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.IDualCameraFaceInfoTransformer;
import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.face.model.RecognizeConfiguration;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 适配原因：
 * 1. 人脸检测是基于预览数据的，Demo中使用的是相机回传的NV21，而绘制是基于View的，人脸检测回传的人脸框不能直接用于绘制，
 * 因此需要根据数据宽高、View宽高，显示旋转角度等信息进行转换；
 * <p>
 * 界面说明：
 * 1. 上方使用TextureView进行预览， 其画面和真实数据可能会有旋转、镜像、缩放的关系；
 * 2. 下方使用GLSurfaceView渲染相机回传的NV21（若有人脸框，则先在NV21上绘制人脸框），其画面和真实数据可能会有缩放的关系；
 * <p>
 * 适配操作说明：
 * 1. 在首页激活，并在上层界面选择合适的人脸检测角度；
 * 2. 调整双目偏移，使右下方红外预览画面中的人脸框能够最大范围地框定人脸；
 */

public class CameraConfigureActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, IDualCameraFaceInfoTransformer {
    private ActivityCameraConfigureBinding binding;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    private static final String TAG = "CameraConfigureActivity";

    private FaceRectTransformer rgbRectTransformer;
    private FaceRectTransformer irRectTransformer;

    List<FacePreviewInfo> facePreviewInfoList;

    private FaceHelper faceHelper;

    PreviewConfig previewConfig;

    private DualCameraHelper rgbCameraHelper;
    private DualCameraHelper irCameraHelper;

    private FaceEngine ftEngine;
    private Camera.Size previewSize;
    private Point specificPreviewSize;

    private int dualCameraOffsetHorizontal = 0;
    private int dualCameraOffsetVertical = 0;

    private boolean drawRgbRectHorizontalMirror = false;
    private boolean drawRgbRectVerticalMirror = false;
    private boolean drawIrRectHorizontalMirror = false;
    private boolean drawIrRectVerticalMirror = false;
    private boolean drawRgbPreviewHorizontalMirror = false;
    private boolean drawIrPreviewHorizontalMirror = false;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };
    private CameraPreferenceFragment cameraConfigPreferenceFragment;
    private DualCameraOffsetPreferenceFragment dualCameraOffsetPreferenceFragment;
    private PreviewPreferenceFragment previewPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_configure);
        initData();
        initView();
    }

    private void initData() {

        drawRgbRectHorizontalMirror = ConfigUtil.isDrawRgbRectHorizontalMirror(this);
        drawRgbRectVerticalMirror = ConfigUtil.isDrawRgbRectVerticalMirror(this);
        drawIrRectHorizontalMirror = ConfigUtil.isDrawIrRectHorizontalMirror(this);
        drawIrRectVerticalMirror = ConfigUtil.isDrawIrRectVerticalMirror(this);
        drawRgbPreviewHorizontalMirror = ConfigUtil.isDrawRgbPreviewHorizontalMirror(this);
        drawIrPreviewHorizontalMirror = ConfigUtil.isDrawIrPreviewHorizontalMirror(this);
        dualCameraOffsetHorizontal = ConfigUtil.getDualCameraHorizontalOffset(this);
        dualCameraOffsetVertical = ConfigUtil.getDualCameraVerticalOffset(this);

        boolean switchCamera = ConfigUtil.isSwitchCamera(this);
        previewConfig = new PreviewConfig(
                switchCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK,
                switchCamera ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT,
                Integer.parseInt(ConfigUtil.getRgbCameraAdditionalRotation(this)),
                Integer.parseInt(ConfigUtil.getIrCameraAdditionalRotation(this))
        );
        specificPreviewSize = loadPreviewSize(this);
    }

    private void initView() {
        //在布局结束后才做初始化操作
        if (!DualCameraHelper.hasDualCamera()) {
            binding.flIrPreview.setVisibility(View.GONE);
            binding.glSurfaceViewIr.setVisibility(View.GONE);
        }
        binding.textureViewIr.getViewTreeObserver().addOnGlobalLayoutListener(this);
        cameraConfigPreferenceFragment = new CameraPreferenceFragment();
        dualCameraOffsetPreferenceFragment = new DualCameraOffsetPreferenceFragment();
        previewPreferenceFragment = new PreviewPreferenceFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_camera_config_container, cameraConfigPreferenceFragment)
                .commit();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_dual_camera_offset_container, dualCameraOffsetPreferenceFragment)
                .commit();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_preview_container, previewPreferenceFragment)
                .commit();
    }


    private void initRgbCamera() {
        CameraListener cameraListener = new CameraListener() {
            List<FaceRectView.DrawInfo> rgbDrawInfoList = new ArrayList<>();
            int glSurfaceRectStrokeWidth = 2;

            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size lastPreviewSize = previewSize;
                Camera.Size previewSizeRgb = camera.getParameters().getPreviewSize();
                ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(
                        binding.textureViewRgb, binding.faceRectViewRgb, binding.glSurfaceViewRgb,
                        previewSizeRgb, displayOrientation);
                // GLSurfaceView以原画显示
                binding.glSurfaceViewRgb.init(false, 0, previewSizeRgb.width, previewSizeRgb.height);
                rgbRectTransformer = new FaceRectTransformer(previewSizeRgb.width, previewSizeRgb.height, layoutParams.width, layoutParams.height, displayOrientation,
                        cameraId, isMirror, drawRgbRectHorizontalMirror, drawRgbRectVerticalMirror);
                TextView textViewRgb = new TextView(CameraConfigureActivity.this, null);
                textViewRgb.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textViewRgb.setText(getString(R.string.camera_rgb_preview_size, previewSizeRgb.width, previewSizeRgb.height));
                textViewRgb.setTextColor(Color.WHITE);
                textViewRgb.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));

                FrameLayout container = ((FrameLayout) binding.textureViewRgb.getParent());
                int childCount = container.getChildCount();
                for (int i = childCount - 1; i >= 0; i--) {
                    View childView = container.getChildAt(i);
                    if (childView instanceof TextView) {
                        container.removeView(childView);
                    }
                }
                container.addView(textViewRgb);

                ViewGroup.LayoutParams parentViewLayoutParams = container.getLayoutParams();
                if (parentViewLayoutParams.height < layoutParams.height) {
                    parentViewLayoutParams.height = layoutParams.height;
                }
                container.setLayoutParams(parentViewLayoutParams);

                previewSize = previewSizeRgb;
                initFaceHelper(lastPreviewSize);
                if (faceHelper != null) {
                    faceHelper.setRgbFaceRectTransformer(rgbRectTransformer);
                    faceHelper.setIrFaceRectTransformer(irRectTransformer);
                }
                Log.i(TAG, "onCameraOpened: " + faceHelper);
                previewConfig.setRgbCameraId(cameraId);
                cameraConfigPreferenceFragment.notifyCurrentPreviewSize(previewSize);

                if (previewSizeRgb.width > binding.glSurfaceViewRgb.getLayoutParams().width) {
                    glSurfaceRectStrokeWidth = 2 * previewSizeRgb.width / binding.glSurfaceViewRgb.getLayoutParams().width;
                }
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                facePreviewInfoList = faceHelper.onPreviewFrame(nv21, null, false);
                rgbDrawInfoList.clear();
                if (!facePreviewInfoList.isEmpty()) {
                    Rect originalRect = facePreviewInfoList.get(0).getFaceInfoRgb().getRect();
                    // 将Rect绘制到NV21数据上并渲染，黄色
                    binding.glSurfaceViewRgb.renderNV21WithFaceRect(nv21, originalRect, glSurfaceRectStrokeWidth);

                    // 使用FaceRectView绘制已适配的人脸框，绿色
                    rgbDrawInfoList.add(new FaceRectView.DrawInfo(facePreviewInfoList.get(0).getRgbTransformedRect(), GenderInfo.UNKNOWN,
                            AgeInfo.UNKNOWN_AGE, LivenessInfo.UNKNOWN, Color.GREEN, ""));
                } else {
                    binding.glSurfaceViewRgb.renderNV21(nv21);
                }
                binding.faceRectViewRgb.drawRealtimeFaceInfo(rgbDrawInfoList);
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                e.printStackTrace();
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {

            }
        };

        rgbCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.textureViewRgb.getMeasuredWidth(), binding.textureViewRgb.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .additionalRotation(previewConfig.getRgbAdditionalDisplayOrientation())
                .specificCameraId(previewConfig.getRgbCameraId())
                .previewSize(specificPreviewSize)
                .isMirror(drawRgbPreviewHorizontalMirror)
                .previewOn(binding.textureViewRgb)
                .cameraListener(cameraListener)
                .build();
        rgbCameraHelper.init();
        rgbCameraHelper.start();
    }


    private void initIrCamera() {
        CameraListener irCameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size previewSizeIr = camera.getParameters().getPreviewSize();
                ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(
                        binding.textureViewIr, binding.faceRectViewIr, binding.glSurfaceViewIr,
                        previewSizeIr, displayOrientation);
                // GLSurfaceView以原画显示
                binding.glSurfaceViewIr.init(false, 0, previewSizeIr.width, previewSizeIr.height);
                irRectTransformer = new FaceRectTransformer(previewSizeIr.width, previewSizeIr.height, layoutParams.width, layoutParams.height, displayOrientation,
                        cameraId, isMirror, drawIrRectHorizontalMirror, drawIrRectVerticalMirror);
                TextView textViewIr = new TextView(CameraConfigureActivity.this, null);
                textViewIr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textViewIr.setText(getString(R.string.camera_ir_preview_size, previewSizeIr.width, previewSizeIr.height));
                textViewIr.setTextColor(Color.WHITE);
                textViewIr.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));

                FrameLayout container = ((FrameLayout) binding.textureViewIr.getParent());
                int childCount = container.getChildCount();
                for (int i = childCount - 1; i >= 0; i--) {
                    View childView = container.getChildAt(i);
                    if (childView instanceof TextView) {
                        container.removeView(childView);
                    }
                }
                container.addView(textViewIr);

                ViewGroup.LayoutParams parentViewLayoutParams = container.getLayoutParams();
                if (parentViewLayoutParams.height < layoutParams.height) {
                    parentViewLayoutParams.height = layoutParams.height;
                }
                container.setLayoutParams(parentViewLayoutParams);

                if (faceHelper != null) {
                    faceHelper.setRgbFaceRectTransformer(rgbRectTransformer);
                    faceHelper.setIrFaceRectTransformer(irRectTransformer);
                }
                previewConfig.setIrCameraId(cameraId);


                if (previewSizeIr.width > binding.glSurfaceViewIr.getLayoutParams().width) {
                    glSurfaceRectStrokeWidth = 2 * previewSizeIr.width / binding.glSurfaceViewIr.getLayoutParams().width;
                }
            }

            List<FaceRectView.DrawInfo> irDrawInfoList = new ArrayList<>();
            int glSurfaceRectStrokeWidth = 2;

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                irDrawInfoList.clear();
                if (facePreviewInfoList != null && !facePreviewInfoList.isEmpty()) {
                    Rect originalRect = facePreviewInfoList.get(0).getFaceInfoIr().getRect();

                    // 将Rect绘制到NV21数据上并渲染
                    binding.glSurfaceViewIr.renderNV21WithFaceRect(nv21, originalRect, glSurfaceRectStrokeWidth);
                    // 使用FaceRectView绘制已适配的人脸框
                    irDrawInfoList.add(new FaceRectView.DrawInfo(facePreviewInfoList.get(0).getIrTransformedRect(), GenderInfo.UNKNOWN,
                            AgeInfo.UNKNOWN_AGE, LivenessInfo.UNKNOWN, Color.YELLOW, ""));
                } else {
                    binding.glSurfaceViewIr.renderNV21(nv21);
                }
                binding.faceRectViewIr.drawRealtimeFaceInfo(irDrawInfoList);
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                e.printStackTrace();
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {

            }
        };

        irCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.textureViewIr.getMeasuredWidth(), binding.textureViewIr.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .additionalRotation(previewConfig.getIrAdditionalDisplayOrientation())
                .specificCameraId(previewConfig.getIrCameraId())
                .previewOn(binding.textureViewIr)
                .cameraListener(irCameraListener)
                .isMirror(drawIrPreviewHorizontalMirror)
                .previewSize(specificPreviewSize)
                .build();
        irCameraHelper.init();
        try {
            irCameraHelper.start();
        } catch (RuntimeException e) {
            irCameraHelper.release();
            irCameraHelper = null;
            showToast(e.getMessage() + getString(R.string.camera_error_notice));
            hideIrView();
        }
    }

    private void hideIrView() {
        binding.flIrPreview.setVisibility(View.GONE);
        binding.glSurfaceViewIr.setVisibility(View.GONE);
    }


    /**
     * 调整View的宽高，使2个预览同时显示
     *
     * @param previewView        显示预览数据的view
     * @param faceRectView       画框的view
     * @param previewSize        预览大小
     * @param displayOrientation 相机旋转角度
     * @return 调整后的LayoutParams
     */
    private ViewGroup.LayoutParams adjustPreviewViewSize(View previewView, FaceRectView faceRectView, CameraGLSurfaceView glSurfaceView, Camera.Size previewSize, int displayOrientation) {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int halfScreenWidth = metrics.widthPixels / 4;
        // TextureView
        float ratio = ((float) previewSize.height) / (float) previewSize.width;
        if (ratio > 1) {
            ratio = 1 / ratio;
        }
        ViewGroup.LayoutParams previewViewLayoutParams = previewView.getLayoutParams();
        if (displayOrientation % 180 == 0) {
            previewViewLayoutParams.width = halfScreenWidth;
            previewViewLayoutParams.height = (int) (halfScreenWidth * ratio);
        } else {
            previewViewLayoutParams.width = halfScreenWidth;
            previewViewLayoutParams.height = (int) (halfScreenWidth / ratio);
        }
        Log.i(TAG, "adjustPreviewViewSize: " + previewViewLayoutParams.width + "x" + previewViewLayoutParams.height);
        previewView.setLayoutParams(previewViewLayoutParams);
        faceRectView.setLayoutParams(previewViewLayoutParams);
        // GLSurfaceView
        ViewGroup.LayoutParams glSurfaceLayoutParams = glSurfaceView.getLayoutParams();
        glSurfaceLayoutParams.width = halfScreenWidth;
        glSurfaceLayoutParams.height = halfScreenWidth * previewSize.height / previewSize.width;
        Log.i(TAG, "glSurfaceLayoutParams: " + glSurfaceLayoutParams.width + "x" + glSurfaceLayoutParams.height);

        glSurfaceView.setLayoutParams(glSurfaceLayoutParams);

        return previewViewLayoutParams;
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        super.afterRequestPermission(requestCode, isAllGranted);
        if (isAllGranted) {
            initEngine();
            initRgbCamera();
            if (DualCameraHelper.hasDualCamera()) {
                initIrCamera();
            }
            // 取双目公共分辨率，使用rgbCameraHelper和irCameraHelper都没事
            if (rgbCameraHelper != null || irCameraHelper != null) {
                DualCameraHelper dualCameraHelper = rgbCameraHelper == null ? irCameraHelper : rgbCameraHelper;
                cameraConfigPreferenceFragment.notifyCommonPreviewSize(dualCameraHelper.getCommonSupportedPreviewSize());
                cameraConfigPreferenceFragment.notifyCurrentPreviewSize(previewSize);
            }
        } else {
            showToast(getString(R.string.permission_denied));
        }
    }

    @Override
    public void onGlobalLayout() {
        binding.textureViewIr.getViewTreeObserver().removeOnGlobalLayoutListener(this);

        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initRgbCamera();
            if (DualCameraHelper.hasDualCamera()) {
                initIrCamera();
            }
        }
        // 取双目公共分辨率，使用rgbCameraHelper和irCameraHelper都没事
        if (rgbCameraHelper != null || irCameraHelper != null) {
            DualCameraHelper dualCameraHelper = rgbCameraHelper == null ? irCameraHelper : rgbCameraHelper;
            cameraConfigPreferenceFragment.notifyCommonPreviewSize(dualCameraHelper.getCommonSupportedPreviewSize());
            cameraConfigPreferenceFragment.notifyCurrentPreviewSize(previewSize);
        }

    }

    private void initEngine() {
        ftEngine = new FaceEngine();
        int initCode = ftEngine.init(this,
                DetectMode.ASF_DETECT_MODE_VIDEO,
                ConfigUtil.getFtOrient(this),
                1,
                FaceEngine.ASF_FACE_DETECT);

        if (initCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "ftEngine", initCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(initCode));
            Log.i(TAG, "initEngine: " + error);
            showToast(error);
        }
    }

    @Override
    protected void onDestroy() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.release();
        }
        if (irCameraHelper != null) {
            irCameraHelper.release();
        }
        if (ftEngine != null) {
            ftEngine.unInit();
        }
        super.onDestroy();
    }

    /**
     * 切换相机。注意：若切换相机发现检测不到人脸，则极有可能是检测角度导致的，需要销毁引擎重新创建或者在设置界面修改配置的检测角度
     *
     * @param needChangeDefault 是否需要切换默认的cameraId。 true：1 - 可见光，0 - 红外；false: 1 - 红外，0 - 可见光
     */
    public void switchCamera(boolean needChangeDefault) {
        try {
            boolean isCurrentDefault = false;

            if (rgbCameraHelper != null) {
                isCurrentDefault = rgbCameraHelper.getCurrentOpenedCameraId() == PreviewConfig.DEFAULT_RGB_CAMERA_ID;
            }
            if (irCameraHelper != null) {
                isCurrentDefault = irCameraHelper.getCurrentOpenedCameraId() == PreviewConfig.DEFAULT_IR_CAMERA_ID;
            }
            if (isCurrentDefault != needChangeDefault) {
                return;
            }

            if (rgbCameraHelper != null) {
                rgbCameraHelper.stop();
            }
            if (irCameraHelper != null) {
                irCameraHelper.stop();
            }

            boolean hasSwitched = false;
            if (rgbCameraHelper != null) {
                rgbCameraHelper.switchCameraId();
                rgbCameraHelper.start();
                hasSwitched = true;
            }
            if (irCameraHelper != null) {
                irCameraHelper.switchCameraId();
                irCameraHelper.start();
                hasSwitched = true;
            }
            if (hasSwitched) {
                showLongToast(getString(R.string.notice_change_detect_degree));
            } else {
                showToast(getString(R.string.switch_camera_failed));
            }
        } catch (RuntimeException e) {
            showToast(e.getMessage() + getString(R.string.camera_error_notice));
        }
    }

    private void initFaceHelper(Camera.Size lastPreviewSize) {
        if (faceHelper == null ||
                lastPreviewSize == null ||
                lastPreviewSize.width != previewSize.width || lastPreviewSize.height != previewSize.height) {
            Integer trackedFaceCount = null;
            // 记录切换时的人脸序号
            if (faceHelper != null) {
                trackedFaceCount = faceHelper.getTrackedFaceCount();
                faceHelper.release();
            }
            Context context = ArcFaceApplication.getApplication().getApplicationContext();

            faceHelper = new FaceHelper.Builder()
                    .ftEngine(ftEngine)
                    .previewSize(previewSize)
                    .onlyDetectLiveness(true)
                    .recognizeConfiguration(new RecognizeConfiguration.Builder().build())
                    .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(context) : trackedFaceCount)
                    .dualCameraFaceInfoTransformer(this)
                    .build();
        }
    }

    public void drawIrRectHorizontalMirror(boolean mirror) {
        drawIrRectHorizontalMirror = mirror;
        if (irRectTransformer != null) {
            irRectTransformer.setMirrorHorizontal(mirror);
        }
    }

    public void drawIrRectVerticalMirror(boolean mirror) {
        drawIrRectVerticalMirror = mirror;
        if (irRectTransformer != null) {
            irRectTransformer.setMirrorVertical(mirror);
        }
    }

    public void drawRgbRectHorizontalMirror(boolean mirror) {
        drawRgbRectHorizontalMirror = mirror;
        if (rgbRectTransformer != null) {
            rgbRectTransformer.setMirrorHorizontal(mirror);
        }
    }

    public void drawRgbRectVerticalMirror(boolean mirror) {
        drawRgbRectVerticalMirror = mirror;
        if (rgbRectTransformer != null) {
            rgbRectTransformer.setMirrorVertical(mirror);
        }
    }

    public void setDualCameraHorizontalOffset(int offset) {
        dualCameraOffsetHorizontal = offset;
    }

    public void setDualCameraVerticalOffset(int offset) {
        dualCameraOffsetVertical = offset;
    }

    @Override
    public FaceInfo transformFaceInfo(FaceInfo faceInfo) {
        FaceInfo cloneFaceInfo = new FaceInfo(faceInfo);
        cloneFaceInfo.getRect().offset(dualCameraOffsetHorizontal, dualCameraOffsetVertical);
        return cloneFaceInfo;
    }

    private void setMirrorPreviewRgb(boolean mirror) {
        binding.textureViewRgb.setScaleX(mirror ? -1 : 1);
        if (rgbRectTransformer != null) {
            rgbRectTransformer.setMirror(mirror);
        }
    }

    private void setMirrorPreviewIr(boolean mirror) {
        binding.textureViewIr.setScaleX(mirror ? -1 : 1);
        if (irRectTransformer != null) {
            irRectTransformer.setMirror(mirror);
        }
    }

    private Point loadPreviewSize(Context context) {
        String[] size = ConfigUtil.getPreviewSize(context).split("x");
        return new Point(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
    }

    private void reopenCameraByPreviewSize(String previewSize) {
        String[] size = previewSize.split("x");
        this.specificPreviewSize = new Point(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        if (this.previewSize.width == specificPreviewSize.x && this.previewSize.height == specificPreviewSize.y) {
            return;
        }
        try {
            if (rgbCameraHelper != null) {
                rgbCameraHelper.stop();
            }
            if (irCameraHelper != null) {
                irCameraHelper.stop();
            }
            if (rgbCameraHelper != null) {
                rgbCameraHelper.setSpecificPreviewSize(specificPreviewSize);
                rgbCameraHelper.start();
            }
            if (irCameraHelper != null) {
                irCameraHelper.setSpecificPreviewSize(specificPreviewSize);
                irCameraHelper.start();
            }
        } catch (RuntimeException e) {
            showToast(e.getMessage() + getString(R.string.camera_error_notice));
        }
    }

    private void rotateRgbCamera(int rotation) {
        previewConfig.setRgbAdditionalDisplayOrientation(rotation);
        rotateCameraPreview(rotation, binding.textureViewRgb, binding.faceRectViewRgb, rgbRectTransformer, rgbCameraHelper, previewSize);
    }

    private void rotateIrCamera(int rotation) {
        previewConfig.setIrAdditionalDisplayOrientation(rotation);
        rotateCameraPreview(rotation, binding.textureViewIr, binding.faceRectViewIr, irRectTransformer, irCameraHelper, previewSize);
    }

    private void rotateCameraPreview(int rotation, View previewView, FaceRectView faceRectView, FaceRectTransformer faceRectTransformer, DualCameraHelper cameraHelper, Camera.Size previewSize) {
        if (cameraHelper != null) {
            int displayOrientation = cameraHelper.rotateAdditional(rotation);
            float ratio = ((float) previewSize.height) / (float) previewSize.width;
            if (ratio > 1) {
                ratio = 1 / ratio;
            }
            ViewGroup.LayoutParams previewViewLayoutParams = previewView.getLayoutParams();
            previewViewLayoutParams.width = previewView.getMeasuredWidth();
            previewViewLayoutParams.height = previewView.getMeasuredHeight();
            if (displayOrientation % 180 == 0) {
                previewViewLayoutParams.height = (int) (previewViewLayoutParams.width * ratio);
            } else {
                previewViewLayoutParams.height = (int) (previewViewLayoutParams.width / ratio);
            }
            previewView.setLayoutParams(previewViewLayoutParams);
            faceRectView.setLayoutParams(previewViewLayoutParams);

            ViewGroup parentView = (ViewGroup) previewView.getParent();
            ViewGroup.LayoutParams parentLayoutParams = parentView.getLayoutParams();

            if (parentLayoutParams.height < previewViewLayoutParams.height) {
                parentLayoutParams.height = previewViewLayoutParams.height;
                parentView.setLayoutParams(parentLayoutParams);
            }

            faceRectTransformer.setCameraDisplayOrientation(displayOrientation);
            faceRectTransformer.setCanvasWidth(previewViewLayoutParams.width);
            faceRectTransformer.setCanvasHeight(previewViewLayoutParams.height);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeCamera();
    }

    private void resumeCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.start();
            binding.glSurfaceViewRgb.onResume();
        }
        if (irCameraHelper != null) {
            irCameraHelper.start();
            binding.glSurfaceViewIr.onResume();
        }
    }

    @Override
    protected void onPause() {
        pauseCamera();
        super.onPause();
    }

    private void pauseCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.stop();
            binding.glSurfaceViewRgb.onPause();
        }
        if (irCameraHelper != null) {
            irCameraHelper.stop();
            binding.glSurfaceViewIr.onPause();
        }
    }


    public static class CameraPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

        private ListPreference dualCameraPreviewSizePreference;

        private String rgbPreviewRotation;//keep
        private String irPreviewRotation;//keep
        private String switchCamera; //keep
        private String dualCameraPreviewSize;//keep

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            CameraConfigureActivity cameraConfigureActivity = (CameraConfigureActivity) getActivity();
            if (cameraConfigureActivity == null) {
                return false;
            }
            if (preference instanceof SwitchPreferenceCompat) {
                if (key.equals(switchCamera)) {
                    cameraConfigureActivity.switchCamera((Boolean) newValue);
                }
            }
            if (preference instanceof ListPreference) {
                if (key.equals(rgbPreviewRotation)) {
                    cameraConfigureActivity.rotateRgbCamera(Integer.parseInt((String) newValue));
                } else if (key.equals(irPreviewRotation)) {
                    cameraConfigureActivity.rotateIrCamera(Integer.parseInt((String) newValue));
                } else if (key.equals(dualCameraPreviewSize)) {
                    cameraConfigureActivity.reopenCameraByPreviewSize((String) newValue);
                }
            }
            return true;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_camera, rootKey);

            switchCamera = getString(R.string.preference_switch_camera);
            rgbPreviewRotation = getString(R.string.preference_rgb_camera_rotation);
            irPreviewRotation = getString(R.string.preference_ir_camera_rotation);
            dualCameraPreviewSize = getString(R.string.preference_dual_camera_preview_size);

            findPreference(rgbPreviewRotation).setOnPreferenceChangeListener(this::onPreferenceChange);
            findPreference(irPreviewRotation).setOnPreferenceChangeListener(this::onPreferenceChange);
            findPreference(switchCamera).setOnPreferenceChangeListener(this::onPreferenceChange);
            if (!DualCameraHelper.hasDualCamera()) {
                findPreference(irPreviewRotation).setEnabled(false);
                findPreference(switchCamera).setEnabled(false);
            }

            dualCameraPreviewSizePreference = findPreference(dualCameraPreviewSize);
            dualCameraPreviewSizePreference.setOnPreferenceChangeListener(this::onPreferenceChange);

        }

        public void notifyCommonPreviewSize(List<Camera.Size> commonSupportedPreviewSize) {
            CharSequence[] charSequences = new CharSequence[commonSupportedPreviewSize.size()];
            for (int i = 0; i < commonSupportedPreviewSize.size(); i++) {
                Camera.Size size = commonSupportedPreviewSize.get(i);
                charSequences[i] = size.width + "x" + size.height;
            }
            dualCameraPreviewSizePreference.setEntries(charSequences);
            dualCameraPreviewSizePreference.setEntryValues(charSequences);
        }

        public void notifyCurrentPreviewSize(Camera.Size previewSize) {
            String previewSizeStr = previewSize.width + "x" + previewSize.height;
            dualCameraPreviewSizePreference.setValue(previewSizeStr);
            dualCameraPreviewSizePreference.setSummary(previewSizeStr);
        }
    }

    public static class DualCameraOffsetPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

        private String horizontalOffset;
        private String verticalOffset;

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            CameraConfigureActivity cameraConfigureActivity = (CameraConfigureActivity) getActivity();
            if (cameraConfigureActivity == null) {
                return false;
            }

            if (preference instanceof SeekBarPreference) {
                if (key.equals(horizontalOffset)) {
                    cameraConfigureActivity.setDualCameraHorizontalOffset((Integer) newValue);
                } else if (key.equals(verticalOffset)) {
                    cameraConfigureActivity.setDualCameraVerticalOffset((Integer) newValue);
                }
            }
            return true;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_dual_camera_offset, rootKey);

            horizontalOffset = getString(R.string.preference_dual_camera_offset_horizontal);
            verticalOffset = getString(R.string.preference_dual_camera_offset_vertical);

            SeekBarPreference horizontalOffsetPreference = findPreference(horizontalOffset);
            SeekBarPreference verticalOffsetPreference = findPreference(verticalOffset);
            if (!DualCameraHelper.hasDualCamera()) {
                horizontalOffsetPreference.setEnabled(false);
                verticalOffsetPreference.setEnabled(false);
            }
            horizontalOffsetPreference.setOnPreferenceChangeListener(this);
            verticalOffsetPreference.setOnPreferenceChangeListener(this);
        }
    }

    public static class PreviewPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        private String mirrorRgbRectHorizontal;
        private String mirrorRgbRectVertical;
        private String mirrorIrRectHorizontal;
        private String mirrorIrRectVertical;
        private String mirrorPreviewRgb;
        private String mirrorPreviewIr;

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            CameraConfigureActivity cameraConfigureActivity = (CameraConfigureActivity) getActivity();
            if (cameraConfigureActivity == null) {
                return false;
            }
            if (preference instanceof SwitchPreferenceCompat) {
                if (key.equals(mirrorRgbRectHorizontal)) {
                    cameraConfigureActivity.drawRgbRectHorizontalMirror((Boolean) newValue);
                } else if (key.equals(mirrorRgbRectVertical)) {
                    cameraConfigureActivity.drawRgbRectVerticalMirror((Boolean) newValue);
                } else if (key.equals(mirrorIrRectHorizontal)) {
                    cameraConfigureActivity.drawIrRectHorizontalMirror((Boolean) newValue);
                } else if (key.equals(mirrorIrRectVertical)) {
                    cameraConfigureActivity.drawIrRectVerticalMirror((Boolean) newValue);
                } else if (key.equals(mirrorPreviewRgb)) {
                    cameraConfigureActivity.setMirrorPreviewRgb((Boolean) newValue);
                } else if (key.equals(mirrorPreviewIr)) {
                    cameraConfigureActivity.setMirrorPreviewIr((Boolean) newValue);
                }
            }
            return true;
        }


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_preview, rootKey);

            mirrorPreviewRgb = getString(R.string.preference_rgb_preview_horizontal_mirror);
            mirrorPreviewIr = getString(R.string.preference_ir_preview_horizontal_mirror);
            mirrorRgbRectHorizontal = getString(R.string.preference_draw_rgb_rect_horizontal_mirror);
            mirrorRgbRectVertical = getString(R.string.preference_draw_rgb_rect_vertical_mirror);
            mirrorIrRectHorizontal = getString(R.string.preference_draw_ir_rect_horizontal_mirror);
            mirrorIrRectVertical = getString(R.string.preference_draw_ir_rect_vertical_mirror);


            findPreference(mirrorPreviewRgb).setOnPreferenceChangeListener(this::onPreferenceChange);
            findPreference(mirrorPreviewIr).setOnPreferenceChangeListener(this::onPreferenceChange);
            findPreference(mirrorRgbRectHorizontal).setOnPreferenceChangeListener(this::onPreferenceChange);
            findPreference(mirrorRgbRectVertical).setOnPreferenceChangeListener(this::onPreferenceChange);
            findPreference(mirrorIrRectHorizontal).setOnPreferenceChangeListener(this::onPreferenceChange);
            findPreference(mirrorIrRectVertical).setOnPreferenceChangeListener(this::onPreferenceChange);

            if (!DualCameraHelper.hasDualCamera()) {
                findPreference(mirrorPreviewIr).setEnabled(false);
                findPreference(mirrorIrRectHorizontal).setEnabled(false);
                findPreference(mirrorIrRectVertical).setEnabled(false);
            }
        }
    }
}

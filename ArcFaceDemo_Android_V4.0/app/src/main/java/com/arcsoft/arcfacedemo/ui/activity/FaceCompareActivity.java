package com.arcsoft.arcfacedemo.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.ui.adapter.MultiFaceInfoAdapter;
import com.arcsoft.arcfacedemo.ui.model.ItemShowInfo;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.ImageQualitySimilar;
import com.arcsoft.face.MaskInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.ExtractType;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FaceCompareActivity extends BaseActivity {

    private static final String TAG = "MultiImageActivity";

    private static final int ACTION_CHOOSE_MAIN_IMAGE = 0x201;
    private static final int ACTION_ADD_RECYCLER_ITEM_IMAGE = 0x202;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final int INIT_MASK = FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_GENDER |
            FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_MASK_DETECT | FaceEngine.ASF_IMAGEQUALITY;

    private ImageView ivMainImage;
    private TextView tvMainImageInfo;
    /**
     * 选择图片时的类型
     */
    private static final int TYPE_MAIN = 0;
    private static final int TYPE_ITEM = 1;

    /**
     * 主图的第0张人脸的特征数据
     */
    private FaceFeature mainFeature;
    private MultiFaceInfoAdapter multiFaceInfoAdapter;
    private List<ItemShowInfo> showInfoList;

    private FaceEngine mainFaceEngine;
    private int faceEngineCode = -1;
    private Bitmap mainBitmap;

    private String[] neededPermissions = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_multi_image);
        /*
         * 在选择图片的时候，在android 7.0及以上通过FileProvider获取Uri，不需要文件权限
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            List<String> permissionList = new ArrayList<>(Arrays.asList(neededPermissions));
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            neededPermissions = permissionList.toArray(new String[0]);
        }

        if (!checkPermissions(neededPermissions)) {
            ActivityCompat.requestPermissions(this, neededPermissions, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
        }
        initView();
    }

    private void initView() {
        ivMainImage = findViewById(R.id.iv_main_image);
        tvMainImageInfo = findViewById(R.id.tv_main_image_info);
        RecyclerView recyclerFaces = findViewById(R.id.recycler_faces);
        showInfoList = new ArrayList<>();
        multiFaceInfoAdapter = new MultiFaceInfoAdapter(showInfoList, this);
        recyclerFaces.setAdapter(multiFaceInfoAdapter);
        recyclerFaces.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerFaces.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initEngine() {
        mainFaceEngine = new FaceEngine();
        faceEngineCode = mainFaceEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                6, INIT_MASK);
        if (faceEngineCode != ErrorInfo.MOK) {
            showToast(getString(R.string.init_failed, faceEngineCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(faceEngineCode)));
        }
    }

    private void unInitEngine() {
        if (mainFaceEngine != null) {
            faceEngineCode = mainFaceEngine.unInit();
            Log.i(TAG, "unInitEngine: " + faceEngineCode);
        }
    }

    @Override
    protected void onDestroy() {
        unInitEngine();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || data.getData() == null) {
            showToast(getString(R.string.get_picture_failed));
            return;
        }
        if (requestCode == ACTION_CHOOSE_MAIN_IMAGE) {
            try {
                mainBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
                showToast(getString(R.string.get_picture_failed));
                return;
            }
            if (mainBitmap == null) {
                showToast(getString(R.string.get_picture_failed));
                return;
            }
            processImage(mainBitmap, TYPE_MAIN);
        } else if (requestCode == ACTION_ADD_RECYCLER_ITEM_IMAGE) {
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
                showToast(getString(R.string.get_picture_failed));
                return;
            }
            if (bitmap == null) {
                showToast(getString(R.string.get_picture_failed));
                return;
            }
            if (mainFeature == null) {
                return;
            }
            processImage(bitmap, TYPE_ITEM);
        }
    }

    public void processImage(Bitmap bitmap, int type) {
        if (bitmap == null) {
            return;
        }
        if (mainFaceEngine == null) {
            return;
        }
        // 接口需要的bgr24宽度必须为4的倍数
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // bitmap转bgr24
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            showToast("failed to transform bitmap to imageData, code is " + transformCode);
            return;
        }
        List<FaceInfo> faceInfoList = new ArrayList<>();
        //人脸检测
        int detectCode = mainFaceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
        if (detectCode != 0 || faceInfoList.isEmpty()) {
            showToast("face detection finished, code is " + detectCode + ", face num is " + faceInfoList.size());
            return;
        }
        //绘制bitmap
        bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10);
        paint.setColor(Color.YELLOW);

        if (!faceInfoList.isEmpty()) {
            for (int i = 0; i < faceInfoList.size(); i++) {
                //绘制人脸框
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(faceInfoList.get(i).getRect(), paint);
                //绘制人脸序号
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setTextSize((float) faceInfoList.get(i).getRect().width() / 2);
                canvas.drawText("" + i, faceInfoList.get(i).getRect().left, faceInfoList.get(i).getRect().top, paint);
            }
        }
        int faceProcessCode = mainFaceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList,
                FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_MASK_DETECT);
        if (faceProcessCode != ErrorInfo.MOK) {
            showToast("face process finished, code is " + faceProcessCode);
            return;
        }
        //年龄信息结果
        List<AgeInfo> ageInfoList = new ArrayList<>();
        //性别信息结果
        List<GenderInfo> genderInfoList = new ArrayList<>();
        //三维角度结果
        List<Face3DAngle> face3dAngleList = new ArrayList<>();
        //口罩信息结果
        List<MaskInfo> maskInfoList = new ArrayList<>();
        //获取年龄、性别、三维角度
        int ageCode = mainFaceEngine.getAge(ageInfoList);
        int genderCode = mainFaceEngine.getGender(genderInfoList);
        int face3dAngleCode = mainFaceEngine.getFace3DAngle(face3dAngleList);
        int maskInfoCode = mainFaceEngine.getMask(maskInfoList);
        if ((ageCode | genderCode | face3dAngleCode | maskInfoCode) != ErrorInfo.MOK) {
            showToast("at lease one of age、gender、face3DAngle、mask detect failed! codes are: " + ageCode
                    + " ," + genderCode + " ," + face3dAngleCode + " ," + maskInfoCode);
            return;
        }

        int isMask = MaskInfo.UNKNOWN;
        if (!maskInfoList.isEmpty()) {
            isMask = maskInfoList.get(0).getMask();
        }
        /*
         * 过滤掉mask值为MaskInfo.UNKNOWN的图片
         */
        if (isMask == MaskInfo.UNKNOWN) {
            showToast("mask is unknown");
            return;
        }

        if (type == TYPE_MAIN && isMask == MaskInfo.WORN) {
            /*
             * 当type == Type_MAIN时，为人员注册场景，不能使用戴口罩的照片进行注册，否则会出现人脸比对相似度异常的问题
             */
            showToast(getResources().getString(R.string.notice_register_image_no_mask));
            return;
        }

        ImageQualitySimilar imageQualitySimilar = new ImageQualitySimilar();
        int qualityCode = mainFaceEngine.imageQualityDetect(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0),
                isMask, imageQualitySimilar);
        if (qualityCode != ErrorInfo.MOK) {
            showToast("imageQualityDetect failed! code is " + qualityCode);
            return;
        }
        float quality = imageQualitySimilar.getScore();
        float destQuality;
        if (type == TYPE_MAIN) {
            /*
             * 当type == Type_MAIN时，为人员注册场景
             */
            destQuality = ConfigUtil.getImageQualityNoMaskRegisterThreshold(this);
        } else {
            /*
             * 当type == TYPE_ITEM时，为人员识别场景
             */
            if (isMask == MaskInfo.WORN) {
                /*
                 * 戴口罩时的场景
                 */
                destQuality = ConfigUtil.getImageQualityMaskRecognizeThreshold(this);
            } else {
                /*
                 * 不戴口罩时的场景
                 */
                destQuality = ConfigUtil.getImageQualityNoMaskRecognizeThreshold(this);
            }
        }
        if (quality < destQuality) {
            showToast("image quality invalid");
            return;
        }

        //人脸比对数据显示
        if (!faceInfoList.isEmpty()) {
            if (type == TYPE_MAIN) {
                int size = showInfoList.size();
                showInfoList.clear();
                multiFaceInfoAdapter.notifyItemRangeRemoved(0, size);
                mainFeature = new FaceFeature();
                int res = mainFaceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0),
                        ExtractType.REGISTER, isMask, mainFeature);
                if (res != ErrorInfo.MOK) {
                    mainFeature = null;
                }
                Glide.with(ivMainImage.getContext())
                        .load(bitmap)
                        .into(ivMainImage);
                StringBuilder stringBuilder = new StringBuilder();
                if (!faceInfoList.isEmpty()) {
                    stringBuilder.append("face info:\n\n");
                }
                for (int i = 0; i < faceInfoList.size(); i++) {
                    stringBuilder.append("face[")
                            .append(i)
                            .append("]:\n")
                            .append(faceInfoList.get(i))
                            .append("\nage:")
                            .append(ageInfoList.get(i).getAge())
                            .append("\ngender:")
                            .append(genderInfoList.get(i).getGender() == GenderInfo.MALE ? "MALE"
                                    : (genderInfoList.get(i).getGender() == GenderInfo.FEMALE ? "FEMALE" : "UNKNOWN"))
                            .append("\nface3DAngle:")
                            .append(face3dAngleList.get(i))
                            .append("\nmaskInfo:")
                            .append(maskInfoList.get(i).getMask() == MaskInfo.WORN ? "戴口罩"
                                    : (maskInfoList.get(i).getMask() == MaskInfo.NOT_WORN ? "未戴口罩" : "UNKNOWN"))
                            .append("\n\n");
                }
                tvMainImageInfo.setText(stringBuilder);
            } else if (type == TYPE_ITEM) {
                FaceFeature faceFeature = new FaceFeature();
                int res = mainFaceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0),
                        ExtractType.RECOGNIZE, isMask, faceFeature);
                if (res == 0) {
                    FaceSimilar faceSimilar = new FaceSimilar();
                    int compareResult = mainFaceEngine.compareFaceFeature(mainFeature, faceFeature, faceSimilar);
                    if (compareResult == ErrorInfo.MOK) {
                        ItemShowInfo showInfo = new ItemShowInfo(bitmap, ageInfoList.get(0).getAge(), genderInfoList.get(0).getGender(), faceSimilar.getScore());
                        showInfoList.add(showInfo);
                        multiFaceInfoAdapter.notifyItemInserted(showInfoList.size() - 1);
                    } else {
                        showToast(getString(R.string.compare_failed, compareResult));
                    }
                }
            }
        } else {
            if (type == TYPE_MAIN) {
                mainBitmap = null;
            }
        }
    }

    /**
     * 从本地选择文件
     *
     * @param action 可为选择主图{@link #ACTION_CHOOSE_MAIN_IMAGE}或者选择item图{@link #ACTION_ADD_RECYCLER_ITEM_IMAGE}
     */
    public void chooseLocalImage(int action) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, action);
    }

    public void addItemFace(View view) {
        if (faceEngineCode != ErrorInfo.MOK) {
            showToast(getString(R.string.engine_not_initialized, faceEngineCode));
            return;
        }
        if (mainBitmap == null) {
            showToast(getString(R.string.notice_choose_main_img));
            return;
        }
        chooseLocalImage(ACTION_ADD_RECYCLER_ITEM_IMAGE);
    }

    public void chooseMainImage(View view) {

        if (faceEngineCode != ErrorInfo.MOK) {
            showToast(getString(R.string.engine_not_initialized, faceEngineCode));
            return;
        }
        chooseLocalImage(ACTION_CHOOSE_MAIN_IMAGE);
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                initEngine();
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }
}

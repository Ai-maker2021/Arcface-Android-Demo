package com.arcsoft.arcfacedemo.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.ParcelableSpan;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.arcfacedemo.util.ImageUtil;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.MaskInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectModel;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 图片人脸属性检测界面
 */
public class ImageFaceAttrDetectActivity extends BaseActivity {
    private static final String TAG = "ImageFaceAttrDetect";
    private ImageView ivShow;
    private TextView tvNotice;
    private FaceEngine faceEngine;
    private int faceEngineCode = -1;

    int processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS | FaceEngine.ASF_MASK_DETECT | FaceEngine.ASF_FACE_SHELTER;
    /**
     * 提示对话框
     */
    private AlertDialog progressDialog;
    /**
     * 被处理的图片
     */
    private Bitmap mBitmap = null;

    /**
     * 所需的所有权限信息
     */
    private static String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_face_attr_detect);
        initView();
        /**
         * 在选择图片的时候，在android 7.0及以上通过FileProvider获取Uri，不需要文件权限
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            List<String> permissionList = new ArrayList<>(Arrays.asList(NEEDED_PERMISSIONS));
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            NEEDED_PERMISSIONS = permissionList.toArray(new String[0]);
        }

        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
        }

    }

    private void initEngine() {
        faceEngine = new FaceEngine();
        faceEngineCode = faceEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                5, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | processMask);
        Log.i(TAG, "initEngine: init: " + faceEngineCode);

        if (faceEngineCode != ErrorInfo.MOK) {
            showToast(getString(R.string.init_failed, faceEngineCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(faceEngineCode)));
        }
    }

    /**
     * 销毁引擎
     */
    private void unInitEngine() {
        if (faceEngine != null) {
            faceEngineCode = faceEngine.unInit();
            faceEngine = null;
            Log.i(TAG, "unInitEngine: " + faceEngineCode);
        }
    }

    @Override
    protected void onDestroy() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
        mBitmap = null;

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;

        unInitEngine();
        super.onDestroy();
    }

    private void initView() {
        tvNotice = findViewById(R.id.tv_notice);
        ivShow = findViewById(R.id.iv_show);
        ivShow.setImageResource(R.drawable.faces);
        progressDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.processing)
                .setView(new ProgressBar(this))
                .create();
    }

    /**
     * 按钮点击响应事件
     *
     * @param view
     */
    public void process(final View view) {

        view.setClickable(false);
        if (progressDialog == null || progressDialog.isShowing()) {
            return;
        }
        progressDialog.show();
        //图像转化操作和部分引擎调用比较耗时，建议放子线程操作
        Observable.create(emitter -> {
            processImage();
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        view.setClickable(true);
                    }
                });
    }


    /**
     * 主要操作逻辑部分
     */
    public void processImage() {
        /*
         * 1.准备操作（校验，显示，获取BGR）
         */
        if (mBitmap == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.faces, options);
        }
        // 图像对齐
        Bitmap bitmap = ArcSoftImageUtil.getAlignedBitmap(mBitmap, true);

        final SpannableStringBuilder notificationSpannableStringBuilder = new SpannableStringBuilder();
        if (faceEngineCode != ErrorInfo.MOK) {
            addNotificationInfo(notificationSpannableStringBuilder, null, " face engine not initialized!");
            showNotificationAndFinish(notificationSpannableStringBuilder);
            return;
        }
        if (bitmap == null) {
            addNotificationInfo(notificationSpannableStringBuilder, null, " bitmap is null!");
            showNotificationAndFinish(notificationSpannableStringBuilder);
            return;
        }
        if (faceEngine == null) {
            addNotificationInfo(notificationSpannableStringBuilder, null, " faceEngine is null!");
            showNotificationAndFinish(notificationSpannableStringBuilder);
            return;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // bitmap转bgr24
        long start = System.currentTimeMillis();
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            Log.e(TAG, "transform failed, code is " + transformCode);
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "transform bitmap To ImageData failed", "code is ", String.valueOf(transformCode), "\n");
            return;
        }
//        Log.i(TAG, "processImage:bitmapToBgr24 cost =  " + (System.currentTimeMillis() - start));
        addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "start face detection,imageWidth is ", String.valueOf(width), ", imageHeight is ", String.valueOf(height), "\n");

        List<FaceInfo> faceInfoList = new ArrayList<>();

        /*
         * 2.成功获取到了BGR24 数据，开始人脸检测
         */
        long fdStartTime = System.currentTimeMillis();
//        ArcSoftImageInfo arcSoftImageInfo = new ArcSoftImageInfo(width,height,FaceEngine.CP_PAF_BGR24,new byte[][]{bgr24},new int[]{width * 3});
//        Log.i(TAG, "processImage: " + arcSoftImageInfo.getPlanes()[0].length);
//        int detectCode = faceEngine.detectFaces(arcSoftImageInfo, faceInfoList);
        int detectCode = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, DetectModel.RGB, faceInfoList);
        if (detectCode == ErrorInfo.MOK) {
//            Log.i(TAG, "processImage: fd costTime = " + (System.currentTimeMillis() - fdStartTime));
        }

        //绘制bitmap
        Bitmap bitmapForDraw = bitmap.copy(Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(bitmapForDraw);
        Paint paint = new Paint();
        addNotificationInfo(notificationSpannableStringBuilder, null, "detect result:\nerrorCode is :", String.valueOf(detectCode), "   face Number is ", String.valueOf(faceInfoList.size()), "\n");
        /*
         * 3.若检测结果人脸数量大于0，则在bitmap上绘制人脸框并且重新显示到ImageView，若人脸数量为0，则无法进行下一步操作，操作结束
         */
        if (faceInfoList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, null, "face list:\n");
            paint.setAntiAlias(true);
            paint.setStrokeWidth(5);
            paint.setColor(Color.YELLOW);
            for (int i = 0; i < faceInfoList.size(); i++) {
                //绘制人脸框
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(faceInfoList.get(i).getRect(), paint);
                //绘制人脸序号
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                int textSize = faceInfoList.get(i).getRect().width() / 2;
                paint.setTextSize(textSize);

                canvas.drawText(String.valueOf(i), faceInfoList.get(i).getRect().left, faceInfoList.get(i).getRect().top - 10, paint);
                addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:", faceInfoList.get(i).toString(), "\n");
            }
            //显示
            final Bitmap finalBitmapForDraw = bitmapForDraw;
            runOnUiThread(() -> Glide.with(ivShow.getContext())
                    .load(finalBitmapForDraw)
                    .into(ivShow));
        } else {
            addNotificationInfo(notificationSpannableStringBuilder, null, "can not do further action, exit!");
            showNotificationAndFinish(notificationSpannableStringBuilder);
            return;
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");


        /*
         * 4.上一步已获取到人脸位置和角度信息，传入给process函数，进行年龄、性别、三维角度、活体检测
         */
        long processStartTime = System.currentTimeMillis();
        int faceProcessCode = faceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList, processMask);

        if (faceProcessCode != ErrorInfo.MOK) {
            addNotificationInfo(notificationSpannableStringBuilder, new ForegroundColorSpan(Color.RED), "process failed! code is ", String.valueOf(faceProcessCode), "\n");
        } else {
            Log.i(TAG, "processImage: process costTime = " + (System.currentTimeMillis() - processStartTime));
        }
        //年龄信息结果
        List<AgeInfo> ageInfoList = new ArrayList<>();
        //性别信息结果
        List<GenderInfo> genderInfoList = new ArrayList<>();
        //人脸三维角度结果
        List<Face3DAngle> face3DAngleList = new ArrayList<>();
        //活体检测结果
        List<LivenessInfo> livenessInfoList = new ArrayList<>();
        //口罩检测结果
        List<MaskInfo> maskInfoList = new ArrayList<>();
        //获取年龄、性别、三维角度、活体结果
        int ageCode = faceEngine.getAge(ageInfoList);
        int genderCode = faceEngine.getGender(genderInfoList);
        int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);
        int livenessCode = faceEngine.getLiveness(livenessInfoList);
        int maskCode = faceEngine.getMask(maskInfoList);

        if ((ageCode | genderCode | face3DAngleCode | livenessCode | maskCode) != ErrorInfo.MOK) {
            addNotificationInfo(notificationSpannableStringBuilder, null,
                    "at least one of age, gender, face3DAngle, liveness, mask, shelter detect failed, codes are:",
                    String.valueOf(ageCode), " , ", String.valueOf(genderCode), " , ", String.valueOf(face3DAngleCode),
                    " , ", String.valueOf(livenessCode), " , ", String.valueOf(maskCode));
            showNotificationAndFinish(notificationSpannableStringBuilder);
            return;
        }

        /*
         * 5.年龄、性别、三维角度已获取成功，添加信息到提示文字中
         */
        //年龄数据
        if (ageInfoList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "age of each face:\n");
        }
        for (int i = 0; i < ageInfoList.size(); i++) {
            addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:", String.valueOf(ageInfoList.get(i).getAge()), "\n");
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");

        //性别数据
        if (genderInfoList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "gender of each face:\n");
        }
        for (int i = 0; i < genderInfoList.size(); i++) {
            addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:"
                    , genderInfoList.get(i).getGender() == GenderInfo.MALE ?
                            "MALE" : (genderInfoList.get(i).getGender() == GenderInfo.FEMALE ? "FEMALE" : "UNKNOWN"), "\n");
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");

        //人脸三维角度数据
        if (face3DAngleList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "face3DAngle of each face:\n");
            for (int i = 0; i < face3DAngleList.size(); i++) {
                addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:", face3DAngleList.get(i).toString(), "\n");
            }
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");

        //活体检测数据
        if (livenessInfoList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "liveness of each face:\n");
            for (int i = 0; i < livenessInfoList.size(); i++) {
                String liveness = null;
                switch (livenessInfoList.get(i).getLiveness()) {
                    case LivenessInfo.ALIVE:
                        liveness = "ALIVE";
                        break;
                    case LivenessInfo.NOT_ALIVE:
                        liveness = "NOT_ALIVE";
                        break;
                    case LivenessInfo.FACE_NUM_MORE_THAN_ONE:
                        liveness = "FACE_NUM_MORE_THAN_ONE";
                        break;
                    case LivenessInfo.UNKNOWN:
                    default:
                        liveness = "UNKNOWN";
                        break;
                }
                addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:", liveness, "\n");
            }
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");

        //口罩检测数据
        if (!maskInfoList.isEmpty()) {
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "mask of each face:\n");
            for (int i = 0; i < maskInfoList.size(); i++) {
                MaskInfo maskInfo = maskInfoList.get(i);
                int mask = maskInfo.getMask();
                String stringMask;
                switch (mask) {
                    case MaskInfo.NOT_WORN:
                        stringMask = "未佩戴口罩";
                        break;
                    case MaskInfo.WORN:
                        stringMask = "已佩戴口罩";
                        break;
                    default:
                        stringMask = "不确定";
                        break;
                }
                addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:", stringMask, "\n");
            }
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");
        showNotificationAndFinish(notificationSpannableStringBuilder);
    }

    /**
     * 展示提示信息并且关闭提示框
     *
     * @param stringBuilder 带格式的提示文字
     */
    private void showNotificationAndFinish(final SpannableStringBuilder stringBuilder) {
        runOnUiThread(() -> {
            if (tvNotice != null) {
                tvNotice.setText(stringBuilder);
            }
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    /**
     * 追加提示信息
     *
     * @param stringBuilder 提示的字符串的存放对象
     * @param styleSpan     添加的字符串的格式
     * @param strings       字符串数组
     */
    private void addNotificationInfo(SpannableStringBuilder stringBuilder, ParcelableSpan styleSpan, String... strings) {
        if (stringBuilder == null || strings == null || strings.length == 0) {
            return;
        }
        int startLength = stringBuilder.length();
        for (String string : strings) {
            stringBuilder.append(string);
        }
        int endLength = stringBuilder.length();
        if (styleSpan != null) {
            stringBuilder.setSpan(styleSpan, startLength, endLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 从本地选择文件
     *
     * @param view
     */
    public void chooseLocalImage(View view) {
        getImageFromAlbum(new OnGetImageFromAlbumCallback() {
            @Override
            public void onGetImageFromAlbumSuccess(Uri uri) {
                mBitmap = ImageUtil.uriToScaledBitmap(ImageFaceAttrDetectActivity.this, uri, ImageUtil.DEFAULT_MAX_WIDTH, ImageUtil.DEFAULT_MAX_HEIGHT);
                Glide.with(ivShow.getContext())
                        .load(mBitmap)
                        .into(ivShow);
                tvNotice.setText("");
            }

            @Override
            public void onGetImageFromAlbumFailed() {
                showToast(getString(R.string.get_picture_failed));
            }
        });
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

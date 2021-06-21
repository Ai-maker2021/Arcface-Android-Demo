package com.arcsoft.arcfacedemo.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.databinding.ActivityHomeBinding;
import com.arcsoft.arcfacedemo.ui.viewmodel.HomeViewModel;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.arcfacedemo.widget.NavigateItemView;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.VersionInfo;

/**
 * 首页，包括激活、界面选择等功能
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private ActivityHomeBinding activityHomeBinding;
    private static final String TAG = "HomeActivity";
    private NavigateItemView activeView;

    HomeViewModel homeViewModel;

    private static final String[] NEEDED_PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE};

    private static final int REQUEST_ACTIVE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initViewModel();
        initView();
        if (checkPermissions(NEEDED_PERMISSIONS)) {
            initData();
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        }
    }

    private void initData() {
        homeViewModel.getActivated().postValue(homeViewModel.isActivated(this));
    }

    private void initViewModel() {
        homeViewModel = new ViewModelProvider(
                getViewModelStore(),
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        )
                .get(HomeViewModel.class);


        // 设置监听，在数据变更时，更新View中内容
        homeViewModel.getActivated().observe(this, activated
                -> activeView.changeTipHint(getString(activated ? R.string.already_activated : R.string.not_activated))
        );

        homeViewModel.getActiveCode().observe(this, activeCode -> {
            String notification;
            switch (activeCode) {
                case ErrorInfo.MOK:
                    notification = getString(R.string.active_success);
                    break;
                case ErrorInfo.MERR_ASF_ALREADY_ACTIVATED:
                    notification = getString(R.string.dont_need_active_anymore);
                    break;
                default:
                    notification = getString(R.string.active_failed, activeCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(activeCode));
                    break;
            }
            activeView.changeTipHint(notification);
            showToast(notification);
        });
    }

    private void initView() {
        VersionInfo versionInfo = new VersionInfo();
        if (FaceEngine.getVersion(versionInfo) == ErrorInfo.MOK) {
            activityHomeBinding.setSdkVersion("ArcFace SDK Version:" + versionInfo.getVersion());
        }

        activityHomeBinding.llRootView.addView(new NavigateItemView(this, R.drawable.ic_face_id_ir, getString(R.string.page_ir_face_recognize), RegisterAndRecognizeActivity.class));
        activityHomeBinding.llRootView.addView(new NavigateItemView(this, R.drawable.ic_liveness_check, getString(R.string.page_liveness_detect), LivenessDetectActivity.class));
        activityHomeBinding.llRootView.addView(new NavigateItemView(this, R.drawable.ic_face_attr, getString(R.string.page_single_image), ImageFaceAttrDetectActivity.class));
        activityHomeBinding.llRootView.addView(new NavigateItemView(this, R.drawable.ic_face_compare, getString(R.string.page_face_compare), FaceCompareActivity.class));
        activityHomeBinding.llRootView.addView(new NavigateItemView(this, R.drawable.ic_face_manage, getString(R.string.page_face_manage), FaceManageActivity.class));
        activityHomeBinding.llRootView.addView(new NavigateItemView(this, R.drawable.ic_settings, getString(R.string.page_settings), RecognizeSettingsActivity.class));

        activeView = new NavigateItemView(this, R.drawable.ic_online_active, getString(R.string.active_engine), "", ActivationActivity.class);
        activityHomeBinding.llRootView.addView(activeView);

//        activityHomeBinding.llRootView.addView(new NavigateItemView(this, R.drawable.ic_readme, getString(R.string.page_readme), ReadmeActivity.class));


        int childCount = activityHomeBinding.llRootView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View itemView = activityHomeBinding.llRootView.getChildAt(i);
            itemView.setOnClickListener(this);
        }
    }


    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (isAllGranted) {
            initData();
        } else {
            showToast(getString(R.string.permission_denied));
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof NavigateItemView) {
            switch (((NavigateItemView) v).getImgRes()) {
                case R.drawable.ic_online_active:
                    navigateToNewPageForResult(((Class) ((NavigateItemView) v).getExtraData()), REQUEST_ACTIVE_CODE);
                    break;
                case R.drawable.ic_readme:
                    navigateToNewPage(((Class) ((NavigateItemView) v).getExtraData()));
                    break;
                default:

                    boolean activated = homeViewModel.isActivated(this);
                    if (!activated) {
                        showLongToast(getString(R.string.notice_please_active_before_use));
                        activeView.performClick();
                    } else {
                        navigateToNewPage(((Class) ((NavigateItemView) v).getExtraData()));
                    }
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACTIVE_CODE:
                homeViewModel.getActivated().postValue(homeViewModel.isActivated(this));
                break;
            default:
                break;
        }
    }
}

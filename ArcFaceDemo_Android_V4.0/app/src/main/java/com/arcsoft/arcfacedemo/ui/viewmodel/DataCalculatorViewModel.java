package com.arcsoft.arcfacedemo.ui.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.R;

public class DataCalculatorViewModel extends ViewModel {
    private MutableLiveData<String> imageWidthNotice = new MutableLiveData<>();
    private MutableLiveData<String> imageHeightNotice = new MutableLiveData<>();
    private MutableLiveData<String> dataLengthNotice = new MutableLiveData<>();


    public MutableLiveData<String> getDataLengthNotice() {
        return dataLengthNotice;
    }

    public MutableLiveData<String> getImageWidthNotice() {
        return imageWidthNotice;
    }

    public MutableLiveData<String> getImageHeightNotice() {
        return imageHeightNotice;
    }

    public String getWidthHelperText(CharSequence s, int maxLength) {
        if (s.length() == 0) {
            return ArcFaceApplication.getApplication().getString(R.string.notice_input_width);
        }
        if (s.length() > maxLength) {
            return ArcFaceApplication.getApplication().getString(R.string.large_resolution_not_recommended);
        }
        if (Integer.parseInt(s.toString()) % 4 != 0) {
            return ArcFaceApplication.getApplication().getString(R.string.width_must_be_multiple_of_4);
        }
        return "";
    }

    public String getHeightHelperText(CharSequence s, int maxLength) {
        if (s.length() == 0) {
            return ArcFaceApplication.getApplication().getString(R.string.notice_input_height);
        }
        if (s.length() > maxLength) {
            return ArcFaceApplication.getApplication().getString(R.string.large_resolution_not_recommended);
        }
        return "";
    }

    public void updateWidthHelperText(CharSequence s, int maxLength) {
        imageWidthNotice.postValue(getWidthHelperText(s, maxLength));
    }

    public void updateHeightHelperText(CharSequence s, int maxLength) {
        imageHeightNotice.postValue(getHeightHelperText(s, maxLength));
    }

    public void calculateSize(String imageWidthText, String imageHeightNotice) {
        int width = Integer.parseInt(imageWidthText);
        int height = Integer.parseInt(imageHeightNotice);
        boolean widthValid = (width > 0 && width % 4 == 0);
        if (widthValid) {
            StringBuilder stringBuilder = new StringBuilder();
            boolean isHeightEven = height > 0 && height % 2 == 0;
            stringBuilder
                    .append("数据长度：").append("\n")
                    .append("BGR24: ").append(width * height * 3).append("\n")
                    .append("GRAY: ").append(width * height).append("\n")
                    .append("DEPTH_U16: ").append(width * height * 2).append("\n")
                    .append("NV21: ").append(isHeightEven ? String.valueOf(width * height * 3 / 2) : "NV21图像高度不合法");
            getDataLengthNotice().postValue(stringBuilder.toString());
        } else {
            getDataLengthNotice().postValue(null);
        }
    }
}

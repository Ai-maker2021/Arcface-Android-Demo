package com.arcsoft.arcfacedemo.ui.viewmodel;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.DetectFaceOrientPriority;


public class HomeViewModel extends ViewModel {
    private MutableLiveData<Boolean> activated = new MutableLiveData<>();
    private MutableLiveData<Integer> activeCode = new MutableLiveData<>();

    public MutableLiveData<Boolean> getActivated() {
        return activated;
    }

    public MutableLiveData<Integer> getActiveCode() {
        return activeCode;
    }

    public boolean isActivated(Context context) {
        return FaceEngine.getActiveFileInfo(context, new ActiveFileInfo()) == ErrorInfo.MOK;
    }
}

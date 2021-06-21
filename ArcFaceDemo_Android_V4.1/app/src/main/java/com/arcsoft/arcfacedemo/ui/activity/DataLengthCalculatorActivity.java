package com.arcsoft.arcfacedemo.ui.activity;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.databinding.ActivityDataLengthCalculatorBinding;
import com.arcsoft.arcfacedemo.ui.viewmodel.DataCalculatorViewModel;

/**
 * 根据输入的宽高计算各个图像格式的数据长度
 */
public class DataLengthCalculatorActivity extends BaseActivity {
    private ActivityDataLengthCalculatorBinding binding;
    private DataCalculatorViewModel dataCalculatorViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_data_length_calculator);
        initViewModel();
        initView();
    }

    private void initViewModel() {
        dataCalculatorViewModel = ViewModelProviders.of(this).get(DataCalculatorViewModel.class);

        dataCalculatorViewModel.getDataLengthNotice().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.setDataLengthNotice(s == null ? "" : s);
            }
        });
        dataCalculatorViewModel.getImageWidthNotice().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String widthHelperText) {
                Log.i("TAG", "onTextChanged: " + widthHelperText);
                if (widthHelperText.length() != 0) {
                    binding.tilImageWidth.setHelperTextTextAppearance(R.style.HintErrorTextStyle);
                } else {
                    binding.tilImageWidth.setHelperTextTextAppearance(R.style.HintNormalTextStyle);
                }
                binding.tilImageWidth.setHelperText(widthHelperText);
            }
        });
        dataCalculatorViewModel.getImageHeightNotice().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String heightHelperText) {
                if (heightHelperText.length() != 0) {
                    binding.tilImageHeight.setHelperTextTextAppearance(R.style.HintErrorTextStyle);
                } else {
                    binding.tilImageHeight.setHelperTextTextAppearance(R.style.HintNormalTextStyle);
                }
                binding.tilImageHeight.setHelperText(heightHelperText);
            }
        });
    }

    private void initView() {
        enableBackIfActionBarExists();

        binding.etImageWidth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dataCalculatorViewModel.updateWidthHelperText(s, binding.tilImageWidth.getCounterMaxLength());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etImageHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dataCalculatorViewModel.updateHeightHelperText(s, binding.tilImageHeight.getCounterMaxLength());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.btCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCalculate();
            }
        });
    }

    private void doCalculate() {
        String imageWidthText = binding.etImageWidth.getText().toString();
        String imageHeightText = binding.etImageHeight.getText().toString();
        if (TextUtils.isEmpty(imageWidthText)) {
            binding.tilImageWidth.setHelperTextTextAppearance(R.style.HintErrorTextStyle);
            binding.tilImageWidth.setHelperText("请输入图像宽度");
        }
        if (TextUtils.isEmpty(imageHeightText)) {
            binding.tilImageHeight.setHelperTextTextAppearance(R.style.HintErrorTextStyle);
            binding.tilImageHeight.setHelperText("请输入图像宽度");
        }
        // width & height均没有错误提示
        Log.i("TAG", "onClick: " + imageWidthText + " " + imageHeightText);
        if (!binding.tilImageWidth.isHelperTextEnabled() && !binding.tilImageHeight.isHelperTextEnabled()) {
            dataCalculatorViewModel.calculateSize(imageWidthText, imageHeightText);
        }
    }

}

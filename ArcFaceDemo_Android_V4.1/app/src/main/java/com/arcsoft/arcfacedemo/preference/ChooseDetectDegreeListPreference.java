package com.arcsoft.arcfacedemo.preference;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.widget.MarginImageSpan;
import com.arcsoft.arcfacedemo.widget.VerticalAlignTextSpan;


public class ChooseDetectDegreeListPreference extends ListPreference {
    public ChooseDetectDegreeListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ChooseDetectDegreeListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChooseDetectDegreeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChooseDetectDegreeListPreference(Context context) {
        super(context);
    }

    @Override
    public void setEntries(CharSequence[] entries) {
        TypedArray icons = getContext().getResources().obtainTypedArray(R.array.recognize_orient_priority_icon);
        for (int i = 0; i < entries.length; i++) {
            entries[i] = createEntry(entries[i].toString(), icons.getDrawable(i));
        }
        icons.recycle();
        super.setEntries(entries);
    }

    private SpannableString createEntry(String str, Drawable drawable) {
        Resources res = getContext().getResources();
        if (drawable == null) {
            return new SpannableString(str);
        }
        final SpannableString spannableString = new SpannableString("REPLACE" + str );
        int iconSize = res.getDimensionPixelSize(R.dimen.preference_image_size);
        drawable.setBounds(0, 0, iconSize, iconSize);
        MarginImageSpan imageSpan = new MarginImageSpan(drawable);
        spannableString.setSpan(imageSpan, 0, spannableString.length() - str.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new VerticalAlignTextSpan(), spannableString.length() - str.length(), spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannableString;
    }
}

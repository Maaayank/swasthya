package com.example.mcan_miniproject.Dialogs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.example.mcan_miniproject.R;

public class SetGoalDialog extends DialogPreference {

    private static final int DEFAULT_VALUE = 3;
    private int initialValue;
    private NumberPicker numberPicker;

    public SetGoalDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SetGoalDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SetGoalDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SetGoalDialog(Context context) {
        super(context);
    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObject) {
        if (restorePersistedValue) {
            initialValue = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            initialValue = (Integer) defaultValueObject;
            persistInt(initialValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    public void onBindDialogView(View view) {
        numberPicker =  view.findViewById(R.id.numberPicker);

        //Display the number picker values in thousands
        String[] displayedValues = new String[19];

        //Starting from 2000
        for (int i = 0; i < 19; i++)
            displayedValues [i] = String.valueOf((i + 2) * 1000);

        numberPicker.setMinValue(2);
        numberPicker.setMaxValue(20);
        numberPicker.setDisplayedValues(displayedValues);
        numberPicker.setValue(initialValue);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            initialValue = numberPicker.getValue();
            persistInt(initialValue);
        }
    }
}

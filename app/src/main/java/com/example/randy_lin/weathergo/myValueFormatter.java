package com.example.randy_lin.weathergo;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class myValueFormatter implements ValueFormatter {

    private DecimalFormat mFormat;
    private String suffix;

    public myValueFormatter(String s) {
        mFormat = new DecimalFormat("#");
        suffix = s;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value) + suffix;
    }
}

package com.example.randy_lin.weathergo;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class CustomXAxisRenderer extends XAxisRenderer {
    public CustomXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
        super(viewPortHandler, xAxis, trans);
    }

    @Override
    protected void drawLabel(Canvas c, String label, int xIndex, float x, float y, PointF anchor, float angleDegrees) {
        String line[] = mXAxis.getValueFormatter().getXValue(label, xIndex, mViewPortHandler).split("\n");
        for (String texts : line) {
            y += mAxisLabelPaint.getTextSize();
            Utils.drawText(c, texts, x, y, mAxisLabelPaint, anchor, angleDegrees);
        }
    }
}
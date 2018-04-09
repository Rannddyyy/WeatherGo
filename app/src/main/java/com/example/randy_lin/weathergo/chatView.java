package com.example.randy_lin.weathergo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

public class chatView extends View {

    private Paint mPaint;

    private Rect mBounds;

    private float mWidth;
    private float mHeight;
    private float boardSize;

    final private String defaultString = "N/A";
    final private String rain_format = "降雨機率: %s%%";
    final private String temperate_format = "氣溫: %s °C";
    private String loc;
    private String city;
    private String district;
    private String weather;
    private String rain_p;
    private String temperature;

    private Bitmap WP; // weather picture

    public chatView(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBounds = new Rect();
        mWidth = 540;
        mHeight = 300;
        boardSize = 0;
        rain_p = String.format(rain_format, defaultString);
        temperature = String.format(temperate_format, defaultString);
        loc = city = district = weather = defaultString;
        setBackgroundColor(Color.TRANSPARENT);
        setLayoutParams(new ViewGroup.LayoutParams((int) (mWidth + boardSize * 2), (int) (mHeight + mHeight / 5 + boardSize)));
        setMinimumWidth((int) (mWidth + boardSize * 2));
        setMinimumHeight((int) (mHeight + mHeight / 5 + boardSize));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(Color.GRAY);

        Path path = new Path();
        canvas.drawRoundRect(new RectF(0, 0, mWidth + boardSize, mHeight + boardSize), 50f, 50f, mPaint);
        path.moveTo(mWidth / 2 + mWidth / 8 + boardSize, mHeight + boardSize);
        path.lineTo(mWidth / 2, mHeight + mHeight / 5 + boardSize);
        path.lineTo(mWidth / 2 - mWidth / 8 - boardSize, mHeight + boardSize);
        canvas.drawPath(path, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(boardSize, boardSize, mWidth, mHeight), 50f, 50f, mPaint);

        path.reset();
        path.moveTo(mWidth / 2 + mWidth / 8, mHeight - 1);
        path.lineTo(mWidth / 2 - mWidth / 8, mHeight - 1);
        path.lineTo(mWidth / 2, mHeight + mHeight / 5);
        canvas.drawPath(path, mPaint);

        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(50);

        mPaint.getTextBounds(loc, 0, loc.length(), mBounds);
        int Pw = WP == null ? 120 : WP.getWidth();
        float textHeight = mBounds.height();
        float x = boardSize + Pw + 10;
        float y = textHeight / 2;
        canvas.drawText(loc, x, y + 30, mPaint);
        y += 60;
        if (weather.length() > 6) {
            String w = weather.substring(0, 6);
            canvas.drawText(w, x, y + 30, mPaint);
            y += 50;
            w = weather.substring(6);
            canvas.drawText(w, x, y + 30, mPaint);
            y += 60;
        } else {
            canvas.drawText(weather, x, y + 30, mPaint);
            y += 60;
        }
        canvas.drawText(temperature, x, y + 30, mPaint);
        y += 60;
        canvas.drawText(rain_p, x, y + 30, mPaint);

        if (WP != null)
            canvas.drawBitmap(WP, 10, (mHeight + boardSize - WP.getHeight()) / 2, mPaint);
        else canvas.drawText(defaultString, 20, (mHeight + boardSize) / 2, mPaint);
    }

    public float getmWidth() {
        return mWidth;
    }

    public float getmHeight() {
        return mHeight;
    }

    public chatView setmBounds(float b) {
        boardSize = b;
        setLayoutParams(new ViewGroup.LayoutParams((int) (mWidth + boardSize * 2), (int) (mHeight + mHeight / 5 + boardSize)));
        setMinimumWidth((int) (mWidth + boardSize * 2));
        setMinimumHeight((int) (mHeight + mHeight / 5 + boardSize));
        return this;
    }

    public chatView setLoc(String l) {
        if (l == null || l.length() <= 0) return this;
        loc = l;
        return this;
    }

    public chatView setCity(String c) {
        if (c == null || c.length() <= 0) return this;
        city = c;
        loc = city + ", " + district;
        return this;
    }

    public chatView setDistrict(String d) {
        if (d == null || d.length() <= 0) return this;
        district = d;
        loc = city + ", " + district;
        return this;
    }

    public chatView setWeather(String w) {
        if (w == null || w.length() <= 0) return this;
        weather = w;
        return this;
    }

    public chatView setRain_p(String p) {
        if (p == null || p.length() <= 0) return this;
        rain_p = String.format(rain_format, p);
        return this;
    }

    public chatView setTemperature(String t) {
        if (t == null || t.length() <= 0) return this;
        temperature = String.format(temperate_format, t);
        return this;
    }

    public chatView setPicture(Bitmap pic) {
        WP = pic;
        return this;
    }

    public void setClickLocation(float x, float y) {
        this.setX(x - mWidth / 2);
        this.setY(y - mHeight - mHeight / 5);
    }
}

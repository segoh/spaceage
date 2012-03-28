package com.github.segoh;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class SynthView extends View {

    public static int STEPS = 11;
    private static final float TOUCH_TOLERANCE = 3f;

    private final Paint _paint;
    private final Paint _bgPaint;
    private float _x;
    private float _y;
    private final int _backgroundColor = Color.BLACK;
    private final int _foregroundColor = Color.GREEN;
    private OnTouchListener _onTouchListener = null;

    public SynthView(final Context context) {
        super(context);

        _paint = new Paint();
        _paint.setColor(_foregroundColor);
        _paint.setAntiAlias(true);
        _paint.setDither(true);
        _paint.setStyle(Paint.Style.FILL_AND_STROKE);
        _paint.setStrokeWidth(1);

        _bgPaint = new Paint();
        _bgPaint.setColor(Color.rgb(0x20, 0x20, 0x20));
        _bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        _bgPaint.setStrokeWidth(1);

        _x = 0.0f;
        _y = 0.0f;

        onActionEnd();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.drawColor(_backgroundColor);

        final float stepX = ((float)canvas.getWidth()) / STEPS;
        final float stepY = ((float)canvas.getHeight()) / STEPS;
        for (int i = 1; i < STEPS; ++i) {
            final float x = i * stepX;
            final float y = i * stepY;
            canvas.drawLine(0, y, canvas.getWidth(), y, _bgPaint);
            canvas.drawLine(x, 0, x, canvas.getHeight(), _bgPaint);
        }

        final float currentX = _x - (_x % stepX);
        final float currentY = _y - (_y % stepY);
        canvas.drawRect(currentX, currentY, currentX + stepX, currentY + stepY, _paint);
    }

    @Override
    final public boolean onTouchEvent(final MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        final float delta = Math.max(Math.abs(x - _x), Math.abs(y - _y));

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                onActionStart();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                onActionEnd();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (delta >= TOUCH_TOLERANCE) {
                    _x = x;
                    _y = y;
                    invalidate();
                }
                break;
            }
        }

        if (_onTouchListener != null) {
            return _onTouchListener.onTouch(this, event);
        }
        return true;
    }

    @Override
    public void setOnTouchListener(final OnTouchListener listener) {
        _onTouchListener = listener;
    }

    private void onActionStart() {
        _paint.setColor(Color.rgb(0x00, 0xff, 0x00));
        _paint.setStrokeWidth(1);
        _paint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.SOLID));
    }

    private void onActionEnd() {
        _paint.setColor(Color.rgb(0x00, 0x88, 0x00));
        _paint.setStrokeWidth(5);
        _paint.setMaskFilter(new BlurMaskFilter(7, BlurMaskFilter.Blur.NORMAL));
    }
}

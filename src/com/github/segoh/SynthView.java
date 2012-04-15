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

    private final int[] _colors = ColorTheme.PEAR_LEMON_FIZZ;
    private final Paint _paint;
    private final Paint _bgPaint;
    private float _x;
    private float _y;
    private OnTouchListener _onTouchListener = null;

    public SynthView(final Context context) {
        super(context);

        _paint = new Paint();
        _paint.setAntiAlias(true);
        _paint.setDither(true);
        _paint.setStyle(Paint.Style.FILL_AND_STROKE);
        _paint.setStrokeWidth(1);

        _bgPaint = new Paint();
        _bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        _bgPaint.setStrokeWidth(1);

        _x = 0.0f;
        _y = 0.0f;

        onActionEnd();
    }

    private void updateColor(final Paint paint, final int fieldX, final int fieldY) {
        paint.setColor(_colors[(fieldX + fieldY) % _colors.length]);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        final float stepX = ((float)canvas.getWidth()) / STEPS;
        final float stepY = ((float)canvas.getHeight()) / STEPS;
        final int offset = (int)(stepX * 0.45);

        for (int fieldX = 0; fieldX < STEPS; ++fieldX) {
            for (int fieldY = 0; fieldY < STEPS; ++fieldY) {
                final float x = fieldX * stepX;
                final float y = fieldY * stepY;
                updateColor(_bgPaint, fieldX, fieldY);
                canvas.drawRect(x + offset, y + offset, x + stepX - offset, y + stepY - offset, _bgPaint);
            }
        }

        final float currentX = _x - (_x % stepX);
        final float currentY = _y - (_y % stepY);
        updateColor(_paint, (int)(currentX / stepX), (int)(currentY / stepY));
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
        _paint.setStrokeWidth(1);
        _paint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.SOLID));
    }

    private void onActionEnd() {
        _paint.setStrokeWidth(5);
        _paint.setMaskFilter(new BlurMaskFilter(7, BlurMaskFilter.Blur.NORMAL));
    }
}

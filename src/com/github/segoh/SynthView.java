package com.github.segoh;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class SynthView extends View {

    private static final float TOUCH_TOLERANCE = 3f;

    private final Paint _paint;
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

        _x = 0.0f;
        _y = 0.0f;

        onActionEnd();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.drawColor(_backgroundColor);
        canvas.drawLine(0, _y, canvas.getWidth(), _y, _paint);
        canvas.drawLine(_x, 0, _x, canvas.getHeight(), _paint);
        canvas.drawCircle(_x, _y, 10, _paint);
    }

    @Override
    final public boolean onTouchEvent(final MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float delta = Math.max(Math.abs(x - _x), Math.abs(y - _y));

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onActionStart();
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            onActionEnd();
            invalidate();
        } else if (delta >= TOUCH_TOLERANCE) {
            _x = x;
            _y = y;
            invalidate();
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

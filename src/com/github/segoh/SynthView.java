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
    private final Pointer _pointer = new Pointer();
    private SynthViewListener _synthListener = null;

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

        final float currentX = _pointer.getX() - (_pointer.getX() % stepX);
        final float currentY = _pointer.getY() - (_pointer.getY() % stepY);
        updateColor(_paint, (int)(currentX / stepX), (int)(currentY / stepY));
        canvas.drawRect(currentX, currentY, currentX + stepX, currentY + stepY, _paint);
    }

    @Override
    final public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                _pointer.setId(event.getPointerId(0));
                onActionStart();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = _pointer.indexForId(event);
                if (pointerIndex > -1) {
                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);
                    final float delta = Math.max(
                            Math.abs(x - _pointer.getX()),
                            Math.abs(y - _pointer.getY()));

                    if (delta >= TOUCH_TOLERANCE) {
                        onActionMove(x, y);
                        invalidate();
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                _pointer.invalidate();
                onActionEnd();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                _pointer.invalidate();
                onActionEnd();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (_pointer.isPointer(pointerId)) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    onActionMove(event.getX(newPointerIndex), event.getY(newPointerIndex));
                    _pointer.setId(event.getPointerId(newPointerIndex));
                    invalidate();
                }
                break;
            }
        }

        return true;
    }

    public void setSynthViewListener(final SynthViewListener listener) {
        _synthListener = listener;
    }

    private void onActionStart() {
        _paint.setStrokeWidth(1);
        _paint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.SOLID));

        if (_synthListener != null) {
            _synthListener.onNoteOn(_pointer.normalizedX(), _pointer.normalizedY());
        }
    }

    private void onActionEnd() {
        _paint.setStrokeWidth(5);
        _paint.setMaskFilter(new BlurMaskFilter(7, BlurMaskFilter.Blur.NORMAL));

        if (_synthListener != null) {
            _synthListener.onNoteOff(_pointer.normalizedX(), _pointer.normalizedY());
        }
    }

    private void onActionMove(final float x, final float y) {
        _pointer.setX(x);
        _pointer.setY(y);

        if (_synthListener != null) {
            _synthListener.onNoteChange(_pointer.normalizedX(), _pointer.normalizedY());
        }
    }


    final class Pointer {

        private static final int INVALID_ID = -1;
        private int _pointerId;
        private float _x;
        private float _y;

        Pointer() {
            _pointerId = INVALID_ID;
            _x = 0.0f;
            _y = 0.0f;
        }

        private float normalizePosition(final float position, final float range) {
            return Math.min(1, Math.max(0, position / range));
        }

        public float normalizedX() {
            return normalizePosition(_x, SynthView.this.getWidth());
        }

        public float normalizedY() {
            return normalizePosition(_y, SynthView.this.getHeight());
        }

        public void invalidate() {
            _pointerId = INVALID_ID;
        }

        public void setX(final float x) {
            _x = x;
        }

        public float getX() {
            return _x;
        }

        public void setY(final float y) {
            _y = y;
        }

        public float getY() {
            return _y;
        }

        public void setId(final int pointerId) {
            _pointerId = pointerId;
        }

        public boolean isPointer(final int activeId) {
            return _pointerId != INVALID_ID && activeId == _pointerId;
        }

        public int indexForId(final MotionEvent event) {
            return event.findPointerIndex(_pointerId);
        }
    }
}

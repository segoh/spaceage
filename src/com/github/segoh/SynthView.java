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
    private final Paint _primaryPaint;
    private final Paint _secondaryPaint;
    private final Paint _bgPaint;
    private final Pointer _primaryPointer = new Pointer();
    private final Pointer _secondaryPointer = new Pointer();
    private SynthViewListener _synthListener = null;

    public SynthView(final Context context) {
        super(context);

        _primaryPaint = new Paint();
        _primaryPaint.setAntiAlias(true);
        _primaryPaint.setDither(true);
        _primaryPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        _primaryPaint.setStrokeWidth(1);

        _secondaryPaint = new Paint();
        _secondaryPaint.setStyle(Paint.Style.STROKE);
        _secondaryPaint.setStrokeWidth(3);
        _secondaryPaint.setMaskFilter(new BlurMaskFilter(7, BlurMaskFilter.Blur.NORMAL));
        updateColor(_secondaryPaint, 0, 0);

        _bgPaint = new Paint();
        _bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        _bgPaint.setStrokeWidth(1);

        onActionMainEnd();
    }

    private void updateColor(final Paint paint, final int fieldX, final int fieldY) {
        paint.setColor(_colors[(fieldX + (STEPS - 1 - fieldY)) % _colors.length]);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final float stepX = ((float)canvas.getWidth()) / STEPS;
        final float stepY = ((float)canvas.getHeight()) / STEPS;
        final int alpha = 35 + (int)(_secondaryPointer.getX() / canvas.getHeight() * 220);

        canvas.drawColor(Color.BLACK);

        // Draw background
        final int offset = (int)(stepX * 0.45);
        for (int fieldX = 0; fieldX < STEPS; ++fieldX) {
            for (int fieldY = 0; fieldY < STEPS; ++fieldY) {
                final float x = fieldX * stepX;
                final float y = fieldY * stepY;
                updateColor(_bgPaint, fieldX, fieldY);
                _bgPaint.setAlpha(alpha);
                canvas.drawRect(x + offset, y + offset, x + stepX - offset, y + stepY - offset, _bgPaint);
            }
        }

        // Draw secondary position
        canvas.drawLine(_secondaryPointer.getX(), 0, _secondaryPointer.getX(), canvas.getHeight(), _secondaryPaint);

        // Draw primary position
        final float currentX = _primaryPointer.getX() - (_primaryPointer.getX() % stepX);
        final float currentY = _primaryPointer.getY() - (_primaryPointer.getY() % stepY);
        updateColor(_primaryPaint, (int)(currentX / stepX), (int)(currentY / stepY));
        canvas.drawRect(currentX, currentY, currentX + stepX, currentY + stepY, _primaryPaint);
    }

    @Override
    final public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                _primaryPointer.setId(event.getPointerId(0));
                onActionMainStart();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final boolean isMainUpdated = _primaryPointer.update(event);
                if (isMainUpdated) {
                    onActionMainMove();
                    invalidate();
                }
                final boolean isUtilityUpdated = _secondaryPointer.update(event);
                if (isUtilityUpdated) {
                    onActionUtilityMode();
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                _primaryPointer.invalidate();
                _secondaryPointer.invalidate();
                onActionMainEnd();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                _primaryPointer.invalidate();
                _secondaryPointer.invalidate();
                onActionMainEnd();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                final Pointer newPointer = _primaryPointer.isActive() ? _secondaryPointer : _primaryPointer;
                newPointer.setId(pointerId);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (_primaryPointer.isPointer(pointerId)) {
                    _primaryPointer.invalidate();
                } else {
                    _secondaryPointer.invalidate();
                }
                invalidate();
                break;
            }
        }

        return true;
    }

    public void setSynthViewListener(final SynthViewListener listener) {
        _synthListener = listener;
    }

    private void onActionMainStart() {
        _primaryPaint.setStrokeWidth(1);
        _primaryPaint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.SOLID));
        _secondaryPaint.setAlpha(255);

        if (_synthListener != null) {
            _synthListener.onNoteOn(_primaryPointer.normalizedX(), _primaryPointer.normalizedY());
        }
    }

    private void onActionMainEnd() {
        _primaryPaint.setStrokeWidth(5);
        _primaryPaint.setMaskFilter(new BlurMaskFilter(7, BlurMaskFilter.Blur.NORMAL));
        _secondaryPaint.setAlpha(0);

        if (_synthListener != null) {
            _synthListener.onNoteOff(_primaryPointer.normalizedX(), _primaryPointer.normalizedY());
        }
    }

    private void onActionMainMove() {
        if (_synthListener != null) {
            _synthListener.onNoteChange(_primaryPointer.normalizedX(), _primaryPointer.normalizedY());
        }
    }

    private void onActionUtilityMode() {
        if (_synthListener != null) {
            _synthListener.onControlChange(_secondaryPointer.normalizedX(), _secondaryPointer.normalizedY());
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

        public boolean isActive() {
            return _pointerId != INVALID_ID;
        }

        public boolean isPointer(final int activeId) {
            return _pointerId != INVALID_ID && activeId == _pointerId;
        }

        public int indexForId(final MotionEvent event) {
            return _pointerId == INVALID_ID ? -1 : event.findPointerIndex(_pointerId);
        }

        public boolean update(final MotionEvent event) {
            final int pointerIndex = indexForId(event);
            if (pointerIndex > -1) {
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);
                final float delta = Math.max(Math.abs(x - _x), Math.abs(y - _y));

                if (delta >= TOUCH_TOLERANCE) {
                    _x = x;
                    _y = y;
                    return true;
                }
            }
            return false;
        }
    }
}

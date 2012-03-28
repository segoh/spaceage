package com.github.segoh;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.github.segoh.control.FreqConversion;
import com.github.segoh.control.PentatonicFreqConversion;

public class SpaceAgeActivity extends Activity {

    private final Synth _synth = new Synth();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SynthView mainView = new SynthView(this);
        setUpControls(mainView);
        setContentView(mainView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _synth.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _synth.stop();
    }

    private void setUpControls(final View view) {
        view.setOnTouchListener(new OnTouchListener() {

            private final FreqConversion _freqConv = new PentatonicFreqConversion();

            private float normalizePosition(final float position, final float range) {
                return java.lang.Math.min(1, java.lang.Math.max(0, position / range));
            }

            private void setSynthFreqs(final Synth s, final View v, final MotionEvent event) {
                _synth.setFreqSaw(_freqConv.toFreq(normalizePosition(event.getX(), v.getWidth())));
                _synth.setFreqSine(_freqConv.toFreq(normalizePosition(event.getY(), v.getHeight())));
            }

            public boolean onTouch(final View v, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    setSynthFreqs(_synth, v, event);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    _synth.trigger();
                    setSynthFreqs(_synth, v, event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    _synth.damp();
                }
                return true;
            }
        });
    }
}

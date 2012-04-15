package com.github.segoh;

import android.app.Activity;
import android.os.Bundle;

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

    private void setUpControls(final SynthView view) {
        view.setSynthViewListener(new SynthViewListener() {

            private final FreqConversion _freqConv = new PentatonicFreqConversion(SynthView.STEPS);

            public void onNoteOn(final float x, final float y) {
                _synth.setFreqOsc1(_freqConv.toFreq(x));
                _synth.setFreqOsc2(_freqConv.toFreq(y));
                _synth.trigger();
            }

            public void onNoteOff(final float x, final float y) {
                _synth.damp();
            }

            public void onNoteChange(final float x, final float y) {
                _synth.setFreqOsc1(_freqConv.toFreq(x));
                _synth.setFreqOsc2(_freqConv.toFreq(y));
            }

            public void onControlChange(final float x, final float y) {
                _synth.setCutoff(400.0f + x * x * 4000f);
            }
        });
    }
}

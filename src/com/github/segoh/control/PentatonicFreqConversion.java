package com.github.segoh.control;


public class PentatonicFreqConversion implements FreqConversion {

    private final int _min;
    private final int _steps;
    private final int [] _scale;

    public PentatonicFreqConversion(final int steps) {
        _min = 40;
        _steps = steps;
        _scale = new int[] { 0, 3, 5, 7, 10 };
    }

    public float toFreq(final float value) {
        final int index = (int)Math.floor(value * _steps);
        final int octave = (int)Math.floor(index / _scale.length);
        final int reminder = index % _scale.length;
        final int note = _min + (12 * octave + _scale[reminder]);
        return Conversion.midiToFreq(note);
    }
}

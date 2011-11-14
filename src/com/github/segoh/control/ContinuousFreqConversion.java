package com.github.segoh.control;


public class ContinuousFreqConversion implements FreqConversion {

    private final float _min;
    private final float _factor;

    public ContinuousFreqConversion() {
        _min = 40.0f;
        _factor = 600.0f;
    }

    public float toFreq(final float value) {
        return _min + value * _factor;
    }
}

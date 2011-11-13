package com.github.segoh.dsp;

public class ExpEnv extends UGen {

    public static final float FACTOR_HARD = 0.005f;
    public static final float FACTOR_SOFT = 0.00005f;

    private boolean _state;
    private float _attenuation;
    private float _factor = FACTOR_SOFT;
    private final float _idealMarker = 0.25f;
    private float _marker = _idealMarker;

    public synchronized void setActive(final boolean nextState) {
        _state = nextState;
    }

    public synchronized void setFactor(final float nextFactor) {
        _factor = nextFactor;
    }

    public synchronized void setGain(final float gain) {
        _marker = gain * _idealMarker;
    }

    @Override
    public synchronized boolean render(final float[] buffer) {
        if (!_state && _attenuation < 0.0001f) {
            return false; // Envelope is closed
        }
        if (!renderInputs(buffer)) {
            return false; // No input
        }

        for (int i = 0; i < UGen.BUFFER_SIZE; i++) {
            buffer[i] *= _attenuation;
            _attenuation += ((_state ? _marker : 0) - _attenuation) * _factor;
        }
        return true;
    }
}

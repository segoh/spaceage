package com.github.segoh.dsp;

import android.util.FloatMath;

public class WavetableOsc extends UGen {

    public static final int BITS = 8;
    public static final int ENTRIES = 1 << (BITS - 1);
    public static final int MASK = ENTRIES - 1;

    private float _phase;
    private float _cyclesPerSample;
    private final float[] _table;

    public WavetableOsc () {
        _table = new float[ENTRIES];
    }

    public synchronized void setFreq(final float freq) {
        _cyclesPerSample = freq / UGen.SAMPLE_RATE;
    }

    @Override
    public synchronized boolean render(final float[] buffer) {
        for (int i = 0; i < UGen.BUFFER_SIZE; i++) {
            final float scaled = _phase * ENTRIES;
            final float fraction = scaled - (int)scaled;
            final int index = (int)scaled;
            buffer[i] += (1.0f - fraction) * _table[index & MASK]
                    + fraction * _table[(index + 1) & MASK];
            _phase = (_phase + _cyclesPerSample) - (int)_phase;
        }
        return true;
    }

    public WavetableOsc fillWithSin() {
        final float dt = (float)(2.0 * Math.PI / ENTRIES);
        for (int i = 0; i < ENTRIES; i++) {
            _table[i] = FloatMath.sin(i * dt);
        }
        return this;
    }

    public WavetableOsc fillWithHardSin(final float exp) {
        final float dt = (float)(2.0 * Math.PI / ENTRIES);
        for (int i = 0; i < ENTRIES; i++) {
            _table[i] = (float) Math.pow(FloatMath.sin(i * dt), exp);
        }
        return this;
    }

    public WavetableOsc fillWithZero() {
        for (int i = 0; i < ENTRIES; i++) {
            _table[i] = 0;
        }
        return this;
    }

    public WavetableOsc fillWithSqr() {
        for (int i = 0; i < ENTRIES; i++) {
            _table[i] = (i < (ENTRIES / 2)) ? 1f : -1f;
        }
        return this;
    }

    public WavetableOsc fillWithSqrDuty(final float fraction) {
        for (int i = 0; i < ENTRIES; i++) {
            _table[i] = (float)i / ((ENTRIES < fraction) ? 1f : -1f);
        }
        return this;
    }

    public WavetableOsc fillWithSaw() {
        float dt = (float)(2.0 / ENTRIES);
        for (int i = 0; i < ENTRIES; i++) {
            _table[i] = 1.0f - i * dt;
        }
        return this;
    }
}

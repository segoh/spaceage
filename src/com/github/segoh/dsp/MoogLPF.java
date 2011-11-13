package com.github.segoh.dsp;

/**
 * Digital approximation of Moog VCF.
 * based on http://www.musicdsp.org/showArchiveComment.php?ArchiveID=24
 */
public class MoogLPF extends UGen {

    private float _cutoff;
    private float _res;
    private float _y1, _y2, _y3, _y4;
    private float _oldx;
    private float _oldy1, _oldy2, _oldy3;
    private float _x;
    private float _r;
    private float _p;
    private float _k;

    public MoogLPF(final float cutoff, final float resonance) {
        _cutoff = cutoff;
        _res = resonance;
        _y1 = _y2 = _y3 = _y4 = _oldx = _oldy1 = _oldy2 = _oldy3 = 0;
        updateState();
    }

    public synchronized void setCutoff(final float _ctoff) {
        _cutoff = _ctoff;
        updateState();
    }

    public synchronized void setResonance(final float resonance) {
        _res = resonance;
        updateState();
    }

    private synchronized void updateState() {
        final float f = (_cutoff + _cutoff) / UGen.SAMPLE_RATE;  // [0 - 1]
        _p = f * (1.8f - 0.8f * f);
        _k = _p + _p - 1.f;

        final float t = (1.f - _p) * 1.386249f;
        final float t2 = 12.f + t * t;
        _r = _res * (t2 + 6.f * t) / (t2 - 6.f * t);
    }

    private float processSample(final float input) {
        _x = input - _r*_y4;

        _y1 =  _x*_p +  _oldx*_p - _k*_y1;
        _y2 = _y1*_p + _oldy1*_p - _k*_y2;
        _y3 = _y2*_p + _oldy2*_p - _k*_y3;
        _y4 = _y3*_p + _oldy3*_p - _k*_y4;

        _y4 -= (_y4 * _y4 * _y4) / 6.f;

        _oldx = _x;
        _oldy1 = _y1;
        _oldy2 = _y2;
        _oldy3 = _y3;
        return _y4;
    }

    @Override
    public boolean render(final float[] buffer) {
        renderInputs(buffer);

        for (int i = 0; i < UGen.BUFFER_SIZE; i++) {
            buffer[i] = processSample(buffer[i]);
        }
        return true;
    }
}

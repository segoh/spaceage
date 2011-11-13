package com.github.segoh.dsp;

public class Delay extends UGen {

    private final float[] _delayLine;
    private int _delayPointer;
    private float _wet;
    private float _dry;

    public Delay(final int length, final float wetAmount) {
        super();
        _delayLine = new float[length];
        setWet(wetAmount);
    }

    public synchronized void setWet(final float wetAmount) {
        _wet = wetAmount;
        _dry = 1f - _wet;
    }

    @Override
    public boolean render(final float[] buffer) {
        renderInputs(buffer);

        final float[] localLine = _delayLine;
        final int lineLength = _delayLine.length;

        for (int i = 0; i < UGen.BUFFER_SIZE; i++) {
            buffer[i] = (_dry * buffer[i]) - (_wet * localLine[_delayPointer]);
            localLine[_delayPointer] = buffer[i];
            _delayPointer = (_delayPointer + 1) % lineLength;
        }

        return true;
    }
}

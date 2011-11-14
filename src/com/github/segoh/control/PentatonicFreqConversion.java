package com.github.segoh.control;

public class PentatonicFreqConversion implements FreqConversion {

    private final int _min;
    private final int _max;
    private final int [] _scale;

    public PentatonicFreqConversion() {
        _min = 20;
        _max = 70;
        _scale = new int[] { 3, 5, 7, 10 };
    }

    private int quantize(final int note) {
        final int octaves = (int)(note / 12.0) - 1;
        final int reminder = note % 12;

        int quantized = 0;
        for (int s : _scale) {
            if (reminder > s) {
                quantized = s;
            } else {
                break;
            }
        }
        return _min + (octaves * 12) + quantized;
    }

    public float toFreq(final float value) {
        final int note = (int)(_min + (_max - _min + 1) * value);
        return Conversion.midiToFreq(quantize(note));
    }
}

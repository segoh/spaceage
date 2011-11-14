package com.github.segoh.control;

public class Conversion {

    public static float midiToFreq(final int note) {
        return (float)(440.0 * java.lang.Math.pow(2, (note - 69) / 12.0));
    }

    public static int freqToMidi(final double freq) {
        return (int)((java.lang.Math.log(freq / 440.0) / java.lang.Math.log(2.0)) * 12.0 + 69);
    }
}

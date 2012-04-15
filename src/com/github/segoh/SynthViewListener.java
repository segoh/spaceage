package com.github.segoh;

public interface SynthViewListener {

    void onNoteOn(float x, float y);

    void onNoteOff(float x, float y);

    void onNoteChange(float x, float y);

    void onControlChange(float x, float y);
}

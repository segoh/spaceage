package com.github.segoh.dsp;

import java.util.ArrayList;

/**
 * Base class for unit-generators.
 * This is based on Adam Smith's Ethereal Dialpad DSP code
 * https://gist.github.com/376028
 */
public abstract class UGen {

    public static final int BUFFER_SIZE = 256;
    public static final int SAMPLE_RATE = 22050;

    protected final ArrayList<UGen> inputs = new ArrayList<UGen>(0);

    /**
     * Fill samples in the given buffer.
     * @param buffer
     * @return true if the buffer was updated.
     */
    abstract public boolean render(final float[] buffer);

    public synchronized UGen chuck(final UGen parent) {
        if (!parent.inputs.contains(this)) {
            parent.inputs.add(this);
        }
        return parent;
    }

    public synchronized UGen unchuck(final UGen parent) {
        if (parent.inputs.contains(this)) {
            parent.inputs.remove(this);
        }
        return parent;
    }

    protected void silenceBuffer(final float[] buffer) {
        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer[i] = 0;
        }
    }

    protected boolean renderInputs(final float[] buffer) {
        boolean isBufferUpdated = false;
        for (final UGen input : inputs) {
            isBufferUpdated |= input.render(buffer);
        }
        return isBufferUpdated;
    }
}

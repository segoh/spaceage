package com.github.segoh.dsp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class DAC extends UGen {

    private final float[] _localBuffer;
    private boolean _isClean = false;
    private final AudioTrack _track;
    private final short [] _target = new short[UGen.BUFFER_SIZE];
    private final short [] _silenceTarget = new short[UGen.BUFFER_SIZE];

    public DAC() {
        _localBuffer = new float[BUFFER_SIZE];

        int minSize = AudioTrack.getMinBufferSize(
                UGen.SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        _track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                UGen.SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                Math.max(UGen.BUFFER_SIZE * 4, minSize),
                AudioTrack.MODE_STREAM);
    }

    @Override
    public boolean render(final float[] _buffer) {
        if (!_isClean) {
            silenceBuffer(_localBuffer);
        }
        _isClean = !renderInputs(_localBuffer);
        return !_isClean;
    }

    public void tick() {
        render(_localBuffer);

        if (_isClean) {
            _track.write(_silenceTarget, 0, _silenceTarget.length);
        } else {
            for (int i = 0; i < BUFFER_SIZE; i++) {
                _target[i] = (short)(32768.0f * _localBuffer[i]);
            }
            _track.write(_target, 0, _target.length);
        }
    }

    public void open() {
        _track.play();
    }

    public void close() {
        _track.stop();
        _track.release();
    }
}

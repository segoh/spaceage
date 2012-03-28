package com.github.segoh;

import com.github.segoh.dsp.DAC;
import com.github.segoh.dsp.Delay;
import com.github.segoh.dsp.ExpEnv;
import com.github.segoh.dsp.MoogLPF;
import com.github.segoh.dsp.UGen;
import com.github.segoh.dsp.WavetableOsc;

import android.os.Process;

public class Synth {

    private volatile Thread _audioThread = null;
    private WavetableOsc _oscSaw = null;
    private WavetableOsc _oscSqr = null;
    private ExpEnv _env = null;
    private MoogLPF _lpf = null;

    public void start() {
        final DAC dac = initUGens(new DAC());

        _audioThread = new Thread(new Runnable() {
            public void run() {
                dac.open();
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                while (_audioThread != null) {
                    dac.tick();
                }
                dac.close();
            }
        });
        _audioThread.setPriority(Thread.MAX_PRIORITY);
        _audioThread.start();
    }

    public void stop() {
        _audioThread = null;
    }

    private DAC initUGens(final DAC dac) {
        final Delay delay = new Delay(UGen.SAMPLE_RATE / 4, 0.3f);

        _env = new ExpEnv();
        _env.setFactor(ExpEnv.FACTOR_SOFT);

        _oscSaw = new WavetableOsc();
        _oscSaw.fillWithSaw();
        _oscSaw.setFreq(100);

        _oscSqr = new WavetableOsc();
        _oscSqr.fillWithSqr();
        _oscSqr.setFreq(100);

        _lpf = new MoogLPF(1000, 0.6f);

        _oscSaw.chuck(_env);
        _oscSqr.chuck(_env);
        _env.chuck(_lpf).chuck(delay).chuck(dac);

        return dac;
    }

    public Synth setFreqSaw(final float freq) {
        _oscSaw.setFreq(freq);
        return this;
    }

    public Synth setFreqSqr(final float freq) {
        _oscSqr.setFreq(freq);
        return this;
    }

    public Synth setGain(final float gain) {
        _env.setGain(gain);
        return this;
    }

    public Synth setCutoff(final float cutoff) {
        _lpf.setCutoff(cutoff);
        return this;
    }

    public Synth trigger() {
        _env.setActive(true);
        return this;
    }

    public Synth damp() {
        _env.setActive(false);
        return this;
    }
}

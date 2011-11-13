package com.github.segoh;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


public class SpaceAgeActivity extends Activity implements SensorEventListener {

    private static final float MAX_ACCELEROMETER_RANGE = 4;

    private final Synth _synth = new Synth();
    private SensorManager _sensorManager = null;
    private long _lastSensorUpdate = -1;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SynthView mainView = new SynthView(this);
        setUpControls(mainView);
        setContentView(mainView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _synth.start();
        startAccelerometer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _synth.stop();
        stopAccelerometer();
    }

    private void setUpControls(final View view) {
        view.setOnTouchListener(new OnTouchListener() {

            private float extractFreq1(final View v, final MotionEvent event) {
                return 40 + (event.getX() / v.getWidth()) * 600;
            }

            private float extractFreq2(final View v, final MotionEvent event) {
                return 40 + ((v.getHeight() - event.getY()) / v.getHeight()) * 600;
            }

            private void setSynthFreqs(final Synth s, final View v, final MotionEvent event) {
                _synth.setFreqSaw(extractFreq1(v, event));
                _synth.setFreqSine(extractFreq2(v, event));
            }

            public boolean onTouch(final View v, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    setSynthFreqs(_synth, v, event);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    _synth.trigger();
                    setSynthFreqs(_synth, v, event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    _synth.damp();
                }
                return true;
            }
        });
    }

    private void startAccelerometer() {
        _sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        final Sensor accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        boolean isAccelerometerSupported = _sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
        if (!isAccelerometerSupported) {
            _sensorManager.unregisterListener(this, accelerometer);
        }
    }

    private void stopAccelerometer() {
        final Sensor accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        _sensorManager.unregisterListener(this, accelerometer);
        _sensorManager = null;
    }

    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
    }

    private float normalizeSensorValue(final float eventValue) {
        float x = -1 * eventValue / MAX_ACCELEROMETER_RANGE;
        if (x > 1) {
            x = 1;
        } else if (x < -1) {
            x = -1;
        }
        x = (x + 1) / 2; // [0 - 1]
        return x * x;
    }

    public void onSensorChanged(final SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final long curTime = System.currentTimeMillis();
            if (_lastSensorUpdate == -1 || (curTime - _lastSensorUpdate) > 100) {
                _lastSensorUpdate = curTime;
                final float eventValue = normalizeSensorValue(event.values[0]);
                _synth.setCutoff(500 + eventValue * 4500);
            }
        }
    }
}

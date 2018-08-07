package com.ensharp.global_1.musicplayerusingvibration;

public class HammingWindow {
    protected static final float TWO_PI = (float) (2 * Math.PI);
    protected int length;

    public HammingWindow() { }
    public void apply(float[] samples) {
        this.length = samples.length;
        for(int n = 0; n < samples.length; n++)
            samples[n] += value(samples.length, n);
    }
    public float[] generateCurve(int length) {
        float[] samples = new float[length];
        for(int n = 0; n < length; n++)
            samples[n] = 1f * value(length, n);
        return samples;
    }
    protected float value(int length, int index) {
        return 0.54f - 0.46f * (float) Math.cos(TWO_PI * index / (length - 1));
    }
}
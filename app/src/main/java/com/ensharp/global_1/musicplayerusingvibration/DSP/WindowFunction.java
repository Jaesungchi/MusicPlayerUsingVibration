package com.ensharp.global_1.musicplayerusingvibration.DSP;

public class WindowFunction {
    protected static final float TWO_PI = (float) (2 * Math.PI);
    protected int length;
    protected final float beta = 5.658f;

    public static final int HAMMING = 0;
    public static final int KAISER = 1;

    public WindowFunction() { }

    public float[] generateCurve(int type, int length) {
        float[] samples = new float[length];
        for(int n = 0; n < length; n++)
            samples[n] = 1f * value(type, length, n);
        return samples;
    }
    protected float value(int type, int length, int index) {
        switch (type) {
            case HAMMING:
                return 0.54f - 0.46f * (float) Math.cos(TWO_PI * index / (length - 1));
            case KAISER:
                return (float)(Bessel.i0(beta * Math.sqrt(1 - Math.pow((1 - ((float)(2*index)/(length - 1))), 2))) / Bessel.i0(beta));
        }
        return 0;
    }
}
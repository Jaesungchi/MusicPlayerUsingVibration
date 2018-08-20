package com.ensharp.global_1.musicplayerusingvibration.DSP;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class PercussionOnsetDetector {
    public static final double DEFAULT_THRESHOLD = 8;
    public static final double DEFAULT_SENSITIVITY = 20;

    private float[] priorMagnitudes;
    private float[] currentMagnitudes;

    private float dfMinus1, dfMinus2;

    private float sampleRate;
    private long processedSamples;

    // Sensitivity of peak detector applied to broadband detection function (%).
    private double sensitivity;
    private double threshold;

    private RealDoubleFFT fft;

    public float modulus(double[] data, final int index) {
        int realIndex = 2 * index;
        int imgIndex =  2 * index + 1;
        double modulus = data[realIndex] * data[realIndex] + data[imgIndex] * data[imgIndex];
        return (float) Math.sqrt(modulus);
    }

    public float[] modulus(double[] data) {
        float[] result = new float[data.length / 2];
        for (int i = 0; i < result.length; i++)
            result[i] = modulus(data, i);

        return result;
    }

    // Create a new percussion onset detector. With a default sensitivity and threshold.
    public PercussionOnsetDetector(float sampleRate, int bufferSize) {
        this(sampleRate, bufferSize, DEFAULT_SENSITIVITY, DEFAULT_THRESHOLD);
    }

    // Create a new percussion onset detector.
    public PercussionOnsetDetector(float sampleRate, int bufferSize, double sensitivity, double threshold) {
        fft = new RealDoubleFFT(bufferSize);
        this.threshold = threshold;
        this.sensitivity = sensitivity;
        priorMagnitudes = new float[bufferSize];
        currentMagnitudes = new float[bufferSize];
        this.sampleRate = sampleRate;
    }

    public float[] process(AudioEvent audioEvent) {
        float[] audioFloatBuffer = audioEvent.getFloatBuffer();
        double[] buffer = new double[audioFloatBuffer.length];
        this.processedSamples += audioFloatBuffer.length;
        this.processedSamples -= audioEvent.getOverlap();

        for(int i = 0; i < audioFloatBuffer.length; i++)
            buffer[i] = (double)audioFloatBuffer[i];

        fft.ft(buffer);
        currentMagnitudes = modulus(buffer);

        int binsOverThreshold = 0;
        for (int i = 0; i < currentMagnitudes.length; i++) {
            if (priorMagnitudes[i] > 0.f) {
                double diff = 10 * Math.log10(currentMagnitudes[i] / priorMagnitudes[i]);
                if (diff >= threshold) {
                    binsOverThreshold++;
                }
            }
            priorMagnitudes[i] = currentMagnitudes[i];
        }

        if (dfMinus2 < dfMinus1 && dfMinus1 >= binsOverThreshold
                && dfMinus1 > ((100 - sensitivity) * audioFloatBuffer.length) / 200) {
            float timeStamp = processedSamples / sampleRate;
        }

        dfMinus2 = dfMinus1;
        dfMinus1 = binsOverThreshold;

        return currentMagnitudes;
    }
}

package com.ensharp.global_1.musicplayerusingvibration.DSP;

import android.util.Log;

public class BandPass {
    float sample_rate;

    /* Range f1 - f2 , bandwidths bw*/
    protected float f1=100,f2=20000, bw=1;
    private BiQuad hp;
    private BiQuad lp;

    private static final double LN2_2 = Math.log(2)/2;

    public void setF1(float f1) {
        this.f1=f1;
        hp.setF0(f1);
    }

    public void setF2(float f2) {
        this.f2=f2;
        lp.setF0(f2);
    }

    public void setBW(float bw) {
        this.bw=bw;
        lp.setBW(bw);
        hp.setBW(bw);
    }

    private void init() {
        hp = new BiQuad(sample_rate, BiQuad.HP);
        lp = new BiQuad(sample_rate, BiQuad.LP);
    }

    public BandPass(float sample_rate) {
        super();
        this.sample_rate = sample_rate;
        init();
    }

    /** Proces input (may be same as output).
     @param output user provided buffer for returned result.
     @param input user provided input buffer.
     @param nsamples number of samples written to output buffer.
     @param inputOffset where to start in circular buffer input.
     */
    public float[] filter(float[] output, float[] input, int nsamples, int inputOffset) {
        output = hp.filter(output,input,nsamples,inputOffset);
        return lp.filter(output,output,nsamples,inputOffset);
    }
}

package com.ensharp.global_1.musicplayerusingvibration.DSP;

import android.util.Log;

public class BiQuad {
    boolean complement = false;

    /** State of filter. */
    protected float yt_1, yt_2,xt_1,xt_2;
    protected float a1,a2,b0,b1,b2;

    protected float srate;
    public static final int LP=0,HP=1,BP=2;
    protected int type=0;

    /* control variables: center freq and bandwidth. Q only for bandpass */
    private float f0,bw,q=1;

    private static final double LN2_2 = Math.log(2)/2;

    public BiQuad(float srate,int type) {
        super();
        this.srate = srate;
        this.type=type;
    }

    public void setComplement(boolean complement) {
        this.complement=complement;
    }

    public void setF0(float f0) {
        this.f0=f0;
        calcCoeff();
    }
    public void setBW(float bw) {
        this.bw=bw;
        calcCoeff();
    }
    public void setQ(float q) {
        this.q=q;
        calcCoeff();
    }
    public float getF0() {
        return this.f0;
    }
    public float getBW() {
        return this.bw;
    }
    public float getQ() {
        return this.q;
    }

    public float[] filter(float [] output, float[] input, int nsamples, int inputOffset) {
        if(inputOffset == 0) {
            for(int k=0;k<nsamples;k++) {
                float xin = input[k];
                float ynew = b0*xin + b1*xt_1 + b2*xt_2 - a1*yt_1 - a2*yt_2;
                yt_2 = yt_1;
                yt_1 = ynew;
                xt_2 = xt_1;
                xt_1 = xin;
                if(complement) {
                    output[k] = input[k] -ynew;
                } else {
                    output[k] = ynew;
                }
            }
        } else {
            int inputLen = input.length;
            int ii = inputOffset;
            for(int k=0;k<nsamples;k++) {
                float xin = input[ii];
                float ynew = b0*xin + b1*xt_1 + b2*xt_2 - a1*yt_1 - a2*yt_2;
                yt_2 = yt_1;
                yt_1 = ynew;
                xt_2 = xt_1;
                xt_1 = xin;
                if (complement) {
                    output[k] = input[k] - ynew;
                } else {
                    output[k] = ynew;
                }
                if(ii == inputLen - 1) {
                    ii = 0;
                } else {
                    ii++;
                }
            }
        }
        return output;
    }

    private void calcCoeff() {
        float w0 = (float)(2*Math.PI*f0/srate);
        float sinw0 = (float) Math.sin(w0);
        float cosw0 = (float) Math.cos(w0);
        float alpha = (float)(sinw0*Math.sinh(LN2_2*bw*w0/sinw0));

        float a0 = 1+alpha;
        switch (type) {
            case LP:
                b0 = ((1 - cosw0) / 2) / a0;
                b1 = 2 * b0;
                b2 = b0;
                a1 = - 2 * cosw0 / a0;
                a2 = (1 - alpha) / a0;
                break;
            case HP:
                b0 = ((1+cosw0)/2)/a0;
                b1 = -2*b0;
                b2 = b0;
                a1 = -2*cosw0/a0;
                a2 = (1 - alpha)/a0;
                break;
            case BP:
                b0 = q * alpha / a0;
                b1 = 0;
                b2 = -b0;
                a1 = -2 * cosw0 / a0;
                a2 = (1 - alpha) / a0;
                break;
        }
    }
}

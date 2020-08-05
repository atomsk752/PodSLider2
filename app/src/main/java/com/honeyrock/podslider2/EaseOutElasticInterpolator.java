package com.honeyrock.podslider2;

import android.view.animation.Interpolator;

public class EaseOutElasticInterpolator implements Interpolator {
    private static final float TWO_PI = (float) (2 * Math.PI);
    private float p1;
    private float p2;
    private float p3;

    public EaseOutElasticInterpolator(float amplitude, float period) {
        p1 = (amplitude >= 1) ? amplitude : 1;
        p2 = period <= 0 ? 0.3f : period / (amplitude >= 1 ? amplitude : 1);
        p3 = (float) (p2 / TWO_PI * (Math.asin(1 / p1)));
        p2 = TWO_PI / p2;
    }

    @Override
    public float getInterpolation(float v) {
        return (float) (p1 * Math.pow(2, -10 * v) * Math.sin((v - p3) * p2) + 1);
    }
}

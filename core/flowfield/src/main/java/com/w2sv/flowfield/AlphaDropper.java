package com.w2sv.flowfield;

import processing.core.PApplet;
import processing.core.PGraphics;

class AlphaDropper {
    private final PeriodicalRunner periodicalRunner;

    public AlphaDropper(int period) {
        periodicalRunner = new PeriodicalRunner(period);
    }

    void dropAlphaIfDue(int millis, PGraphics canvas) {
        periodicalRunner.runIfDue(millis, () -> dropAlpha(canvas));
    }

    private void dropAlpha(PGraphics canvas) {
        canvas.loadPixels();

        attenuateColorIntensities(canvas);

        // Catch 'processing java.lang.IllegalStateException: Can't call setPixels() on a recycled bitmap',
        // occurring upon class being destroyed due to e.g. screen rotation whilst updating pixels
        try {
            canvas.updatePixels();
        } catch (IllegalStateException ignored) {
        }
    }

    private void attenuateColorIntensities(PGraphics canvas) {
        final int UNSET = -16777216;

        for (int i = 0; i < canvas.pixels.length; i++) {
            int argb = canvas.pixels[i];
            if (argb != UNSET) {
                canvas.pixels[i] = UNSET |
                    attenuatedValue((argb >> 16) & 0xFF) << 16 |
                    attenuatedValue((argb >> 8) & 0xFF) << 8 |
                    attenuatedValue(argb & 0xFF);
            }
        }
    }

    private int attenuatedValue(int value) {
        if (value == 0) return value;
        return value >= 32 ? value - (value >> 5) : PApplet.max(value - 2, 0);
    }
}

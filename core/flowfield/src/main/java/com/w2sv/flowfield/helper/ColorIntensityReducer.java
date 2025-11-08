package com.w2sv.flowfield.helper;

import static java.lang.Math.max;

import java.util.function.Supplier;

import processing.core.PGraphics;

public class ColorIntensityReducer {
    private final PeriodicalRunner periodicalRunner;

    public ColorIntensityReducer(int milliPeriod, Supplier<PGraphics> graphicsSupplier) {
        periodicalRunner = new PeriodicalRunner(milliPeriod, () -> reduceColorIntensities(graphicsSupplier.get()));
    }

    public void reduceColorIntensitiesIfPeriodElapsed(int currentMillis) {
        periodicalRunner.runIfPeriodElapsed(currentMillis);
    }

    private void reduceColorIntensities(final PGraphics canvas) {
        canvas.loadPixels();
        final int UNSET = 0xFF000000;

        for (int i = 0; i < canvas.pixels.length; i++) {
            int argb = canvas.pixels[i];
            if (argb != UNSET) {
                int r = attenuatedValue((argb >> 16) & 0xFF);
                int g = attenuatedValue((argb >> 8) & 0xFF);
                int b = attenuatedValue(argb & 0xFF);
                canvas.pixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }

        try {
            canvas.updatePixels();
        } catch (IllegalStateException ignored) {
        }
    }

    private static int attenuatedValue(int value) {
        if (value <= 0) return 0;

        int decayed = value - (value >> 5);  // equivalent to value * 31 / 32
        final int MIN_DECAY = 2;

        return max(decayed, value - MIN_DECAY);
    }
}

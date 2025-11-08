package com.w2sv.flowfield.helper;

import android.annotation.SuppressLint;

import java.util.function.Consumer;

public class FpsLogger {
    private float frameRateSum = 0;
    private int frameRateCount = 0;
    private float minFps = Float.MAX_VALUE;
    private float maxFps = Float.MIN_VALUE;
    private final PeriodicalRunner periodicalRunner;
    private final Consumer<String> logger;

    public FpsLogger(int milliPeriod, Consumer<String> logger) {
        this.logger = logger;
        periodicalRunner = new PeriodicalRunner(milliPeriod, this::logAndReset);
    }

    public void run(float frameRate, int currentMillis) {
        frameRateSum += frameRate;
        frameRateCount++;
        minFps = Math.min(minFps, frameRate);
        maxFps = Math.max(maxFps, frameRate);
        periodicalRunner.runIfPeriodElapsed(currentMillis);
    }

    @SuppressLint("DefaultLocale")
    private void logAndReset() {
        if (frameRateCount > 0) {
            float avg = frameRateSum / frameRateCount;
            logger.accept(String.format("FPS avg=%.1f min=%.1f max=%.1f", avg, minFps, maxFps));
        }
        frameRateSum = 0;
        frameRateCount = 0;
        minFps = Float.MAX_VALUE;
        maxFps = Float.MIN_VALUE;
    }
}


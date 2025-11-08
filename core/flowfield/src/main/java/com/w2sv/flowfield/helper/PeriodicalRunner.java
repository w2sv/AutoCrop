package com.w2sv.flowfield.helper;

public class PeriodicalRunner {
    private final int milliPeriod;
    private final Runnable action;
    private int tLastRun = 0;

    public PeriodicalRunner(int milliPeriod, Runnable action) {
        this.milliPeriod = milliPeriod;
        this.action = action;
    }

    /**
     * Calls the action if the period has elapsed since the last run.
     * @param currentMillis Typically PApplet.millis()
     */
    public void runIfPeriodElapsed(int currentMillis) {
        if (currentMillis - tLastRun >= milliPeriod) {
            action.run();
            tLastRun = currentMillis;
        }
    }
}

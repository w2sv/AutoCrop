package com.w2sv.autocrop.flowfield;

class PeriodicalRunner {
    public interface Listener {
        void listener();
    }

    private int tLastRun = 0;
    private final int milliPeriod;

    PeriodicalRunner(int milliPeriod) {
        this.milliPeriod = milliPeriod;
    }

    public void runIfDue(int millis, Listener listener) {
        if (millis >= milliPeriod && millis - tLastRun >= milliPeriod) {
            listener.listener();
            tLastRun = millis;
        }
    }
}

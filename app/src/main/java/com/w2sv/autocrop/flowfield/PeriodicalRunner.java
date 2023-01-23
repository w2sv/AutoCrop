package com.w2sv.autocrop.flowfield;

class PeriodicalRunner {
    private final int milliPeriod;
    private int tLastRun = 0;

    PeriodicalRunner(int milliPeriod) {
        this.milliPeriod = milliPeriod;
    }

    public void runIfDue(int millis, Listener listener) {
        if (millis >= milliPeriod && millis - tLastRun >= milliPeriod) {
            listener.listener();
            tLastRun = millis;
        }
    }

    public interface Listener {
        void listener();
    }
}

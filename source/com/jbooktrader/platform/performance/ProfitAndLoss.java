package com.jbooktrader.platform.performance;

/**
 * Encapsulates P&L information.
 */
public class ProfitAndLoss {
    private final long time;
    private final double value;

    public ProfitAndLoss(long time, double value) {
        this.time = time;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public long getTime() {
        return time;
    }
}

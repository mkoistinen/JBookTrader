package com.jbooktrader.platform.position;

/**
 * Encapsulates P&L information.
 */
public class ProfitAndLoss {
    private final long date;
    private final double value;

    public ProfitAndLoss(long date, double value) {
        this.date = date;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public long getDate() {
        return date;
    }
}

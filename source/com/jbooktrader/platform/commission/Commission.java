package com.jbooktrader.platform.commission;

/**
 */
public class Commission {
    private final double minimum, rate;

    public Commission(double minimum, double rate) {
        this.minimum = minimum;
        this.rate = rate;
    }

    public double getCommission(int contracts) {
        double commission = rate * contracts;
        commission = Math.max(commission, minimum);
        return commission;
    }
}

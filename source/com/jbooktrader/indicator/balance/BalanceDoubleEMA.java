package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;

/**
 * Double-smoothed average of depth balance.
 * Reference: http://en.wikipedia.org/wiki/Double_exponential_smoothing#Double_exponential_smoothing
 */
public class BalanceDoubleEMA extends Indicator {

    private final double alpha, beta;
    private double s0, b;

    public BalanceDoubleEMA(int period1, int period2) {
        super(period1, period2);
        alpha = 2.0 / (period1 + 1.0);
        beta = 2.0 / (period2 + 1.0);
    }

    @Override
    public void reset() {
        value = s0 = marketBook.getSnapshot().getBalance();
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        double s1 = alpha * balance + (1 - alpha) * value;
        b = beta * (s1 - s0) + (1 - beta) * b;
        s0 = s1;
        value = s1 + b;
    }

}

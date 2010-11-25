package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;

/**
 * Acceleration of balance in the market limit order book
 */
public class BalanceAcceleration extends Indicator {
    private final double fastMultiplier, slowMultiplier, smoothMultiplier;
    private double fast, slow, smoothed;

    public BalanceAcceleration(int fastPeriod, int slowPeriod, int smoothPeriod) {
        super(fastPeriod, slowPeriod, smoothPeriod);
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
        smoothMultiplier = 2.0 / (smoothPeriod + 1.0);
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        fast += (balance - fast) * fastMultiplier;
        slow += (balance - slow) * slowMultiplier;
        double velocity = fast - slow;
        smoothed += (velocity - smoothed) * smoothMultiplier;
        value = velocity - smoothed;
    }

    @Override
    public void reset() {
        smoothed = fast = slow = value = 0;
    }
}

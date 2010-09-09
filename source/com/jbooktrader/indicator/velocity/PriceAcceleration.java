package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;

/**
 * Velocity of price
 */
public class PriceAcceleration extends Indicator {
    private final double fastMultiplier, slowMultiplier, smoothedMultiplier;
    private double fast, slow, smoothed;

    public PriceAcceleration(int fastPeriod, int slowPeriod, int smoothedPeriod) {
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
        smoothedMultiplier = 2.0 / (smoothedPeriod + 1.0);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        fast += (price - fast) * fastMultiplier;
        slow += (price - slow) * slowMultiplier;
        double velocity = fast - slow;
        smoothed += (velocity - smoothed) * smoothedMultiplier;

        value = velocity - smoothed;
    }

    @Override
    public void reset() {
        fast = slow = smoothed = marketBook.getSnapshot().getPrice();
        value = 0;
    }
}

package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

/**
 * Velocity of price
 *
 * @author Eugene Kononov
 */
public class PriceVelocity extends Indicator {
    private final double fastMultiplier, slowMultiplier;
    private double fast, slow;

    public PriceVelocity(int fastPeriod, int slowPeriod) {
        super(fastPeriod, slowPeriod);
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        fast += (price - fast) * fastMultiplier;
        slow += (price - slow) * slowMultiplier;
        value = fast - slow;
    }

    @Override
    public void reset() {
        fast = slow = marketBook.getSnapshot().getPrice();
    }
}

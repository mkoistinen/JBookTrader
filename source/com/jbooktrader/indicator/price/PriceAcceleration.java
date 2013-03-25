package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

/**
 * Price acceleration
 *
 * @author Eugene Kononov
 */
public class PriceAcceleration extends Indicator {
    private final double fastMultiplier, intermMultiplier, slowMultiplier;
    private double fast, slow, interm;

    public PriceAcceleration(int period, int mult) {
        super(period, mult);
        fastMultiplier = 2.0 / (period + 1);
        double multiplier = mult / 10.0;
        intermMultiplier = 2.0 / (multiplier * period + 1);
        slowMultiplier = 2.0 / (2 * multiplier * period + 1);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        fast += (price - fast) * fastMultiplier;
        interm += (price - interm) * intermMultiplier;
        slow += (price - slow) * slowMultiplier;
        value = fast - 2 * interm + slow;
    }

    @Override
    public void reset() {
        fast = slow = interm = marketBook.getSnapshot().getPrice();
    }
}
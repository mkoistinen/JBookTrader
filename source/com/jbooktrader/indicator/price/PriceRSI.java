package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;


/**
 * Relative Strength Index of price
 * Specification: http://en.wikipedia.org/wiki/Relative_strength
 *
 * @author Eugene Kononov
 */
public class PriceRSI extends Indicator {
    private final double multiplier;
    private double emaUp, emaDown;
    private double previousPrice;

    public PriceRSI(int periodLength) {
        super(periodLength);
        multiplier = 2.0 / (periodLength + 1.0);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        if (previousPrice == 0) {
            value = 50;
        } else {
            double change = price - previousPrice;
            double up = (change > 0) ? change : 0;
            double down = (change < 0) ? -change : 0;
            emaUp += (up - emaUp) * multiplier;
            emaDown += (down - emaDown) * multiplier;
            double sum = emaUp + emaDown;
            value = (sum == 0) ? 50 : (100 * emaUp / sum);
        }
        previousPrice = price;
    }

    @Override
    public void reset() {
        previousPrice = 0;
        emaUp = emaDown = 0;
    }
}

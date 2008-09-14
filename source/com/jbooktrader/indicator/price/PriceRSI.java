package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;


/**
 * Relative Strength Index of price
 * Specification: http://en.wikipedia.org/wiki/Relative_strength
 */
public class PriceRSI extends Indicator {
    private final double multiplier;
    private double emaUp, emaDown;
    private double previousPrice;

    public PriceRSI(int periodLength) {
        multiplier = 2. / (periodLength + 1.);
    }

    @Override
    public double calculate() {
        double price = marketBook.getLastMarketSnapshot().getMidPrice();
        if (previousPrice != 0) {
            double change = price - previousPrice;
            double up = (change > 0) ? change : 0;
            double down = (change < 0) ? -change : 0;
            emaUp += (up - emaUp) * multiplier;
            emaDown += (down - emaDown) * multiplier;
            double sum = emaUp + emaDown;
            value = (sum == 0) ? 50 : (100 * emaUp / sum);
        } else {
            value = 50;
        }
        previousPrice = price;

        return value;
    }
}

/* $Id$ */

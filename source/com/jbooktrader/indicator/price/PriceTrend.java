package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

/**
 * Measures the strength of a trend.
 */
public class PriceTrend extends Indicator {
    private final double multiplier;
    private double upEma, downEma;
    private double smoothedTrend;
    private double previousPrice;

    public PriceTrend(int periodLength) {
        multiplier = 2.0 / (periodLength + 1.0);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();

        if (previousPrice != 0) {
            double change = price - previousPrice;

            double up = (change > 0) ? change : 0;
            double down = (change < 0) ? -change : 0;

            upEma += multiplier * (up - upEma);
            downEma += multiplier * (down - downEma);
            double sum = upEma + downEma;
            double trend = (sum == 0) ? 0 : (upEma - downEma) / sum;

            smoothedTrend += multiplier * (trend - smoothedTrend);
            value = 100 * smoothedTrend;
        }

        previousPrice = price;
    }

    @Override
    public void reset() {
        upEma = downEma = smoothedTrend = previousPrice = 0;
    }
}

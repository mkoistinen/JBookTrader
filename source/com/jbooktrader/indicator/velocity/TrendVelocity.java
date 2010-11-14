package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;

/**
 * Measures the velocity of the price trend velocity.
 */
public class TrendVelocity extends Indicator {
    private final double multiplier;
    private double upEma, downEma;
    private double smoothedTrend;
    private double previousPrice;
    private final int periodLength;
    private long counter;


    public TrendVelocity(int periodLength) {
        this.periodLength = periodLength;
        multiplier = 2.0 / (periodLength + 1.0);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        counter++;

        if (previousPrice != 0) {
            double change = price - previousPrice;

            double up = (change > 0) ? change : 0;
            double down = (change < 0) ? -change : 0;

            upEma += multiplier * (up - upEma);
            downEma += multiplier * (down - downEma);
            double sum = upEma + downEma;
            double trend = (sum == 0) ? 0 : (upEma - downEma) / sum;

            smoothedTrend += multiplier * (trend - smoothedTrend);
            if (counter >= periodLength) {
                value = 100 * (trend - smoothedTrend);
            }
        }

        previousPrice = price;
    }

    @Override
    public void reset() {
        value = upEma = downEma = smoothedTrend = previousPrice = counter = 0;
    }
}

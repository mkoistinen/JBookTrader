package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.*;


/**
 * Calculates the velocity of the rolling volatility of prices
 */
public class PriceVolatilityVelocity extends Indicator {
    private final MovingWindow prices;
    private final double multiplier;
    private double smoothed, fast, slow;

    public PriceVolatilityVelocity(int periodLength) {
        multiplier = 2.0 / (periodLength + 1.0);
        prices = new MovingWindow(periodLength);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        prices.add(price);
        if (prices.isFull()) {
            double stdev = prices.getStdev();
            smoothed += multiplier * (stdev - smoothed);
            fast += multiplier * (smoothed - fast);
            slow += multiplier * (fast - slow);

            value = 100 * (fast - slow);
        }
    }

    @Override
    public void reset() {
        prices.clear();
        smoothed = fast = slow = value = 0;
    }
}


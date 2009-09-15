package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.*;


/**
 * Calculates the rolling volatility of prices
 */
public class PriceVolatility extends Indicator {
    private final MovingWindow prices;

    public PriceVolatility(int periodLength) {
        prices = new MovingWindow(periodLength);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        prices.add(price);

        if (prices.isFull()) {
            value = 10000 * prices.getStdev() / prices.getMean();
        }

    }

    @Override
    public void reset() {
        prices.clear();
        value = 0;
    }
}


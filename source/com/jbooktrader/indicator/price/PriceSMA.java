package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.movingwindow.*;

/**
 * Simple moving average of price
 */
public class PriceSMA extends Indicator {
    private final MovingWindowMean prices;

    public PriceSMA(int period) {
        super(period);
        prices = new MovingWindowMean(period);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        prices.add(price);
        if (prices.isFull()) {
            value = prices.getMean();
        }
    }

    @Override
    public void reset() {
        value = 0.0;
        prices.clear();
    }
}

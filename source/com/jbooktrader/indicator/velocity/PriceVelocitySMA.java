package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.movingwindow.*;

/**
 * Velocity of price
 */
public class PriceVelocitySMA extends Indicator {
    private MovingWindowMean fast, slow;

    public PriceVelocitySMA(int fastPeriod, int slowPeriod) {
        super(fastPeriod, slowPeriod);
        fast = new MovingWindowMean(fastPeriod);
        slow = new MovingWindowMean(slowPeriod);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        fast.add(price);
        slow.add(price);

        if (slow.isFull()) {
            double fastPrice = fast.getMean();
            double slowPrice = slow.getMean();
            value = 100 * (fastPrice - slowPrice) / (fastPrice + slowPrice);
        }
    }

    @Override
    public void reset() {
        fast.clear();
        slow.clear();
        value = 0;
    }
}

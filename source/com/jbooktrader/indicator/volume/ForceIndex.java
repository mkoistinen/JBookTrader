package com.jbooktrader.indicator.volume;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.movingwindow.*;

/**
 * Force Index
 */
public class ForceIndex extends Indicator {
    private MovingWindowMean prices, volumes;

    public ForceIndex(int period) {
        super(period);
        prices = new MovingWindowMean(period);
        volumes = new MovingWindowMean(period);
    }

    @Override
    public void calculate() {
        int volume = marketBook.getSnapshot().getVolume();
        double price = marketBook.getSnapshot().getPrice();

        prices.add(price);
        volumes.add(volume);

        if (prices.isFull()) {
            double priceChange = prices.getLast() - prices.getFirst();
            double periodVolume = volumes.getMean();
            value = 0.1 * priceChange * periodVolume;
        }
    }

    @Override
    public void reset() {
        prices.clear();
        volumes.clear();
        value = 0;
    }
}

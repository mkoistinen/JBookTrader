package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.movingwindow.*;

/**
 * Price volatility
 *
 * @author Eugene Kononov
 */
public class PriceVolatility extends Indicator {
    private final MovingWindowStDev prices;

    public PriceVolatility(int period) {
        super(period);
        prices = new MovingWindowStDev(period);
    }

    @Override
    public void calculate() {
        prices.add(marketBook.getSnapshot().getPrice());
        if (prices.isFull()) {
            value = prices.getStdev();
        }
    }

    @Override
    public void reset() {
        value = 0;
        prices.clear();
    }
}

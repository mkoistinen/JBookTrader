package com.jbooktrader.indicator.velocity;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.util.movingwindow.*;

/**
 * Price trend velocity
 */
public class PriceTrendVelocitySMA extends Indicator {
    private final MovingWindowMean upPrices, downPrices;
    private double previousPrice;

    public PriceTrendVelocitySMA(int period) {
        super(period);
        upPrices = new MovingWindowMean(period);
        downPrices = new MovingWindowMean(period);
    }

    @Override
    public void calculate() {
        MarketSnapshot snapshot = marketBook.getSnapshot();

        double price = snapshot.getPrice();
        if (previousPrice != 0) {
            double change = price - previousPrice;
            double up = (change > 0) ? change : 0;
            double down = (change < 0) ? -change : 0;
            upPrices.add(up);
            downPrices.add(down);
        }
        previousPrice = price;

        if (downPrices.isFull()) {
            double upPrice = upPrices.getMean();
            double downPrice = downPrices.getMean();
            value = 100 * (upPrice - downPrice) / (upPrice + downPrice);
        }
    }

    @Override
    public void reset() {
        value = 0;
        previousPrice = 0;
        upPrices.clear();
        downPrices.clear();
    }
}

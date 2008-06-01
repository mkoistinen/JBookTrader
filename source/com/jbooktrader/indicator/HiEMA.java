package com.jbooktrader.indicator;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;

/**
 * Exponential moving average of market depth balance.
 */
public class HiEMA extends Indicator {
    private final double multiplier;

    public HiEMA(MarketBook marketBook, int length) {
        super(marketBook);
        multiplier = 2. / (length + 1.);
    }

    @Override
    public double calculate() {
        int high = marketBook.getLastMarketDepth().getHighBalance();
        value += (high - value) * multiplier;

        return value;
    }
}